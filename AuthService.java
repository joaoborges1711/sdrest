package sd.rest1.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles user authentication and token management.
 */
@Path("auth")
public class AuthService {

    private static final Key SECRET_KEY = Keys.hmacShaKeyFor("your-very-secret-key-should-be-long".getBytes());
   // private static final long EXPIRATION_TIME = 3600000; // 1 hour in milliseconds
    private static final Map<String, String> USERS = new HashMap<>();

    static {
        // Predefined users (username:password)
        USERS.put("postgres", "123");
    }

    /**
     * Handles user login and token generation.
     *
     * @param username Username of the user.
     * @param password Password of the user.
     * @return JWT token if authentication is successful.
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@FormParam("username") String username,
                          @FormParam("password") String password) {
        if (username == null || password == null || !USERS.containsKey(username) || !USERS.get(username).equals(password)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"Invalid username or password\"}").build();
        }

        String token = Jwts.builder()
        .setSubject("postgres")
        .claim("role", "admin")
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
        .signWith(SECRET_KEY)
        .compact();

        return Response.ok("{\"token\": \"" + token + "\"}").build();
    }

    /**
     * Validates the provided JWT token.
     *
     * @param token JWT token to validate.
     * @return Response indicating whether the token is valid.
     */
    @GET
    @Path("/validate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateToken(@HeaderParam("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"Invalid or missing token\"}").build();
        }

        try {
            String jwt = token.substring(7);
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(jwt);
            String username = claims.getBody().getSubject();
            String role = claims.getBody().get("role", String.class);
            return Response.ok("{\"message\": \"Token is valid\", \"username\": \"" + username + "\", \"role\": \"" + role + "\"}").build();
        } catch (JwtException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\": \"Invalid token\"}").build();
        }
    }

    /**
     * Logout endpoint (optional for stateless JWT, but included for completeness).
     *
     * @return Response indicating logout success.
     */
    @POST
    @Path("/logout")
    @Produces(MediaType.TEXT_PLAIN)
    public Response logout() {
        // In a real-world application, consider revoking tokens (e.g., adding them to a blocklist).
        return Response.ok("Logout successful").build();
    }
}
