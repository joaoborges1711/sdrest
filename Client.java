package sd.rest1;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Client class to interact with the REST API.
 */
public class Client {

    private static final String BASE_URL = "http://localhost:8080/";
    private final BufferedReader inputReader;
    private String token; // Stores the JWT token after login

    public Client() {
        this.inputReader = new BufferedReader(new InputStreamReader(System.in));
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    public void run() {
        int option = -1;
        while (option != 0) {
            displayMenu();
            try {
                option = Integer.parseInt(inputReader.readLine().trim());
                handleOption(option);
            } catch (IOException | NumberFormatException e) {
                System.out.println("Invalid input. Please try again.");
            }
        }
    }

    private void displayMenu() {
        System.out.println(" ------------------------------ ");
        System.out.println("|                              |");
        System.out.println("| 0 - Exit                     |");
        System.out.println("| 1 - Login                    |");
        System.out.println("| 2 - List Devices             |");
        System.out.println("| 3 - Create Device            |");
        System.out.println("| 4 - Update Device            |");
        System.out.println("| 5 - Delete Device            |");
        System.out.println("|                              |");
        System.out.println(" ------------------------------ ");
        System.out.print("Choose an option: ");
    }

    private void handleOption(int option) throws IOException {
        switch (option) {
            case 1:
                login();
                break;
            case 2:
                if (validateToken()) listDevices();
                break;
            case 3:
                if (validateToken()) createDevice();
                break;
            case 4:
                if (validateToken()) updateDevice();
                break;
            case 5:
                if (validateToken()) deleteDevice();
                break;
            case 0:
                System.out.println("Exiting...");
                break;
            default:
                System.out.println("Invalid option. Please choose again.");
                break;
        }
    }

    private void login() throws IOException {
        System.out.print("Enter username: ");
        String username = inputReader.readLine().trim();
        System.out.print("Enter password: ");
        String password = inputReader.readLine().trim();
    
        // Enviar os dados no formato application/x-www-form-urlencoded
        String body = "username=" + username + "&password=" + password;
    
        // Fazer a requisição POST
        String response = sendHttpRequest(URI.create(BASE_URL + "auth/login"), "POST", body, null, "application/x-www-form-urlencoded");
    
        if (response.startsWith("Error") || response.contains("Invalid username or password")) {
            System.out.println("Login failed: " + response);
        } else {
            // Extrair o valor do token do JSON
            token = response.substring(response.indexOf(":") + 3, response.length() - 2); // Remove {"token": " e "}
            System.out.println("Login successful. Token: " + token);
        }
    }
    
    

    private boolean validateToken() {
        if (token == null) {
            System.out.println("You are not logged in. Please login first.");
            return false;
        }

        String response = sendGetRequestWithAuth(BASE_URL + "auth/validate", token);
        if (response.contains("Token is valid")) {
            return true;
        } else {
            System.out.println("Token validation failed: " + response);
            return false;
        }
    }

    private void listDevices() {
        String response = sendGetRequestWithAuth(BASE_URL + "devices", token);
        System.out.println("Response: " + response);
        if (response.isEmpty()) {
            System.out.println("No response or failed to connect to the server.");
        } else {
            System.out.println("Successfully retrieved devices: " + response);
        }
    }
    

    private void createDevice() throws IOException {
        System.out.print("Enter room: ");
        String room = inputReader.readLine().trim();
        System.out.print("Enter floor: ");
        String floor = inputReader.readLine().trim();
        System.out.print("Enter building: ");
        String building = inputReader.readLine().trim();
        System.out.print("Enter service: ");
        String service = inputReader.readLine().trim();
    
        // Monta os parâmetros no formato esperado pelo servidor
        String body = "room=" + room + "&floor=" + floor + "&building=" + building + "&service=" + service;
    
        // Envia a requisição POST
        String response = sendHttpRequest(
            URI.create(BASE_URL + "create/device"), 
            "POST", 
            body, 
            token, 
            "application/x-www-form-urlencoded"
        );
    
        // Processa a resposta
        if (response.contains("message")) {
            System.out.println("Response: " + response);
        } else {
            System.out.println("Failed to create device. Response: " + response);
        }
    }
    
    

    private void updateDevice() throws IOException {
        System.out.print("Enter device ID: ");
        String id = inputReader.readLine().trim();
        System.out.print("Enter new room (or leave blank): ");
        String room = inputReader.readLine().trim();
        System.out.print("Enter new floor (or leave blank): ");
        String floor = inputReader.readLine().trim();
        System.out.print("Enter new building (or leave blank): ");
        String building = inputReader.readLine().trim();
        System.out.print("Enter new service (or leave blank): ");
        String service = inputReader.readLine().trim();

        Map<String, String> params = new HashMap<>();
        params.put("id", id);
        if (!room.isEmpty()) params.put("room", room);
        if (!floor.isEmpty()) params.put("floor", floor);
        if (!building.isEmpty()) params.put("building", building);
        if (!service.isEmpty()) params.put("service", service);

        String response = sendPostRequestWithAuth(BASE_URL + "devices/update", params, token);
        System.out.println("Response: " + response);
    }

    private void deleteDevice() throws IOException {
        System.out.print("Enter device ID to delete: ");
        String id = inputReader.readLine().trim();

        String response = sendDeleteRequestWithAuth(URI.create(BASE_URL + "devices/" + id), token);
        System.out.println("Response: " + response);
    }

    private String sendGetRequestWithAuth(String url, String token) {
        return sendHttpRequest(URI.create(url), "GET", null, token, "application/json");
    }
    

    private String sendPostRequestWithAuth(String url, Map<String, String> params, String token) {
        String body = buildQueryString(params).substring(1); // Remove leading "?"
        return sendHttpRequest(URI.create(url), "POST", body, token, "application/x-www-form-urlencoded");
    }
    

    private String sendDeleteRequestWithAuth(URI targetURI, String token) {
        return sendHttpRequest(targetURI, "DELETE", null, token, "application/json");
    }
    

    private String sendHttpRequest(URI targetURI, String method, String body, String token, String contentType) {
        StringBuilder response = new StringBuilder();
        HttpURLConnection connection = null;
    
        try {
            URL url = targetURI.toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method.toUpperCase());
            connection.setRequestProperty("Accept", "application/json");
    
            // Set Content-Type if provided
            if (contentType != null) {
                connection.setRequestProperty("Content-Type", contentType);
            }
    
            // Add Authorization header if token is provided
            if (token != null && !token.isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer " + token);
            }
    
            // Write body if method is POST or PUT
            if (body != null && (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT"))) {
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(body.getBytes("UTF-8"));
                    os.flush();
                }
            }
    
            // Get the response code and input stream
            int status = connection.getResponseCode();
            InputStream inputStream = (status >= 200 && status < 300) ?
                                      connection.getInputStream() : connection.getErrorStream();
    
            // Read response if inputStream is not null
            if (inputStream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
            }
    
            // Log status and response
            if (status >= 200 && status < 300) {
                System.out.println("HTTP Success (" + status + "): " + response);
            } else {
                System.out.println("HTTP Error (" + status + "): " + response);
            }
    
        } catch (IOException e) {
            System.out.println("Failed to connect to the server: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return response.toString();
    }
    
    
    
    

    private String buildQueryString(Map<String, String> params) {
        if (params == null || params.isEmpty()) return "";
        StringBuilder queryString = new StringBuilder("?");
        params.forEach((key, value) -> queryString.append(key).append("=").append(value).append("&"));
        return queryString.substring(0, queryString.length() - 1);
    }
    private String sendPostRequest(String targetURL, String body) {
        return sendHttpRequest(URI.create(targetURL), "POST", body, null, "application/json");
    }
    
    
}
