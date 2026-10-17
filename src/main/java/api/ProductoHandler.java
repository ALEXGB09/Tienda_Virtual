package api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;

import controller.ProductoController;
import exception.ValidationException;
import model.Producto;

public class ProductoHandler extends BaseHandler {

    private final ProductoController productoController;

    public ProductoHandler(ProductoController productoController) {
        this.productoController = productoController;
    }

    @Override
    protected void doHandle(HttpExchange exchange, String method) throws Exception {
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getRawQuery());
        String idParam = queryParams.get("id");

        switch (method) {
            case "GET":
                if (idParam != null && !idParam.trim().isEmpty()) {
                    Producto producto = productoController.buscarPorId(idParam);
                    sendJson(exchange, 200, producto);
                } else {
                    List<Producto> productos = productoController.listarProductos();
                    sendJson(exchange, 200, productos);
                }
                break;

            case "POST": {
                String body = readBody(exchange);
                JsonNode json = JsonUtils.parseTree(body);

                if (!json.hasNonNull("producto_id") || json.get("producto_id").asText().trim().isEmpty()) {
                    throw new ValidationException("El campo 'producto_id' es obligatorio.");
                }
                if (!json.hasNonNull("nombre") || json.get("nombre").asText().trim().isEmpty()) {
                    throw new ValidationException("El campo 'nombre' es obligatorio.");
                }
                if (!json.hasNonNull("precio_base")) {
                    throw new ValidationException("El precio base ('precio_base') es obligatorio.");
                }

                String id = json.get("producto_id").asText().trim();
                String nombre = json.get("nombre").asText().trim();
                String descripcion = json.hasNonNull("descripcion") ? json.get("descripcion").asText().trim() : "";
                double precio = json.get("precio_base").asDouble();

                if (!Double.isFinite(precio) || precio < 0) {
                    throw new ValidationException("El precio debe ser un número válido mayor o igual a cero.");
                }

                boolean activo = !json.has("estado_activo") || json.get("estado_activo").asBoolean(true);
                int cantidadInicial = json.hasNonNull("cantidad_inicial") ? json.get("cantidad_inicial").asInt() : 10;
                int stockMinimo = json.hasNonNull("stock_minimo") ? json.get("stock_minimo").asInt() : 2;

                if (cantidadInicial < 0 || stockMinimo < 0) {
                    throw new ValidationException("La cantidad inicial y el stock mínimo no pueden ser negativos.");
                }

                Producto producto = new Producto(id, nombre, descripcion, precio, activo);
                productoController.crearProducto(producto, cantidadInicial, stockMinimo);

                sendJson(exchange, 201, producto);
                break;
            }

            case "PUT": {
                String body = readBody(exchange);
                JsonNode json = JsonUtils.parseTree(body);

                String id = json.hasNonNull("producto_id") ? json.get("producto_id").asText().trim() : idParam;
                if (id == null || id.trim().isEmpty()) {
                    throw new ValidationException("Se requiere especificar el ID del producto (en query param 'id' o en el cuerpo).");
                }

                String nombre = json.hasNonNull("nombre") ? json.get("nombre").asText().trim() : null;
                String descripcion = json.hasNonNull("descripcion") ? json.get("descripcion").asText().trim() : null;

                double precio = -1;
                if (json.hasNonNull("precio_base")) {
                    precio = json.get("precio_base").asDouble();
                    if (!Double.isFinite(precio) || precio < 0) {
                        throw new ValidationException("El precio debe ser un número válido mayor o igual a cero.");
                    }
                }

                Boolean activo = json.hasNonNull("estado_activo") ? json.get("estado_activo").asBoolean() : null;

                productoController.actualizarProducto(id, nombre, descripcion, precio, activo);

                Map<String, String> response = new HashMap<>();
                response.put("mensaje", "Producto " + id + " actualizado exitosamente.");
                sendJson(exchange, 200, response);
                break;
            }

            case "DELETE": {
                if (idParam == null || idParam.trim().isEmpty()) {
                    throw new ValidationException("Se requiere especificar el parámetro 'id' para eliminar el producto.");
                }

                productoController.eliminarProducto(idParam);

                Map<String, String> response = new HashMap<>();
                response.put("mensaje", "Producto " + idParam + " eliminado exitosamente.");
                sendJson(exchange, 200, response);
                break;
            }

            default:
                exchange.getResponseHeaders().set("Allow", "GET, POST, PUT, DELETE, OPTIONS");
                sendError(exchange, 405, "Método HTTP " + method + " no permitido.");
        }
    }
}
