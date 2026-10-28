package mx.edu.utch.catalog.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Contrato JSON interno del microservicio de catalogo.
 */
public record Product(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("category") String category,
        @JsonProperty("brand") String brand,
        @JsonProperty("base_price_usd") BigDecimal basePriceUsd,
        @JsonProperty("image_url") String imageUrl
) {
}
