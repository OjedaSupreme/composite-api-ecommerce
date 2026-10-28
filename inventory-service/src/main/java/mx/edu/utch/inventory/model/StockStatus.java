package mx.edu.utch.inventory.model;

public enum StockStatus {
    IN_STOCK,
    LOW_STOCK,
    OUT_OF_STOCK;

    /** Regla de negocio: 0 = agotado, 1-5 = pocas unidades, mas de 5 = disponible. */
    public static StockStatus fromUnits(int units) {
        if (units <= 0) {
            return OUT_OF_STOCK;
        }
        return units <= 5 ? LOW_STOCK : IN_STOCK;
    }
}
