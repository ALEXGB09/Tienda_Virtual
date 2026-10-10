package data_access.dao;

import java.util.List;
import model.Pedido;

public interface PedidoDAO {
    List<Pedido> obtenerTodos() throws Exception;
    Pedido obtenerPorId(String id) throws Exception;
    boolean insertarPedidoConDetalles(Pedido pedido) throws Exception;
}
