package model;

import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private String pedido_id;
    private String cliente_id;
    private String direccion_envio;
    private String fecha_pedido;
    private double monto_total;
    private String estado;
    private List<DetallePedido> detalles;

    public Pedido() {
        this.detalles = new ArrayList<>();
    }

    public Pedido(String pedido_id, String cliente_id, String direccion_envio, String fecha_pedido, double monto_total, String estado) {
        this.pedido_id = pedido_id;
        this.cliente_id = cliente_id;
        this.direccion_envio = direccion_envio;
        this.fecha_pedido = fecha_pedido;
        this.monto_total = monto_total;
        this.estado = estado;
        this.detalles = new ArrayList<>();
    }

    public String getPedido_id() {
        return pedido_id;
    }

    public void setPedido_id(String pedido_id) {
        this.pedido_id = pedido_id;
    }

    public String getCliente_id() {
        return cliente_id;
    }

    public void setCliente_id(String cliente_id) {
        this.cliente_id = cliente_id;
    }

    public String getDireccion_envio() {
        return direccion_envio;
    }

    public void setDireccion_envio(String direccion_envio) {
        this.direccion_envio = direccion_envio;
    }

    public String getFecha_pedido() {
        return fecha_pedido;
    }

    public void setFecha_pedido(String fecha_pedido) {
        this.fecha_pedido = fecha_pedido;
    }

    public double getMonto_total() {
        return monto_total;
    }

    public void setMonto_total(double monto_total) {
        this.monto_total = monto_total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles;
    }
}
