package sd.rest1;

import java.sql.Connection;
import java.sql.DriverManager;

public class TestDB {
    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/HospitalEvora", "postgres", "123")) {
            System.out.println("Connection successful!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

