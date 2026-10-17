package controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import data_access.dao.ClienteDAO;
import data_access.dao.PedidoDAO;
import data_access.dao.ProductoDAO;
import data_access.dao.StockDAO;
import model.DetallePedido;
import model.Pedido;
import model.Producto;
import model.Stock;

public class VentaController {

    private PedidoDAO pedidoDAO;
    private StockDAO stockDAO;
    private ProductoDAO productoDAO;
    private ClienteDAO clienteDAO;

    public VentaController(
            PedidoDAO pedidoDAO,
            StockDAO stockDAO,
            ProductoDAO productoDAO,
            ClienteDAO clienteDAO) {

        this.pedidoDAO = pedidoDAO;
        this.stockDAO = stockDAO;
        this.productoDAO = productoDAO;
        this.clienteDAO = clienteDAO;
    }

    public List<Pedido> listarVentas() throws Exception {
        return pedidoDAO.obtenerTodos();
    }

    public Pedido buscarVentaPorId(String pedidoId) throws Exception {
        if (pedidoId == null || pedidoId.trim().isEmpty()) {
            throw new Exception("El ID del pedido no puede estar vacío.");
        }

        return pedidoDAO.obtenerPorId(pedidoId.trim());
    }

    public Pedido procesarVenta(
            String clienteId,
            String direccionEnvio,
            List<DetallePedido> items) throws Exception {

        if (clienteId == null || clienteId.trim().isEmpty()) {
            throw new Exception("Se requiere un ID de cliente válido.");
        }

        if (items == null || items.isEmpty()) {
            throw new Exception(
                "El pedido debe contener al menos un producto."
            );
        }

        // Verificar que el cliente exista.
        if (clienteDAO.obtenerPorId(clienteId.trim()) == null) {
            throw new Exception(
                "El cliente " + clienteId + " no existe."
            );
        }

        double totalVenta = 0.0;

        // Validar productos, cantidades y existencias.
        for (DetallePedido item : items) {

            if (item.getProducto_id() == null
                    || item.getProducto_id().trim().isEmpty()) {
                throw new Exception(
                    "Cada ítem debe especificar producto_id."
                );
            }

            if (item.getCantidad() <= 0) {
                throw new Exception(
                    "La cantidad debe ser mayor a 0."
                );
            }

            String productoId = item.getProducto_id().trim();
            item.setProducto_id(productoId);

            Producto prod = productoDAO.obtenerPorId(productoId);

            if (prod == null || !prod.isEstado_activo()) {
                throw new Exception(
                    "El producto " + productoId
                    + " no está activo o no existe."
                );
            }

            Stock stock = stockDAO.obtenerPorProductoId(productoId);

            if (stock == null
                    || stock.getCantidad_disponible() < item.getCantidad()) {

                int disponible = (stock != null)
                    ? stock.getCantidad_disponible()
                    : 0;

                throw new Exception(
                    "Inventario insuficiente para '"
                    + prod.getNombre()
                    + "'. Disponible: " + disponible
                    + ", Solicitado: " + item.getCantidad()
                );
            }

            item.setPrecio_unitario(prod.getPrecio_base());

            totalVenta += item.getPrecio_unitario()
                    * item.getCantidad();
        }

        // Crear los datos del pedido.
        String now = LocalDateTime.now().format(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        );

        String pedidoId = "PED_" + System.currentTimeMillis();

        String dir = (direccionEnvio != null
                && !direccionEnvio.trim().isEmpty())
                ? direccionEnvio.trim()
                : "Dirección Principal";

        Pedido pedido = new Pedido(
            pedidoId,
            clienteId.trim(),
            dir,
            now,
            totalVenta,
            "COMPLETADO"
        );

        // Asociar cada detalle al pedido.
        int count = 1;

        for (DetallePedido item : items) {
            item.setDetalle_id(
                "DET_" + pedidoId + "_" + count++
            );

            item.setPedido_id(pedidoId);

            pedido.getDetalles().add(item);
        }

        // El DAO guarda el pedido, los detalles y descuenta
        // el inventario dentro de una sola transacción.
        pedidoDAO.insertarPedidoConDetalles(pedido);

        return pedido;
    }
}