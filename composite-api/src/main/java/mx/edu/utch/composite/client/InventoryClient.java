package mx.edu.utch.composite.client;

import mx.edu.utch.composite.config.ServicesProperties;
import mx.edu.utch.composite.dto.InventoryInfo;
import mx.edu.utch.composite.exception.DownstreamServiceException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Cliente del Microservicio 2 - Inventario y Almacen.
 */
@Component
public class InventoryClient {

    private static final String SERVICE = "inventory-service";

    private final WebClient webClient;
    private final Duration timeout;

    public InventoryClient(@Qualifier("inventoryWebClient") WebClient webClient, ServicesProperties props) {
        this.webClient = webClient;
        this.timeout = Duration.ofMillis(props.internalTimeoutMs());
    }

    public Mono<InventoryInfo> getInventory(String productId) {
        return webClient.get()
                .uri("/microservices/inventory/{productId}", productId)
                .retrieve()
                .bodyToMono(InventoryInfo.class)
                .timeout(timeout)
                .retryWhen(Retry.backoff(1, Duration.ofMillis(200))
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()))
                .onErrorMap(ex -> new DownstreamServiceException(SERVICE,
                        "Inventario no disponible: " + ex.getClass().getSimpleName(), ex));
    }
}
