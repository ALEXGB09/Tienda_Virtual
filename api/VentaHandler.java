package api;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.VentaController;
import model.DetallePedido;
import model.Pedido;

public class VentaHandler implements HttpHandler {
    private VentaController ventaController;

    public VentaHandler(VentaController ventaController) {
        this.ventaController = ventaController;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod();
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equalsIgnoreCase(method)) {
                List<Pedido> pedidos = ventaController.listarVentas();
                sendResponse(exchange, 200, JsonUtils.toJsonPedidos(pedidos));
            } else if ("POST".equalsIgnoreCase(method)) {
                InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                StringBuilder sb = new StringBuilder();
                char[] buf = new char[1024];
                int len;
                while ((len = reader.read(buf)) > 0) {
                    sb.append(buf, 0, len);
                }
                reader.close();

                Map<String, String> map = JsonUtils.parseJsonSimple(sb.toString());
                String clienteId = map.getOrDefault("cliente_id", "CLI001");
                String direccionEnvio = map.getOrDefault("direccion_envio", "Calle 100 # 15-30, Bogota");
                String productoId = map.get("producto_id");
                int cantidad = map.containsKey("cantidad") ? Integer.parseInt(map.get("cantidad")) : 1;

                List<DetallePedido> detalles = new ArrayList<>();
                detalles.add(new DetallePedido(null, null, productoId, cantidad, 0.0));

                Pedido ventaRealizada = ventaController.procesarVenta(clienteId, direccionEnvio, detalles);

                sendResponse(exchange, 201, JsonUtils.toJsonPedido(ventaRealizada));
            } else {
                sendError(exchange, 405, "Método HTTP no permitido.");
            }
        } catch (Exception e) {
            sendError(exchange, 400, e.getMessage());
        }
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
