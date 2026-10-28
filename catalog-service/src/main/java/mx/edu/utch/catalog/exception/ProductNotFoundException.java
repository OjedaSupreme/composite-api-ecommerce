package mx.edu.utch.catalog.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String id) {
        super("No existe un producto con id " + id);
    }
}
