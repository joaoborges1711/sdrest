package sd.rest1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handles periodic updates of IoT device metrics in the database.
 */
public class DeviceController {

    private final String host;
    private final String dbName;
    private final String user;
    private final String password;

    public DeviceController(String host, String dbName, String user, String password) {
        this.host = host;
        this.dbName = dbName;
        this.user = user;
        this.password = password;
    }

    /**
     * Updates all device metrics in the database every 2 seconds.
     */
    public void startUpdatingMetrics() {
        String updateQuery = "UPDATE device SET temperatura = ?, humidade = ?, timestamp = ?";

        try (PostgresConnector connector = new PostgresConnector(host, dbName, user, password);
             Connection connection = connector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(updateQuery)) {

            updateMetricsLoop(stmt);

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    /**
     * Performs the update loop.
     */
    private void updateMetricsLoop(PreparedStatement stmt) {
        try {
            while (true) {
                stmt.setDouble(1, generateRandomTemperature());
                stmt.setDouble(2, generateRandomHumidity());
                stmt.setString(3, getCurrentTimestamp());
                stmt.executeUpdate();

                System.out.println("Updated device metrics at: " + getCurrentTimestamp());
                Thread.sleep(2000);
            }
        } catch (InterruptedException e) {
            System.err.println("Thread interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (SQLException e) {
            System.err.println("SQL execution error: " + e.getMessage());
        }
    }

    /**
     * Generates a random temperature between 20 and 31.
     *
     * @return a random temperature value.
     */
    private double generateRandomTemperature() {
        return 20 + Math.random() * 11;
    }

    /**
     * Generates a random humidity between 0.4 and 0.6.
     *
     * @return a random humidity value.
     */
    private double generateRandomHumidity() {
        return 0.4 + Math.random() * 0.2;
    }

    /**
     * Gets the current timestamp as a formatted string.
     *
     * @return the current timestamp.
     */
    private String getCurrentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    /**
     * Main method to run the DeviceController.
     *
     * @param args command-line arguments (not used).
     */
    public static void main(String[] args) {
        DeviceController controller = new DeviceController("localhost", "HospitalEvora", "postgres", "123");
        controller.startUpdatingMetrics();
    }
}
