package controller;

import java.util.List;

import data_access.dao.ProductoDAO;
import data_access.dao.StockDAO;
import model.Producto;
import service.ProductoService;

public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(
            ProductoDAO productoDAO, StockDAO stockDAO
    ) {
        this.productoService = new ProductoService(
                productoDAO, stockDAO
        );
    }

    public List<Producto> listarProductos() throws Exception {
        return productoService.listarProductos();
    }

    public Producto buscarPorId(String id) throws Exception {
        return productoService.buscarPorId(id);
    }

    public boolean crearProducto(
            Producto producto, int cantidadInicial, int stockMinimo
    ) throws Exception {
        return productoService.crearProducto(
                producto, cantidadInicial, stockMinimo
        );
    }

    public boolean actualizarProducto(
            String id, String nuevoNombre, String nuevaDesc,
            double nuevoPrecio, Boolean activo
    ) throws Exception {
        return productoService.actualizarProducto(
                id, nuevoNombre, nuevaDesc, nuevoPrecio, activo
        );
    }

    public boolean eliminarProducto(String id) throws Exception {
        return productoService.eliminarProducto(id);
    }
}

