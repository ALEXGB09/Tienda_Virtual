package service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
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

    // ==========================================
    // PRUEBAS: listarClientes
    // ==========================================

    @Test
    public void testListarClientes() throws Exception {
        List<Cliente> clientes = clienteService.listarClientes();
        assertNotNull(clientes);
        assertTrue(clientes.size() >= 2);
    }

    // ==========================================
    // PRUEBAS: buscarCliente
    // ==========================================

    @Test
    public void testBuscarClienteExitoso() throws Exception {
        Cliente c = clienteService.buscarCliente("CLI001");
        assertNotNull(c);
        assertEquals("Alexis", c.getNombre());
        assertEquals("Giraldo", c.getApellido());
        assertEquals("alexis.giraldo@email.com", c.getEmail());
    }

    @Test
    public void testBuscarClienteConEspaciosEnId() throws Exception {
        Cliente c = clienteService.buscarCliente("   CLI001   ");
        assertNotNull(c);
        assertEquals("CLI001", c.getCliente_id());
    }

    @Test
    public void testBuscarClienteInexistente() {
        assertThrows(ResourceNotFoundException.class, () -> {
            clienteService.buscarCliente("CLI_INEXISTENTE");
        });
    }

    @Test
    public void testBuscarClienteIdNulo() {
        assertThrows(ValidationException.class, () -> {
            clienteService.buscarCliente(null);
        });
    }

    @Test
    public void testBuscarClienteIdVacio() {
        assertThrows(ValidationException.class, () -> {
            clienteService.buscarCliente("");
        });
    }

    @Test
    public void testBuscarClienteIdSoloEspacios() {
        assertThrows(ValidationException.class, () -> {
            clienteService.buscarCliente("     ");
        });
    }

    // ==========================================
    // PRUEBAS: registrarCliente
    // ==========================================

    @Test
    public void testRegistrarClienteExitoso() throws Exception {
        Cliente nuevo = new Cliente("CLI099", "Laura", "Gomez", "laura.gomez@test.com", "+573009998877", "2026-10-17");
        boolean registrado = clienteService.registrarCliente(nuevo);

        assertTrue(registrado);
        Cliente consultado = clienteService.buscarCliente("CLI099");
        assertEquals("Laura", consultado.getNombre());
        assertEquals("laura.gomez@test.com", consultado.getEmail());
        assertEquals("2026-10-17", consultado.getFecha_registro());
    }

    @Test
    public void testRegistrarClienteSinFechaRegistroAsignaFechaActual() throws Exception {
        Cliente nuevo = new Cliente("CLI088", "Diana", "Torres", "diana.torres@test.com", "+573112223344", null);
        boolean registrado = clienteService.registrarCliente(nuevo);

        assertTrue(registrado);
        Cliente consultado = clienteService.buscarCliente("CLI088");
        assertEquals(LocalDate.now().toString(), consultado.getFecha_registro(), "Debe asignar la fecha del día si es nula");
    }

    @Test
    public void testRegistrarClienteConFechaRegistroVaciaAsignaFechaActual() throws Exception {
        Cliente nuevo = new Cliente("CLI089", "Julian", "Mora", "julian.mora@test.com", "+573112223355", "   ");
        boolean registrado = clienteService.registrarCliente(nuevo);

        assertTrue(registrado);
        Cliente consultado = clienteService.buscarCliente("CLI089");
        assertEquals(LocalDate.now().toString(), consultado.getFecha_registro(), "Debe asignar la fecha del día si está vacía");
    }

    @Test
    public void testRegistrarClienteRecortaEspacios() throws Exception {
        Cliente nuevo = new Cliente("  CLI077  ", "  Esteban  ", "  Castro  ", "  esteban.castro@test.com  ", "123", "2026-10-17");
        boolean registrado = clienteService.registrarCliente(nuevo);

        assertTrue(registrado);
        Cliente consultado = clienteService.buscarCliente("CLI077");
        assertEquals("CLI077", consultado.getCliente_id());
        assertEquals("Esteban", consultado.getNombre());
        assertEquals("Castro", consultado.getApellido());
        assertEquals("esteban.castro@test.com", consultado.getEmail());
    }

    @Test
    public void testRegistrarClienteNulo() {
        assertThrows(ValidationException.class, () -> {
            clienteService.registrarCliente(null);
        });
    }

    @Test
    public void testRegistrarClienteIdNulo() {
        Cliente cliente = new Cliente(null, "Pedro", "Alvarez", "pedro@test.com", "123", "2026-10-17");
        assertThrows(ValidationException.class, () -> {
            clienteService.registrarCliente(cliente);
        });
    }

    @Test
    public void testRegistrarClienteIdVacio() {
        Cliente cliente = new Cliente("   ", "Pedro", "Alvarez", "pedro@test.com", "123", "2026-10-17");
        assertThrows(ValidationException.class, () -> {
            clienteService.registrarCliente(cliente);
        });
    }

    @Test
    public void testRegistrarClienteNombreNulo() {
        Cliente cliente = new Cliente("CLI070", null, "Alvarez", "pedro@test.com", "123", "2026-10-17");
        assertThrows(ValidationException.class, () -> {
            clienteService.registrarCliente(cliente);
        });
    }

    @Test
    public void testRegistrarClienteNombreVacio() {
        Cliente cliente = new Cliente("CLI071", "   ", "Alvarez", "pedro@test.com", "123", "2026-10-17");
        assertThrows(ValidationException.class, () -> {
            clienteService.registrarCliente(cliente);
        });
    }

    @Test
    public void testRegistrarClienteApellidoNulo() {
        Cliente cliente = new Cliente("CLI072", "Pedro", null, "pedro@test.com", "123", "2026-10-17");
        assertThrows(ValidationException.class, () -> {
            clienteService.registrarCliente(cliente);
        });
    }

    @Test
    public void testRegistrarClienteApellidoVacio() {
        Cliente cliente = new Cliente("CLI073", "Pedro", "    ", "pedro@test.com", "123", "2026-10-17");
        assertThrows(ValidationException.class, () -> {
            clienteService.registrarCliente(cliente);
        });
    }

    @Test
    public void testRegistrarClienteEmailNulo() {
        Cliente invalido = new Cliente("CLI100", "Juan", "Perez", null, "123", "2026-10-17");
        assertThrows(ValidationException.class, () -> {
            clienteService.registrarCliente(invalido);
        });
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

    @Test
    public void testRegistrarClienteEmailDuplicado() {
        // Mismo email que CLI001 pero diferente ID
        Cliente duplicadoEmail = new Cliente("CLI999", "Otro", "Nombre", "alexis.giraldo@email.com", "123", "2026-10-17");
        assertThrows(ConflictException.class, () -> {
            clienteService.registrarCliente(duplicadoEmail);
        });
    }

    @Test
    public void testRegistrarClienteEmailDuplicadoCaseInsensitive() {
        // Mismo email en mayúsculas
        Cliente duplicadoEmail = new Cliente("CLI998", "Otro", "Nombre", "ALEXIS.GIRALDO@EMAIL.COM", "123", "2026-10-17");
        assertThrows(ConflictException.class, () -> {
            clienteService.registrarCliente(duplicadoEmail);
        });
    }
}
