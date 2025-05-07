package org.example.network_simulator.db;

import org.example.network_simulator.DragAndDrop.PC;
import org.example.network_simulator.NetworkDevice;
import org.example.network_simulator.models.Device;
import org.example.network_simulator.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

            String deviceTableSql = """
        CREATE TABLE IF NOT EXISTS devices (
            id INT AUTO_INCREMENT PRIMARY KEY,
            user_id INT NOT NULL,
            type VARCHAR(50) NOT NULL,
            x DOUBLE NOT NULL,
            y DOUBLE NOT NULL,
            ip_address VARCHAR(50),
            port INT,
            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
        )
    """;

            stmt.executeUpdate(sql);
            System.out.println("User table ready.");

            stmt.executeUpdate(deviceTableSql);
            System.out.println("Devices table ready.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    // Register a new user

    /**
     * save the user to the database
     * @param username username
     * @param email email
     * @param password password
     * @return ture is the save was successful
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

    /**
     * authenticate the user by using a specific query
     * @param username username
     * @param passwordInput password
     * @return Specific user object for session usage.
     */
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

    /**
     * Save the current device usage to the database
     * for the specific user.
     * @param userId specific user
     * @param devices have the device
     */
    public static void saveDevices(int userId, List<NetworkDevice> devices) {
        String clearSql = "DELETE FROM devices WHERE user_id = ?";
        String insertSql = "INSERT INTO devices (user_id, type, x, y, ip_address, port) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {

            // we have to clear the old spot before we even can add a new one
            try (PreparedStatement clearStat = conn.prepareStatement(clearSql)) {
                clearStat.setInt(1, userId);
                clearStat.executeUpdate();
            }

            //insert the device to the database
            try (PreparedStatement insertStat = conn.prepareStatement(insertSql)) {
                for (NetworkDevice device : devices) {
                    String type = device.getType();
                    double x = device.getXPosition();
                    double y = device.getYPosition();
                    String ip = null;
                    int port = 0;

                    if (device instanceof PC pc) {
                        ip = pc.getIpAddress();
                        port = pc.getPort();
                    }

                    insertStat.setInt(1, userId);
                    insertStat.setString(2, type);
                    insertStat.setDouble(3, x);
                    insertStat.setDouble(4, y);
                    insertStat.setString(5, ip);
                    insertStat.setInt(6, port);
                    insertStat.addBatch();
                }

                insertStat.executeBatch();
            }

            System.out.println("Devices saved successfully for user: " + userId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static List<Device> loadDevices(int userId) {
        List<Device> devices = new ArrayList<>();

        String sql = "SELECT * FROM devices WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String type = rs.getString("type");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                String ip = rs.getString("ip_address");
                int port = rs.getInt("port");

                Device device = new Device(id, userId, type, x, y, ip, port);
                devices.add(device);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return devices;
    }



}
