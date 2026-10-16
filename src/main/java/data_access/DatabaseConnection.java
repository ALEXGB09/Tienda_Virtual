
package data_access;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:sqlserver://DESKTOP-DAVV:1433;"
            + "databaseName=tienda_virtual;"
            + "encrypt=true;"
            + "trustServerCertificate=true;";

    private static final String USER = "tienda_app";
    private static final String PASSWORD = "TiendaVirtual123";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            return DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "No se encontró el controlador JDBC de SQL Server.", e);

        } catch (SQLException e) {
            throw new SQLException(
                    "Error de conexión a SQL Server: " + e.getMessage(), e);
        }
    }
}

