
package service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            throw new ResourceNotFoundException(
                    "No se encontró ninguna venta con el ID: " + pedidoId.trim()
            );
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
            throw new ResourceNotFoundException(
                    "El cliente " + clienteIdLimpio + " no existe."
            );
        }

        double totalVenta = 0.0;

        // Acumular las cantidades por producto para evitar
        // que un mismo producto se valide varias veces por separado.
        Map<String, Integer> cantidadesPorProducto = new LinkedHashMap<>();

        // 1. Validar productos y calcular el total de la venta.
        for (DetallePedido item : items) {

            if (item.getProducto_id() == null || item.getProducto_id().trim().isEmpty()) {
                throw new ValidationException(
                        "Cada ítem debe especificar producto_id."
                );
            }

            if (item.getCantidad() <= 0) {
                throw new ValidationException(
                        "La cantidad debe ser mayor a 0."
                );
            }

            String productoId = item.getProducto_id().trim();
            item.setProducto_id(productoId);

            Producto prod = productoDAO.obtenerPorId(productoId);

            if (prod == null) {
                throw new ResourceNotFoundException(
                        "El producto " + productoId + " no existe."
                );
            }

            if (!prod.isEstado_activo()) {
                throw new ConflictException(
                        "El producto '" + prod.getNombre()
                        + "' (" + productoId + ") no está activo."
                );
            }

            item.setPrecio_unitario(prod.getPrecio_base());

            totalVenta += item.getPrecio_unitario() * item.getCantidad();

            // Sumar cantidades si el producto aparece varias veces.
            String claveProducto = productoId.toUpperCase();

            cantidadesPorProducto.merge(
                    claveProducto,
                    item.getCantidad(),
                    Math::addExact
            );
        }

        // 2. Comprobar que exista suficiente inventario
        // para TODOS los productos antes de descontar cualquiera.
        for (Map.Entry<String, Integer> entrada : cantidadesPorProducto.entrySet()) {

            String productoId = entrada.getKey();
            int cantidadSolicitada = entrada.getValue();

            Stock stock = stockDAO.obtenerPorProductoId(productoId);

            int disponible = (stock != null)
                    ? stock.getCantidad_disponible()
                    : 0;

            if (stock == null || disponible < cantidadSolicitada) {

                Producto prod = productoDAO.obtenerPorId(productoId);
                String nombreProducto = (prod != null)
                        ? prod.getNombre()
                        : productoId;

                throw new InsufficientStockException(
                        "Inventario insuficiente para '" + nombreProducto
                        + "'. Disponible: " + disponible
                        + ", Solicitado: " + cantidadSolicitada
                );
            }
        }

        // 3. Construir el pedido.
        String now = LocalDateTime.now().format(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
        );

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

        // 4. Descontar inventario y registrar el pedido.
        // Guardamos los descuentos realizados para intentar revertirlos
        // si ocurre algún error posteriormente.
        List<Map.Entry<String, Integer>> descuentosRealizados = new ArrayList<>();

        try {

            for (Map.Entry<String, Integer> entrada : cantidadesPorProducto.entrySet()) {

                String productoId = entrada.getKey();
                int cantidad = entrada.getValue();

                boolean descontado = stockDAO.descontarCantidad(
                        productoId,
                        cantidad,
                        now
                );

                if (!descontado) {
                    throw new Exception(
                            "No fue posible descontar el inventario del producto: "
                            + productoId
                    );
                }

                descuentosRealizados.add(
                        Map.entry(productoId, cantidad)
                );
            }

            // Registrar el pedido después de descontar el inventario.
            boolean guardado = pedidoDAO.insertarPedidoConDetalles(pedido);

            if (!guardado) {
                throw new RuntimeException(
                        "No fue posible persistir la venta. Inténtelo de nuevo."
                );
            }

        } catch (Exception e) {

            // 5. Si ocurre un error, intentar devolver los descuentos.
            for (int i = descuentosRealizados.size() - 1; i >= 0; i--) {

                Map.Entry<String, Integer> descuento = descuentosRealizados.get(i);

                try {
                    boolean restaurado = stockDAO.incrementarCantidad(
                            descuento.getKey(),
                            descuento.getValue(),
                            now
                    );

                    if (!restaurado) {
                        e.addSuppressed(
                                new Exception(
                                        "No se pudo restaurar el inventario de "
                                        + descuento.getKey()
                                )
                        );
                    }

                } catch (Exception errorRestauracion) {
                    e.addSuppressed(errorRestauracion);
                }
            }

            throw e;
        }

        return pedido;
    }
}
