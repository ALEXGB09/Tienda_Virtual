package api;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.ProductoController;
import model.Producto;

public class ProductoHandler implements HttpHandler {
    private ProductoController productoController;

    public ProductoHandler(ProductoController productoController) {
        this.productoController = productoController;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod();
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            Map<String, String> queryParams = parseQueryParams(query);
            String idParam = queryParams.get("id");

            if ("GET".equalsIgnoreCase(method)) {
                if (idParam != null && !idParam.isEmpty()) {
                    Producto producto = productoController.buscarPorId(idParam);
                    if (producto != null) {
                        sendResponse(exchange, 200, JsonUtils.toJsonProducto(producto));
                    } else {
                        sendError(exchange, 404, "Producto no encontrado con el ID: " + idParam);
                    }
                } else {
                    List<Producto> lista = productoController.listarProductos();
                    sendResponse(exchange, 200, JsonUtils.toJsonProductos(lista));
                }
            } else if ("POST".equalsIgnoreCase(method)) {
                String bodyStr = readBody(exchange);
                Map<String, String> map = JsonUtils.parseJsonSimple(bodyStr);

                String id = map.get("producto_id");
                String nombre = map.get("nombre");
                String descripcion = map.get("descripcion");
                double precio = map.containsKey("precio_base") ? Double.parseDouble(map.get("precio_base")) : 0.0;
                boolean activo = map.containsKey("estado_activo") ? Boolean.parseBoolean(map.get("estado_activo")) : true;
                int cantidadInicial = map.containsKey("cantidad_inicial") ? Integer.parseInt(map.get("cantidad_inicial")) : 10;
                int stockMinimo = map.containsKey("stock_minimo") ? Integer.parseInt(map.get("stock_minimo")) : 2;

                Producto p = new Producto(id, nombre, descripcion, precio, activo);
                productoController.crearProducto(p, cantidadInicial, stockMinimo);

                sendResponse(exchange, 201, JsonUtils.toJsonProducto(p));
            } else if ("PUT".equalsIgnoreCase(method)) {
                String bodyStr = readBody(exchange);
                Map<String, String> map = JsonUtils.parseJsonSimple(bodyStr);

                String id = map.get("producto_id");
                if (id == null && idParam != null) id = idParam;

                String nombre = map.get("nombre");
                String descripcion = map.get("descripcion");
                double precio = map.containsKey("precio_base") ? Double.parseDouble(map.get("precio_base")) : -1;
                Boolean activo = map.containsKey("estado_activo") ? Boolean.parseBoolean(map.get("estado_activo")) : null;

                productoController.actualizarProducto(id, nombre, descripcion, precio, activo);
                sendResponse(exchange, 200, JsonUtils.toJsonMessage("mensaje", "Producto " + id + " actualizado exitosamente."));
            } else if ("DELETE".equalsIgnoreCase(method)) {
                if (idParam == null || idParam.isEmpty()) {
                    sendError(exchange, 400, "Se requiere especificar el parámetro ID.");
                    return;
                }
                productoController.eliminarProducto(idParam);
                sendResponse(exchange, 200, JsonUtils.toJsonMessage("mensaje", "Producto " + idParam + " eliminado exitosamente."));
            } else {
                sendError(exchange, 405, "Solicitud no permitida.");
            }
        } catch (Exception e) {
            sendError(exchange, 500, e.getMessage());
        }
    }

    private String readBody(HttpExchange exchange) throws Exception {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[1024];
        int len;
        while ((len = reader.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }
        reader.close();
        return sb.toString();
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                map.put(entry[0], entry[1]);
            } else {
                map.put(entry[0], "");
            }
        }
        return map;
    }

    private void sendResponse(HttpExchange exchange, int status, String jsonResponse) {
        try {
            byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendError(HttpExchange exchange, int status, String message) {
        sendResponse(exchange, status, JsonUtils.toJsonMessage("error", message));
    }
}
