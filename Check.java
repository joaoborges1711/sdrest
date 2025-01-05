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
 * Root resource for environmental data checks.
 */
@Path("check")
public class Check {

    private static final Logger LOGGER = Logger.getLogger(Check.class.getName());

    /**
     * Queries room metrics based on provided parameters.
     */
    @GET
    @Path("/room")
    @Produces(MediaType.TEXT_PLAIN)
    public String checkRoom(@QueryParam("room") String room,
                            @QueryParam("floor") String floor,
                            @QueryParam("building") String building,
                            @QueryParam("startDate") String startDate,
                            @QueryParam("endDate") String endDate) {
        validateParams(room, floor, building);
        String query = buildQuery("room", room, floor, building, startDate, endDate);
        return executeQuery(query, "Room: " + room);
    }

    /**
     * Queries floor metrics based on provided parameters.
     */
    @GET
    @Path("/floor")
    @Produces(MediaType.TEXT_PLAIN)
    public String checkFloor(@QueryParam("floor") String floor,
                             @QueryParam("service") String service,
                             @QueryParam("building") String building,
                             @QueryParam("startDate") String startDate,
                             @QueryParam("endDate") String endDate) {
        validateParams(floor, service, building);
        String query = buildQuery("floor", null, floor, building, startDate, endDate, service);
        return executeQuery(query, "Floor: " + floor);
    }

    /**
     * Queries building metrics based on provided parameters.
     */
    @GET
    @Path("/building")
    @Produces(MediaType.TEXT_PLAIN)
    public String checkBuilding(@QueryParam("building") String building,
                                 @QueryParam("service") String service,
                                 @QueryParam("startDate") String startDate,
                                 @QueryParam("endDate") String endDate) {
        validateParams(building, service);
        String query = buildQuery("building", null, null, building, startDate, endDate, service);
        return executeQuery(query, "Building: " + building);
    }

    /**
     * Queries service metrics based on provided parameters.
     */
    @GET
    @Path("/service")
    @Produces(MediaType.TEXT_PLAIN)
    public String checkService(@QueryParam("service") String service,
                                @QueryParam("startDate") String startDate,
                                @QueryParam("endDate") String endDate) {
        validateParams(service);
        String query = buildQuery("service", null, null, null, startDate, endDate, service);
        return executeQuery(query, "Service: " + service);
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

    private String buildQuery(String level, String room, String floor, String building,
                              String startDate, String endDate) {
        return buildQuery(level, room, floor, building, startDate, endDate, null);
    }

    private String buildQuery(String level, String room, String floor, String building,
                              String startDate, String endDate, String service) {
        StringBuilder query = new StringBuilder("SELECT AVG(temperatura)::NUMERIC(10,2) AS tempAVG, " +
                "AVG(humidade)::NUMERIC(10,2) AS humiAVG FROM device, location WHERE ");

        if (room != null) query.append("room = '" + room + "' AND ");
        if (floor != null) query.append("floor = '" + floor + "' AND ");
        if (building != null) query.append("building = '" + building + "' AND ");
        if (service != null) query.append("service = '" + service + "' AND ");

        query.append("device.id = location.id AND ");

        if (startDate == null && endDate == null) {
            query.append("timestamp > now() - INTERVAL '24 HOURS'");
        } else {
            if (startDate != null) query.append("timestamp >= '" + startDate + "' AND ");
            if (endDate != null) query.append("timestamp <= '" + endDate + "' AND ");
            query.setLength(query.length() - 5); // Remove trailing ' AND '
        }
        query.append(";");
        return query.toString();
    }

    private String executeQuery(String query, String label) {
        StringBuilder response = new StringBuilder();
        try (PostgresConnector pc = new PostgresConnector("localhost", "HospitalEvora", "postgres", "123");
             Connection conn = pc.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
    
            while (rs.next()) {
                response.append("------------------------\n").append(label)
                        .append("\nTemperature: ").append(rs.getString("tempAVG"))
                        .append("\nHumidity: ").append(rs.getString("humiAVG"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error: {0}", e.getMessage());
        }
        return response.toString();
    }
    
}
