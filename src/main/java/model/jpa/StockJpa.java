package model.jpa;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "STOCK")
public class StockJpa {

    @Id
    @Column(name = "stock_id", length = 50)
    private String stockId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false, unique = true)
    private ProductoJpa producto;

    @Column(name = "cantidad_disponible", nullable = false)
    private int cantidadDisponible;

    @Column(name = "stock_minimo", nullable = false)
    private int stockMinimo;

    @Column(name = "ultima_actualizacion", length = 50)
    private String ultimaActualizacion;

    public StockJpa() {}

    public String getStockId() { return stockId; }
    public ProductoJpa getProducto() { return producto; }
    public int getCantidadDisponible() { return cantidadDisponible; }
    public int getStockMinimo() { return stockMinimo; }
}