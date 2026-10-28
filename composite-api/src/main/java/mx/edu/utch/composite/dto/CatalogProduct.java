package mx.edu.utch.composite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/** Respuesta del Microservicio 1 (Catalogo). */
public record CatalogProduct(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("category") String category,
        @JsonProperty("brand") String brand,
        @JsonProperty("base_price_usd") BigDecimal basePriceUsd,
        @JsonProperty("image_url") String imageUrl
) {
}
