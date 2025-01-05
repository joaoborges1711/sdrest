package sd.rest1.resources.Administration;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sd.rest1.PostgresConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Root resource for updating device location information.
 */
@Path("update/device")
public class UpdateDevice {

    private static final Logger LOGGER = Logger.getLogger(UpdateDevice.class.getName());

    /**
     * Updates the location of an existing IoT device.
     *
     * @param id Device ID to update.
     * @param room New room (optional).
     * @param floor New floor (optional).
     * @param service New service (optional).
     * @param building New building (optional).
     * @return Response indicating the success or failure of the operation.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String updateDevice(@QueryParam("id") int id,
                               @QueryParam("room") String room,
                               @QueryParam("floor") String floor,
                               @QueryParam("service") String service,
                               @QueryParam("building") String building) {

        validateParams(id);

        try (PostgresConnector pc = new PostgresConnector("localhost", "HospitalEvora", "postgres", "123");
             Connection conn = pc.getConnection()) {

            // Retrieve existing location details
            String existingDetailsQuery = "SELECT * FROM location WHERE id = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(existingDetailsQuery)) {
                stmt.setInt(1, id);
                ResultSet res = stmt.executeQuery();

                if (res.next()) {
                    room = (room != null) ? room : res.getString("room");
                    floor = (floor != null) ? floor : res.getString("floor");
                    building = (building != null) ? building : res.getString("building");
                    service = (service != null) ? service : res.getString("service");
                } else {
                    throw new WebApplicationException(
                            Response.status(Response.Status.NOT_FOUND)
                                    .entity("Device with ID " + id + " not found.")
                                    .build()
                    );
                }
            }

            // Update location details
            String updateLocationQuery = "UPDATE location SET room = ?, floor = ?, service = ?, building = ? WHERE id = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(updateLocationQuery)) {
                stmt.setString(1, room);
                stmt.setString(2, floor);
                stmt.setString(3, service);
                stmt.setString(4, building);
                stmt.setInt(5, id);
                stmt.executeUpdate();
            }

            return "Device location updated successfully!";
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error: {0}", e.getMessage());
            return "Failed to update device location.";
        }
    }

    private void validateParams(int id) {
        if (id <= 0) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("Device ID must be greater than 0.")
                            .build()
            );
        }
    }
}
