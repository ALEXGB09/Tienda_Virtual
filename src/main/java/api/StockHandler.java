package api;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.StockController;
import model.Stock;

public class StockHandler implements HttpHandler {
    private StockController stockController;

    public StockHandler(StockController stockController) {
        this.stockController = stockController;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String method = exchange.getRequestMethod();
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

            if ("GET".equalsIgnoreCase(method)) {
                List<Stock> stockList = stockController.listarStock();
                sendResponse(exchange, 200, JsonUtils.toJsonStocks(stockList));
            } else if ("PUT".equalsIgnoreCase(method)) {
                InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                StringBuilder sb = new StringBuilder();
                char[] buf = new char[1024];
                int len;
                while ((len = reader.read(buf)) > 0) {
                    sb.append(buf, 0, len);
                }
                reader.close();

                Map<String, String> map = JsonUtils.parseJsonSimple(sb.toString());
                String productoId = map.get("producto_id");
                int cantidad = Integer.parseInt(map.get("cantidad"));

                stockController.actualizarCantidad(productoId, cantidad);
                sendResponse(exchange, 200, JsonUtils.toJsonMessage("mensaje", "Stock de " + productoId + " actualizado a " + cantidad));
            } else {
                sendError(exchange, 405, "Método HTTP no permitido.");
            }
        } catch (Exception e) {
            sendError(exchange, 500, e.getMessage());
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
