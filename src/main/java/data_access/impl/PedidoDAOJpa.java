package data_access.impl;

import data_access.dao.PedidoDAO;
import data_access.jpa.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.DetallePedido;
import model.Pedido;
import model.jpa.ClienteJpa;
import model.jpa.DetallePedidoJpa;
import model.jpa.PedidoJpa;
import model.jpa.ProductoJpa;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class PedidoDAOJpa implements PedidoDAO {

    @Override
    public List<Pedido> obtenerTodos() {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();

        try {
            List<PedidoJpa> pedidos = em.createQuery(
                    "SELECT DISTINCT p FROM PedidoJpa p "
                    + "LEFT JOIN FETCH p.detalles "
                    + "ORDER BY p.fechaPedido DESC",
                    PedidoJpa.class
            ).getResultList();

            return pedidos.stream()
                    .map(this::convertirAModelo)
                    .collect(Collectors.toList());

        } finally {
            em.close();
        }
    }

    @Override
    public Pedido obtenerPorId(String id) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();

        try {
            List<PedidoJpa> resultados = em.createQuery(
                    "SELECT DISTINCT p FROM PedidoJpa p "
                    + "LEFT JOIN FETCH p.detalles "
                    + "WHERE p.pedidoId = :id",
                    PedidoJpa.class
            ).setParameter("id", id)
             .getResultList();

            return resultados.isEmpty()
                    ? null
                    : convertirAModelo(resultados.get(0));

        } finally {
            em.close();
        }
    }

    @Override
    public boolean insertarPedidoConDetalles(Pedido pedido) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            PedidoJpa entidad = new PedidoJpa();
            entidad.setPedidoId(pedido.getPedido_id());

            ClienteJpa cliente = em.find(
                    ClienteJpa.class,
                    pedido.getCliente_id()
            );

            if (cliente == null) {
                throw new IllegalArgumentException(
                        "No existe el cliente: " + pedido.getCliente_id()
                );
            }

            entidad.setCliente(cliente);
            entidad.setDireccionEnvio(pedido.getDireccion_envio());
            entidad.setFechaPedido(pedido.getFecha_pedido());
            entidad.setMontoTotal(BigDecimal.valueOf(pedido.getMonto_total()));
            entidad.setEstado(pedido.getEstado());

            if (pedido.getDetalles() != null) {
                for (DetallePedido detalle : pedido.getDetalles()) {

                    DetallePedidoJpa detalleJpa = new DetallePedidoJpa();

                    detalleJpa.setDetalleId(detalle.getDetalle_id());
                    detalleJpa.setPedido(entidad);

                    ProductoJpa producto = em.getReference(
                            ProductoJpa.class,
                            detalle.getProducto_id()
                    );

                    detalleJpa.setProducto(producto);
                    detalleJpa.setCantidad(detalle.getCantidad());
                    detalleJpa.setPrecioUnitario(
                            BigDecimal.valueOf(detalle.getPrecio_unitario())
                    );

                    entidad.getDetalles().add(detalleJpa);
                }
            }

            em.persist(entidad);
            tx.commit();

            return true;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;

        } finally {
            em.close();
        }
    }

    private Pedido convertirAModelo(PedidoJpa entidad) {
        Pedido pedido = new Pedido(
                entidad.getPedidoId(),
                entidad.getCliente().getClienteId(),
                entidad.getDireccionEnvio(),
                entidad.getFechaPedido(),
                entidad.getMontoTotal().doubleValue(),
                entidad.getEstado()
        );

        List<DetallePedido> detalles = entidad.getDetalles()
                .stream()
                .map(d -> new DetallePedido(
                        d.getDetalleId(),
                        entidad.getPedidoId(),
                        d.getProducto().getProductoId(),
                        d.getCantidad(),
                        d.getPrecioUnitario().doubleValue()
                ))
                .collect(Collectors.toList());

        pedido.setDetalles(detalles);

        return pedido;
    }
}