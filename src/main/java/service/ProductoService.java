package service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import data_access.dao.ProductoDAO;
import data_access.dao.StockDAO;
import model.Producto;
import model.Stock;

public class ProductoService {

    private final ProductoDAO productoDAO;
    private final StockDAO stockDAO;

    public ProductoService(ProductoDAO productoDAO, StockDAO stockDAO) {
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

    public boolean crearProducto(
            Producto producto, int cantidadInicial, int stockMinimo
    ) throws Exception {

        if (producto == null) {
            throw new Exception("El producto no puede ser nulo.");
        }

        if (producto.getProducto_id() == null
                || producto.getProducto_id().trim().isEmpty()) {
            throw new Exception("El ID del producto es obligatorio.");
        }

        if (producto.getNombre() == null
                || producto.getNombre().trim().isEmpty()) {
            throw new Exception("El nombre del producto es obligatorio.");
        }

        if (producto.getPrecio_base() < 0) {
            throw new Exception("El precio no puede ser negativo.");
        }

        String id = producto.getProducto_id().trim();

        if (productoDAO.obtenerPorId(id) != null) {
            throw new Exception("Ya existe un producto con el ID: " + id);
        }

        boolean insertado = productoDAO.insertar(producto);

        if (!insertado) {
            return false;
        }

        String stockId = "STK_" + id;
        String fecha = LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Stock stock = new Stock(
                stockId, id, cantidadInicial, stockMinimo, fecha
        );

        stockDAO.insertar(stock);

        return true;
    }

    public boolean actualizarProducto(
            String id,
            String nuevoNombre,
            String nuevaDesc,
            double nuevoPrecio,
            Boolean activo
    ) throws Exception {

        if (id == null || id.trim().isEmpty()) {
            throw new Exception("El ID del producto no puede estar vacío.");
        }

        Producto producto = productoDAO.obtenerPorId(id.trim());

        if (producto == null) {
            throw new Exception(
                    "No se encontró ningún producto con el ID: " + id
            );
        }

        if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
            producto.setNombre(nuevoNombre.trim());
        }

        if (nuevaDesc != null && !nuevaDesc.trim().isEmpty()) {
            producto.setDescripcion(nuevaDesc.trim());
        }

        if (nuevoPrecio >= 0) {
            producto.setPrecio_base(nuevoPrecio);
        }

        if (activo != null) {
            producto.setEstado_activo(activo);
        }

        return productoDAO.actualizar(producto);
    }

    public boolean eliminarProducto(String id) throws Exception {
        if (id == null || id.trim().isEmpty()) {
            throw new Exception("El ID del producto no puede estar vacío.");
        }

        String idLimpio = id.trim();

        if (productoDAO.obtenerPorId(idLimpio) == null) {
            throw new Exception(
                    "No se encontró ningún producto con el ID: " + idLimpio
            );
        }

        stockDAO.eliminarPorProductoId(idLimpio);

        return productoDAO.eliminar(idLimpio);
    }
}