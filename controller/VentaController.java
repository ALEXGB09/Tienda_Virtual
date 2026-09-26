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

    public VentaController(PedidoDAO pedidoDAO, StockDAO stockDAO, ProductoDAO productoDAO, ClienteDAO clienteDAO) {
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

    public Pedido procesarVenta(String clienteId, String direccionEnvio, List<DetallePedido> items) throws Exception {
        if (clienteId == null || clienteId.trim().isEmpty()) {
            throw new Exception("Se requiere un ID de cliente válido.");
        }
        if (items == null || items.isEmpty()) {
            throw new Exception("El pedido debe contener al menos un producto.");
        }

        double totalVenta = 0.0;
        for (DetallePedido item : items) {
            if (item.getProducto_id() == null || item.getProducto_id().trim().isEmpty()) {
                throw new Exception("Cada ítem debe especificar producto_id.");
            }
            if (item.getCantidad() <= 0) {
                throw new Exception("La cantidad debe ser mayor a 0.");
            }

            Producto prod = productoDAO.obtenerPorId(item.getProducto_id().trim());
            if (prod == null || !prod.isEstado_activo()) {
                throw new Exception("El producto " + item.getProducto_id() + " no está activo o no existe.");
            }

            Stock stock = stockDAO.obtenerPorProductoId(item.getProducto_id().trim());
            if (stock == null || stock.getCantidad_disponible() < item.getCantidad()) {
                int disp = (stock != null) ? stock.getCantidad_disponible() : 0;
                throw new Exception("Inventario insuficiente para '" + prod.getNombre() + "'. Disponible: " + disp + ", Solicitado: " + item.getCantidad());
            }

            item.setPrecio_unitario(prod.getPrecio_base());
            totalVenta += item.getPrecio_unitario() * item.getCantidad();
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String pedidoId = "PED_" + System.currentTimeMillis();
        String dir = (direccionEnvio != null && !direccionEnvio.trim().isEmpty()) ? direccionEnvio : "Dirección Principal";

        Pedido pedido = new Pedido(pedidoId, clienteId, dir, now, totalVenta, "COMPLETADO");

        int count = 1;
        for (DetallePedido item : items) {
            item.setDetalle_id("DET_" + pedidoId + "_" + count++);
            item.setPedido_id(pedidoId);
            pedido.getDetalles().add(item);
        }

        for (DetallePedido item : items) {
            stockDAO.descontarCantidad(item.getProducto_id(), item.getCantidad(), now);
        }

        pedidoDAO.insertarPedidoConDetalles(pedido);

        return pedido;
    }
}
