package service;

import java.time.LocalDate;
import java.util.List;

import data_access.dao.ClienteDAO;
import exception.ConflictException;
import exception.ResourceNotFoundException;
import exception.ValidationException;
import model.Cliente;

public class ClienteService {

    private final ClienteDAO clienteDAO;

    public ClienteService(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    public List<Cliente> listarClientes() throws Exception {
        return clienteDAO.obtenerTodos();
    }

    public Cliente buscarCliente(String clienteId) throws Exception {
        if (clienteId == null || clienteId.trim().isEmpty()) {
            throw new ValidationException("El ID del cliente no puede estar vacío.");
        }

        Cliente cliente = clienteDAO.obtenerPorId(clienteId.trim());
        if (cliente == null) {
            throw new ResourceNotFoundException("No se encontró ningún cliente con el ID: " + clienteId.trim());
        }
        return cliente;
    }

    public boolean registrarCliente(Cliente cliente) throws Exception {
        if (cliente == null) {
            throw new ValidationException("Los datos del cliente no pueden ser nulos.");
        }

        if (cliente.getCliente_id() == null || cliente.getCliente_id().trim().isEmpty()) {
            throw new ValidationException("El ID del cliente es obligatorio.");
        }

        if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
            throw new ValidationException("El nombre del cliente es obligatorio.");
        }

        if (cliente.getApellido() == null || cliente.getApellido().trim().isEmpty()) {
            throw new ValidationException("El apellido del cliente es obligatorio.");
        }

        if (cliente.getEmail() == null || !cliente.getEmail().contains("@")) {
            throw new ValidationException("Se requiere una dirección de correo electrónico válida.");
        }

        String idLimpio = cliente.getCliente_id().trim();
        if (clienteDAO.obtenerPorId(idLimpio) != null) {
            throw new ConflictException("Ya existe un cliente con el ID: " + idLimpio);
        }

        // Validar unicidad de email
        for (Cliente existente : clienteDAO.obtenerTodos()) {
            if (existente.getEmail() != null && existente.getEmail().equalsIgnoreCase(cliente.getEmail().trim())) {
                throw new ConflictException("Ya existe un cliente registrado con el correo: " + cliente.getEmail());
            }
        }

        if (cliente.getFecha_registro() == null || cliente.getFecha_registro().trim().isEmpty()) {
            cliente.setFecha_registro(LocalDate.now().toString());
        }

        cliente.setCliente_id(idLimpio);
        cliente.setNombre(cliente.getNombre().trim());
        cliente.setApellido(cliente.getApellido().trim());
        cliente.setEmail(cliente.getEmail().trim());

        return clienteDAO.insertar(cliente);
    }
}
