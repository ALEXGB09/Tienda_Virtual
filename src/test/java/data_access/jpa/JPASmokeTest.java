package data_access.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JPASmokeTest {

    @Test
    void debeConectarseASqlServer() {

        EntityManagerFactory emf =
                JPAUtil.getEntityManagerFactory();

        EntityManager em = emf.createEntityManager();

        try {
            Object resultado = em.createNativeQuery("SELECT 1")
                    .getSingleResult();

            assertEquals(1, ((Number) resultado).intValue());

        } finally {
            em.close();
        }
    }
}