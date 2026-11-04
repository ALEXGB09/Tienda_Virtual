package model.jpa;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CLIENTE")
public class ClienteJpa {

    @Id
    @Column(name = "cliente_id", length = 50)
    private String clienteId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Column(name = "fecha_registro", length = 50)
    private String fechaRegistro;

    @OneToMany(mappedBy = "cliente")
    private List<PedidoJpa> pedidos = new ArrayList<>();

    public ClienteJpa() {}

    public String getClienteId() { return clienteId; }
    public void setClienteId(String id) { this.clienteId = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fecha) { this.fechaRegistro = fecha; }

    public List<PedidoJpa> getPedidos() { return pedidos; }
}