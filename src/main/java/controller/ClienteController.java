package controller;

import java.util.List;
import data_access.dao.ClienteDAO;
import model.Cliente;

public class ClienteController {
    private ClienteDAO clienteDAO;

    public ClienteController(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    public List<Cliente> listarClientes() throws Exception {
        return clienteDAO.obtenerTodos();
    }

    public Cliente buscarCliente(String clienteId) throws Exception {
        if (clienteId == null || clienteId.trim().isEmpty()) {
            throw new Exception("El ID del cliente no puede estar vacío.");
        }
        return clienteDAO.obtenerPorId(clienteId.trim());
    }

    public boolean registrarCliente(Cliente cliente) throws Exception {
        if (cliente == null || cliente.getCliente_id() == null || cliente.getCliente_id().trim().isEmpty()) {
            throw new Exception("Datos de cliente inválidos.");
        }
        if (clienteDAO.obtenerPorId(cliente.getCliente_id().trim()) != null) {
            throw new Exception("Ya existe un cliente con el ID: " + cliente.getCliente_id());
        }
        return clienteDAO.insertar(cliente);
    }
}
