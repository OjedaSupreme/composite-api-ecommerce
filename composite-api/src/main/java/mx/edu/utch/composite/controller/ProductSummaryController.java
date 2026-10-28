package mx.edu.utch.composite.controller;

import mx.edu.utch.composite.client.CatalogClient;
import mx.edu.utch.composite.client.ExchangeRateClient;
import mx.edu.utch.composite.dto.CatalogProduct;
import mx.edu.utch.composite.dto.ExchangeRate;
import mx.edu.utch.composite.dto.ProductSummaryResponse;
import mx.edu.utch.composite.service.ProductSummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Punto de entrada unificado para el cliente (Frontend / App movil).
 */
@RestController
@RequestMapping("/api/v1")
public class ProductSummaryController {

    private final ProductSummaryService summaryService;
    private final CatalogClient catalogClient;
    private final ExchangeRateClient exchangeRateClient;

    public ProductSummaryController(ProductSummaryService summaryService,
                                    CatalogClient catalogClient,
                                    ExchangeRateClient exchangeRateClient) {
        this.summaryService = summaryService;
        this.catalogClient = catalogClient;
        this.exchangeRateClient = exchangeRateClient;
    }

    /**
     * Composite API: una sola peticion devuelve producto + inventario + precio en MXN.
     * Ambas rutas son equivalentes (las dos aparecen en el planteamiento de la practica).
     */
    @GetMapping({"/product-summary/{id}", "/products/{id}/summary"})
    public Mono<ProductSummaryResponse> getProductSummary(@PathVariable String id) {
        return summaryService.getSummary(id);
    }

    /** Lista de productos (para poblar el selector del dashboard). */
    @GetMapping("/products")
    public Flux<CatalogProduct> getProducts() {
        return catalogClient.getAll();
    }

    /** Tipo de cambio normalizado USD -> MXN (diagnostico de la API externa). */
    @GetMapping("/exchange-rate")
    public Mono<ExchangeRate> getExchangeRate() {
        return exchangeRateClient.getUsdRate();
    }
}
