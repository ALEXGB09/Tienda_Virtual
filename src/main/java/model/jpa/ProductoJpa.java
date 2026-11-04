package model.jpa;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Entity
@Immutable
@Table(name = "PRODUCTO")
public class ProductoJpa {

    @Id
    @Column(name = "producto_id", length = 50)
    private String productoId;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column
    private String descripcion;

    @Column(name = "precio_base", nullable = false,
            precision = 12, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "estado_activo", nullable = false)
    private boolean estadoActivo;

    @OneToOne(mappedBy = "producto", fetch = FetchType.LAZY)
    private StockJpa stock;

    public ProductoJpa() {}

    public String getProductoId() { return productoId; }
    public String getNombre() { return nombre; }
}