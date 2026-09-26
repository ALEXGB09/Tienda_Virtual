package data_access.dao;

import java.util.List;
import model.Cliente;

public interface ClienteDAO {
    List<Cliente> obtenerTodos() throws Exception;
    Cliente obtenerPorId(String id) throws Exception;
    boolean insertar(Cliente cliente) throws Exception;
}
