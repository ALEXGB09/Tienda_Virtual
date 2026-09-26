package data_access.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import data_access.DatabaseConnection;
import data_access.dao.ProductoDAO;
import model.Producto;

public class ProductoDAOJdbc implements ProductoDAO {

    @Override
    public List<Producto> obtenerTodos() throws Exception {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT producto_id, nombre, descripcion, precio_base, estado_activo FROM PRODUCTO";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Producto p = new Producto(
                    rs.getString("producto_id"),
                    rs.getString("nombre"),
                    rs.getString("descripcion"),
                    rs.getDouble("precio_base"),
                    rs.getBoolean("estado_activo")
                );
                lista.add(p);
            }
        }
        return lista;
    }

    @Override
    public Producto obtenerPorId(String id) throws Exception {
        String sql = "SELECT producto_id, nombre, descripcion, precio_base, estado_activo FROM PRODUCTO WHERE producto_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Producto(
                        rs.getString("producto_id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getDouble("precio_base"),
                        rs.getBoolean("estado_activo")
                    );
                }
            }
        }
        return null;
    }

    @Override
    public boolean insertar(Producto p) throws Exception {
        String sql = "INSERT INTO PRODUCTO (producto_id, nombre, descripcion, precio_base, estado_activo) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getProducto_id());
            stmt.setString(2, p.getNombre());
            stmt.setString(3, p.getDescripcion());
            stmt.setDouble(4, p.getPrecio_base());
            stmt.setBoolean(5, p.isEstado_activo());

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean actualizar(Producto p) throws Exception {
        String sql = "UPDATE PRODUCTO SET nombre = ?, descripcion = ?, precio_base = ?, estado_activo = ? WHERE producto_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getNombre());
            stmt.setString(2, p.getDescripcion());
            stmt.setDouble(3, p.getPrecio_base());
            stmt.setBoolean(4, p.isEstado_activo());
            stmt.setString(5, p.getProducto_id());

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(String id) throws Exception {
        String sql = "DELETE FROM PRODUCTO WHERE producto_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}
