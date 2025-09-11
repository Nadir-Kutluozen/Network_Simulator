package org.example.network_simulator.models;

public class Device {
    private int id;
    private int userId;
    private String type;
    private double x;
    private double y;
    private String ipAddress;
    private int port;

    public Device(int userId, String type, double x, double y, String ipAddress, int port) {
        this.userId = userId;
        this.type = type;
        this.x = x;
        this.y = y;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    // Here just in case if the device has ID, we can reach it with it!
    public Device(int id, int userId, String type, double x, double y, String ipAddress, int port) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.x = x;
        this.y = y;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
