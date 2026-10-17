package service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import data_access.dao.ProductoDAO;
import data_access.dao.StockDAO;
import exception.ConflictException;
import exception.ResourceNotFoundException;
import exception.ValidationException;
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
            throw new ValidationException("El ID del producto no puede estar vacío.");
        }

        Producto p = productoDAO.obtenerPorId(id.trim());
        if (p == null) {
            throw new ResourceNotFoundException("No se encontró ningún producto con el ID: " + id.trim());
        }
        return p;
    }

    public boolean crearProducto(Producto producto, int cantidadInicial, int stockMinimo) throws Exception {
        if (producto == null) {
            throw new ValidationException("El producto no puede ser nulo.");
        }

        if (producto.getProducto_id() == null || producto.getProducto_id().trim().isEmpty()) {
            throw new ValidationException("El ID del producto es obligatorio.");
        }

        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new ValidationException("El nombre del producto es obligatorio.");
        }

        if (producto.getPrecio_base() < 0) {
            throw new ValidationException("El precio base no puede ser negativo.");
        }

        if (cantidadInicial < 0 || stockMinimo < 0) {
            throw new ValidationException("La cantidad inicial y el stock mínimo no pueden ser negativos.");
        }

        String id = producto.getProducto_id().trim();

        if (productoDAO.obtenerPorId(id) != null) {
            throw new ConflictException("Ya existe un producto con el ID: " + id);
        }

        boolean insertado = productoDAO.insertar(producto);
        if (!insertado) {
            return false;
        }

        String stockId = "STK_" + id;
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Stock stock = new Stock(stockId, id, cantidadInicial, stockMinimo, fecha);
        stockDAO.insertar(stock);

        return true;
    }

    public boolean actualizarProducto(String id, String nuevoNombre, String nuevaDesc, double nuevoPrecio, Boolean activo) throws Exception {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("El ID del producto no puede estar vacío.");
        }

        String idLimpio = id.trim();
        Producto producto = productoDAO.obtenerPorId(idLimpio);

        if (producto == null) {
            throw new ResourceNotFoundException("No se encontró ningún producto con el ID: " + idLimpio);
        }

        if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
            producto.setNombre(nuevoNombre.trim());
        }

        if (nuevaDesc != null && !nuevaDesc.trim().isEmpty()) {
            producto.setDescripcion(nuevaDesc.trim());
        }

        if (nuevoPrecio >= 0) {
            producto.setPrecio_base(nuevoPrecio);
        } else if (nuevoPrecio < -1) {
            throw new ValidationException("El precio no puede ser negativo.");
        }

        if (activo != null) {
            producto.setEstado_activo(activo);
        }

        return productoDAO.actualizar(producto);
    }

    public boolean eliminarProducto(String id) throws Exception {
        if (id == null || id.trim().isEmpty()) {
            throw new ValidationException("El ID del producto no puede estar vacío.");
        }

        String idLimpio = id.trim();
        if (productoDAO.obtenerPorId(idLimpio) == null) {
            throw new ResourceNotFoundException("No se encontró ningún producto con el ID: " + idLimpio);
        }

        stockDAO.eliminarPorProductoId(idLimpio);
        return productoDAO.eliminar(idLimpio);
    }
}