package org.example.network_simulator.db;

import org.example.network_simulator.models.User;

import java.sql.*;

public class DbOps {
    private static final String DB_URL = "jdbc:mysql://networksimulator.mysql.database.azure.com:3306/network_db?useSSL=true&requireSSL=true";
    private static final String USERNAME = "nadirkutluozen";  // Azure requires full username
    private static final String PASSWORD = "1234811llA!";

    // todo - organize this!!
    public void initializeDatabase() {
        String serverUrl = "jdbc:mysql://networksimulator.mysql.database.azure.com:3306/?useSSL=true&requireSSL=true";
        String fullDbUrl = "jdbc:mysql://networksimulator.mysql.database.azure.com:3306/network_db?useSSL=true&requireSSL=true";
        String username = "nadirkutluozen";
        String password = "1234811llA!";

        try (
                // Connect to server (no DB here)
                Connection serverConn = DriverManager.getConnection(serverUrl, username, password);
                Statement serverStmt = serverConn.createStatement()
        ) {
            serverStmt.executeUpdate("CREATE DATABASE IF NOT EXISTS network_db");
            System.out.println("Database 'network_db' checked/created.");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        try (
                // Connect to the actual DB
                Connection conn = DriverManager.getConnection(fullDbUrl, username, password);
                Statement stmt = conn.createStatement()
        ) {
            String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(100) NOT NULL,
                email VARCHAR(200) NOT NULL UNIQUE,
                password VARCHAR(100) NOT NULL
            )
        """;
            stmt.executeUpdate(sql);
            System.out.println("User table ready.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Register a new user

    /**
     *
     * @param username
     * @param email
     * @param password
     * @return
     */
    public static boolean registerUser(String username, String email, String password) {
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement prepStat = conn.prepareStatement(sql)) {

            prepStat.setString(1, username);
            prepStat.setString(2, email);
            // todo - hash this later.
            prepStat.setString(3, password);

            prepStat.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    public static User authenticateUser(String username, String passwordInput) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (
                Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                PreparedStatement prepStat = conn.prepareStatement(sql)
        ) {
            prepStat.setString(1, username);
            prepStat.setString(2, passwordInput);

            ResultSet rs = prepStat.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                String email = rs.getString("email");
                return new User(id, username, email); // new user object created for the session
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


}
