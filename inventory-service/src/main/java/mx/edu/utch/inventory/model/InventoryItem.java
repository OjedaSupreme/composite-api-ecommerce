package mx.edu.utch.inventory.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Contrato JSON interno del microservicio de inventario.
 */
public record InventoryItem(
        @JsonProperty("product_id") String productId,
        @JsonProperty("available_units") int availableUnits,
        @JsonProperty("stock_status") StockStatus stockStatus,
        @JsonProperty("warehouse") String warehouse,
        @JsonProperty("location") String location,
        @JsonProperty("estimated_delivery_days") int estimatedDeliveryDays
) {
}
