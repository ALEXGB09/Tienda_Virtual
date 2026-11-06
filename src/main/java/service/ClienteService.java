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
    private boolean estaVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    public List<Cliente> listarClientes() {
        return clienteDAO.obtenerTodos();
    }

    public Cliente buscarCliente(String clienteId) {
    if (estaVacio(clienteId)) {
        throw new ValidationException("El ID del cliente no puede estar vacío.");
    }

        Cliente cliente = clienteDAO.obtenerPorId(clienteId.trim());
        if (cliente == null) {
            throw new ResourceNotFoundException("No se encontró ningún cliente con el ID: " + clienteId.trim());
        }
        return cliente;
    }

    public boolean registrarCliente(Cliente cliente)  {
        if (cliente == null) {
            throw new ValidationException("Los datos del cliente no pueden ser nulos.");
        }

        if (estaVacio(cliente.getCliente_id())) {
    throw new ValidationException("El ID del cliente es obligatorio.");
        }

        if (estaVacio(cliente.getNombre())) {
    throw new ValidationException("El nombre del cliente es obligatorio.");
        }

        if (estaVacio(cliente.getApellido())) {
    throw new ValidationException("El apellido del cliente es obligatorio.");
    }

        if (estaVacio(cliente.getEmail()) || !cliente.getEmail().contains("@")) {
    throw new ValidationException("Se requiere una dirección de correo electrónico válida.");
    }

        String idCliente = cliente.getCliente_id().trim();
        if (clienteDAO.obtenerPorId(idCliente) != null) {
            throw new ConflictException("Ya existe un cliente con el ID: " + idCliente);
        }

        for (Cliente existente : clienteDAO.obtenerTodos()) {
            if (existente.getEmail() != null && existente.getEmail().equalsIgnoreCase(cliente.getEmail().trim())) {
                throw new ConflictException("Ya existe un cliente registrado con el correo: " + cliente.getEmail());
            }
        }

        if (cliente.getFecha_registro() == null || cliente.getFecha_registro().trim().isEmpty()) {
            cliente.setFecha_registro(LocalDate.now().toString());
        }

        cliente.setCliente_id(idCliente);
        cliente.setNombre(cliente.getNombre().trim());
        cliente.setApellido(cliente.getApellido().trim());
        cliente.setEmail(cliente.getEmail().trim());

        return clienteDAO.insertar(cliente);
    }
}
