package service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import data_access.dao.StockDAO;
import exception.InsufficientStockException;
import exception.ResourceNotFoundException;
import exception.ValidationException;
import model.Stock;

public class StockService {

    private final StockDAO stockDAO;

    public StockService(StockDAO stockDAO) {
        this.stockDAO = stockDAO;
    }

    public List<Stock> listarStock() throws Exception {
        return stockDAO.obtenerTodos();
    }

    public Stock buscarPorProductoId(String productoId) throws Exception {
        if (productoId == null || productoId.trim().isEmpty()) {
            throw new ValidationException("El ID del producto no puede estar vacío.");
        }

        Stock stock = stockDAO.obtenerPorProductoId(productoId.trim());
        if (stock == null) {
            throw new ResourceNotFoundException("No se encontró registro de inventario para el producto: " + productoId.trim());
        }
        return stock;
    }

    public boolean actualizarCantidad(String productoId, int nuevaCantidad) throws Exception {
        if (productoId == null || productoId.trim().isEmpty()) {
            throw new ValidationException("El ID del producto no puede estar vacío.");
        }
        if (nuevaCantidad < 0) {
            throw new ValidationException("La cantidad de inventario no puede ser negativa.");
        }

        String idLimpio = productoId.trim();
        Stock existente = stockDAO.obtenerPorProductoId(idLimpio);
        if (existente == null) {
            throw new ResourceNotFoundException("No se encontró inventario para el producto: " + idLimpio);
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return stockDAO.actualizarCantidad(idLimpio, nuevaCantidad, now);
    }

    public boolean descontarStock(String productoId, int cantidadADescontar) throws Exception {
        if (productoId == null || productoId.trim().isEmpty()) {
            throw new ValidationException("El ID del producto no puede estar vacío.");
        }
        if (cantidadADescontar <= 0) {
            throw new ValidationException("La cantidad a descontar debe ser mayor a 0.");
        }

        String idLimpio = productoId.trim();
        Stock stock = stockDAO.obtenerPorProductoId(idLimpio);
        if (stock == null) {
            throw new ResourceNotFoundException("No se encontró registro de stock para el producto: " + idLimpio);
        }

        if (stock.getCantidad_disponible() < cantidadADescontar) {
            throw new InsufficientStockException(
                "Inventario insuficiente para el producto " + idLimpio +
                ". Disponible: " + stock.getCantidad_disponible() +
                ", Solicitado: " + cantidadADescontar
            );
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return stockDAO.descontarCantidad(idLimpio, cantidadADescontar, now);
    }
}
