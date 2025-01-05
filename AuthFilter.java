package sd.rest1.filters;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.security.Key;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
@Priority(1)
public class AuthFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(AuthFilter.class.getName());
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor("your-very-secret-key-should-be-long".getBytes());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        
        // Allow access to the login endpoint
        if (path.equals("auth/login")) {
            return;
        }
    
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            LOGGER.log(Level.WARNING, "Authorization header missing or invalid");
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Missing or invalid token").build());
            return;
        }
    
        try {
            String token = authHeader.substring(7); // Remove "Bearer "
            LOGGER.log(Level.INFO, "Token received: {0}", token);
            
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
    
            // Extract information from the token
            String username = claims.getBody().getSubject();
            LOGGER.log(Level.INFO, "Authenticated user: {0}", username);
            
        } catch (JwtException e) {
            LOGGER.log(Level.WARNING, "Invalid token: {0}", e.getMessage());
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Invalid token").build());
        }
    }
    

}