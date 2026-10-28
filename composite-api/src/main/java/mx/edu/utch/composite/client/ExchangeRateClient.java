package mx.edu.utch.composite.client;

import mx.edu.utch.composite.config.ServicesProperties;
import mx.edu.utch.composite.dto.ErApiResponse;
import mx.edu.utch.composite.dto.ExchangeRate;
import mx.edu.utch.composite.dto.FrankfurterResponse;
import mx.edu.utch.composite.exception.DownstreamServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Cliente del Microservicio 3 - API externa de terceros de Tipo de Cambio.
 *
 * Estrategia de resiliencia en cascada:
 *   1) Frankfurter API (fuente primaria)
 *   2) ExchangeRate-API open.er-api.com (fuente de respaldo)
 *   3) Ultimo tipo de cambio valido en cache (memoria)
 *   4) Si no hay nada, se propaga DownstreamServiceException
 */
@Component
public class ExchangeRateClient {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateClient.class);
    private static final String SERVICE = "exchange-rate-api";
    public static final String FRANKFURTER_SOURCE = "Frankfurter External API";
    public static final String ER_API_SOURCE = "ExchangeRate-API (fallback)";

    private final WebClient frankfurterClient;
    private final WebClient erApiClient;
    private final String target;
    private final Duration timeout;
    private final AtomicReference<ExchangeRate> lastKnownRate = new AtomicReference<>();

    public ExchangeRateClient(@Qualifier("frankfurterWebClient") WebClient frankfurterClient,
                              @Qualifier("erApiWebClient") WebClient erApiClient,
                              ServicesProperties props) {
        this.frankfurterClient = frankfurterClient;
        this.erApiClient = erApiClient;
        this.target = props.targetCurrency();
        this.timeout = Duration.ofMillis(props.externalTimeoutMs());
    }

    public Mono<ExchangeRate> getUsdRate() {
        return fromFrankfurter()
                .onErrorResume(ex -> {
                    log.warn("Frankfurter no respondio ({}). Usando ExchangeRate-API como respaldo.", ex.toString());
                    return fromErApi();
                })
                .doOnNext(lastKnownRate::set)
                .onErrorResume(ex -> {
                    ExchangeRate cached = lastKnownRate.get();
                    if (cached != null) {
                        log.warn("Ninguna API de divisas respondio. Usando ultimo tipo de cambio en cache.");
                        return Mono.just(new ExchangeRate(cached.base(), cached.target(), cached.rate(),
                                cached.rateDate(), cached.source() + " - cache"));
                    }
                    return Mono.error(new DownstreamServiceException(SERVICE,
                            "No fue posible obtener el tipo de cambio USD/" + target, ex));
                });
    }

    /** GET https://api.frankfurter.app/latest?from=USD&to=MXN */
    private Mono<ExchangeRate> fromFrankfurter() {
        return frankfurterClient.get()
                .uri(uri -> uri.path("/latest")
                        .queryParam("from", "USD")
                        .queryParam("to", target)
                        .build())
                .retrieve()
                .bodyToMono(FrankfurterResponse.class)
                .timeout(timeout)
                .map(resp -> new ExchangeRate("USD", target, requireRate(resp.rates() == null ? null : resp.rates().get(target)),
                        resp.date(), FRANKFURTER_SOURCE));
    }

    /** GET https://open.er-api.com/v6/latest/USD */
    private Mono<ExchangeRate> fromErApi() {
        return erApiClient.get()
                .uri("/v6/latest/USD")
                .retrieve()
                .bodyToMono(ErApiResponse.class)
                .timeout(timeout)
                .map(resp -> {
                    if (!"success".equalsIgnoreCase(resp.result())) {
                        throw new IllegalStateException("ExchangeRate-API respondio result=" + resp.result());
                    }
                    return new ExchangeRate("USD", target,
                            requireRate(resp.rates() == null ? null : resp.rates().get(target)),
                            resp.timeLastUpdateUtc(), ER_API_SOURCE);
                });
    }

    private BigDecimal requireRate(BigDecimal rate) {
        if (rate == null || rate.signum() <= 0) {
            throw new IllegalStateException("La respuesta no contiene una tasa valida para " + target);
        }
        return rate;
    }
}
