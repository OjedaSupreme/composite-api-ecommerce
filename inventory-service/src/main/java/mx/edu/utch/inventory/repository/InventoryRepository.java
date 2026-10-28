package mx.edu.utch.inventory.repository;

import mx.edu.utch.inventory.model.InventoryItem;
import mx.edu.utch.inventory.model.StockStatus;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Repositorio en memoria que simula el sistema de almacen.
 */
@Repository
public class InventoryRepository {

    private final Map<String, InventoryItem> stock = new HashMap<>();

    public InventoryRepository() {
        save("P-101", 15, "CEDIS Chihuahua", "Pasillo A - Rack 03 - Nivel 2", 2);
        save("P-102", 4, "CEDIS Monterrey", "Pasillo C - Rack 11 - Nivel 1", 4);
        save("P-103", 58, "CEDIS Chihuahua", "Pasillo B - Rack 07 - Nivel 3", 1);
        save("P-104", 0, "CEDIS Guadalajara", "Pasillo D - Rack 02 - Nivel 4", 10);
        save("P-105", 9, "CEDIS CDMX", "Pasillo F - Rack 01 - Nivel 1", 5);
    }

    private void save(String productId, int units, String warehouse, String location, int days) {
        stock.put(productId, new InventoryItem(productId, units, StockStatus.fromUnits(units),
                warehouse, location, days));
    }

    public Mono<InventoryItem> findByProductId(String productId) {
        return Mono.justOrEmpty(stock.get(productId.toUpperCase()));
    }
}
