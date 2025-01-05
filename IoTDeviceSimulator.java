package sd.rest1;
import org.eclipse.paho.client.mqttv3.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Locale;

public class IoTDeviceSimulator {

    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String TOPIC = "hospital/metrics";
    private static final String CLIENT_ID = "IoTDeviceSimulator";

    public static void main(String[] args) {
        try {
            MqttClient client = new MqttClient(BROKER_URL, CLIENT_ID);
            client.connect();

            Random random = new Random();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

            // Garante uso do Locale padrão com separador decimal "."
            Locale.setDefault(Locale.US);

            // Conectar à base de dados para obter todos os dispositivos
            try (PostgresConnector connector = new PostgresConnector("localhost", "HospitalEvora", "postgres", "123");
                 Connection connection = connector.getConnection();
                 Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {

                // Consulta para obter todos os IDs dos dispositivos
                String query = "SELECT id FROM device";
                ResultSet rs = stmt.executeQuery(query);

                // Continuamente envia dados para todos os dispositivos
                while (true) {
                    rs.beforeFirst(); // Reseta o ResultSet para reiniciar a iteração
                    while (rs.next()) {
                        int deviceId = rs.getInt("id");

                        // Simula dados para cada dispositivo
                        double temperature = 20 + random.nextDouble() * 10; // Temperatura aleatória entre 20 e 30
                        double humidity = 0.4 + random.nextDouble() * 0.2;  // Humidade aleatória entre 0.4 e 0.6
                        String timestamp = LocalDateTime.now().format(formatter);

                        String payload = String.format(Locale.US,
                                "{\"id\":%d,\"temperature\":%.2f,\"humidity\":%.2f,\"timestamp\":\"%s\"}",
                                deviceId, temperature, humidity, timestamp);

                        // Publica a mensagem MQTT para o tópico
                        client.publish(TOPIC, new MqttMessage(payload.getBytes()));
                        System.out.println("Message sent: " + payload);
                    }

                    // Aguarda antes de enviar a próxima rodada de mensagens
                    Thread.sleep(5000); // Envia uma mensagem a cada 5 segundos
                }

            } catch (SQLException e) {
                System.err.println("Error accessing database: " + e.getMessage());
            }

        } catch (MqttException | InterruptedException e) {
            System.err.println("Error in IoT simulator: " + e.getMessage());
        }
    }
}
