package sd.rest1;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Server class for managing the HTTP server instance.
 */
public class Server {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/";

    /**
     * Starts the Grizzly HTTP server exposing JAX-RS resources defined in this application.
     *
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        LOGGER.log(Level.INFO, "Configuring server with resource packages...");
        final ResourceConfig rc = new ResourceConfig()
                .packages("sd.rest1.resources") // Carrega os recursos (endpoints)
                .packages("sd.rest1.filters");  // Carrega o filtro de autenticação
    
        LOGGER.log(Level.INFO, "Starting server at {0}", BASE_URI);
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }
    

    /**
     * Main method to launch the server.
     *
     * @param args command-line arguments (not used).
     */
    public static void main(String[] args) {
        HttpServer server = null;
        try {
            server = startServer();
            LOGGER.log(Level.INFO, "Jersey application started with endpoints available at {0}", BASE_URI);
            LOGGER.log(Level.INFO, "Hit Enter to stop the server...");
            System.in.read();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error starting server: {0}", e.getMessage());
        } finally {
            if (server != null) {
                server.shutdownNow();
                LOGGER.log(Level.INFO, "Server stopped.");
            }
        }
    }
}
