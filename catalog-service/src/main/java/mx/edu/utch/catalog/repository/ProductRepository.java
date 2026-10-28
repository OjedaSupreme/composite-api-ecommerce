package mx.edu.utch.catalog.repository;

import mx.edu.utch.catalog.model.Product;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Repositorio en memoria (datos semilla) que simula la base de datos del catalogo.
 */
@Repository
public class ProductRepository {

    private final Map<String, Product> products = new LinkedHashMap<>();

    public ProductRepository() {
        save(new Product("P-101", "Laptop Gamer XYZ",
                "Laptop de 16 pulgadas, procesador de 8 nucleos, 32 GB RAM, SSD 1 TB y GPU dedicada de 8 GB.",
                "Computo", "XYZ Tech", new BigDecimal("1200.00"), "laptop"));
        save(new Product("P-102", "Monitor UltraWide 34\"",
                "Monitor curvo de 34 pulgadas, resolucion 3440x1440, 165 Hz y panel IPS.",
                "Monitores", "ViewMax", new BigDecimal("450.00"), "monitor"));
        save(new Product("P-103", "Teclado Mecanico RGB",
                "Teclado mecanico TKL con switches lineales, iluminacion RGB y conexion USB-C.",
                "Perifericos", "KeyForge", new BigDecimal("89.99"), "keyboard"));
        save(new Product("P-104", "Audifonos Inalambricos Pro",
                "Audifonos over-ear con cancelacion activa de ruido y 40 h de bateria.",
                "Audio", "SoundWave", new BigDecimal("199.50"), "headphones"));
        save(new Product("P-105", "Silla Ergonomica Elite",
                "Silla ergonomica con soporte lumbar ajustable, reposabrazos 4D y malla transpirable.",
                "Mobiliario", "ErgoSeat", new BigDecimal("320.00"), "chair"));
    }

    private void save(Product product) {
        products.put(product.id(), product);
    }

    public Mono<Product> findById(String id) {
        return Mono.justOrEmpty(products.get(id.toUpperCase()));
    }

    public Flux<Product> findAll() {
        return Flux.fromIterable(products.values());
    }
}
