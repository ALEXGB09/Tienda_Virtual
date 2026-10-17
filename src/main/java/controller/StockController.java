package controller;

import java.util.List;
import data_access.dao.StockDAO;
import model.Stock;
import service.StockService;

public class StockController {
    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    public StockController(StockDAO stockDAO) {
        this(new StockService(stockDAO));
    }

    public List<Stock> listarStock() throws Exception {
        return stockService.listarStock();
    }

    public Stock buscarPorProductoId(String productoId) throws Exception {
        return stockService.buscarPorProductoId(productoId);
    }

    public boolean actualizarCantidad(String productoId, int nuevaCantidad) throws Exception {
        return stockService.actualizarCantidad(productoId, nuevaCantidad);
    }

    public boolean descontarStock(String productoId, int cantidadADescontar) throws Exception {
        return stockService.descontarStock(productoId, cantidadADescontar);
    }
}
