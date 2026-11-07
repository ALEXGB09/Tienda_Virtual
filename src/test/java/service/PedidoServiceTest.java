package service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import data_access.dao.PedidoDAO;
import data_access.impl.MemoryDAOFactory;
import exception.ResourceNotFoundException;
import exception.ValidationException;
import model.DetallePedido;
import model.Pedido;

public class PedidoServiceTest {

    private PedidoService pedidoService;
    private PedidoDAO pedidoDAO;

    @BeforeEach
    public void setUp() {
        MemoryDAOFactory.reiniciarDatos();
        this.pedidoDAO = MemoryDAOFactory.getPedidoDAO();
        this.pedidoService = new PedidoService(pedidoDAO);
    }

    @Test
    public void testListarPedidosInicialmenteVacio() throws Exception {
        List<Pedido> lista = pedidoService.listarPedidos();
        assertNotNull(lista, "La lista de pedidos no debe ser nula");
        assertTrue(lista.isEmpty(), "La lista inicial de pedidos debe estar vacía");
    }

    @Test
    public void testListarPedidosConDatos() throws Exception {
        Pedido pedido1 = new Pedido("PED001", "CLI001", "Calle 100 # 15-30", "2026-10-17T10:00:00", 4500000.0, "COMPLETADO");
        pedido1.getDetalles().add(new DetallePedido("DET001", "PED001", "PROD001", 1, 4500000.0));
        pedidoDAO.insertarPedidoConDetalles(pedido1);

        Pedido pedido2 = new Pedido("PED002", "CLI002", "Carrera 7 # 45-10", "2026-10-17T11:00:00", 250000.0, "COMPLETADO");
        pedidoDAO.insertarPedidoConDetalles(pedido2);

        List<Pedido> lista = pedidoService.listarPedidos();
        assertNotNull(lista);
        assertEquals(2, lista.size(), "Debe retornar los 2 pedidos ingresados");
    }

    @Test
    public void testBuscarPorIdExitoso() throws Exception {
        Pedido pedido = new Pedido("PED100", "CLI001", "Av. Circunvalar # 10-20", "2026-10-18T14:30:00", 9000000.0, "COMPLETADO");
        pedido.getDetalles().add(new DetallePedido("DET100", "PED100", "PROD001", 2, 4500000.0));
        pedidoDAO.insertarPedidoConDetalles(pedido);

        Pedido encontrado = pedidoService.buscarPorId("PED100");
        assertNotNull(encontrado);
        assertEquals("PED100", encontrado.getPedido_id());
        assertEquals("CLI001", encontrado.getCliente_id());
        assertEquals(9000000.0, encontrado.getMonto_total(), 0.001);
        assertEquals("COMPLETADO", encontrado.getEstado());
        assertEquals(1, encontrado.getDetalles().size());
        assertEquals("PROD001", encontrado.getDetalles().get(0).getProducto_id());
    }

    @Test
    public void testBuscarPorIdConEspacios() throws Exception {
        Pedido pedido = new Pedido("PED200", "CLI002", "Calle 26 # 68", "2026-10-18T16:00:00", 120000.0, "COMPLETADO");
        pedidoDAO.insertarPedidoConDetalles(pedido);

        // Caso límite: el ID enviado contiene espacios en blanco al inicio y al final
        Pedido encontrado = pedidoService.buscarPorId("   PED200   ");
        assertNotNull(encontrado);
        assertEquals("PED200", encontrado.getPedido_id());
    }

    @Test
    public void testBuscarPorIdInexistente() {
        assertThrows(ResourceNotFoundException.class, () -> {
            pedidoService.buscarPorId("PED_NO_EXISTE");
        }, "Debe lanzar ResourceNotFoundException si el pedido no existe");
    }

    @Test
    public void testBuscarPorIdNulo() {
        assertThrows(ValidationException.class, () -> {
            pedidoService.buscarPorId(null);
        }, "Debe lanzar ValidationException si el ID es nulo");
    }

    @Test
    public void testBuscarPorIdVacio() {
        assertThrows(ValidationException.class, () -> {
            pedidoService.buscarPorId("");
        }, "Debe lanzar ValidationException si el ID es una cadena vacía");
    }

    @Test
    public void testBuscarPorIdSoloEspacios() {
        assertThrows(ValidationException.class, () -> {
            pedidoService.buscarPorId("     ");
        }, "Debe lanzar ValidationException si el ID contiene únicamente espacios");
    }

    @Test
    public void testPedidoConMultiplesDetalles() throws Exception {
        Pedido pedido = new Pedido("PED300", "CLI001", "Calle 50 # 10-10", "2026-10-18T18:00:00", 4750000.0, "COMPLETADO");
        pedido.getDetalles().add(new DetallePedido("DET301", "PED300", "PROD001", 1, 4500000.0));
        pedido.getDetalles().add(new DetallePedido("DET302", "PED300", "PROD003", 1, 250000.0));
        pedidoDAO.insertarPedidoConDetalles(pedido);

        Pedido resultado = pedidoService.buscarPorId("PED300");
        assertNotNull(resultado);
        assertEquals(2, resultado.getDetalles().size());
        assertEquals("DET301", resultado.getDetalles().get(0).getDetalle_id());
        assertEquals("DET302", resultado.getDetalles().get(1).getDetalle_id());
    }
}
