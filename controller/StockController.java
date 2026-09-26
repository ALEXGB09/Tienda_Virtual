package controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import data_access.dao.StockDAO;
import model.Stock;

public class StockController {
    private StockDAO stockDAO;

    public StockController(StockDAO stockDAO) {
        this.stockDAO = stockDAO;
    }

    public List<Stock> listarStock() throws Exception {
        return stockDAO.obtenerTodos();
    }

    public Stock buscarPorProductoId(String productoId) throws Exception {
        if (productoId == null || productoId.trim().isEmpty()) {
            throw new Exception("El ID del producto no puede estar vacío.");
        }
        return stockDAO.obtenerPorProductoId(productoId.trim());
    }

    public boolean actualizarCantidad(String productoId, int nuevaCantidad) throws Exception {
        if (productoId == null || productoId.trim().isEmpty()) {
            throw new Exception("El ID del producto no puede estar vacío.");
        }
        if (nuevaCantidad < 0) {
            throw new Exception("La cantidad de inventario no puede ser negativa.");
        }
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return stockDAO.actualizarCantidad(productoId.trim(), nuevaCantidad, now);
    }

    public boolean descontarStock(String productoId, int cantidadADescontar) throws Exception {
        if (cantidadADescontar <= 0) {
            throw new Exception("La cantidad a descontar debe ser mayor a 0.");
        }
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return stockDAO.descontarCantidad(productoId.trim(), cantidadADescontar, now);
    }
}
