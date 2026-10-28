package mx.edu.utch.composite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contrato JSON consolidado que la Composite API entrega al cliente web/movil.
 * Los primeros 8 campos corresponden exactamente al contrato solicitado en la practica;
 * el resto son campos de enriquecimiento para el dashboard.
 */
@JsonPropertyOrder({"product_id", "title", "price_usd", "price_mxn", "stock_status", "available_units",
        "currency_conversion_source", "status"})
public record ProductSummaryResponse(
        @JsonProperty("product_id") String productId,
        @JsonProperty("title") String title,
        @JsonProperty("price_usd") BigDecimal priceUsd,
        @JsonProperty("price_mxn") BigDecimal priceMxn,
        @JsonProperty("stock_status") String stockStatus,
        @JsonProperty("available_units") Integer availableUnits,
        @JsonProperty("currency_conversion_source") String currencyConversionSource,
        @JsonProperty("status") String status,
        @JsonProperty("description") String description,
        @JsonProperty("category") String category,
        @JsonProperty("brand") String brand,
        @JsonProperty("exchange_rate") BigDecimal exchangeRate,
        @JsonProperty("exchange_rate_date") String exchangeRateDate,
        @JsonProperty("warehouse") String warehouse,
        @JsonProperty("location") String location,
        @JsonProperty("estimated_delivery_days") Integer estimatedDeliveryDays,
        @JsonProperty("warnings") List<String> warnings,
        @JsonProperty("response_time_ms") long responseTimeMs,
        @JsonProperty("timestamp") String timestamp
) {
}
