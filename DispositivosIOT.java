package sd.rest1;

//import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the IoT Devices and their integration.
 */
public class DispositivosIOT {

    private static final Logger LOGGER = Logger.getLogger(DispositivosIOT.class.getName());
    public static final String BASE_URI = "http://localhost:8080/";

    /**
     * Initializes the IoT devices and logs their status.
     */
    public void initializeDevices() {
        LOGGER.log(Level.INFO, "Initializing IoT devices...");
        // Simulated device initialization logic can go here.
    }

    /**
     * Starts the server to listen for IoT device communications.
     */
    public void startServer() {
        LOGGER.log(Level.INFO, "Starting IoT server at: {0}", BASE_URI);
        // Code to start Grizzly server or any other server can be added here.
    }

    /**
     * Main method to launch the IoT devices and server.
     *
     * @param args command-line arguments (not used).
     */
    public static void main(String[] args) {
        DispositivosIOT dispositivosIOT = new DispositivosIOT();
        dispositivosIOT.initializeDevices();
        dispositivosIOT.startServer();
    }
}
