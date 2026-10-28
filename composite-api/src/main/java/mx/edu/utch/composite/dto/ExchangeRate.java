package mx.edu.utch.composite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/** Modelo interno normalizado del tipo de cambio, independiente del proveedor externo. */
public record ExchangeRate(
        @JsonProperty("base") String base,
        @JsonProperty("target") String target,
        @JsonProperty("rate") BigDecimal rate,
        @JsonProperty("rate_date") String rateDate,
        @JsonProperty("source") String source
) {
}
