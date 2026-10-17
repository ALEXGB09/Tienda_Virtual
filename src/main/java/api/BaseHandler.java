package api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import exception.ApiException;

public abstract class BaseHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) {
        String method = exchange.getRequestMethod().toUpperCase();

        try {
            // Cabeceras comunes JSON y CORS
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");

            if ("OPTIONS".equals(method)) {
                sendEmpty(exchange, 204);
                return;
            }

            doHandle(exchange, method);

        } catch (Exception e) {
            handleException(exchange, e);
        }
    }

    protected abstract void doHandle(HttpExchange exchange, String method) throws Exception;

    protected void sendJson(HttpExchange exchange, int status, Object data) throws IOException {
        String json = (data instanceof String) ? (String) data : JsonUtils.toJson(data);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendEmpty(HttpExchange exchange, int status) throws IOException {
        exchange.sendResponseHeaders(status, -1);
        exchange.close();
    }

    protected void sendError(HttpExchange exchange, int status, String message) throws IOException {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("error", message);
        errorMap.put("status", status);
        sendJson(exchange, status, errorMap);
    }

    protected void handleException(HttpExchange exchange, Exception e) {
        try {
            if (e instanceof ApiException) {
                ApiException apiEx = (ApiException) e;
                sendError(exchange, apiEx.getStatusCode(), apiEx.getMessage());
                return;
            }

            if (e instanceof JsonProcessingException || e instanceof IllegalArgumentException) {
                sendError(exchange, 400, "Solicitud mal formada: " + e.getMessage());
                return;
            }

            // Fallos inesperados o de BD: no exponer detalles internos de SQL ni credenciales
            System.err.println("Error no controlado en el servidor: " + e.getMessage());
            e.printStackTrace();
            sendError(exchange, 500, "Error interno del servidor al procesar la solicitud.");

        } catch (IOException ioException) {
            System.err.println("Error al enviar respuesta de error: " + ioException.getMessage());
            exchange.close();
        }
    }

    protected String readBody(HttpExchange exchange) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            int length;
            while ((length = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, length);
            }
            return sb.toString();
        }
    }

    protected Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return map;
        }

        for (String param : query.split("&")) {
            String[] entry = param.split("=", 2);
            String key = URLDecoder.decode(entry[0], StandardCharsets.UTF_8);
            String value = entry.length > 1
                    ? URLDecoder.decode(entry[1], StandardCharsets.UTF_8)
                    : "";
            map.put(key, value);
        }
        return map;
    }
}
