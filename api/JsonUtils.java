package api;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import model.Cliente;
import model.DetallePedido;
import model.Pedido;
import model.Producto;
import model.Stock;

public class JsonUtils {

    public static String toJsonProducto(Producto p) {
        if (p == null) return "null";
        return String.format(
            "{\n  \"producto_id\": \"%s\",\n  \"nombre\": \"%s\",\n  \"descripcion\": \"%s\",\n  \"precio_base\": %.2f,\n  \"estado_activo\": %b\n}",
            escape(p.getProducto_id()), escape(p.getNombre()), escape(p.getDescripcion()), p.getPrecio_base(), p.isEstado_activo()
        );
    }

    public static String toJsonProductos(List<Producto> lista) {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(toJsonProducto(lista.get(i)));
            if (i < lista.size() - 1) sb.append(",\n");
        }
        sb.append("\n]");
        return sb.toString();
    }

    public static String toJsonStock(Stock s) {
        if (s == null) return "null";
        return String.format(
            "{\n  \"stock_id\": \"%s\",\n  \"producto_id\": \"%s\",\n  \"cantidad_disponible\": %d,\n  \"stock_minimo\": %d,\n  \"ultima_actualizacion\": \"%s\"\n}",
            escape(s.getStock_id()), escape(s.getProducto_id()), s.getCantidad_disponible(), s.getStock_minimo(), escape(s.getUltima_actualizacion())
        );
    }

    public static String toJsonStocks(List<Stock> lista) {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(toJsonStock(lista.get(i)));
            if (i < lista.size() - 1) sb.append(",\n");
        }
        sb.append("\n]");
        return sb.toString();
    }

    public static String toJsonPedido(Pedido p) {
        if (p == null) return "null";
        StringBuilder sb = new StringBuilder("{\n");
        sb.append(String.format("  \"pedido_id\": \"%s\",\n", escape(p.getPedido_id())));
        sb.append(String.format("  \"cliente_id\": \"%s\",\n", escape(p.getCliente_id())));
        sb.append(String.format("  \"direccion_envio\": \"%s\",\n", escape(p.getDireccion_envio())));
        sb.append(String.format("  \"fecha_pedido\": \"%s\",\n", escape(p.getFecha_pedido())));
        sb.append(String.format("  \"monto_total\": %.2f,\n", p.getMonto_total()));
        sb.append(String.format("  \"estado\": \"%s\",\n", escape(p.getEstado())));
        sb.append("  \"detalles\": [\n");
        List<DetallePedido> detalles = p.getDetalles();
        for (int i = 0; i < detalles.size(); i++) {
            DetallePedido d = detalles.get(i);
            sb.append(String.format("    {\n      \"detalle_id\": \"%s\", \"producto_id\": \"%s\", \"cantidad\": %d, \"precio_unitario\": %.2f\n    }",
                escape(d.getDetalle_id()), escape(d.getProducto_id()), d.getCantidad(), d.getPrecio_unitario()));
            if (i < detalles.size() - 1) sb.append(",\n");
        }
        sb.append("\n  ]\n}");
        return sb.toString();
    }

    public static String toJsonPedidos(List<Pedido> lista) {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < lista.size(); i++) {
            sb.append(toJsonPedido(lista.get(i)));
            if (i < lista.size() - 1) sb.append(",\n");
        }
        sb.append("\n]");
        return sb.toString();
    }

    public static String toJsonMessage(String key, String value) {
        return String.format("{\n  \"%s\": \"%s\"\n}", escape(key), escape(value));
    }

    public static Map<String, String> parseJsonSimple(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return map;

        String clean = json.trim().replaceAll("[{}\"]", "");
        String[] pairs = clean.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length >= 2) {
                map.put(kv[0].trim(), kv[1].trim());
            }
        }
        return map;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
