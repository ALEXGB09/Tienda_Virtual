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

    @BeforeEach
    public void setUp() {
        MemoryDAOFactory.reiniciarDatos();
        ProductoDAO pDAO = MemoryDAOFactory.getProductoDAO();
        StockDAO sDAO = MemoryDAOFactory.getStockDAO();
        ClienteDAO cDAO = MemoryDAOFactory.getClienteDAO();
        PedidoDAO oDAO = MemoryDAOFactory.getPedidoDAO();

        this.productoDAO = pDAO;
        this.stockDAO = sDAO;
        this.ventaService = new VentaService(oDAO, sDAO, pDAO, cDAO);
    }

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
        assertEquals("COMPLETADO", pedido.getEstado());
        assertEquals(1, pedido.getDetalles().size());
        assertEquals(13500000.0, pedido.getMonto_total(), 0.001); // 4500000 * 3

        Stock stockDespues = stockDAO.obtenerPorProductoId("PROD001");
        assertEquals(cantidadAntes - 3, stockDespues.getCantidad_disponible());
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
    public void testProcesarVentaSinProductos() {
        List<DetallePedido> items = new ArrayList<>();

        assertThrows(ValidationException.class, () -> {
            ventaService.procesarVenta("CLI001", "Dirección", items);
        });
    }
}
