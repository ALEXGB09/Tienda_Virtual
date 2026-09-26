package data_access.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import data_access.DatabaseConnection;
import data_access.dao.ClienteDAO;
import model.Cliente;

public class ClienteDAOJdbc implements ClienteDAO {

    @Override
    public List<Cliente> obtenerTodos() throws Exception {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT cliente_id, nombre, apellido, email, telefono, fecha_registro FROM CLIENTE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Cliente c = new Cliente(
                    rs.getString("cliente_id"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("email"),
                    rs.getString("telefono"),
                    rs.getString("fecha_registro")
                );
                lista.add(c);
            }
        }
        return lista;
    }

    @Override
    public Cliente obtenerPorId(String id) throws Exception {
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
        }
        return null;
    }

    @Override
    public boolean insertar(Cliente c) throws Exception {
        String sql = "INSERT INTO CLIENTE (cliente_id, nombre, apellido, email, telefono, fecha_registro) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, c.getCliente_id());
            stmt.setString(2, c.getNombre());
            stmt.setString(3, c.getApellido());
            stmt.setString(4, c.getEmail());
            stmt.setString(5, c.getTelefono());
            stmt.setString(6, c.getFecha_registro());

            return stmt.executeUpdate() > 0;
        }
    }
}
