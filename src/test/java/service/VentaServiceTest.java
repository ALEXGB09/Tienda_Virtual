package service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import data_access.dao.ClienteDAO;
import data_access.dao.PedidoDAO;
import data_access.dao.ProductoDAO;
import data_access.dao.StockDAO;
import data_access.impl.MemoryDAOFactory;
import exception.ConflictException;
import exception.InsufficientStockException;
import exception.ResourceNotFoundException;
import exception.ValidationException;
import model.DetallePedido;
import model.Pedido;
import model.Producto;
import model.Stock;

public class VentaServiceTest {

    private VentaService ventaService;
    private StockDAO stockDAO;
    private ProductoDAO productoDAO;
    private PedidoDAO pedidoDAO;
    private ClienteDAO clienteDAO;

    @BeforeEach
    public void setUp() {
        MemoryDAOFactory.reiniciarDatos();
        this.productoDAO = MemoryDAOFactory.getProductoDAO();
        this.stockDAO = MemoryDAOFactory.getStockDAO();
        this.clienteDAO = MemoryDAOFactory.getClienteDAO();
        this.pedidoDAO = MemoryDAOFactory.getPedidoDAO();

        this.ventaService = new VentaService(pedidoDAO, stockDAO, productoDAO, clienteDAO);
    }

    // ==========================================
    // PRUEBAS: listarVentas
    // ==========================================

    @Test
    public void testListarVentasInicialmenteVacio() throws Exception {
        List<Pedido> ventas = ventaService.listarVentas();
        assertNotNull(ventas);
        assertTrue(ventas.isEmpty(), "Inicialmente no debe haber ventas registradas");
    }

    @Test
    public void testListarVentasConVentasRealizadas() throws Exception {
        List<DetallePedido> items = new ArrayList<>();
        items.add(new DetallePedido(null, null, "PROD001", 1, 0.0));
        ventaService.procesarVenta("CLI001", "Calle 100", items);

        List<Pedido> ventas = ventaService.listarVentas();
        assertNotNull(ventas);
        assertEquals(1, ventas.size());
    }

    // ==========================================
    // PRUEBAS: buscarVentaPorId
    // ==========================================

    @Test
    public void testBuscarVentaPorIdExitoso() throws Exception {
        List<DetallePedido> items = new ArrayList<>();
        items.add(new DetallePedido(null, null, "PROD002", 2, 0.0));
        Pedido venta = ventaService.procesarVenta("CLI001", "Calle 100", items);

        Pedido encontrada = ventaService.buscarVentaPorId(venta.getPedido_id());
        assertNotNull(encontrada);
        assertEquals(venta.getPedido_id(), encontrada.getPedido_id());
        assertEquals(240000.0, encontrada.getMonto_total(), 0.001);
    }

    @Test
    public void testBuscarVentaPorIdConEspacios() throws Exception {
        List<DetallePedido> items = new ArrayList<>();
        items.add(new DetallePedido(null, null, "PROD002", 1, 0.0));
        Pedido venta = ventaService.procesarVenta("CLI001", "Calle 100", items);

        Pedido encontrada = ventaService.buscarVentaPorId("   " + venta.getPedido_id() + "   ");
        assertNotNull(encontrada);
        assertEquals(venta.getPedido_id(), encontrada.getPedido_id());
    }

    @Test
    public void testBuscarVentaPorIdInexistente() {
        assertThrows(ResourceNotFoundException.class, () -> {
            ventaService.buscarVentaPorId("PED_INEXISTENTE");
        });
    }

    @Test
    public void testBuscarVentaPorIdNulo() {
        assertThrows(ValidationException.class, () -> {
            ventaService.buscarVentaPorId(null);
        });
    }

    @Test
    public void testBuscarVentaPorIdVacio() {
        assertThrows(ValidationException.class, () -> {
            ventaService.buscarVentaPorId("   ");
        });
    }

    // ==========================================
    // PRUEBAS: procesarVenta (Escenarios Exitosos)
    // ==========================================

    @Test
    public void testProcesarVentaExitosa() throws Exception {
        Stock stockAntes = stockDAO.obtenerPorProductoId("PROD001");
        int cantidadAntes = stockAntes.getCantidad_disponible();

        List<DetallePedido> items = new ArrayList<>();
        items.add(new DetallePedido(null, null, "PROD001", 3, 0.0));

        Pedido pedido = ventaService.procesarVenta("CLI001", "Calle 100 # 15-30", items);

        assertNotNull(pedido);
        assertNotNull(pedido.getPedido_id());
        assertEquals("CLI001", pedido.getCliente_id());
        assertEquals("Calle 100 # 15-30", pedido.getDireccion_envio());
        assertEquals("COMPLETADO", pedido.getEstado());
        assertEquals(1, pedido.getDetalles().size());
        assertEquals(13500000.0, pedido.getMonto_total(), 0.001); // 4500000 * 3

        Stock stockDespues = stockDAO.obtenerPorProductoId("PROD001");
        assertEquals(cantidadAntes - 3, stockDespues.getCantidad_disponible());
    }

