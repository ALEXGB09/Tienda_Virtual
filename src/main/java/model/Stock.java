package model;

public class Stock {
    private String stock_id;
    private String producto_id;
    private int cantidad_disponible;
    private int stock_minimo;
    private String ultima_actualizacion;

    public Stock() {}

    public Stock(String stock_id, String producto_id, int cantidad_disponible, int stock_minimo, String ultima_actualizacion) {
        this.stock_id = stock_id;
        this.producto_id = producto_id;
        this.cantidad_disponible = cantidad_disponible;
        this.stock_minimo = stock_minimo;
        this.ultima_actualizacion = ultima_actualizacion;
    }

    public String getStock_id() {
        return stock_id;
    }

    public void setStock_id(String stock_id) {
        this.stock_id = stock_id;
    }

    public String getProducto_id() {
        return producto_id;
    }

    public void setProducto_id(String producto_id) {
        this.producto_id = producto_id;
    }

    public int getCantidad_disponible() {
        return cantidad_disponible;
    }

    public void setCantidad_disponible(int cantidad_disponible) {
        this.cantidad_disponible = cantidad_disponible;
    }

    public int getStock_minimo() {
        return stock_minimo;
    }

    public void setStock_minimo(int stock_minimo) {
        this.stock_minimo = stock_minimo;
    }

    public String getUltima_actualizacion() {
        return ultima_actualizacion;
    }

    public void setUltima_actualizacion(String ultima_actualizacion) {
        this.ultima_actualizacion = ultima_actualizacion;
    }
}
