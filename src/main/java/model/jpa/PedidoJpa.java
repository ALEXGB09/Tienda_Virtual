package model.jpa;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PEDIDO")
public class PedidoJpa {

    @Id
    @Column(name = "pedido_id", length = 50)
    private String pedidoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteJpa cliente;

    @Column(name = "direccion_envio", nullable = false)
    private String direccionEnvio;

    @Column(name = "fecha_pedido", nullable = false, length = 50)
    private String fechaPedido;

    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Column(nullable = false, length = 50)
    private String estado;

    @OneToMany(mappedBy = "pedido",
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<DetallePedidoJpa> detalles = new ArrayList<>();

    public PedidoJpa() {}

    public String getPedidoId() { return pedidoId; }
    public void setPedidoId(String id) { this.pedidoId = id; }

    public ClienteJpa getCliente() { return cliente; }
    public void setCliente(ClienteJpa cliente) { this.cliente = cliente; }

    public String getDireccionEnvio() { return direccionEnvio; }
    public void setDireccionEnvio(String direccion) { this.direccionEnvio = direccion; }

    public String getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(String fecha) { this.fechaPedido = fecha; }

    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal monto) { this.montoTotal = monto; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<DetallePedidoJpa> getDetalles() { return detalles; }
}