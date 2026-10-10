package data_access.dao;

import java.util.List;
import model.Producto;

public interface ProductoDAO {
    List<Producto> obtenerTodos() throws Exception;
    Producto obtenerPorId(String id) throws Exception;
    boolean insertar(Producto producto) throws Exception;
    boolean actualizar(Producto producto) throws Exception;
    boolean eliminar(String id) throws Exception;
}
