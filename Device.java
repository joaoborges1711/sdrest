package sd.rest1;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Represents an IoT Device that collects and sends data.
 */
public class Device {

    private final int id;
    private double temperature;
    private double humidity;
    private LocalDateTime timestamp;

    public Device(int id) {
        this.id = id;
        updateMetrics();
    }

    /**
     * Gets the device ID.
     *
     * @return the unique identifier of the device.
     */
    public int getDeviceId() {
        return id;
    }

    /**
     * Updates the temperature, humidity, and timestamp with new random values.
     */
    public void updateMetrics() {
        Random random = new Random();
        this.temperature = 20 + random.nextDouble() * 11; // Random temperature between 20 and 31
        this.humidity = 0.4 + random.nextDouble() * 0.2;  // Random humidity between 0.4 and 0.6
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Formats the device's metrics as a string.
     *
     * @return a string representation of the metrics.
     */
    public String getMetrics() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("Device ID: %d, Temperature: %.2f, Humidity: %.2f, Timestamp: %s",
                id, temperature, humidity, timestamp.format(formatter));
    }

    /**
     * Main method to demonstrate the functionality of the Device class.
     *
     * @param args command-line arguments (not used).
     */
    public static void main(String[] args) {
        Device device = new Device(1);
        System.out.println("Initial Metrics:");
        System.out.println(device.getMetrics());

        // Simulate an update
        System.out.println("\nUpdating Metrics...");
        device.updateMetrics();
        System.out.println(device.getMetrics());
    }
}
