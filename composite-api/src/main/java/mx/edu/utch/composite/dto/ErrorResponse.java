package mx.edu.utch.composite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Contrato de error uniforme de la Composite API. */
public record ErrorResponse(
        @JsonProperty("status") String status,
        @JsonProperty("http_status") int httpStatus,
        @JsonProperty("error_code") String errorCode,
        @JsonProperty("message") String message,
        @JsonProperty("failed_service") String failedService,
        @JsonProperty("timestamp") String timestamp
) {
}
