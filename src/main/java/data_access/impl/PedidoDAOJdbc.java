package data_access.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import data_access.DatabaseConnection;
import data_access.dao.PedidoDAO;
import model.DetallePedido;
import model.Pedido;

public class PedidoDAOJdbc implements PedidoDAO {

    @Override
    public List<Pedido> obtenerTodos() throws Exception {
        List<Pedido> lista = new ArrayList<>();
        String sqlPedido = "SELECT pedido_id, cliente_id, direccion_envio, fecha_pedido, monto_total, estado FROM PEDIDO";
        String sqlDetalles = "SELECT detalle_id, pedido_id, producto_id, cantidad, precio_unitario FROM DETALLE_PEDIDO WHERE pedido_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtP = conn.prepareStatement(sqlPedido);
             ResultSet rsP = stmtP.executeQuery()) {

            while (rsP.next()) {
                Pedido p = new Pedido(
                    rsP.getString("pedido_id"),
                    rsP.getString("cliente_id"),
                    rsP.getString("direccion_envio"),
                    rsP.getString("fecha_pedido"),
                    rsP.getDouble("monto_total"),
                    rsP.getString("estado")
                );

                try (PreparedStatement stmtD = conn.prepareStatement(sqlDetalles)) {
                    stmtD.setString(1, p.getPedido_id());
                    try (ResultSet rsD = stmtD.executeQuery()) {
                        while (rsD.next()) {
                            DetallePedido d = new DetallePedido(
                                rsD.getString("detalle_id"),
                                rsD.getString("pedido_id"),
                                rsD.getString("producto_id"),
                                rsD.getInt("cantidad"),
                                rsD.getDouble("precio_unitario")
                            );
                            p.getDetalles().add(d);
                        }
                    }
                }
                lista.add(p);
            }
        }
        return lista;
    }

    @Override
    public Pedido obtenerPorId(String id) throws Exception {
        String sqlPedido = "SELECT pedido_id, cliente_id, direccion_envio, fecha_pedido, monto_total, estado FROM PEDIDO WHERE pedido_id = ?";
        String sqlDetalles = "SELECT detalle_id, pedido_id, producto_id, cantidad, precio_unitario FROM DETALLE_PEDIDO WHERE pedido_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtP = conn.prepareStatement(sqlPedido)) {

            stmtP.setString(1, id);
            try (ResultSet rsP = stmtP.executeQuery()) {
                if (rsP.next()) {
                    Pedido p = new Pedido(
                        rsP.getString("pedido_id"),
                        rsP.getString("cliente_id"),
                        rsP.getString("direccion_envio"),
                        rsP.getString("fecha_pedido"),
                        rsP.getDouble("monto_total"),
                        rsP.getString("estado")
                    );

                    try (PreparedStatement stmtD = conn.prepareStatement(sqlDetalles)) {
                        stmtD.setString(1, id);
                        try (ResultSet rsD = stmtD.executeQuery()) {
                            while (rsD.next()) {
                                DetallePedido d = new DetallePedido(
                                    rsD.getString("detalle_id"),
                                    rsD.getString("pedido_id"),
                                    rsD.getString("producto_id"),
                                    rsD.getInt("cantidad"),
                                    rsD.getDouble("precio_unitario")
                                );
                                p.getDetalles().add(d);
                            }
                        }
                    }
                    return p;
                }
            }
        }
        return null;
    }

    @Override
    public boolean insertarPedidoConDetalles(Pedido p) throws Exception {
        String sqlInsertPedido = "INSERT INTO PEDIDO (pedido_id, cliente_id, direccion_envio, fecha_pedido, monto_total, estado) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlInsertDetalle = "INSERT INTO DETALLE_PEDIDO (detalle_id, pedido_id, producto_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmtP = conn.prepareStatement(sqlInsertPedido)) {
                stmtP.setString(1, p.getPedido_id());
                stmtP.setString(2, p.getCliente_id());
                stmtP.setString(3, p.getDireccion_envio());
                stmtP.setString(4, p.getFecha_pedido());
                stmtP.setDouble(5, p.getMonto_total());
                stmtP.setString(6, p.getEstado());
                stmtP.executeUpdate();
            }

            try (PreparedStatement stmtD = conn.prepareStatement(sqlInsertDetalle)) {
                for (DetallePedido d : p.getDetalles()) {
                    stmtD.setString(1, d.getDetalle_id());
                    stmtD.setString(2, p.getPedido_id());
                    stmtD.setString(3, d.getProducto_id());
                    stmtD.setInt(4, d.getCantidad());
                    stmtD.setDouble(5, d.getPrecio_unitario());
                    stmtD.addBatch();
                }
                stmtD.executeBatch();
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception rollbackEx) { rollbackEx.printStackTrace(); }
            }
            throw new Exception("Error al procesar el pedido: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (Exception closeEx) { closeEx.printStackTrace(); }
            }
        }
    }
}
