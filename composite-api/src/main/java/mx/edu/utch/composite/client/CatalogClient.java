package mx.edu.utch.composite.client;

import mx.edu.utch.composite.config.ServicesProperties;
import mx.edu.utch.composite.dto.CatalogProduct;
import mx.edu.utch.composite.exception.DownstreamServiceException;
import mx.edu.utch.composite.exception.ProductNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Cliente del Microservicio 1 - Catalogo de Productos.
 */
@Component
public class CatalogClient {

    private static final String SERVICE = "catalog-service";

    private final WebClient webClient;
    private final Duration timeout;

    public CatalogClient(@Qualifier("catalogWebClient") WebClient webClient, ServicesProperties props) {
        this.webClient = webClient;
        this.timeout = Duration.ofMillis(props.internalTimeoutMs());
    }

    public Mono<CatalogProduct> getProduct(String id) {
        return webClient.get()
                .uri("/microservices/products/{id}", id)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(),
                        response -> Mono.error(new ProductNotFoundException(id)))
                .bodyToMono(CatalogProduct.class)
                .timeout(timeout)
                // Un reintento con espera corta ante fallas transitorias (no aplica para 404)
                .retryWhen(Retry.backoff(1, Duration.ofMillis(200))
                        .filter(ex -> !(ex instanceof ProductNotFoundException))
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()))
                .onErrorMap(ex -> !(ex instanceof ProductNotFoundException),
                        ex -> new DownstreamServiceException(SERVICE,
                                "El catalogo de productos no respondio: " + ex.getClass().getSimpleName(), ex));
    }

    public Flux<CatalogProduct> getAll() {
        return webClient.get()
                .uri("/microservices/products")
                .retrieve()
                .bodyToFlux(CatalogProduct.class)
                .timeout(timeout)
                .onErrorMap(ex -> new DownstreamServiceException(SERVICE,
                        "El catalogo de productos no respondio: " + ex.getClass().getSimpleName(), ex));
    }
}
