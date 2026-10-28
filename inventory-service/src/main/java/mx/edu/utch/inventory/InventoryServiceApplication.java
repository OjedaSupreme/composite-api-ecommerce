package mx.edu.utch.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio 2 - Inventario y Almacen.
 * Consulta el stock disponible, la ubicacion fisica y el tiempo estimado de entrega.
 * Puerto: 8082
 */
@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
