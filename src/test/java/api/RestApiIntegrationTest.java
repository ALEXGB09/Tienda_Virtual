package api;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import data_access.dao.ClienteDAO;
import data_access.dao.PedidoDAO;
import data_access.dao.ProductoDAO;
import data_access.dao.StockDAO;
import data_access.impl.MemoryDAOFactory;

public class RestApiIntegrationTest {

    private static RestServer server;
    private static final int TEST_PORT = 8092;
    private static final String BASE_URL = "http://localhost:" + TEST_PORT;
    private static HttpClient client;

    @BeforeAll
    public static void startServer() throws Exception {
        MemoryDAOFactory.reiniciarDatos();
        ProductoDAO pDAO = MemoryDAOFactory.getProductoDAO();
        StockDAO sDAO = MemoryDAOFactory.getStockDAO();
        ClienteDAO cDAO = MemoryDAOFactory.getClienteDAO();
        PedidoDAO oDAO = MemoryDAOFactory.getPedidoDAO();

        server = new RestServer(TEST_PORT, pDAO, sDAO, cDAO, oDAO);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    public static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testGetProductos() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/productos"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("PROD001"));
    }

    @Test
    public void testGetProductoPorIdExistente() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/productos?id=PROD001"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Laptop Gaming Pro 15"));
    }

    @Test
    public void testGetProductoPorIdInexistente() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/productos?id=PROD_FANTASMA"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("error"));
    }

    @Test
    public void testPostProductoExitoso() throws Exception {
        String json = """
            {
                "producto_id": "PROD_INT_1",
                "nombre": "Soporte Monitor",
                "descripcion": "Brazo articulado para monitor",
                "precio_base": 150000.0,
                "estado_activo": true,
                "cantidad_inicial": 10,
                "stock_minimo": 2
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/productos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("PROD_INT_1"));
    }

    @Test
    public void testPostProductoPrecioInvalido() throws Exception {
        String json = """
            {
                "producto_id": "PROD_BAD",
                "nombre": "Producto Malo",
                "precio_base": -50.0
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/productos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
    }

    @Test
    public void testGetStock() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/stock"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("cantidad_disponible"));
    }

    @Test
    public void testGetStockPorProductoId() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/stock?producto_id=PROD001"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("PROD001"));
    }

    @Test
    public void testPutStock() throws Exception {
        String json = """
            {
                "producto_id": "PROD001",
                "cantidad": 30
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/stock"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("actualizado"));
    }

    @Test
    public void testGetClientes() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/clientes"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("CLI001"));
    }

    @Test
    public void testGetClientePorId() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/clientes?id=CLI001"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Alexis"));
    }

    @Test
    public void testPostCliente() throws Exception {
        String json = """
            {
                "cliente_id": "CLI_INT_1",
                "nombre": "Pedro",
                "apellido": "Alvarez",
                "email": "pedro.alvarez@integration.com",
                "telefono": "+573005554433"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/clientes"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
    }

    @Test
    public void testGetPedidos() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/pedidos"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    @Test
    public void testGetVentas() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/ventas"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
    }

    @Test
    public void testPostVentaExitosa() throws Exception {
        String json = """
            {
                "cliente_id": "CLI001",
                "direccion_envio": "Av El Dorado # 68-50",
                "detalles": [
                    { "producto_id": "PROD002", "cantidad": 2 }
                ]
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/ventas"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("PED_"));
    }

    @Test
    public void testPostVentaFormatoSimple() throws Exception {
        String json = """
            {
                "cliente_id": "CLI001",
                "direccion_envio": "Calle 45 # 13-20",
                "producto_id": "PROD003",
                "cantidad": 1
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/ventas"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("PED_"));
    }

    @Test
    public void testPostVentaStockInsuficiente() throws Exception {
        String json = """
            {
                "cliente_id": "CLI001",
                "direccion_envio": "Av El Dorado # 68-50",
                "detalles": [
                    { "producto_id": "PROD002", "cantidad": 9999 }
                ]
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/ventas"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(409, response.statusCode());
        assertTrue(response.body().contains("Inventario insuficiente"));
    }

    @Test
    public void testSwaggerUi() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/swagger"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("swagger-ui"));
    }

    @Test
    public void testOpenApiYaml() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api-docs/openapi.yaml"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("openapi: 3.0.3"));
    }

    @Test
    public void testDeleteProducto() throws Exception {
        // Primero crear el producto a eliminar
        String jsonCreate = """
            {
                "producto_id": "PROD_TO_DELETE",
                "nombre": "Producto Temporal",
                "descripcion": "Para prueba de eliminacion",
                "precio_base": 10000.0,
                "cantidad_inicial": 5,
                "stock_minimo": 1
            }
            """;
        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/productos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonCreate))
                .build();
        client.send(createRequest, HttpResponse.BodyHandlers.ofString());

        // Eliminar: debe retornar 204 No Content
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/productos?id=PROD_TO_DELETE"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, response.statusCode());
    }

    @Test
    public void testDeleteProductoInexistente() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/productos?id=PROD_NO_EXISTE_JAMAS"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }
}
