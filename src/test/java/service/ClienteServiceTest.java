package service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import data_access.dao.ClienteDAO;
import data_access.impl.MemoryDAOFactory;
import exception.ConflictException;
import exception.ResourceNotFoundException;
import exception.ValidationException;
import model.Cliente;

public class ClienteServiceTest {

    private ClienteService clienteService;

    @BeforeEach
    public void setUp() {
        MemoryDAOFactory.reiniciarDatos();
        ClienteDAO clienteDAO = MemoryDAOFactory.getClienteDAO();
        clienteService = new ClienteService(clienteDAO);
    }

    @Test
    public void testListarClientes() throws Exception {
        List<Cliente> clientes = clienteService.listarClientes();
        assertNotNull(clientes);
        assertTrue(clientes.size() >= 2);
    }

    @Test
    public void testBuscarClienteExitoso() throws Exception {
        Cliente c = clienteService.buscarCliente("CLI001");
        assertNotNull(c);
        assertEquals("Alexis", c.getNombre());
    }

    @Test
    public void testBuscarClienteInexistente() {
        assertThrows(ResourceNotFoundException.class, () -> {
            clienteService.buscarCliente("CLI_INEXISTENTE");
        });
    }

    @Test
    public void testRegistrarClienteExitoso() throws Exception {
        Cliente nuevo = new Cliente("CLI099", "Laura", "Gomez", "laura.gomez@test.com", "+573009998877", "2026-10-17");
        boolean registrado = clienteService.registrarCliente(nuevo);

        assertTrue(registrado);
        Cliente consultado = clienteService.buscarCliente("CLI099");
        assertEquals("Laura", consultado.getNombre());
        assertEquals("laura.gomez@test.com", consultado.getEmail());
    }

    @Test
    public void testRegistrarClienteEmailInvalido() {
        Cliente invalido = new Cliente("CLI100", "Juan", "Perez", "correo-sin-arroba", "123", "2026-10-17");
        assertThrows(ValidationException.class, () -> {
            clienteService.registrarCliente(invalido);
        });
    }

    @Test
    public void testRegistrarClienteIdDuplicado() {
        Cliente duplicado = new Cliente("CLI001", "Alexis", "Copia", "otro.email@test.com", "123", "2026-10-17");
        assertThrows(ConflictException.class, () -> {
            clienteService.registrarCliente(duplicado);
        });
    }
}
