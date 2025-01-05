package sd.rest1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONObject;

public class MqttConsumer {

    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String CLIENT_ID = "HospitalEvoraConsumer";
    private static final String TOPIC = "hospital/metrics";

    private MqttClient client;

    public static void main(String[] args) {
        new MqttConsumer().start();
    }

    public void start() {
        try {
            client = new MqttClient(BROKER_URL, CLIENT_ID);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.err.println("Connection lost: " + cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    System.out.println("Received message: " + new String(message.getPayload()));
                    processMessage(new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Não é relevante para consumidores
                }
            });

            client.connect();
            client.subscribe(TOPIC);

            System.out.println("Connected to broker and subscribed to topic: " + TOPIC);

            // Adiciona um shutdown hook para desconectar o cliente
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (client != null && client.isConnected()) {
                        client.disconnect();
                        System.out.println("MQTT client disconnected.");
                    }
                } catch (MqttException e) {
                    System.err.println("Error disconnecting MQTT client: " + e.getMessage());
                }
            }));

        } catch (MqttException e) {
            System.err.println("Error initializing MQTT client: " + e.getMessage());
        }
    }

    private void processMessage(String payload) {
        try {
            JSONObject json = new JSONObject(payload);

            int deviceId = json.getInt("id");
            double temperature = json.getDouble("temperature");
            double humidity = json.getDouble("humidity");
            String timestamp = json.getString("timestamp");

            if (isDeviceRegistered(deviceId)) {
                saveMetrics(deviceId, temperature, humidity, timestamp);
            } else {
                System.out.println("Device not registered. Discarding metrics.");
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

   private boolean isDeviceRegistered(int deviceId) {
    String query = "SELECT COUNT(*) FROM device WHERE id = ?";

    try (PostgresConnector connector = new PostgresConnector("localhost", "HospitalEvora", "postgres", "123");
         Connection connection = connector.getConnection();
         PreparedStatement stmt = connection.prepareStatement(query)) {

        stmt.setInt(1, deviceId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getInt(1) > 0; // Retorna true se houver pelo menos um registro
        } else {
            return false;
        }
    } catch (SQLException e) {
        System.err.println("Error checking device registration: " + e.getMessage());
        return false; // Retorna false em caso de erro
    }
}


private void saveMetrics(int deviceId, double temperature, double humidity, String timestamp) {
    String query = "INSERT INTO device (id, temperatura, humidade, timestamp) VALUES (?, ?, ?, ?) " +
                   "ON CONFLICT (id) DO UPDATE SET temperatura = EXCLUDED.temperatura, " +
                   "humidade = EXCLUDED.humidade, timestamp = EXCLUDED.timestamp;";

    try (PostgresConnector connector = new PostgresConnector("localhost", "HospitalEvora", "postgres", "123");
         Connection connection = connector.getConnection();
         PreparedStatement stmt = connection.prepareStatement(query)) {

        stmt.setInt(1, deviceId);
        stmt.setDouble(2, temperature);
        stmt.setDouble(3, humidity);

        // Convert the timestamp format from "yyyy-MM-dd'T'HH:mm:ss" to "yyyy-MM-dd HH:mm:ss"
        String formattedTimestamp = timestamp.replace('T', ' ');

        // Convert the formatted string to java.sql.Timestamp
        stmt.setTimestamp(4, Timestamp.valueOf(formattedTimestamp));

        int rowsAffected = stmt.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Metrics saved for device ID: " + deviceId);
        } else {
            System.out.println("No metrics saved. Device ID might not exist.");
        }
    } catch (SQLException e) {
        System.err.println("Error saving metrics: " + e.getMessage());
    }
}




}
