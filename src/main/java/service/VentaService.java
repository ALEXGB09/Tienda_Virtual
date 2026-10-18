package service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import data_access.dao.ClienteDAO;
import data_access.dao.PedidoDAO;
import data_access.dao.ProductoDAO;
import data_access.dao.StockDAO;
import exception.ConflictException;
import exception.InsufficientStockException;
import exception.ResourceNotFoundException;
import exception.ValidationException;
import model.Cliente;
import model.DetallePedido;
import model.Pedido;
import model.Producto;
import model.Stock;

public class VentaService {

    private final PedidoDAO pedidoDAO;
    private final StockDAO stockDAO;
    private final ProductoDAO productoDAO;
    private final ClienteDAO clienteDAO;

    public VentaService(
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
            throw new ValidationException("El ID de la venta no puede estar vacío.");
        }

        Pedido pedido = pedidoDAO.obtenerPorId(pedidoId.trim());
        if (pedido == null) {
            throw new ResourceNotFoundException("No se encontró ninguna venta con el ID: " + pedidoId.trim());
        }
        return pedido;
    }

    public Pedido procesarVenta(
            String clienteId,
            String direccionEnvio,
            List<DetallePedido> items) throws Exception {

        if (clienteId == null || clienteId.trim().isEmpty()) {
            throw new ValidationException("Se requiere un ID de cliente válido.");
        }

        if (items == null || items.isEmpty()) {
            throw new ValidationException("La venta debe contener al menos un producto.");
        }

        String clienteIdLimpio = clienteId.trim();
        Cliente cliente = clienteDAO.obtenerPorId(clienteIdLimpio);
        if (cliente == null) {
            throw new ResourceNotFoundException("El cliente " + clienteIdLimpio + " no existe.");
        }

        double totalVenta = 0.0;

        for (DetallePedido item : items) {
            if (item.getProducto_id() == null || item.getProducto_id().trim().isEmpty()) {
                throw new ValidationException("Cada ítem debe especificar producto_id.");
            }

            if (item.getCantidad() <= 0) {
                throw new ValidationException("La cantidad debe ser mayor a 0.");
            }

            String productoId = item.getProducto_id().trim();
            item.setProducto_id(productoId);

            Producto prod = productoDAO.obtenerPorId(productoId);
            if (prod == null) {
                throw new ResourceNotFoundException("El producto " + productoId + " no existe.");
            }

            if (!prod.isEstado_activo()) {
                throw new ConflictException("El producto '" + prod.getNombre() + "' (" + productoId + ") no está activo.");
            }

            Stock stock = stockDAO.obtenerPorProductoId(productoId);
            int disponible = (stock != null) ? stock.getCantidad_disponible() : 0;

            if (stock == null || disponible < item.getCantidad()) {
                throw new InsufficientStockException(
                    "Inventario insuficiente para '" + prod.getNombre() +
                    "'. Disponible: " + disponible +
                    ", Solicitado: " + item.getCantidad()
                );
            }

            item.setPrecio_unitario(prod.getPrecio_base());
            totalVenta += item.getPrecio_unitario() * item.getCantidad();
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String pedidoId = "PED_" + System.currentTimeMillis();
        String dir = (direccionEnvio != null && !direccionEnvio.trim().isEmpty())
                ? direccionEnvio.trim()
                : "Dirección Principal";

        Pedido pedido = new Pedido(
                pedidoId,
                clienteIdLimpio,
                dir,
                now,
                totalVenta,
                "COMPLETADO"
        );

        int count = 1;
        for (DetallePedido item : items) {
            item.setDetalle_id("DET_" + pedidoId + "_" + count++);
            item.setPedido_id(pedidoId);
            pedido.getDetalles().add(item);
        }

        // Ejecutar transacción atómica: guardar pedido, detalles y descontar stock
        boolean guardado = pedidoDAO.insertarPedidoConDetalles(pedido);
        if (!guardado) {
            // No exponer detalles internos de la capa de datos al cliente
            throw new RuntimeException("No fue posible persistir la venta. Inténtelo de nuevo.");
        }

        return pedido;
    }
}
