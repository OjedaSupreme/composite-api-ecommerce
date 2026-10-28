package mx.edu.utch.composite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Respuesta del Microservicio 2 (Inventario). */
public record InventoryInfo(
        @JsonProperty("product_id") String productId,
        @JsonProperty("available_units") int availableUnits,
        @JsonProperty("stock_status") String stockStatus,
        @JsonProperty("warehouse") String warehouse,
        @JsonProperty("location") String location,
        @JsonProperty("estimated_delivery_days") int estimatedDeliveryDays
) {
}