    @Test
    public void testProcesarVentaDireccionEnvioPorDefecto() throws Exception {
        List<DetallePedido> items = new ArrayList<>();
        items.add(new DetallePedido(null, null, "PROD002", 1, 0.0));

        // Enviar dirección nula o vacía
        Pedido pedido1 = ventaService.procesarVenta("CLI001", null, items);
        assertEquals("Dirección Principal", pedido1.getDireccion_envio());

        Pedido pedido2 = ventaService.procesarVenta("CLI001", "   ", items);
        assertEquals("Dirección Principal", pedido2.getDireccion_envio());
    }

    @Test
    public void testProcesarVentaMultiplesProductos() throws Exception {
        List<DetallePedido> items = new ArrayList<>();
        items.add(new DetallePedido(null, null, "PROD001", 1, 0.0)); // 4,500,000
        items.add(new DetallePedido(null, null, "PROD002", 2, 0.0)); // 120,000 * 2 = 240,000
        items.add(new DetallePedido(null, null, "PROD003", 1, 0.0)); // 250,000

        Pedido pedido = ventaService.procesarVenta("CLI001", "Carrera 15 # 80", items);

        assertNotNull(pedido);
        assertEquals(3, pedido.getDetalles().size());
        assertEquals(4990000.0, pedido.getMonto_total(), 0.001);

        assertEquals(9, stockDAO.obtenerPorProductoId("PROD001").getCantidad_disponible());
        assertEquals(48, stockDAO.obtenerPorProductoId("PROD002").getCantidad_disponible());
        assertEquals(29, stockDAO.obtenerPorProductoId("PROD003").getCantidad_disponible());
    }

    @Test
    public void testProcesarVentaMismoProductoRepetido() throws Exception {
        // PROD001 tiene 10 disponibles
        List<DetallePedido> items = new ArrayList<>();
        items.add(new DetallePedido(null, null, "PROD001", 2, 0.0));
        items.add(new DetallePedido(null, null, "PROD001", 3, 0.0));

        Pedido pedido = ventaService.procesarVenta("CLI001", "Av El Dorado", items);

        assertNotNull(pedido);
        assertEquals(2, pedido.getDetalles().size());
        assertEquals(22500000.0, pedido.getMonto_total(), 0.001); // (2+3) * 4500000

        Stock stockFinal = stockDAO.obtenerPorProductoId("PROD001");
        assertEquals(5, stockFinal.getCantidad_disponible()); // 10 - 5
    }

    // ==========================================
    // PRUEBAS: procesarVenta (Escenarios de Error y Casos Límite)
    // ==========================================

    @Test
    public void testProcesarVentaMismoProductoRepetidoExcedeStock() throws Exception {
        // PROD001 tiene 10 disponibles. Se solicitan 6 + 5 = 11
        List<DetallePedido> items = new ArrayList<>();
        items.add(new DetallePedido(null, null, "PROD001", 6, 0.0));
        items.add(new DetallePedido(null, null, "PROD001", 5, 0.0));

        assertThrows(InsufficientStockException.class, () -> {
            ventaService.procesarVenta("CLI001", "Av El Dorado", items);
        });

        // Comprobar que no se descontó inventario
        assertEquals(10, stockDAO.obtenerPorProductoId("PROD001").getCantidad_disponible());
    }

    @Test
    public void testProcesarVentaClienteInexistente() {
        List<DetallePedido> items = new ArrayList<>();
        items.add(new DetallePedido(null, null, "PROD001", 1, 0.0));

        assertThrows(ResourceNotFoundException.class, () -> {
            ventaService.procesarVenta("CLI_FANTASMA", "Dirección", items);
        });
    }

    @Test
    public void testProcesarVentaClienteIdNulo() {
        List<DetallePedido> items = new ArrayList<>();
        items.add(new DetallePedido(null, null, "PROD001", 1, 0.0));

        assertThrows(ValidationException.class, () -> {
            ventaService.procesarVenta(null, "Dirección", items);
        });
    }

