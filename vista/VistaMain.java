package vista;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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
import model.Cliente;
import model.DetallePedido;
import model.Pedido;
import model.Producto;
import model.Stock;

public class VistaMain {

    private static Scanner scan = new Scanner(System.in);
    private static ProductoDAO productoDAO;
    private static StockDAO stockDAO;
    private static ClienteDAO clienteDAO;
    private static PedidoDAO pedidoDAO;

    private static ProductoController productoController;
    private static StockController stockController;
    private static ClienteController clienteController;
    private static VentaController ventaController;
    private static RestServer restServer;

    public static void main(String[] args) {
        try {
            productoDAO = MemoryDAOFactory.getProductoDAO();
            stockDAO = MemoryDAOFactory.getStockDAO();
            clienteDAO = MemoryDAOFactory.getClienteDAO();
            pedidoDAO = MemoryDAOFactory.getPedidoDAO();

            productoController = new ProductoController(productoDAO, stockDAO);
            stockController = new StockController(stockDAO);
            clienteController = new ClienteController(clienteDAO);
            ventaController = new VentaController(pedidoDAO, stockDAO, productoDAO, clienteDAO);

            restServer = new RestServer(8080, productoDAO, stockDAO, clienteDAO, pedidoDAO);
            restServer.start();

        } catch (Exception e) {
            System.out.println("Error al iniciar el sistema: " + e.getMessage());
            return;
        }

        byte opc;
        do {
            opc = menu();
            switch (opc) {
                case 1:
                    listarProductos();
                    break;
                case 2:
                    registrarProducto();
                    break;
                case 3:
                    consultarStock();
                    break;
                case 4:
                    procesarVenta();
                    break;
                case 5:
                    listarVentas();
                    break;
                case 6:
                    System.out.println("Cerrando la aplicación...");
                    restServer.stop();
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        } while (opc != 6);
    }

    private static byte menu() {
        System.out.println("\n--- TIENDA VIRTUAL ---");
        System.out.println("1. Listar catálogo de productos");
        System.out.println("2. Registrar nuevo producto");
        System.out.println("3. Consultar inventario disponible");
        System.out.println("4. Realizar una venta (descuenta inventario automáticamente)");
        System.out.println("5. Ver historial de ventas");
        System.out.println("6. Salir");
        System.out.print("Seleccione una opción: ");
        try {
            byte opc = scan.nextByte();
            scan.nextLine();
            return opc;
        } catch (Exception e) {
            scan.nextLine();
            return 0;
        }
    }

    private static void listarProductos() {
        System.out.println("\n--- CATÁLOGO DE PRODUCTOS ---");
        try {
            List<Producto> lista = productoController.listarProductos();
            if (lista.isEmpty()) {
                System.out.println("No hay productos registrados.");
                return;
            }
            for (Producto p : lista) {
                System.out.println("• [ID: " + p.getProducto_id() + "] " + p.getNombre() + 
                                   " | Precio: $" + p.getPrecio_base() + 
                                   " | Activo: " + (p.isEstado_activo() ? "Sí" : "No") + 
                                   " | Descripción: " + p.getDescripcion());
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void registrarProducto() {
        System.out.println("\n--- REGISTRAR NUEVO PRODUCTO ---");
        try {
            System.out.print("ID del producto (ej: PROD005): ");
            String id = scan.nextLine().trim();

            System.out.print("Nombre del producto: ");
            String nombre = scan.nextLine().trim();

            System.out.print("Descripción: ");
            String desc = scan.nextLine().trim();

            System.out.print("Precio base ($): ");
            double precio = scan.nextDouble();

            System.out.print("Cantidad inicial disponible: ");
            int cantidad = scan.nextInt();

            System.out.print("Mínimo de alerta para inventario: ");
            int minimo = scan.nextInt();
            scan.nextLine();

            Producto p = new Producto(id, nombre, desc, precio, true);
            if (productoController.crearProducto(p, cantidad, minimo)) {
                System.out.println("¡Producto guardado exitosamente en el catálogo e inventario!");
            }
        } catch (Exception e) {
            System.out.println("Error al registrar producto: " + e.getMessage());
            scan.nextLine();
        }
    }

    private static void consultarStock() {
        System.out.println("\n--- INVENTARIO DISPONIBLE ---");
        try {
            List<Stock> stockList = stockController.listarStock();
            if (stockList.isEmpty()) {
                System.out.println("No hay existencias registradas.");
                return;
            }
            for (Stock s : stockList) {
                Producto p = productoController.buscarPorId(s.getProducto_id());
                String nombreProd = (p != null) ? p.getNombre() : "Desconocido";
                System.out.println("• [Inventario ID: " + s.getStock_id() + "] Producto: " + nombreProd + 
                                   " (ID: " + s.getProducto_id() + ")" +
                                   " | Disponible: " + s.getCantidad_disponible() + 
                                   " | Mínimo: " + s.getStock_minimo() + 
                                   " | Última actualización: " + s.getUltima_actualizacion());
            }
        } catch (Exception e) {
            System.out.println("Error al consultar inventario: " + e.getMessage());
        }
    }

    private static void procesarVenta() {
        System.out.println("\n--- REGISTRO DE VENTA ---");
        try {
            System.out.print("ID del cliente (ej: CLI001): ");
            String clienteId = scan.nextLine().trim();

            Cliente cliente = clienteController.buscarCliente(clienteId);
            if (cliente == null) {
                System.out.println("Cliente no registrado. Registrando cliente de forma automática...");
                cliente = new Cliente(clienteId, "Cliente", "Invitado", clienteId + "@tienda.com", "0000000", "2026-09-25");
                clienteController.registrarCliente(cliente);
            }

            System.out.print("ID de producto a comprar (ej: PROD001): ");
            String prodId = scan.nextLine().trim();

            System.out.print("Cantidad a comprar: ");
            int cantidad = scan.nextInt();
            scan.nextLine();

            System.out.print("Dirección de envío: ");
            String dirEnvio = scan.nextLine().trim();

            List<DetallePedido> detalles = new ArrayList<>();
            detalles.add(new DetallePedido(null, null, prodId, cantidad, 0.0));

            Pedido pedido = ventaController.procesarVenta(clienteId, dirEnvio, detalles);

            System.out.println("\n¡VENTA REGISTRADA EXITOSAMENTE!");
            System.out.println("ID Pedido:     " + pedido.getPedido_id());
            System.out.println("Monto Total:   $" + pedido.getMonto_total());
            System.out.println("Estado Venta:  " + pedido.getEstado());
            System.out.println("-> El inventario del producto " + prodId + " se ha descontado automáticamente en " + cantidad + " unidades.");

        } catch (Exception e) {
            System.out.println("Error al procesar la venta: " + e.getMessage());
        }
    }

    private static void listarVentas() {
        System.out.println("\n--- HISTORIAL DE VENTAS ---");
        try {
            List<Pedido> pedidos = ventaController.listarVentas();
            if (pedidos.isEmpty()) {
                System.out.println("No hay ventas registradas aún.");
                return;
            }
            for (Pedido p : pedidos) {
                System.out.println("• [Pedido: " + p.getPedido_id() + "] Cliente: " + p.getCliente_id() + 
                                   " | Fecha: " + p.getFecha_pedido() + 
                                   " | Dirección: " + p.getDireccion_envio() +
                                   " | Estado: " + p.getEstado() + 
                                   " | Total: $" + p.getMonto_total());
            }
        } catch (Exception e) {
            System.out.println("Error al listar ventas: " + e.getMessage());
        }
    }
}
