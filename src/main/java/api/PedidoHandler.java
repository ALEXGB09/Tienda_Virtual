package api;

import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import controller.VentaController;
import model.Pedido;

public class PedidoHandler extends BaseHandler {

    private final VentaController ventaController;

    public PedidoHandler(VentaController ventaController) {
        this.ventaController = ventaController;
    }

    @Override
    protected void doHandle(HttpExchange exchange, String method) throws Exception {
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getRawQuery());
        String idParam = queryParams.get("id");

        switch (method) {
            case "GET":
                if (idParam != null && !idParam.trim().isEmpty()) {
                    Pedido pedido = ventaController.buscarPedidoPorId(idParam);
                    sendJson(exchange, 200, pedido);
                } else {
                    List<Pedido> pedidos = ventaController.listarPedidos();
                    sendJson(exchange, 200, pedidos);
                }
                break;

            default:
                exchange.getResponseHeaders().set("Allow", "GET, OPTIONS");
                sendError(exchange, 405, "Método HTTP " + method + " no permitido en /api/pedidos.");
        }
    }
}
