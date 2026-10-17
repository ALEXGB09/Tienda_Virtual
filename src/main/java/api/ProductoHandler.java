
package api;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import controller.ProductoController;
import model.Producto;

public class ProductoHandler implements HttpHandler {

    private final ProductoController productoController;

    public ProductoHandler(ProductoController productoController) {
        this.productoController = productoController;
    }

    @Override
    public void handle(HttpExchange exchange) {
        String method = exchange.getRequestMethod().toUpperCase();

        try {
            exchange.getResponseHeaders().set(
                    "Content-Type", "application/json; charset=UTF-8");
            exchange.getResponseHeaders().set(
                    "Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set(
                    "Access-Control-Allow-Methods",
                    "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().set(
                    "Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            Map<String, String> queryParams =
                    parseQueryParams(exchange.getRequestURI().getRawQuery());

            String idParam = queryParams.get("id");

            switch (method) {
                case "GET":
                    if (idParam != null && !idParam.trim().isEmpty()) {
                        Producto producto =
                                productoController.buscarPorId(idParam);

                        if (producto == null) {
                            sendError(exchange, 404,
                                    "Producto no encontrado con el ID: " + idParam);
                        } else {
                            sendResponse(exchange, 200,
                                    JsonUtils.toJsonProducto(producto));
                        }
                    } else {
                        List<Producto> productos =
                                productoController.listarProductos();

                        sendResponse(exchange, 200,
                                JsonUtils.toJsonProductos(productos));
                    }
                    break;

                case "POST": {
                    String body = readBody(exchange);
                    Map<String, String> map =
                            JsonUtils.parseJsonSimple(body);

                    String id = map.get("producto_id");
                    String nombre = map.get("nombre");
                    String descripcion = map.get("descripcion");

                    if (!map.containsKey("precio_base")) {
                        throw new IllegalArgumentException(
                                "El precio del producto es obligatorio.");
                    }

                    double precio = Double.parseDouble(
                            map.get("precio_base"));

                    if (!Double.isFinite(precio) || precio < 0) {
                        throw new IllegalArgumentException(
                                "El precio debe ser un número válido mayor o igual a cero.");
                    }

                    boolean activo = parseBoolean(
                            map, "estado_activo", true);

                    int cantidadInicial = map.containsKey("cantidad_inicial")
                            ? Integer.parseInt(map.get("cantidad_inicial"))
                            : 10;

                    int stockMinimo = map.containsKey("stock_minimo")
                            ? Integer.parseInt(map.get("stock_minimo"))
                            : 2;

                    if (cantidadInicial < 0 || stockMinimo < 0) {
                        throw new IllegalArgumentException(
                                "La cantidad inicial y el stock mínimo no pueden ser negativos.");
                    }

                    Producto producto = new Producto(
                            id, nombre, descripcion, precio, activo);

                    boolean creado = productoController.crearProducto(
                            producto, cantidadInicial, stockMinimo);

                    if (creado) {
                        sendResponse(exchange, 201,
                                JsonUtils.toJsonProducto(producto));
                    } else {
                        sendError(exchange, 500,
                                "No fue posible registrar el producto.");
                    }
                    break;
                }

                case "PUT": {
                    String body = readBody(exchange);
                    Map<String, String> map =
                            JsonUtils.parseJsonSimple(body);

                    String id = map.get("producto_id");

                    if (id == null || id.trim().isEmpty()) {
                        id = idParam;
                    }

                    String nombre = map.get("nombre");
                    String descripcion = map.get("descripcion");

                    double precio = -1;

                    if (map.containsKey("precio_base")) {
                        precio = Double.parseDouble(
                                map.get("precio_base"));

                        if (!Double.isFinite(precio) || precio < 0) {
                            throw new IllegalArgumentException(
                                    "El precio debe ser un número válido mayor o igual a cero.");
                        }
                    }

                    Boolean activo = map.containsKey("estado_activo")
                            ? parseBoolean(map, "estado_activo", true)
                            : null;

                    boolean actualizado =
                            productoController.actualizarProducto(
                                    id, nombre, descripcion, precio, activo);

                    if (actualizado) {
                        sendResponse(exchange, 200,
                                JsonUtils.toJsonMessage(
                                        "mensaje",
                                        "Producto " + id
                                                + " actualizado exitosamente."));
                    } else {
                        sendError(exchange, 500,
                                "No fue posible actualizar el producto.");
                    }
                    break;
                }

                case "DELETE": {
                    if (idParam == null || idParam.trim().isEmpty()) {
                        sendError(exchange, 400,
                                "Se requiere especificar el parámetro id.");
                        return;
                    }

                    boolean eliminado =
                            productoController.eliminarProducto(idParam);

                    if (eliminado) {
                        sendResponse(exchange, 200,
                                JsonUtils.toJsonMessage(
                                        "mensaje",
                                        "Producto " + idParam
                                                + " eliminado exitosamente."));
                    } else {
                        sendError(exchange, 500,
                                "No fue posible eliminar el producto.");
                    }
                    break;
                }

                default:
                    exchange.getResponseHeaders().set(
                            "Allow", "GET, POST, PUT, DELETE, OPTIONS");

                    sendError(exchange, 405,
                            "Método HTTP no permitido.");
            }

        } catch (Exception e) {
            int status = determinarCodigoError(e);
            String mensaje = e.getMessage();

            if (mensaje == null || mensaje.trim().isEmpty()) {
                mensaje = "Se produjo un error al procesar la solicitud.";
            }

            sendError(exchange, status, mensaje);
        }
    }

    private boolean parseBoolean(
            Map<String, String> map, String key, boolean valorPorDefecto) {

        if (!map.containsKey(key)) {
            return valorPorDefecto;
        }

        String valor = map.get(key);

        if ("true".equalsIgnoreCase(valor)) {
            return true;
        }

        if ("false".equalsIgnoreCase(valor)) {
            return false;
        }

        throw new IllegalArgumentException(
                "El campo '" + key + "' debe ser true o false.");
    }

    private int determinarCodigoError(Exception e) {
        String mensaje = e.getMessage();

        if (e instanceof JsonParseException
                || e instanceof IllegalArgumentException) {
            return 400;
        }

        if (mensaje == null) {
            return 500;
        }

        if (mensaje.startsWith("Ya existe un producto")) {
            return 409;
        }

        if (mensaje.startsWith("No se encontró ningún producto")) {
            return 404;
        }

        if (mensaje.startsWith("El ID del producto")
                || mensaje.startsWith("El nombre del producto")
                || mensaje.startsWith("El producto no puede")
                || mensaje.startsWith("El precio")
                || mensaje.startsWith("El ID")
                || mensaje.startsWith("La cantidad")) {
            return 400;
        }

        return 500;
    }

    private String readBody(HttpExchange exchange) throws Exception {
        try (InputStreamReader reader = new InputStreamReader(
                exchange.getRequestBody(), StandardCharsets.UTF_8)) {

            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            int length;

            while ((length = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, length);
            }

            return sb.toString();
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();

        if (query == null || query.isEmpty()) {
            return map;
        }

        for (String param : query.split("&")) {
            String[] entry = param.split("=", 2);

            String key = URLDecoder.decode(
                    entry[0], StandardCharsets.UTF_8);

            String value = entry.length > 1
                    ? URLDecoder.decode(entry[1], StandardCharsets.UTF_8)
                    : "";

            map.put(key, value);
        }

        return map;
    }

    private void sendResponse(
            HttpExchange exchange, int status, String jsonResponse) {

        try {
            byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

            exchange.sendResponseHeaders(status, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (Exception e) {
            System.err.println(
                    "Error al enviar la respuesta HTTP: " + e.getMessage());
            exchange.close();
        }
    }

    private void sendError(
            HttpExchange exchange, int status, String message) {

        sendResponse(exchange, status,
                JsonUtils.toJsonMessage("error", message));
    }
}
