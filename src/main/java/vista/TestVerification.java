package vista;

import java.util.ArrayList;
import java.util.List;
import api.RestServer;
import controller.ClienteController;
import controller.ProductoController;
import controller.StockController;
import controller.VentaController;
import data_access.dao.ClienteDAO;
import data_access.dao.PedidoDAO;
import data_access.dao.ProductoDAO;
import data_access.dao.StockDAO;
import data_access.impl.MemoryDAOFactory;
import model.DetallePedido;
import model.Pedido;
import model.Producto;
import model.Stock;

public class TestVerification {

    public static void main(String[] args) {
        try {
            System.out.println("=== PRUEBA DE VERIFICACIÓN DE TIENDA VIRTUAL ===");

            ProductoDAO productoDAO = MemoryDAOFactory.getProductoDAO();
            StockDAO stockDAO = MemoryDAOFactory.getStockDAO();
            ClienteDAO clienteDAO = MemoryDAOFactory.getClienteDAO();
            PedidoDAO pedidoDAO = MemoryDAOFactory.getPedidoDAO();

            ProductoController productoCtrl = new ProductoController(productoDAO, stockDAO);
            StockController stockCtrl = new StockController(stockDAO);
            ClienteController clienteCtrl = new ClienteController(clienteDAO);
            VentaController ventaCtrl = new VentaController(pedidoDAO, stockDAO, productoDAO, clienteDAO);

            RestServer restServer = new RestServer(8085, productoDAO, stockDAO, clienteDAO, pedidoDAO);
            restServer.start();

            List<Producto> productos = productoCtrl.listarProductos();
            System.out.println("• Productos registrados en catálogo: " + productos.size());

            Stock stockInicial = stockCtrl.buscarPorProductoId("PROD001");
            int cantInicial = stockInicial.getCantidad_disponible();
            System.out.println("• Inventario inicial de PROD001: " + cantInicial + " unidades.");

            List<DetallePedido> detalles = new ArrayList<>();
            detalles.add(new DetallePedido(null, null, "PROD001", 3, 0.0));

            Pedido ventaRealizada = ventaCtrl.procesarVenta("CLI001", "Calle 100 # 15-30, Bogota", detalles);
            System.out.println("• Venta realizada con éxito! ID Pedido: " + ventaRealizada.getPedido_id() + " | Total: $" + ventaRealizada.getMonto_total());

            Stock stockFinal = stockCtrl.buscarPorProductoId("PROD001");
            int cantFinal = stockFinal.getCantidad_disponible();
            System.out.println("• Inventario final de PROD001: " + cantFinal + " unidades.");

            if (cantFinal == cantInicial - 3) {
                System.out.println("CORRECTO: El descuento de inventario se ejecutó correctamente.");
            } else {
                System.err.println("ERROR: El inventario no coincide.");
            }

            restServer.stop();
            System.out.println("=== VERIFICACIÓN FINALIZADA ===");

        } catch (Exception e) {
            System.err.println("ERROR EN PRUEBA: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
