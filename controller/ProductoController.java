package controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import data_access.dao.ProductoDAO;
import data_access.dao.StockDAO;
import model.Producto;
import model.Stock;

public class ProductoController {
    private ProductoDAO productoDAO;
    private StockDAO stockDAO;

    public ProductoController(ProductoDAO productoDAO, StockDAO stockDAO) {
        this.productoDAO = productoDAO;
        this.stockDAO = stockDAO;
    }

    public List<Producto> listarProductos() throws Exception {
        return productoDAO.obtenerTodos();
    }

    public Producto buscarPorId(String id) throws Exception {
        if (id == null || id.trim().isEmpty()) {
            throw new Exception("El ID del producto no puede estar vacío.");
        }
        return productoDAO.obtenerPorId(id.trim());
    }

    public boolean crearProducto(Producto producto, int cantidadInicial, int stockMinimo) throws Exception {
        if (producto == null) {
            throw new Exception("El producto no puede ser nulo.");
        }
        if (producto.getProducto_id() == null || producto.getProducto_id().trim().isEmpty()) {
            throw new Exception("El ID del producto es obligatorio.");
        }
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new Exception("El nombre del producto es obligatorio.");
        }
        if (producto.getPrecio_base() < 0) {
            throw new Exception("El precio no puede ser negativo.");
        }

        if (productoDAO.obtenerPorId(producto.getProducto_id().trim()) != null) {
            throw new Exception("Ya existe un producto con el ID: " + producto.getProducto_id());
        }

        boolean insertado = productoDAO.insertar(producto);

        String stockId = "STK_" + producto.getProducto_id();
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Stock stock = new Stock(stockId, producto.getProducto_id(), cantidadInicial, stockMinimo, now);
        stockDAO.insertar(stock);

        return insertado;
    }

    public boolean actualizarProducto(String id, String nuevoNombre, String nuevaDesc, double nuevoPrecio, Boolean activo) throws Exception {
        if (id == null || id.trim().isEmpty()) {
            throw new Exception("El ID del producto no puede estar vacío.");
        }

        Producto p = productoDAO.obtenerPorId(id.trim());
        if (p == null) {
            throw new Exception("No se encontró ningún producto con el ID: " + id);
        }

        if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
            p.setNombre(nuevoNombre);
        }
        if (nuevaDesc != null && !nuevaDesc.trim().isEmpty()) {
            p.setDescripcion(nuevaDesc);
        }
        if (nuevoPrecio >= 0) {
            p.setPrecio_base(nuevoPrecio);
        }
        if (activo != null) {
            p.setEstado_activo(activo);
        }

        return productoDAO.actualizar(p);
    }

    public boolean eliminarProducto(String id) throws Exception {
        if (id == null || id.trim().isEmpty()) {
            throw new Exception("El ID del producto no puede estar vacío.");
        }
        stockDAO.eliminarPorProductoId(id.trim());
        return productoDAO.eliminar(id.trim());
    }
}
