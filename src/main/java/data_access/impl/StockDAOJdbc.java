package data_access.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import data_access.DatabaseConnection;
import data_access.dao.StockDAO;
import model.Stock;

public class StockDAOJdbc implements StockDAO {

    @Override
    public List<Stock> obtenerTodos() throws Exception {
        List<Stock> lista = new ArrayList<>();
        String sql = "SELECT stock_id, producto_id, cantidad_disponible, stock_minimo, ultima_actualizacion FROM STOCK";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Stock s = new Stock(
                    rs.getString("stock_id"),
                    rs.getString("producto_id"),
                    rs.getInt("cantidad_disponible"),
                    rs.getInt("stock_minimo"),
                    rs.getString("ultima_actualizacion")
                );
                lista.add(s);
            }
        }
        return lista;
    }

    @Override
    public Stock obtenerPorProductoId(String productoId) throws Exception {
        String sql = "SELECT stock_id, producto_id, cantidad_disponible, stock_minimo, ultima_actualizacion FROM STOCK WHERE producto_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, productoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Stock(
                        rs.getString("stock_id"),
                        rs.getString("producto_id"),
                        rs.getInt("cantidad_disponible"),
                        rs.getInt("stock_minimo"),
                        rs.getString("ultima_actualizacion")
                    );
                }
            }
        }
        return null;
    }

    @Override
    public boolean insertar(Stock s) throws Exception {
        String sql = "INSERT INTO STOCK (stock_id, producto_id, cantidad_disponible, stock_minimo, ultima_actualizacion) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, s.getStock_id());
            stmt.setString(2, s.getProducto_id());
            stmt.setInt(3, s.getCantidad_disponible());
            stmt.setInt(4, s.getStock_minimo());
            stmt.setString(5, s.getUltima_actualizacion());

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean actualizarCantidad(String productoId, int nuevaCantidad, String fechaActualizacion) throws Exception {
        String sql = "UPDATE STOCK SET cantidad_disponible = ?, ultima_actualizacion = ? WHERE producto_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, nuevaCantidad);
            stmt.setString(2, fechaActualizacion);
            stmt.setString(3, productoId);

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean descontarCantidad(String productoId, int cantidadADescontar, String fechaActualizacion) throws Exception {
        String sql = "UPDATE STOCK SET cantidad_disponible = cantidad_disponible - ?, ultima_actualizacion = ? WHERE producto_id = ? AND cantidad_disponible >= ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cantidadADescontar);
            stmt.setString(2, fechaActualizacion);
            stmt.setString(3, productoId);
            stmt.setInt(4, cantidadADescontar);

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new Exception("Inventario insuficiente o producto no encontrado para " + productoId);
            }
            return true;
        }
    }

    @Override
    public boolean eliminarPorProductoId(String productoId) throws Exception {
        String sql = "DELETE FROM STOCK WHERE producto_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, productoId);
            return stmt.executeUpdate() > 0;
        }
    }
}
