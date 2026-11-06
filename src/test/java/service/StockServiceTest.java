package service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import data_access.dao.StockDAO;
import data_access.impl.MemoryDAOFactory;
import exception.InsufficientStockException;
import exception.ResourceNotFoundException;
import exception.ValidationException;
import model.Stock;

public class StockServiceTest {

    private StockService stockService;

    @BeforeEach
    public void setUp() {
        MemoryDAOFactory.reiniciarDatos();
        StockDAO stockDAO = MemoryDAOFactory.getStockDAO();
        stockService = new StockService(stockDAO);
    }

    // ==========================================
    // PRUEBAS: listarStock
    // ==========================================

    @Test
    public void testListarStock() throws Exception {
        List<Stock> list = stockService.listarStock();
        assertNotNull(list);
        assertTrue(list.size() >= 4);
    }

    // ==========================================
    // PRUEBAS: buscarPorProductoId
    // ==========================================

    @Test
    public void testBuscarPorProductoId() throws Exception {
        Stock stock = stockService.buscarPorProductoId("PROD001");
        assertNotNull(stock);
        assertEquals("PROD001", stock.getProducto_id());
        assertEquals(10, stock.getCantidad_disponible());
    }

    @Test
    public void testBuscarPorProductoIdConEspacios() throws Exception {
        Stock stock = stockService.buscarPorProductoId("   PROD001   ");
        assertNotNull(stock);
        assertEquals("PROD001", stock.getProducto_id());
    }

    @Test
    public void testBuscarPorProductoIdInexistente() {
        assertThrows(ResourceNotFoundException.class, () -> {
            stockService.buscarPorProductoId("PROD_INEXISTENTE");
        });
    }

    @Test
    public void testBuscarPorProductoIdNulo() {
        assertThrows(ValidationException.class, () -> {
            stockService.buscarPorProductoId(null);
        });
    }

    @Test
    public void testBuscarPorProductoIdVacio() {
        assertThrows(ValidationException.class, () -> {
            stockService.buscarPorProductoId("   ");
        });
    }

    // ==========================================
    // PRUEBAS: actualizarCantidad
    // ==========================================

    @Test
    public void testActualizarCantidadExitoso() throws Exception {
        boolean actualizado = stockService.actualizarCantidad("PROD001", 35);
        assertTrue(actualizado);

        Stock stock = stockService.buscarPorProductoId("PROD001");
        assertEquals(35, stock.getCantidad_disponible());
    }

    @Test
    public void testActualizarCantidadACero() throws Exception {
        boolean actualizado = stockService.actualizarCantidad("PROD001", 0);
        assertTrue(actualizado);

        Stock stock = stockService.buscarPorProductoId("PROD001");
        assertEquals(0, stock.getCantidad_disponible());
    }

    @Test
    public void testActualizarCantidadNegativa() {
        assertThrows(ValidationException.class, () -> {
            stockService.actualizarCantidad("PROD001", -5);
        });
    }

    @Test
    public void testActualizarCantidadProductoIdNulo() {
        assertThrows(ValidationException.class, () -> {
            stockService.actualizarCantidad(null, 10);
        });
    }

    @Test
    public void testActualizarCantidadProductoIdVacio() {
        assertThrows(ValidationException.class, () -> {
            stockService.actualizarCantidad("   ", 10);
        });
    }

    @Test
    public void testActualizarCantidadProductoInexistente() {
        assertThrows(ResourceNotFoundException.class, () -> {
            stockService.actualizarCantidad("PROD_FANTASMA", 10);
        });
    }

    // ==========================================
    // PRUEBAS: descontarStock
    // ==========================================

    @Test
    public void testDescontarStockExitoso() throws Exception {
        boolean descontado = stockService.descontarStock("PROD001", 4);
        assertTrue(descontado);

        Stock stock = stockService.buscarPorProductoId("PROD001");
        assertEquals(6, stock.getCantidad_disponible());
    }

    @Test
    public void testDescontarStockExactamenteDisponible() throws Exception {
        // PROD001 tiene 10 unidades
        boolean descontado = stockService.descontarStock("PROD001", 10);
        assertTrue(descontado);

        Stock stock = stockService.buscarPorProductoId("PROD001");
        assertEquals(0, stock.getCantidad_disponible());
    }

    @Test
    public void testDescontarStockInsuficiente() {
        assertThrows(InsufficientStockException.class, () -> {
            stockService.descontarStock("PROD001", 999);
        });
    }

    @Test
    public void testDescontarStockCantidadCero() {
        assertThrows(ValidationException.class, () -> {
            stockService.descontarStock("PROD001", 0);
        });
    }

    @Test
    public void testDescontarStockCantidadNegativa() {
        assertThrows(ValidationException.class, () -> {
            stockService.descontarStock("PROD001", -3);
        });
    }

    @Test
    public void testDescontarStockProductoIdNulo() {
        assertThrows(ValidationException.class, () -> {
            stockService.descontarStock(null, 2);
        });
    }

    @Test
    public void testDescontarStockProductoIdVacio() {
        assertThrows(ValidationException.class, () -> {
            stockService.descontarStock("   ", 2);
        });
    }

    @Test
    public void testDescontarStockProductoInexistente() {
        assertThrows(ResourceNotFoundException.class, () -> {
            stockService.descontarStock("PROD_INEXISTENTE", 2);
        });
    }
}
