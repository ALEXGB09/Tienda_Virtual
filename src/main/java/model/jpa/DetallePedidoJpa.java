package model.jpa;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "DETALLE_PEDIDO")
public class DetallePedidoJpa {

    @Id
    @Column(name = "detalle_id", length = 50)
    private String detalleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoJpa pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private ProductoJpa producto;

    @Column(nullable = false)
    private int cantidad;

    @Column(name = "precio_unitario", nullable = false,
            precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    public DetallePedidoJpa() {}

    public String getDetalleId() { return detalleId; }
    public void setDetalleId(String id) { this.detalleId = id; }

    public PedidoJpa getPedido() { return pedido; }
    public void setPedido(PedidoJpa pedido) { this.pedido = pedido; }

    public ProductoJpa getProducto() { return producto; }
    public void setProducto(ProductoJpa producto) { this.producto = producto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precio) { this.precioUnitario = precio; }
}