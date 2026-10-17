package service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import data_access.dao.ProductoDAO;
import data_access.dao.StockDAO;
import data_access.impl.MemoryDAOFactory;
import exception.ConflictException;
import exception.ResourceNotFoundException;
import exception.ValidationException;
import model.Producto;

public class ProductoServiceTest {

    private ProductoService productoService;
    private StockDAO stockDAO;

    @BeforeEach
    public void setUp() {
        MemoryDAOFactory.reiniciarDatos();
        ProductoDAO productoDAO = MemoryDAOFactory.getProductoDAO();
        stockDAO = MemoryDAOFactory.getStockDAO();
        productoService = new ProductoService(productoDAO, stockDAO);
    }

    @Test
    public void testListarProductos() throws Exception {
        List<Producto> lista = productoService.listarProductos();
        assertNotNull(lista);
        assertTrue(lista.size() >= 4);
    }

    @Test
    public void testBuscarPorIdExitoso() throws Exception {
        Producto p = productoService.buscarPorId("PROD001");
        assertNotNull(p);
        assertEquals("PROD001", p.getProducto_id());
        assertTrue(p.getPrecio_base() > 0);
    }

    @Test
    public void testBuscarPorIdInexistente() {
        assertThrows(ResourceNotFoundException.class, () -> {
            productoService.buscarPorId("PROD_INEXISTENTE");
        });
    }

    @Test
    public void testBuscarPorIdVacio() {
        assertThrows(ValidationException.class, () -> {
            productoService.buscarPorId("   ");
        });
    }

    @Test
    public void testCrearProductoExitoso() throws Exception {
        Producto nuevo = new Producto("PROD_TEST_1", "Mousepad Gamer", "Superficie de tela 90x40", 50000.0, true);
        boolean creado = productoService.crearProducto(nuevo, 15, 2);

        assertTrue(creado);
        Producto recuperado = productoService.buscarPorId("PROD_TEST_1");
        assertEquals("Mousepad Gamer", recuperado.getNombre());
        assertNotNull(stockDAO.obtenerPorProductoId("PROD_TEST_1"));
    }

    @Test
    public void testCrearProductoPrecioInvalido() {
        Producto nuevo = new Producto("PROD_TEST_2", "Audífonos", "Inalámbricos", -100.0, true);
        assertThrows(ValidationException.class, () -> {
            productoService.crearProducto(nuevo, 5, 1);
        });
    }

    @Test
    public void testCrearProductoIdDuplicado() {
        Producto duplicado = new Producto("PROD001", "Laptop clon", "Duplicada", 3000000.0, true);
        assertThrows(ConflictException.class, () -> {
            productoService.crearProducto(duplicado, 5, 1);
        });
    }

    @Test
    public void testActualizarProductoExitoso() throws Exception {
        boolean actualizado = productoService.actualizarProducto(
                "PROD001", "Laptop Gaming Modificada", "Nueva descripción", 4900000.0, true);
        assertTrue(actualizado);

        Producto modificado = productoService.buscarPorId("PROD001");
        assertEquals("Laptop Gaming Modificada", modificado.getNombre());
        assertEquals(4900000.0, modificado.getPrecio_base());
    }

    @Test
    public void testActualizarProductoInexistente() {
        assertThrows(ResourceNotFoundException.class, () -> {
            productoService.actualizarProducto("PROD_FANTASMA", "Nombre", "Desc", 1000.0, true);
        });
    }

    @Test
    public void testEliminarProductoExitoso() throws Exception {
        boolean eliminado = productoService.eliminarProducto("PROD002");
        assertTrue(eliminado);

        assertThrows(ResourceNotFoundException.class, () -> {
            productoService.buscarPorId("PROD002");
        });
    }

    @Test
    public void testEliminarProductoInexistente() {
        assertThrows(ResourceNotFoundException.class, () -> {
            productoService.eliminarProducto("PROD_NO_EXISTE");
        });
    }
}
