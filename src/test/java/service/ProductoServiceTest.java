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

    // ==========================================
    // PRUEBAS: listarProductos
    // ==========================================

    @Test
    public void testListarProductos() throws Exception {
        List<Producto> lista = productoService.listarProductos();
        assertNotNull(lista);
        assertTrue(lista.size() >= 4);
    }

    // ==========================================
    // PRUEBAS: buscarPorId
    // ==========================================

    @Test
    public void testBuscarPorIdExitoso() throws Exception {
        Producto p = productoService.buscarPorId("PROD001");
        assertNotNull(p);
        assertEquals("PROD001", p.getProducto_id());
        assertTrue(p.getPrecio_base() > 0);
    }

    @Test
    public void testBuscarPorIdConEspacios() throws Exception {
        Producto p = productoService.buscarPorId("   PROD001   ");
        assertNotNull(p);
        assertEquals("PROD001", p.getProducto_id());
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
    public void testBuscarPorIdNulo() {
        assertThrows(ValidationException.class, () -> {
            productoService.buscarPorId(null);
        });
    }

    // ==========================================
    // PRUEBAS: crearProducto
    // ==========================================

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
    public void testCrearProductoNulo() {
        assertThrows(ValidationException.class, () -> {
            productoService.crearProducto(null, 10, 2);
        });
    }

    @Test
    public void testCrearProductoIdNulo() {
        Producto nuevo = new Producto(null, "Test", "Desc", 1000.0, true);
        assertThrows(ValidationException.class, () -> {
            productoService.crearProducto(nuevo, 10, 2);
        });
    }

    @Test
    public void testCrearProductoIdVacio() {
        Producto nuevo = new Producto("   ", "Test", "Desc", 1000.0, true);
        assertThrows(ValidationException.class, () -> {
            productoService.crearProducto(nuevo, 10, 2);
        });
    }

    @Test
    public void testCrearProductoNombreNulo() {
        Producto nuevo = new Producto("PROD_TEST_NN", null, "Desc", 1000.0, true);
        assertThrows(ValidationException.class, () -> {
            productoService.crearProducto(nuevo, 10, 2);
        });
    }

    @Test
    public void testCrearProductoNombreVacio() {
        Producto nuevo = new Producto("PROD_TEST_NV", "   ", "Desc", 1000.0, true);
        assertThrows(ValidationException.class, () -> {
            productoService.crearProducto(nuevo, 10, 2);
        });
    }

    @Test
    public void testCrearProductoPrecioInvalido() {
        Producto nuevo = new Producto("PROD_TEST_2", "Audífonos", "Inalámbricos", -100.0, true);
        assertThrows(ValidationException.class, () -> {
            productoService.crearProducto(nuevo, 5, 1);
        });
    }

    @Test
    public void testCrearProductoCantidadInicialNegativa() {
        Producto nuevo = new Producto("PROD_TEST_3", "Cámara", "HD", 80000.0, true);
        assertThrows(ValidationException.class, () -> {
            productoService.crearProducto(nuevo, -5, 1);
        });
    }

    @Test
    public void testCrearProductoStockMinimoNegativo() {
        Producto nuevo = new Producto("PROD_TEST_4", "Microfono", "USB", 120000.0, true);
        assertThrows(ValidationException.class, () -> {
            productoService.crearProducto(nuevo, 10, -2);
        });
    }

    @Test
    public void testCrearProductoIdDuplicado() {
        Producto duplicado = new Producto("PROD001", "Laptop clon", "Duplicada", 3000000.0, true);
        assertThrows(ConflictException.class, () -> {
            productoService.crearProducto(duplicado, 5, 1);
        });
    }

    // ==========================================
    // PRUEBAS: actualizarProducto
    // ==========================================

    @Test
    public void testActualizarProductoExitoso() throws Exception {
        boolean actualizado = productoService.actualizarProducto(
                "PROD001", "Laptop Gaming Modificada", "Nueva descripción", 4900000.0, true);
        assertTrue(actualizado);

        Producto modificado = productoService.buscarPorId("PROD001");
        assertEquals("Laptop Gaming Modificada", modificado.getNombre());
        assertEquals("Nueva descripción", modificado.getDescripcion());
        assertEquals(4900000.0, modificado.getPrecio_base());
    }

    @Test
    public void testActualizarProductoSoloEstado() throws Exception {
        boolean actualizado = productoService.actualizarProducto("PROD001", null, null, -1, false);
        assertTrue(actualizado);

        Producto modificado = productoService.buscarPorId("PROD001");
        assertFalse(modificado.isEstado_activo());
        assertEquals("Laptop Gaming Pro 15", modificado.getNombre());
    }

    @Test
    public void testActualizarProductoPrecioCero() throws Exception {
        boolean actualizado = productoService.actualizarProducto("PROD001", null, null, 0.0, null);
        assertTrue(actualizado);

        Producto modificado = productoService.buscarPorId("PROD001");
        assertEquals(0.0, modificado.getPrecio_base());
    }

    @Test
    public void testActualizarProductoIdNulo() {
        assertThrows(ValidationException.class, () -> {
            productoService.actualizarProducto(null, "Nombre", "Desc", 1000.0, true);
        });
    }

    @Test
    public void testActualizarProductoIdVacio() {
        assertThrows(ValidationException.class, () -> {
            productoService.actualizarProducto("   ", "Nombre", "Desc", 1000.0, true);
        });
    }

    @Test
    public void testActualizarProductoPrecioNegativoInvalido() {
        assertThrows(ValidationException.class, () -> {
            productoService.actualizarProducto("PROD001", "Nombre", "Desc", -5.0, true);
        });
    }

    @Test
    public void testActualizarProductoInexistente() {
        assertThrows(ResourceNotFoundException.class, () -> {
            productoService.actualizarProducto("PROD_FANTASMA", "Nombre", "Desc", 1000.0, true);
        });
    }

    // ==========================================
    // PRUEBAS: eliminarProducto
    // ==========================================

    @Test
    public void testEliminarProductoExitoso() throws Exception {
        boolean eliminado = productoService.eliminarProducto("PROD002");
        assertTrue(eliminado);

        assertThrows(ResourceNotFoundException.class, () -> {
            productoService.buscarPorId("PROD002");
        });
        assertNull(stockDAO.obtenerPorProductoId("PROD002"), "El stock asociado debe eliminarse también");
    }

    @Test
    public void testEliminarProductoIdNulo() {
        assertThrows(ValidationException.class, () -> {
            productoService.eliminarProducto(null);
        });
    }

    @Test
    public void testEliminarProductoIdVacio() {
        assertThrows(ValidationException.class, () -> {
            productoService.eliminarProducto("   ");
        });
    }

    @Test
    public void testEliminarProductoInexistente() {
        assertThrows(ResourceNotFoundException.class, () -> {
            productoService.eliminarProducto("PROD_NO_EXISTE");
        });
    }
}
