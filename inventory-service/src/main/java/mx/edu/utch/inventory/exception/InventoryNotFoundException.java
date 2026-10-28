package mx.edu.utch.inventory.exception;

public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(String productId) {
        super("No hay registro de inventario para el producto " + productId);
    }
}
