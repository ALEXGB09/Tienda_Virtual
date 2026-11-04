package data_access.impl;

import data_access.dao.ClienteDAO;
import data_access.jpa.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.Cliente;
import model.jpa.ClienteJpa;

import java.util.List;
import java.util.stream.Collectors;

public class ClienteDAOJpa implements ClienteDAO {

    @Override
    public List<Cliente> obtenerTodos() {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();

        try {
            List<ClienteJpa> clientes = em.createQuery(
                    "SELECT c FROM ClienteJpa c", ClienteJpa.class
            ).getResultList();

            return clientes.stream()
                    .map(this::convertirAModelo)
                    .collect(Collectors.toList());

        } finally {
            em.close();
        }
    }

    @Override
    public Cliente obtenerPorId(String id) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();

        try {
            ClienteJpa cliente = em.find(ClienteJpa.class, id);
            return cliente == null ? null : convertirAModelo(cliente);

        } finally {
            em.close();
        }
    }

    @Override
    public boolean insertar(Cliente cliente) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            ClienteJpa entidad = convertirAEntidad(cliente);
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

    private Cliente convertirAModelo(ClienteJpa entidad) {
        return new Cliente(
                entidad.getClienteId(),
                entidad.getNombre(),
                entidad.getApellido(),
                entidad.getEmail(),
                entidad.getTelefono(),
                entidad.getFechaRegistro()
        );
    }

    private ClienteJpa convertirAEntidad(Cliente modelo) {
        ClienteJpa entidad = new ClienteJpa();

        entidad.setClienteId(modelo.getCliente_id());
        entidad.setNombre(modelo.getNombre());
        entidad.setApellido(modelo.getApellido());
        entidad.setEmail(modelo.getEmail());
        entidad.setTelefono(modelo.getTelefono());
        entidad.setFechaRegistro(modelo.getFecha_registro());

        return entidad;
    }
}