    @Test
    public void testProcesarVentaClienteIdVacio() {
        List<DetallePedido> items = new ArrayList<>();
        items.add(new DetallePedido(null, null, "PROD001", 1, 0.0));

        assertThrows(ValidationException.class, () -> {
            ventaService.procesarVenta("    ", "Dirección", items);
        });
    }

    @Test
    public void testProcesarVentaSinProductos() {
        List<DetallePedido> items = new ArrayList<>();

        assertThrows(ValidationException.class, () -> {
            ventaService.procesarVenta("CLI001", "Dirección", items);
        });
    }

    @Test
    public void testProcesarVentaItemsNulo() {
        assertThrows(ValidationException.class, () -> {
            ventaService.procesarVenta("CLI001", "Dirección", null);
        });
    }

    @Test
    public void testProcesarVentaItemProductoIdNulo() {
        List<DetallePedido> items = new ArrayList<>();
        items.add(new DetallePedido(null, null, null, 1, 0.0));

        assertThrows(ValidationException.class, () -> {
            ventaService.procesarVenta("CLI001", "Dirección", items);
        });
    }

    @Test
    public void testProcesarVentaItemProductoIdVacio() {
        List<DetallePedido> items = new ArrayList<>();
        items.add(new DetallePedido(null, null, "   ", 1, 0.0));

        assertThrows(ValidationException.class, () -> {
            ventaService.procesarVenta("CLI001", "Dirección", items);
        });
    }

    @Test
    public void testProcesarVentaProductoInexistente() {
        List<DetallePedido> items = new ArrayList<>();
        items.add(new DetallePedido(null, null, "PROD_INEXISTENTE", 1, 0.0));

        assertThrows(ResourceNotFoundException.class, () -> {
            ventaService.procesarVenta("CLI001", "Dirección", items);
        });
    }

    @Test
    public void testProcesarVentaProductoInactivo() throws Exception {
        // Desactivar PROD003
        Producto prod = productoDAO.obtenerPorId("PROD003");
        prod.setEstado_activo(false);
        productoDAO.actualizar(prod);

        List<DetallePedido> items = new ArrayList<>();
        items.add(new DetallePedido(null, null, "PROD003", 1, 0.0));

        assertThrows(ConflictException.class, () -> {
            ventaService.procesarVenta("CLI001", "Dirección", items);
        });
    }

    @Test
    public void testProcesarVentaStockInsuficiente() {
        List<DetallePedido> items = new ArrayList<>();
        items.add(new DetallePedido(null, null, "PROD001", 9999, 0.0));

        assertThrows(InsufficientStockException.class, () -> {
            ventaService.procesarVenta("CLI001", "Dirección", items);
        });
    }

    @Test
    public void testProcesarVentaCantidadInvalida() {
        List<DetallePedido> items = new ArrayList<>();
        items.add(new DetallePedido(null, null, "PROD001", 0, 0.0));

        assertThrows(ValidationException.class, () -> {
            ventaService.procesarVenta("CLI001", "Dirección", items);
        });
    }

    @Test
    public void testProcesarVentaCantidadNegativa() {
        List<DetallePedido> items = new ArrayList<>();
        items.add(new DetallePedido(null, null, "PROD001", -5, 0.0));

        assertThrows(ValidationException.class, () -> {
            ventaService.procesarVenta("CLI001", "Dirección", items);
        });
    }

    @Test
    public void testProcesarVentaRollbackInventarioAlFallarPersistencia() throws Exception {
        // Mock de PedidoDAO que falla al persistir el pedido
        PedidoDAO failingPedidoDAO = new PedidoDAO() {
            @Override
            public List<Pedido> obtenerTodos() { return List.of(); }

            @Override
            public Pedido obtenerPorId(String id) { return null; }

            @Override
            public boolean insertarPedidoConDetalles(Pedido pedido) throws Exception {
                throw new RuntimeException("Error simulado en base de datos al guardar pedido");
            }
        };

        VentaService servicioConFallo = new VentaService(failingPedidoDAO, stockDAO, productoDAO, clienteDAO);

        int stockOriginal = stockDAO.obtenerPorProductoId("PROD001").getCantidad_disponible();

        List<DetallePedido> items = new ArrayList<>();
        items.add(new DetallePedido(null, null, "PROD001", 2, 0.0));

        assertThrows(RuntimeException.class, () -> {
            servicioConFallo.procesarVenta("CLI001", "Calle 100", items);
        });

        // Validar que tras el fallo, el inventario fue restaurado (compensación/rollback)
        Stock stockActual = stockDAO.obtenerPorProductoId("PROD001");
        assertEquals(stockOriginal, stockActual.getCantidad_disponible(),
                "El inventario descontado debe ser restaurado ante error en persistencia");
    }
}
