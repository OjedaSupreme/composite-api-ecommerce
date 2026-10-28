package mx.edu.utch.composite.config;

import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Configuracion de los clientes HTTP reactivos (WebClient sobre Reactor Netty).
 * Cada servicio tiene su propio cliente con URL base, timeout de conexion y de respuesta.
 */
@Configuration
public class WebClientConfig {

    private HttpClient httpClient(long timeoutMs) {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .responseTimeout(Duration.ofMillis(timeoutMs))
                .followRedirect(true);
    }

    private WebClient build(WebClient.Builder builder, String baseUrl, long timeoutMs) {
        return builder.clone()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient(timeoutMs)))
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "UTCH-Composite-API/1.0")
                .build();
    }

    @Bean
    public WebClient catalogWebClient(WebClient.Builder builder, ServicesProperties props) {
        return build(builder, props.catalogUrl(), props.internalTimeoutMs());
    }

    @Bean
    public WebClient inventoryWebClient(WebClient.Builder builder, ServicesProperties props) {
        return build(builder, props.inventoryUrl(), props.internalTimeoutMs());
    }

    /** Cliente para la API externa de terceros Frankfurter (fuente primaria). */
    @Bean
    public WebClient frankfurterWebClient(WebClient.Builder builder, ServicesProperties props) {
        return build(builder, props.exchangePrimaryUrl(), props.externalTimeoutMs());
    }

    /** Cliente para ExchangeRate-API (open.er-api.com), usado como respaldo (fallback). */
    @Bean
    public WebClient erApiWebClient(WebClient.Builder builder, ServicesProperties props) {
        return build(builder, props.exchangeFallbackUrl(), props.externalTimeoutMs());
    }
}
