package sd.rest1.resources.Administration;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sd.rest1.PostgresConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RESTful API for IoT Device Management.
 */
@Path("devices")
public class DeviceManagement {

    /**
     * Retrieves a list of all devices.
     *
     * @return JSON response with the list of devices.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listDevices() {
        String query = "SELECT d.id, l.room, l.floor, l.building, l.service, d.temperatura, d.humidade, d.timestamp " +
                       "FROM device d JOIN location l ON d.id = l.id";
    
        List<Map<String, Object>> devices = new ArrayList<>();
    
        try (PostgresConnector connector = new PostgresConnector("localhost", "HospitalEvora", "postgres", "123");
             Connection connection = connector.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
    
            while (rs.next()) {
                Map<String, Object> device = new HashMap<>();
                device.put("id", rs.getInt("id"));
                device.put("room", rs.getString("room"));
                device.put("floor", rs.getString("floor"));
                device.put("building", rs.getString("building"));
                device.put("service", rs.getString("service"));
                device.put("temperature", rs.getDouble("temperatura"));
                device.put("humidity", rs.getDouble("humidade"));
                device.put("timestamp", rs.getTimestamp("timestamp").toString());
    
                devices.add(device);
            }
    
            return Response.ok(devices).build();
    
        } catch (SQLException e) {
            e.printStackTrace();  // Isso vai registrar o erro completo no console ou logs
    
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Failed to retrieve devices: " + e.getMessage())
                           .build();
        }
    }
    
}

