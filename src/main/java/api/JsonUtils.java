package api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import exception.ValidationException;

/**
 * Utilidades centralizadas de serialización / deserialización JSON usando Jackson.
 * Todos los handlers deben usar esta clase en lugar de procesar JSON manualmente.
 */
public class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private JsonUtils() {
        // Clase utilitaria, no instanciar
    }

    /** Devuelve el ObjectMapper configurado para uso avanzado. */
    public static ObjectMapper getObjectMapper() {
        return MAPPER;
    }

    /**
     * Serializa cualquier objeto Java a una cadena JSON.
     * Lanza {@link RuntimeException} si la serialización falla.
     */
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

    /**
     * Deserializa una cadena JSON en una instancia de la clase indicada.
     * Lanza {@link ValidationException} (400) si el JSON está mal formado.
     */
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

    /**
     * Parsea una cadena JSON y devuelve un {@link JsonNode} para inspección dinámica de campos.
     * Lanza {@link ValidationException} (400) si el JSON está mal formado.
     */
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

    /**
     * Parsea un JSON plano (solo campos de primer nivel con valores escalares)
     * y lo retorna como un mapa de cadenas. Útil para cuerpos simples.
     */
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
                map.put(entry.getKey(), val.isValueNode() ? val.asText() : val.toString());
            }
        }
        return map;
    }

    /**
     * Crea un JSON simple de una clave-valor de tipo String.
     * Útil para mensajes de respuesta como {@code {"mensaje": "..."} }.
     */
    public static String toJsonMessage(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return toJson(map);
    }
}
