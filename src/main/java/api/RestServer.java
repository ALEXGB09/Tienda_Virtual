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

public class RestServer {
    private HttpServer server;
    private int port;

    public RestServer(int port, ProductoDAO productoDAO, StockDAO stockDAO, ClienteDAO clienteDAO, PedidoDAO pedidoDAO) throws Exception {
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        ProductoController productoController = new ProductoController(productoDAO, stockDAO);
        StockController stockController = new StockController(stockDAO);
        ClienteController clienteController = new ClienteController(clienteDAO);
        VentaController ventaController = new VentaController(pedidoDAO, stockDAO, productoDAO, clienteDAO);

        this.server.createContext("/api/productos", new ProductoHandler(productoController));
        this.server.createContext("/api/stock", new StockHandler(stockController));
        this.server.createContext("/api/ventas", new VentaHandler(ventaController));
        this.server.createContext("/api/pedidos", new VentaHandler(ventaController));

        this.server.setExecutor(null);
    }

    public void start() {
        this.server.start();
        System.out.println("  Servicio de Tienda Virtual en puerto " + port);
        System.out.println("  Rutas disponibles:");
        System.out.println("    - Catalogo:  http://localhost:" + port + "/api/productos");
        System.out.println("    - Inventario: http://localhost:" + port + "/api/stock");
        System.out.println("    - Ventas:     http://localhost:" + port + "/api/ventas");
        System.out.println("    - Pedidos:    http://localhost:" + port + "/api/pedidos");
    }

    public void stop() {
        this.server.stop(0);
        System.out.println("Servicio detenido.");
    }
}
