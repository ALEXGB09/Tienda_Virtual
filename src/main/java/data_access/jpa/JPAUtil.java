package data_access.jpa;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.jpa.HibernatePersistenceProvider;
import java.util.HashMap;
import java.util.Map;

public class JPAUtil {

    private static EntityManagerFactory entityManagerFactory;

    private static final String URL = System.getenv()
            .getOrDefault(
                    "TIENDA_DB_URL",
                    "jdbc:sqlserver://DESKTOP-DAVV:1433;"
                    + "databaseName=tienda_virtual;"
                    + "encrypt=true;"
                    + "trustServerCertificate=true;"
            );

    private static final String USER = System.getenv()
            .getOrDefault("TIENDA_DB_USER", "tienda_app");

    private static final String PASSWORD =
            System.getenv("TIENDA_DB_PASSWORD");

    private JPAUtil() {
        // Evita instanciar esta clase.
    }

    public static synchronized EntityManagerFactory getEntityManagerFactory() {

        if (PASSWORD == null || PASSWORD.isBlank()) {
            throw new IllegalStateException(
                    "Falta configurar la variable TIENDA_DB_PASSWORD."
            );
        }

        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {

            Map<String, Object> properties = new HashMap<>();

            properties.put("jakarta.persistence.jdbc.driver",
                    "com.microsoft.sqlserver.jdbc.SQLServerDriver");

            properties.put("jakarta.persistence.jdbc.url", URL);
            properties.put("jakarta.persistence.jdbc.user", USER);
            properties.put("jakarta.persistence.jdbc.password", PASSWORD);

            entityManagerFactory = new HibernatePersistenceProvider()
        .createEntityManagerFactory("TiendaVirtualPU", properties);
            
        }

        return entityManagerFactory;
    }

    public static synchronized void cerrar() {

        if (entityManagerFactory != null
                && entityManagerFactory.isOpen()) {

            entityManagerFactory.close();
        }
    }
}