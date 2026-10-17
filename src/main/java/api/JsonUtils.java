package api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import exception.ValidationException;
import model.Cliente;
import model.Pedido;
import model.Producto;
import model.Stock;

public class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static ObjectMapper getObjectMapper() {
        return MAPPER;
    }

    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al serializar a JSON: " + e.getMessage(), e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            throw new ValidationException("El cuerpo JSON no puede estar vacío.");
        }
        try {
            return MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new ValidationException("JSON mal formado o formato de datos inválido: " + e.getOriginalMessage());
        }
    }

    public static JsonNode parseTree(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new ValidationException("El cuerpo JSON no puede estar vacío.");
        }
        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new ValidationException("JSON mal formado: " + e.getOriginalMessage());
        }
    }

    public static Map<String, String> parseJsonSimple(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) {
            return map;
        }

        JsonNode root = parseTree(json);
        if (!root.isObject()) {
            throw new ValidationException("El cuerpo de la solicitud debe ser un objeto JSON.");
        }

        Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            JsonNode val = entry.getValue();
            if (val != null && !val.isNull()) {
                if (val.isValueNode()) {
                    map.put(entry.getKey(), val.asText());
                } else {
                    map.put(entry.getKey(), val.toString());
                }
            }
        }
        return map;
    }

    public static String toJsonProducto(Producto p) {
        return toJson(p);
    }

    public static String toJsonProductos(List<Producto> lista) {
        return toJson(lista);
    }

    public static String toJsonStock(Stock s) {
        return toJson(s);
    }

    public static String toJsonStocks(List<Stock> lista) {
        return toJson(lista);
    }

    public static String toJsonCliente(Cliente c) {
        return toJson(c);
    }

    public static String toJsonClientes(List<Cliente> lista) {
        return toJson(lista);
    }

    public static String toJsonPedido(Pedido p) {
        return toJson(p);
    }

    public static String toJsonPedidos(List<Pedido> lista) {
        return toJson(lista);
    }

    public static String toJsonMessage(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return toJson(map);
    }
}
