package mx.edu.utch.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio 1 - Catalogo de Productos.
 * Gestiona la informacion basica del producto (nombre, descripcion, categoria y precio base en USD).
 * Puerto: 8081
 */
@SpringBootApplication
public class CatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
