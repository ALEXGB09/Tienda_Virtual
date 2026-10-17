package api;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;

import controller.ClienteController;
import exception.ValidationException;
import model.Cliente;

public class ClienteHandler extends BaseHandler {

    private final ClienteController clienteController;

    public ClienteHandler(ClienteController clienteController) {
        this.clienteController = clienteController;
    }

    @Override
    protected void doHandle(HttpExchange exchange, String method) throws Exception {
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getRawQuery());
        String idParam = queryParams.get("id");

        switch (method) {
            case "GET":
                if (idParam != null && !idParam.trim().isEmpty()) {
                    Cliente cliente = clienteController.buscarCliente(idParam);
                    sendJson(exchange, 200, cliente);
                } else {
                    List<Cliente> clientes = clienteController.listarClientes();
                    sendJson(exchange, 200, clientes);
                }
                break;

            case "POST": {
                String body = readBody(exchange);
                JsonNode json = JsonUtils.parseTree(body);

                if (!json.hasNonNull("cliente_id") || json.get("cliente_id").asText().trim().isEmpty()) {
                    throw new ValidationException("El campo 'cliente_id' es obligatorio.");
                }
                if (!json.hasNonNull("nombre") || json.get("nombre").asText().trim().isEmpty()) {
                    throw new ValidationException("El campo 'nombre' es obligatorio.");
                }
                if (!json.hasNonNull("apellido") || json.get("apellido").asText().trim().isEmpty()) {
                    throw new ValidationException("El campo 'apellido' es obligatorio.");
                }
                if (!json.hasNonNull("email") || json.get("email").asText().trim().isEmpty()) {
                    throw new ValidationException("El campo 'email' es obligatorio.");
                }

                String id = json.get("cliente_id").asText().trim();
                String nombre = json.get("nombre").asText().trim();
                String apellido = json.get("apellido").asText().trim();
                String email = json.get("email").asText().trim();
                String telefono = json.hasNonNull("telefono") ? json.get("telefono").asText().trim() : "";
                String fechaRegistro = json.hasNonNull("fecha_registro") ? json.get("fecha_registro").asText().trim() : null;

                Cliente nuevoCliente = new Cliente(id, nombre, apellido, email, telefono, fechaRegistro);
                clienteController.registrarCliente(nuevoCliente);

                sendJson(exchange, 201, nuevoCliente);
                break;
            }

            default:
                exchange.getResponseHeaders().set("Allow", "GET, POST, OPTIONS");
                sendError(exchange, 405, "Método HTTP " + method + " no permitido en /api/clientes.");
        }
    }
}
