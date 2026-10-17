package api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class SwaggerHandler implements HttpHandler {

    private static final String SWAGGER_HTML = """
        <!DOCTYPE html>
        <html lang="es">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Documentación API - Tienda Virtual (Swagger UI)</title>
          <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5.11.0/swagger-ui.css" />
          <style>
            html { box-sizing: border-box; overflow-y: scroll; }
            *, *:before, *:after { box-sizing: inherit; }
            body { margin: 0; background: #f8fafc; font-family: sans-serif; }
            .swagger-ui .topbar { display: none; }
            .header-banner {
              background: #0f172a;
              color: white;
              padding: 16px 24px;
              display: flex;
              justify-content: space-between;
              align-items: center;
            }
            .header-banner h1 { margin: 0; font-size: 20px; }
            .header-banner a {
              color: #38bdf8;
              text-decoration: none;
              font-weight: 500;
              margin-left: 16px;
            }
            .header-banner a:hover { text-decoration: underline; }
          </style>
        </head>
        <body>
          <div class="header-banner">
            <h1>🛒 Tienda Virtual — Especificación OpenAPI / Swagger</h1>
            <div>
              <a href="/api-docs/openapi.yaml" target="_blank">Descargar YAML</a>
              <a href="/api/productos" target="_blank">Ver API JSON</a>
            </div>
          </div>
          <div id="swagger-ui"></div>
          <script src="https://unpkg.com/swagger-ui-dist@5.11.0/swagger-ui-bundle.js"></script>
          <script src="https://unpkg.com/swagger-ui-dist@5.11.0/swagger-ui-standalone-preset.js"></script>
          <script>
            window.onload = function() {
              window.ui = SwaggerUIBundle({
                url: "/api-docs/openapi.yaml",
                dom_id: '#swagger-ui',
                deepLinking: true,
                presets: [
                  SwaggerUIBundle.presets.apis,
                  SwaggerUIStandalonePreset
                ],
                layout: "BaseLayout"
              });
            };
          </script>
        </body>
        </html>
        """;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        if (path.endsWith("openapi.yaml") || path.endsWith("/api-docs")) {
            byte[] yamlBytes = loadOpenApiYaml();
            if (yamlBytes != null) {
                exchange.getResponseHeaders().set("Content-Type", "application/yaml; charset=UTF-8");
                exchange.sendResponseHeaders(200, yamlBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(yamlBytes);
                }
            } else {
                String error = "{\"error\": \"Archivo openapi.yaml no encontrado.\"}";
                byte[] errBytes = error.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(404, errBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(errBytes);
                }
            }
            return;
        }

        // De lo contrario, entregar interfaz Swagger UI HTML
        byte[] htmlBytes = SWAGGER_HTML.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, htmlBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(htmlBytes);
        }
    }

    private byte[] loadOpenApiYaml() {
        try (InputStream is = getClass().getResourceAsStream("/openapi.yaml")) {
            if (is != null) {
                return is.readAllBytes();
            }
        } catch (Exception e) {
            System.err.println("Error al cargar openapi.yaml: " + e.getMessage());
        }
        return null;
    }
}
