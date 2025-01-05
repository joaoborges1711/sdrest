package sd.rest1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages connections to a PostgreSQL database.
 */
public class PostgresConnector implements AutoCloseable {

    private String url;
    private String user;
    private String password;
    private Connection connection;

    public PostgresConnector(String host, String name, String user, String password) {
        this.url = "jdbc:postgresql://" + host + "/" + name; 
        this.user = user;
        this.password = password;
    }

    public void connect() throws SQLException {
        try (Connection conn = getConnection()) {
            System.out.println("conexão bem-sucedida.");
        } catch (SQLException e) {
            System.err.println("erro ao conectar" + e.getMessage());
            throw e;
        }
    }
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
