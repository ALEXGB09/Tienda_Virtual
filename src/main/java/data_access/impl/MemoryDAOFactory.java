package data_access.impl;

import java.util.ArrayList;
import java.util.List;
import data_access.dao.ClienteDAO;
import data_access.dao.PedidoDAO;
import data_access.dao.ProductoDAO;
import data_access.dao.StockDAO;
import model.Cliente;
import model.Pedido;
import model.Producto;
import model.Stock;

public class MemoryDAOFactory {

    private static final List<Producto> productosDB = new ArrayList<>();
    private static final List<Stock> stockDB = new ArrayList<>();
    private static final List<Cliente> clientesDB = new ArrayList<>();
    private static final List<Pedido> pedidosDB = new ArrayList<>();

    static {
        productosDB.add(new Producto("PROD001", "Laptop Gaming Pro 15", "Computador portátil Intel i7 16GB RAM 512GB SSD", 4500000.0, true));
        productosDB.add(new Producto("PROD002", "Mouse Inalámbrico Ergonómico", "Mouse óptico recargable Bluetooth 2.4GHz", 120000.0, true));
        productosDB.add(new Producto("PROD003", "Teclado Mecánico RGB", "Teclado gamer switches blue retroiluminado", 250000.0, true));
        productosDB.add(new Producto("PROD004", "Monitor 27 Pulgadas 144Hz", "Monitor IPS Full HD respuesta 1ms", 1100000.0, true));

        stockDB.add(new Stock("STK001", "PROD001", 10, 2, "2026-09-25T10:00:00"));
        stockDB.add(new Stock("STK002", "PROD002", 50, 5, "2026-09-25T10:00:00"));
        stockDB.add(new Stock("STK003", "PROD003", 30, 3, "2026-09-25T10:00:00"));
        stockDB.add(new Stock("STK004", "PROD004", 15, 2, "2026-09-25T10:00:00"));

        clientesDB.add(new Cliente("CLI001", "Alexis", "Giraldo", "alexis.giraldo@email.com", "+573001234567", "2026-01-15"));
        clientesDB.add(new Cliente("CLI002", "Maria", "Lopez", "maria.lopez@email.com", "+573109876543", "2026-02-20"));
    }

    public static ProductoDAO getProductoDAO() {
        return new ProductoDAO() {
            @Override
            public List<Producto> obtenerTodos() { return new ArrayList<>(productosDB); }

            @Override
            public Producto obtenerPorId(String id) {
                for (Producto p : productosDB) {
                    if (p.getProducto_id().equalsIgnoreCase(id)) return p;
                }
                return null;
            }

            @Override
            public boolean insertar(Producto p) { productosDB.add(p); return true; }

            @Override
            public boolean actualizar(Producto p) {
                Producto exist = obtenerPorId(p.getProducto_id());
                if (exist != null) {
                    exist.setNombre(p.getNombre());
                    exist.setDescripcion(p.getDescripcion());
                    exist.setPrecio_base(p.getPrecio_base());
                    exist.setEstado_activo(p.isEstado_activo());
                    return true;
                }
                return false;
            }

            @Override
            public boolean eliminar(String id) { return productosDB.removeIf(p -> p.getProducto_id().equalsIgnoreCase(id)); }
        };
    }

    public static StockDAO getStockDAO() {
        return new StockDAO() {
            @Override
            public List<Stock> obtenerTodos() { return new ArrayList<>(stockDB); }

            @Override
            public Stock obtenerPorProductoId(String productoId) {
                for (Stock s : stockDB) {
                    if (s.getProducto_id().equalsIgnoreCase(productoId)) return s;
                }
                return null;
            }

            @Override
            public boolean insertar(Stock s) { stockDB.add(s); return true; }

            @Override
            public boolean actualizarCantidad(String productoId, int nuevaCantidad, String fecha) {
                Stock s = obtenerPorProductoId(productoId);
                if (s != null) {
                    s.setCantidad_disponible(nuevaCantidad);
                    s.setUltima_actualizacion(fecha);
                    return true;
                }
                return false;
            }

            @Override
            public boolean descontarCantidad(String productoId, int cantidadADescontar, String fecha) throws Exception {
                Stock s = obtenerPorProductoId(productoId);
                if (s == null || s.getCantidad_disponible() < cantidadADescontar) {
                    throw new Exception("Inventario insuficiente para el producto: " + productoId);
                }
                s.setCantidad_disponible(s.getCantidad_disponible() - cantidadADescontar);
                s.setUltima_actualizacion(fecha);
                return true;
            }

            @Override
            public boolean eliminarPorProductoId(String productoId) {
                return stockDB.removeIf(s -> s.getProducto_id().equalsIgnoreCase(productoId));
            }
        };
    }

    public static ClienteDAO getClienteDAO() {
        return new ClienteDAO() {
            @Override
            public List<Cliente> obtenerTodos() { return new ArrayList<>(clientesDB); }

            @Override
            public Cliente obtenerPorId(String id) {
                for (Cliente c : clientesDB) {
                    if (c.getCliente_id().equalsIgnoreCase(id)) return c;
                }
                return null;
            }

            @Override
            public boolean insertar(Cliente c) { clientesDB.add(c); return true; }
        };
    }

    public static PedidoDAO getPedidoDAO() {
        return new PedidoDAO() {
            @Override
            public List<Pedido> obtenerTodos() { return new ArrayList<>(pedidosDB); }

            @Override
            public Pedido obtenerPorId(String id) {
                for (Pedido p : pedidosDB) {
                    if (p.getPedido_id().equalsIgnoreCase(id)) return p;
                }
                return null;
            }

            @Override
            public boolean insertarPedidoConDetalles(Pedido p) { pedidosDB.add(p); return true; }
        };
    }
}
