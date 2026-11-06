package data_access.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import data_access.DatabaseConnection;
import data_access.dao.ClienteDAO;
import exception.DataAccessException;
import model.Cliente;

public class ClienteDAOJdbc implements ClienteDAO {

    @Override
public List<Cliente> obtenerTodos() {
    List<Cliente> lista = new ArrayList<>();
    String sql = "SELECT cliente_id, nombre, apellido, email, telefono, fecha_registro FROM CLIENTE";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            Cliente cliente = new Cliente(
                rs.getString("cliente_id"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("email"),
                rs.getString("telefono"),
                rs.getString("fecha_registro")
            );
            lista.add(cliente);
        }

    } catch (SQLException e) {
        throw new DataAccessException("Error al consultar los clientes.", e);
    }

    return lista;
}

    @Override
public Cliente obtenerPorId(String id) {
    String sql = "SELECT cliente_id, nombre, apellido, email, telefono, fecha_registro FROM CLIENTE WHERE cliente_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, id);

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return new Cliente(
                    rs.getString("cliente_id"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("email"),
                    rs.getString("telefono"),
                    rs.getString("fecha_registro")
                );
            }
        }

    } catch (SQLException e) {
        throw new DataAccessException("Error al buscar el cliente.", e);
    }

    return null;
}

   @Override
public boolean insertar(Cliente cliente) {
    String sql = "INSERT INTO CLIENTE (cliente_id, nombre, apellido, email, telefono, fecha_registro) VALUES (?, ?, ?, ?, ?, ?)";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, cliente.getCliente_id());
        stmt.setString(2, cliente.getNombre());
        stmt.setString(3, cliente.getApellido());
        stmt.setString(4, cliente.getEmail());
        stmt.setString(5, cliente.getTelefono());
        stmt.setString(6, cliente.getFecha_registro());

        return stmt.executeUpdate() > 0;

    } catch (SQLException e) {
        throw new DataAccessException("Error al registrar el cliente.", e);
    }
}  
}
