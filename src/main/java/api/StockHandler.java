package api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;

import controller.StockController;
import exception.ValidationException;
import model.Stock;

public class StockHandler extends BaseHandler {

    private final StockController stockController;

    public StockHandler(StockController stockController) {
        this.stockController = stockController;
    }

    @Override
    protected void doHandle(HttpExchange exchange, String method) throws Exception {
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getRawQuery());
        String prodIdParam = queryParams.get("producto_id");
        if (prodIdParam == null) {
            prodIdParam = queryParams.get("productoId");
        }

        switch (method) {
            case "GET":
                if (prodIdParam != null && !prodIdParam.trim().isEmpty()) {
                    Stock stock = stockController.buscarPorProductoId(prodIdParam);
                    sendJson(exchange, 200, stock);
                } else {
                    List<Stock> list = stockController.listarStock();
                    sendJson(exchange, 200, list);
                }
                break;

            case "PUT": {
                String body = readBody(exchange);
                JsonNode json = JsonUtils.parseTree(body);

                String productoId = json.hasNonNull("producto_id")
                        ? json.get("producto_id").asText().trim()
                        : prodIdParam;

                if (productoId == null || productoId.isEmpty()) {
                    throw new ValidationException("El campo 'producto_id' es obligatorio.");
                }

                if (!json.hasNonNull("cantidad")) {
                    throw new ValidationException("El campo 'cantidad' es obligatorio.");
                }

                int cantidad = json.get("cantidad").asInt();
                if (cantidad < 0) {
                    throw new ValidationException("La cantidad no puede ser negativa.");
                }

                stockController.actualizarCantidad(productoId, cantidad);

                Map<String, Object> response = new HashMap<>();
                response.put("mensaje", "Stock de " + productoId + " actualizado a " + cantidad);
                response.put("producto_id", productoId);
                response.put("cantidad", cantidad);
                sendJson(exchange, 200, response);
                break;
            }

            default:
                exchange.getResponseHeaders().set("Allow", "GET, PUT, OPTIONS");
                sendError(exchange, 405, "Método HTTP " + method + " no permitido en /api/stock.");
        }
    }
}
