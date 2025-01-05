package sd.rest1.resources.Administration;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sd.rest1.PostgresConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Root resource for device creation.
 */
@Path("create/device")
public class CreateDevice {

    private static final Logger LOGGER = Logger.getLogger(CreateDevice.class.getName());

    /**
     * Handles the creation of a new IoT device and associates it with a location.
     * @param room Room where the device is installed.
     * @param floor Floor where the device is installed.
     * @param building Building where the device is installed.
     * @param service Service where the device is installed.
     * @return Response indicating the success or failure of the operation.
     */
    @POST
@Produces(MediaType.APPLICATION_JSON)
    public Response createDevice(@FormParam("room") String room,
                                 @FormParam("floor") String floor,
                                 @FormParam("building") String building,
                                 @FormParam("service") String service) {
        try {
            validateParams(room, floor, building, service);
    
            try (PostgresConnector pc = new PostgresConnector("localhost", "HospitalEvora", "postgres", "123");
                 Connection conn = pc.getConnection()) {
    
                syncSequence(conn, "device", "id");
                int deviceId = insertDevice(conn);
                associateDeviceWithLocation(conn, deviceId, room, floor, building, service);
    
                return Response.ok("{\"message\": \"Device created\", \"id\": " + deviceId + "}")
                               .type(MediaType.APPLICATION_JSON)
                               .build();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error: {0}", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"Failed to create device.\"}")
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        }
    }
    
    

    private void validateParams(String... params) {
        for (String param : params) {
            if (param == null || param.isEmpty()) {
                throw new WebApplicationException(
                        Response.status(Response.Status.BAD_REQUEST)
                                .entity("Missing mandatory parameter.")
                                .build()
                );
            }
        }
    }

    private void syncSequence(Connection conn, String tableName, String columnName) throws SQLException {
        String syncSequenceQuery = "SELECT setval(pg_get_serial_sequence(?, ?), COALESCE(MAX(" + columnName + "), 0) + 1, false) FROM " + tableName + ";";
        try (PreparedStatement stmt = conn.prepareStatement(syncSequenceQuery)) {
            stmt.setString(1, tableName);
            stmt.setString(2, columnName);
            stmt.execute();
        }
    }

    private int insertDevice(Connection conn) throws SQLException {
        String insertDeviceQuery = "INSERT INTO device (temperatura, humidade, timestamp) VALUES (?, ?, ?) RETURNING id;";
        try (PreparedStatement stmt = conn.prepareStatement(insertDeviceQuery)) {
            stmt.setDouble(1, generateRandomTemperature());
            stmt.setDouble(2, generateRandomHumidity());
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("Failed to retrieve the device ID.");
            }
        }
    }

    private void associateDeviceWithLocation(Connection conn, int deviceId, String room, String floor, String building, String service) throws SQLException {
        String insertLocationQuery = "INSERT INTO location (id, service, floor, building, room) VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement stmt = conn.prepareStatement(insertLocationQuery)) {
            stmt.setInt(1, deviceId);
            stmt.setString(2, service);
            stmt.setString(3, floor);
            stmt.setString(4, building);
            stmt.setString(5, room);
            stmt.executeUpdate();
        }
    }

    private double generateRandomTemperature() {
        return 20 + Math.random() * 11; // Random temperature between 20 and 31
    }

    private double generateRandomHumidity() {
        return 0.4 + Math.random() * 0.2; // Random humidity between 0.4 and 0.6
    }
}
