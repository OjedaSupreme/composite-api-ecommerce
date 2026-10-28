package mx.edu.utch.composite.service;

import mx.edu.utch.composite.client.CatalogClient;
import mx.edu.utch.composite.client.ExchangeRateClient;
import mx.edu.utch.composite.client.InventoryClient;
import mx.edu.utch.composite.dto.CatalogProduct;
import mx.edu.utch.composite.dto.ExchangeRate;
import mx.edu.utch.composite.dto.InventoryInfo;
import mx.edu.utch.composite.dto.ProductSummaryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Logica de orquestacion de la Composite API.
 *
 * Las tres peticiones se lanzan EN PARALELO con Mono.zip (WebClient es no bloqueante):
 *   - Catalogo   -> dependencia obligatoria (sin producto no hay resumen)
 *   - Inventario -> dependencia opcional (degradacion controlada)
 *   - Divisas    -> dependencia opcional (degradacion controlada)
 * El tiempo total es aproximadamente el del servicio mas lento, no la suma de los tres.
 */
@Service
public class ProductSummaryService {

    private static final Logger log = LoggerFactory.getLogger(ProductSummaryService.class);

    private final CatalogClient catalogClient;
    private final InventoryClient inventoryClient;
    private final ExchangeRateClient exchangeRateClient;

    public ProductSummaryService(CatalogClient catalogClient,
                                 InventoryClient inventoryClient,
                                 ExchangeRateClient exchangeRateClient) {
        this.catalogClient = catalogClient;
        this.inventoryClient = inventoryClient;
        this.exchangeRateClient = exchangeRateClient;
    }

    public Mono<ProductSummaryResponse> getSummary(String rawId) {
        final String id = rawId.trim().toUpperCase();
        final long start = System.nanoTime();

        // 1) Catalogo: si falla, el error se propaga (404 o 503) y se cancelan las demas peticiones.
        Mono<CatalogProduct> productMono = catalogClient.getProduct(id);

        // 2) Inventario: si falla se devuelve un resultado vacio con advertencia (no rompe la respuesta).
        Mono<Partial<InventoryInfo>> inventoryMono = inventoryClient.getInventory(id)
                .map(Partial::ok)
                .onErrorResume(ex -> {
                    log.warn("Inventario no disponible para {}: {}", id, ex.getMessage());
                    return Mono.just(Partial.<InventoryInfo>failed("inventory-service no disponible: stock desconocido"));
                });

        // 3) Divisas (API externa): si falla, el precio en MXN se omite con advertencia.
        Mono<Partial<ExchangeRate>> rateMono = exchangeRateClient.getUsdRate()
                .map(Partial::ok)
                .onErrorResume(ex -> {
                    log.warn("Tipo de cambio no disponible: {}", ex.getMessage());
                    return Mono.just(Partial.<ExchangeRate>failed("exchange-rate-api no disponible: precio MXN no calculado"));
                });

        return Mono.zip(productMono, inventoryMono, rateMono)
                .map(t -> assemble(t.getT1(), t.getT2(), t.getT3(),
                        (System.nanoTime() - start) / 1_000_000))
                .doOnNext(r -> log.info("Resumen {} generado en {} ms con estado {}",
                        r.productId(), r.responseTimeMs(), r.status()));
    }

    /** Mapeo y transformacion de las tres respuestas hacia el contrato final. */
    private ProductSummaryResponse assemble(CatalogProduct product,
                                            Partial<InventoryInfo> inventory,
                                            Partial<ExchangeRate> rate,
                                            long elapsedMs) {
        List<String> warnings = new ArrayList<>();

        InventoryInfo inv = inventory.value();
        if (inv == null) {
            warnings.add(inventory.warning());
        }

        ExchangeRate fx = rate.value();
        BigDecimal priceMxn = null;
        if (fx != null) {
            priceMxn = product.basePriceUsd().multiply(fx.rate()).setScale(2, RoundingMode.HALF_UP);
        } else {
            warnings.add(rate.warning());
        }

        String status = warnings.isEmpty() ? "SUCCESS" : "PARTIAL_SUCCESS";

        return new ProductSummaryResponse(
                product.id(),
                product.name(),
                product.basePriceUsd().setScale(2, RoundingMode.HALF_UP),
                priceMxn,
                inv != null ? inv.stockStatus() : "UNKNOWN",
                inv != null ? inv.availableUnits() : null,
                fx != null ? fx.source() : "UNAVAILABLE",
                status,
                product.description(),
                product.category(),
                product.brand(),
                fx != null ? fx.rate() : null,
                fx != null ? fx.rateDate() : null,
                inv != null ? inv.warehouse() : null,
                inv != null ? inv.location() : null,
                inv != null ? inv.estimatedDeliveryDays() : null,
                warnings,
                elapsedMs,
                Instant.now().toString());
    }

    /** Contenedor de resultado parcial: valor o advertencia. */
    private record Partial<T>(T value, String warning) {
        static <T> Partial<T> ok(T value) {
            return new Partial<>(value, null);
        }

        static <T> Partial<T> failed(String warning) {
            return new Partial<>(null, warning);
        }
    }
}
