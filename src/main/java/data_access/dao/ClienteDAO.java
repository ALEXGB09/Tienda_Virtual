package data_access.dao;

import java.util.List;
import model.Cliente;

public interface ClienteDAO {

    List<Cliente> obtenerTodos();

    Cliente obtenerPorId(String id);

    boolean insertar(Cliente cliente);
}