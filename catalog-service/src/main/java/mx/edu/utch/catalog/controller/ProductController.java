package mx.edu.utch.catalog.controller;

import mx.edu.utch.catalog.exception.ProductNotFoundException;
import mx.edu.utch.catalog.model.Product;
import mx.edu.utch.catalog.repository.ProductRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Endpoints REST del catalogo de productos.
 * GET /microservices/products       -> lista de productos
 * GET /microservices/products/{id}  -> detalle de un producto
 */
@RestController
@RequestMapping("/microservices/products")
public class ProductController {

    private final ProductRepository repository;

    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Flux<Product> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Product> findById(@PathVariable String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(id)));
    }
}
