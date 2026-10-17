package controller;

import java.util.List;

import data_access.dao.ClienteDAO;
import data_access.dao.PedidoDAO;
import data_access.dao.ProductoDAO;
import data_access.dao.StockDAO;
import model.DetallePedido;
import model.Pedido;
import service.PedidoService;
import service.VentaService;

public class VentaController {

    private final VentaService ventaService;
    private final PedidoService pedidoService;

    public VentaController(VentaService ventaService, PedidoService pedidoService) {
        this.ventaService = ventaService;
        this.pedidoService = pedidoService;
    }

    public VentaController(
            PedidoDAO pedidoDAO,
            StockDAO stockDAO,
            ProductoDAO productoDAO,
            ClienteDAO clienteDAO) {
        this(new VentaService(pedidoDAO, stockDAO, productoDAO, clienteDAO), new PedidoService(pedidoDAO));
    }

    public List<Pedido> listarVentas() throws Exception {
        return ventaService.listarVentas();
    }

    public Pedido buscarVentaPorId(String pedidoId) throws Exception {
        return ventaService.buscarVentaPorId(pedidoId);
    }

    public Pedido procesarVenta(
            String clienteId,
            String direccionEnvio,
            List<DetallePedido> items) throws Exception {
        return ventaService.procesarVenta(clienteId, direccionEnvio, items);
    }

    public List<Pedido> listarPedidos() throws Exception {
        return pedidoService.listarPedidos();
    }

    public Pedido buscarPedidoPorId(String pedidoId) throws Exception {
        return pedidoService.buscarPorId(pedidoId);
    }
}