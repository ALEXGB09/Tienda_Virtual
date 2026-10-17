package api;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;

import controller.VentaController;
import exception.ValidationException;
import model.DetallePedido;
import model.Pedido;

public class VentaHandler extends BaseHandler {

    private final VentaController ventaController;

    public VentaHandler(VentaController ventaController) {
        this.ventaController = ventaController;
    }

    @Override
    protected void doHandle(HttpExchange exchange, String method) throws Exception {
        switch (method) {
            case "GET":
                List<Pedido> ventas = ventaController.listarVentas();
                sendJson(exchange, 200, ventas);
                break;

            case "POST": {
                String body = readBody(exchange);
                JsonNode json = JsonUtils.parseTree(body);

                if (!json.hasNonNull("cliente_id") || json.get("cliente_id").asText().trim().isEmpty()) {
                    throw new ValidationException("El campo 'cliente_id' es obligatorio para procesar la venta.");
                }

                String clienteId = json.get("cliente_id").asText().trim();
                String direccionEnvio = json.hasNonNull("direccion_envio")
                        ? json.get("direccion_envio").asText().trim()
                        : "Dirección Principal";

                List<DetallePedido> detalles = new ArrayList<>();

                // Soporte para formato con array de detalles (api.http / OpenAPI)
                if (json.has("detalles") && json.get("detalles").isArray()) {
                    JsonNode arrayNode = json.get("detalles");
                    if (arrayNode.isEmpty()) {
                        throw new ValidationException("La lista de 'detalles' no puede estar vacía.");
                    }
                    for (JsonNode itemNode : arrayNode) {
                        if (!itemNode.hasNonNull("producto_id") || itemNode.get("producto_id").asText().trim().isEmpty()) {
                            throw new ValidationException("Cada ítem en 'detalles' debe especificar 'producto_id'.");
                        }
                        String productoId = itemNode.get("producto_id").asText().trim();
                        int cantidad = itemNode.hasNonNull("cantidad") ? itemNode.get("cantidad").asInt() : 1;
                        if (cantidad <= 0) {
                            throw new ValidationException("La cantidad para el producto " + productoId + " debe ser mayor a 0.");
                        }
                        detalles.add(new DetallePedido(null, null, productoId, cantidad, 0.0));
                    }
                }
                // Soporte para formato simple con producto_id directo (pruebas-api.html)
                else if (json.hasNonNull("producto_id")) {
                    String productoId = json.get("producto_id").asText().trim();
                    if (productoId.isEmpty()) {
                        throw new ValidationException("El campo 'producto_id' no puede estar vacío.");
                    }
                    int cantidad = json.hasNonNull("cantidad") ? json.get("cantidad").asInt() : 1;
                    if (cantidad <= 0) {
                        throw new ValidationException("La cantidad debe ser mayor a 0.");
                    }
                    detalles.add(new DetallePedido(null, null, productoId, cantidad, 0.0));
                } else {
                    throw new ValidationException("Debe especificar 'detalles' o un 'producto_id' para registrar la venta.");
                }

                Pedido ventaRealizada = ventaController.procesarVenta(clienteId, direccionEnvio, detalles);
                sendJson(exchange, 201, ventaRealizada);
                break;
            }

            default:
                exchange.getResponseHeaders().set("Allow", "GET, POST, OPTIONS");
                sendError(exchange, 405, "Método HTTP " + method + " no permitido en /api/ventas.");
        }
    }
}
