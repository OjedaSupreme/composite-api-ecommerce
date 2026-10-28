package mx.edu.utch.composite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Respuesta de la API externa Frankfurter.
 * Ejemplo: {"amount":1.0,"base":"USD","date":"2026-10-28","rates":{"MXN":18.42}}
 */
public record FrankfurterResponse(
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("base") String base,
        @JsonProperty("date") String date,
        @JsonProperty("rates") Map<String, BigDecimal> rates
) {
}
