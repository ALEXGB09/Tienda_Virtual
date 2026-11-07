
package service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
            throw new ValidationException(
                "El ID de la venta no puede estar vacío."
            );
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

        // 1. Validar los datos generales de la venta.
        if (clienteId == null || clienteId.trim().isEmpty()) {
            throw new ValidationException(
                "Se requiere un ID de cliente válido."
            );
        }

        if (items == null || items.isEmpty()) {
            throw new ValidationException(
                "La venta debe contener al menos un producto."
            );
        }

        String clienteIdLimpio = clienteId.trim();

        Cliente cliente = clienteDAO.obtenerPorId(clienteIdLimpio);

        if (cliente == null) {
            throw new ResourceNotFoundException(
                "El cliente " + clienteIdLimpio + " no existe."
            );
        }

        // 2. Agrupar las cantidades por producto.
        // Esto evita validar incorrectamente productos repetidos.
        Map<String, Integer> cantidadesPorProducto = new LinkedHashMap<>();

        for (DetallePedido item : items) {

            if (item == null) {
                throw new ValidationException(
                    "Los detalles de la venta no pueden ser nulos."
                );
            }

            if (item.getProducto_id() == null
                    || item.getProducto_id().trim().isEmpty()) {
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

            try {
                cantidadesPorProducto.merge(
                    productoId,
                    item.getCantidad(),
                    Math::addExact
                );
            } catch (ArithmeticException e) {
                throw new ValidationException(
                    "La cantidad total solicitada para el producto "
                    + productoId + " es demasiado grande."
                );
            }
        }

        // 3. Validar productos, estado y stock disponible.
        double totalVenta = 0.0;

        for (DetallePedido item : items) {

            String productoId = item.getProducto_id();

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

            Stock stock = stockDAO.obtenerPorProductoId(productoId);

            int disponible = (stock != null)
                    ? stock.getCantidad_disponible()
                    : 0;

            int cantidadSolicitada =
                    cantidadesPorProducto.get(productoId);

            if (stock == null || disponible < cantidadSolicitada) {
                throw new InsufficientStockException(
                    "Inventario insuficiente para '" + prod.getNombre()
                    + "'. Disponible: " + disponible
                    + ", Solicitado: " + cantidadSolicitada
                );
            }

            // Asignar el precio real del producto al detalle.
            item.setPrecio_unitario(prod.getPrecio_base());

            totalVenta += item.getPrecio_unitario() * item.getCantidad();
        }

        // 4. Construir el pedido.
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

        // 5. Descontar inventario y registrar el pedido.
        Map<String, Integer> cantidadesDescontadas =
                new LinkedHashMap<>();

        try {

            // Descontar una sola vez la cantidad total de cada producto.
            for (Map.Entry<String, Integer> entry
                    : cantidadesPorProducto.entrySet()) {

                String productoId = entry.getKey();
                int cantidad = entry.getValue();

                boolean descontado = stockDAO.descontarCantidad(
                    productoId,
                    cantidad,
                    now
                );

                if (!descontado) {
                    throw new InsufficientStockException(
                        "No fue posible descontar el inventario del producto: "
                        + productoId
                    );
                }

                // Registrar el descuento para poder restaurarlo si falla
                // alguna operación posterior.
                cantidadesDescontadas.put(productoId, cantidad);
            }

            // Guardar el pedido y sus detalles.
            boolean guardado = pedidoDAO.insertarPedidoConDetalles(pedido);

            if (!guardado) {
                throw new IllegalStateException(
                    "No fue posible persistir la venta. Inténtelo de nuevo."
                );
            }

            return pedido;

        } catch (Exception e) {

            // 6. Si falla la venta, intentar restaurar el inventario.
            for (Map.Entry<String, Integer> entry
                    : cantidadesDescontadas.entrySet()) {

                String productoId = entry.getKey();
                int cantidad = entry.getValue();

                try {
                    boolean restaurado = stockDAO.incrementarCantidad(
                        productoId,
                        cantidad,
                        LocalDateTime.now().format(
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        )
                    );

                    if (!restaurado) {
                        e.addSuppressed(new IllegalStateException(
                            "No se pudo restaurar el inventario del producto: "
                            + productoId
                        ));
                    }

                } catch (Exception errorRestauracion) {
                    e.addSuppressed(errorRestauracion);
                }
            }

            // Conservar el error original.
            throw e;
        }
    }
}
