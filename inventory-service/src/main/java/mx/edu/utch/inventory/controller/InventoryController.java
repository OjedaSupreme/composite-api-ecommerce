package mx.edu.utch.inventory.controller;

import mx.edu.utch.inventory.exception.InventoryNotFoundException;
import mx.edu.utch.inventory.exception.ServiceUnavailableException;
import mx.edu.utch.inventory.model.InventoryItem;
import mx.edu.utch.inventory.repository.InventoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Endpoints REST del inventario.
 * GET  /microservices/inventory/{productId}          -> stock, almacen y tiempo de entrega
 * POST /microservices/inventory/chaos?enabled=true   -> simula una caida del servicio (pruebas de resiliencia)
 * POST /microservices/inventory/chaos?delayMs=4000   -> simula latencia alta (pruebas de timeout)
 */
@RestController
@RequestMapping("/microservices/inventory")
public class InventoryController {

    private final InventoryRepository repository;
    private final AtomicBoolean simulateFailure = new AtomicBoolean(false);
    private final AtomicLong simulatedDelayMs = new AtomicLong(0);

    public InventoryController(InventoryRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{productId}")
    public Mono<InventoryItem> findByProductId(@PathVariable String productId) {
        if (simulateFailure.get()) {
            return Mono.error(new ServiceUnavailableException(
                    "Inventario temporalmente fuera de servicio (falla simulada)"));
        }
        return repository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new InventoryNotFoundException(productId)))
                .delayElement(Duration.ofMillis(simulatedDelayMs.get()));
    }

    @PostMapping("/chaos")
    public Mono<Map<String, Object>> chaos(@RequestParam(defaultValue = "false") boolean enabled,
                                           @RequestParam(defaultValue = "0") long delayMs) {
        simulateFailure.set(enabled);
        simulatedDelayMs.set(Math.max(0, delayMs));
        return Mono.just(Map.<String, Object>of(
                "simulate_failure", enabled,
                "simulated_delay_ms", simulatedDelayMs.get()));
    }
}
