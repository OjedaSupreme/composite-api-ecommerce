package mx.edu.utch.composite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Respuesta de la API externa ExchangeRate-API (open.er-api.com), usada como respaldo.
 * Ejemplo: {"result":"success","base_code":"USD","time_last_update_utc":"...","rates":{"MXN":18.4,...}}
 */
public record ErApiResponse(
        @JsonProperty("result") String result,
        @JsonProperty("base_code") String baseCode,
        @JsonProperty("time_last_update_utc") String timeLastUpdateUtc,
        @JsonProperty("rates") Map<String, BigDecimal> rates
) {
}
