package mx.edu.utch.composite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Composite API (API Agregadora / API Gateway de producto).
 * Expone un unico punto de entrada para el cliente web y orquesta en paralelo
 * los microservicios de Catalogo, Inventario y la API externa de Tipo de Cambio.
 * Puerto: 8080
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class CompositeApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompositeApiApplication.class, args);
    }
}
