package data_access.dao;

import java.util.List;
import model.Stock;

public interface StockDAO {
    List<Stock> obtenerTodos() throws Exception;
    Stock obtenerPorProductoId(String productoId) throws Exception;
    boolean insertar(Stock stock) throws Exception;
    boolean actualizarCantidad(String productoId, int nuevaCantidad, String fechaActualizacion) throws Exception;
    boolean descontarCantidad(String productoId, int cantidadADescontar, String fechaActualizacion) throws Exception;
    boolean eliminarPorProductoId(String productoId) throws Exception;
}
