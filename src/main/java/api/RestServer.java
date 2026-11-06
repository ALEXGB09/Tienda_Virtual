package api;

import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import controller.ClienteController;
import controller.ProductoController;
import controller.StockController;
import controller.VentaController;
import data_access.dao.ClienteDAO;
import data_access.dao.PedidoDAO;
import data_access.dao.ProductoDAO;
import data_access.dao.StockDAO;
import service.ClienteService;

public class RestServer {
    private HttpServer server;
    private int port;

    public RestServer(int port,
                      ProductoController productoController,
                      StockController stockController,
                      ClienteController clienteController,
                      VentaController ventaController) throws Exception {
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        this.server.createContext("/api/productos", new ProductoHandler(productoController));
        this.server.createContext("/api/stock", new StockHandler(stockController));
        this.server.createContext("/api/clientes", new ClienteHandler(clienteController));
        this.server.createContext("/api/pedidos", new PedidoHandler(ventaController));
        this.server.createContext("/api/ventas", new VentaHandler(ventaController));

        SwaggerHandler swaggerHandler = new SwaggerHandler();
        this.server.createContext("/swagger", swaggerHandler);
        this.server.createContext("/api-docs", swaggerHandler);

        this.server.setExecutor(null);
    }

    public RestServer(int port,
                      ProductoDAO productoDAO,
                      StockDAO stockDAO,
                      ClienteDAO clienteDAO,
                      PedidoDAO pedidoDAO) throws Exception {
        this(port,
             new ProductoController(productoDAO, stockDAO),
             new StockController(stockDAO),
             new ClienteController(new ClienteService(clienteDAO)),
             new VentaController(pedidoDAO, stockDAO, productoDAO, clienteDAO));
    }

    public HttpServer getHttpServer() {
        return this.server;
    }

    public void start() {
        this.server.start();
        System.out.println("  Servicio de Tienda Virtual en puerto " + port);
        System.out.println("  Rutas disponibles:");
        System.out.println("    - Catalogo:   http://localhost:" + port + "/api/productos");
        System.out.println("    - Inventario: http://localhost:" + port + "/api/stock");
        System.out.println("    - Clientes:   http://localhost:" + port + "/api/clientes");
        System.out.println("    - Pedidos:    http://localhost:" + port + "/api/pedidos");
        System.out.println("    - Ventas:     http://localhost:" + port + "/api/ventas");
        System.out.println("    - Swagger UI: http://localhost:" + port + "/swagger");
        System.out.println("    - OpenAPI:    http://localhost:" + port + "/api-docs/openapi.yaml");
    }

    public void stop() {
        this.server.stop(0);
        System.out.println("Servicio detenido.");
    }
}
