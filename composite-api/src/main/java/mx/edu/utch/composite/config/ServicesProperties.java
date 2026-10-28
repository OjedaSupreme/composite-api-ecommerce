package mx.edu.utch.composite.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * URLs y tiempos de espera de los servicios consumidos (application.yml -> services.*).
 */
@ConfigurationProperties(prefix = "services")
public record ServicesProperties(
        String catalogUrl,
        String inventoryUrl,
        String exchangePrimaryUrl,
        String exchangeFallbackUrl,
        String targetCurrency,
        long internalTimeoutMs,
        long externalTimeoutMs
) {
}
