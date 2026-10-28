package mx.edu.utch.composite.exception;

/** El producto solicitado no existe en el catalogo (HTTP 404). */
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String id) {
        super("El producto " + id + " no existe en el catalogo");
    }
}
