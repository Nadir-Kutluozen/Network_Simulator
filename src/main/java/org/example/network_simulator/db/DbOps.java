package org.example.network_simulator.db;

import org.example.network_simulator.DragAndDrop.PC;
import org.example.network_simulator.NetworkDevice;
import org.example.network_simulator.models.Device;
import org.example.network_simulator.models.Session;
import org.example.network_simulator.models.User;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DbOps {
    private static final Properties PROPS = loadDbProperties();
    private static final String DB_URL = PROPS.getProperty("db.url");
    private static final String USERNAME = PROPS.getProperty("db.username");// Azure requires full username
    private static final String PASSWORD = PROPS.getProperty("db.username");

    // todo - organize this!!
    public void initializeDatabase() {
        String serverUrl = PROPS.getProperty("db.serverUrl");
        String fullDbUrl = PROPS.getProperty("db.url");
        String username = PROPS.getProperty("db.username");
        String password = PROPS.getProperty("db.username");

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
        password VARCHAR(100) NOT NULL,
        profile_pic LONGBLOB
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
     * Save the user to the database and create a session.
     *
     * @param username username
     * @param email    email
     * @param password password
     * @return true if the save was successful and session set
     */
    public static boolean registerUser(String username, String email, String password) {
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement prepStat = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            prepStat.setString(1, username);
            prepStat.setString(2, email);
            // todo - hash this later.
            prepStat.setString(3, password);

            int rowsAffected = prepStat.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = prepStat.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);

                        User newUser = new User(userId, username, email);
                        Session.setUser(newUser);

                        System.out.println("User registered and session set: " + newUser.getUsername());
                        return true;
                    }
                }
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Registration failed: " + e.getMessage());
            return false;
        }
    }


    /**
     * Update the profile picture for a specific user by their ID.
     *
     * @param id         The user's ID.
     * @param profilePic The profile picture as a byte array.
     * @return true if update is successful; false otherwise.
     */
    public static boolean updateProfilePic(int id, byte[] profilePic) {
        String sql = "UPDATE users SET profile_pic = ? WHERE id = ?";
        try (
                Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setBytes(1, profilePic);
            stmt.setInt(2, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Failed to update profile picture by ID: " + e.getMessage());
            return false;
        }
    }


    /**
     * Authenticate the user by using a specific query.
     *
     * @param username      username
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
                byte[] profilePic = rs.getBytes("profile_pic"); // to set the profile pick

                // Create the User object
                User user = new User(id, username, email, profilePic);

                // Set the profile pic
                user.setProfilePic(profilePic);
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @param userId
     * @return
     */
    public static byte[] getProfilePicById(int userId) {
        String sql = "SELECT profile_pic FROM users WHERE id = ?";
        try (
                Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBytes("profile_pic");
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch profile picture: " + e.getMessage());
        }
        return null;
    }


    /**
     * Save the current device usage to the database
     * for the specific user.
     *
     * @param userId  specific user
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

    // we need to gather all the user in the table to display, so later people can access and talk to them

    /**
     * @return list of user to display in table view
     */
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, email, profile_pic FROM users"; // prep Query

        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD); // after establishing the connection.
             PreparedStatement prepStat = conn.prepareStatement(sql); // select the user
             ResultSet rs = prepStat.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                users.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users; // return the l;ist of user!!
    }

    private static Properties loadDbProperties() {
        Properties props = new Properties();
        try (InputStream input = DbOps.class.getClassLoader().getResourceAsStream("db.properties")) {
            props.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }



}
