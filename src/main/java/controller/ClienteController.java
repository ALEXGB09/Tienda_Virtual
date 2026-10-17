package controller;

import java.util.List;
import data_access.dao.ClienteDAO;
import model.Cliente;
import service.ClienteService;

public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    public ClienteController(ClienteDAO clienteDAO) {
        this(new ClienteService(clienteDAO));
    }

    public List<Cliente> listarClientes() throws Exception {
        return clienteService.listarClientes();
    }

    public Cliente buscarCliente(String clienteId) throws Exception {
        return clienteService.buscarCliente(clienteId);
    }

    public boolean registrarCliente(Cliente cliente) throws Exception {
        return clienteService.registrarCliente(cliente);
    }
}
