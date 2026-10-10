package model;

public class DetallePedido {
    private String detalle_id;
    private String pedido_id;
    private String producto_id;
    private int cantidad;
    private double precio_unitario;

    public DetallePedido() {}

    public DetallePedido(String detalle_id, String pedido_id, String producto_id, int cantidad, double precio_unitario) {
        this.detalle_id = detalle_id;
        this.pedido_id = pedido_id;
        this.producto_id = producto_id;
        this.cantidad = cantidad;
        this.precio_unitario = precio_unitario;
    }

    public String getDetalle_id() {
        return detalle_id;
    }

    public void setDetalle_id(String detalle_id) {
        this.detalle_id = detalle_id;
    }

    public String getPedido_id() {
        return pedido_id;
    }

    public void setPedido_id(String pedido_id) {
        this.pedido_id = pedido_id;
    }

    public String getProducto_id() {
        return producto_id;
    }

    public void setProducto_id(String producto_id) {
        this.producto_id = producto_id;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecio_unitario() {
        return precio_unitario;
    }

    public void setPrecio_unitario(double precio_unitario) {
        this.precio_unitario = precio_unitario;
    }
}
