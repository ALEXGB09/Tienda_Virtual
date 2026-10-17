package service;

import java.util.List;

import data_access.dao.PedidoDAO;
import exception.ResourceNotFoundException;
import exception.ValidationException;
import model.Pedido;

public class PedidoService {

    private final PedidoDAO pedidoDAO;

    public PedidoService(PedidoDAO pedidoDAO) {
        this.pedidoDAO = pedidoDAO;
    }

    public List<Pedido> listarPedidos() throws Exception {
        return pedidoDAO.obtenerTodos();
    }

    public Pedido buscarPorId(String id) throws Exception {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("El ID del pedido no puede estar vacío.");
        }

        Pedido pedido = pedidoDAO.obtenerPorId(id.trim());
        if (pedido == null) {
            throw new ResourceNotFoundException("No se encontró ningún pedido con el ID: " + id.trim());
        }
        return pedido;
    }
}
