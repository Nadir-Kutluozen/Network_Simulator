package org.example.network_simulator;

import javafx.scene.shape.Line;

public class Connection {
    private final NetworkDevice device1;
    private final NetworkDevice device2;
    private final Line line; // Visual representation

    public Connection(NetworkDevice device1, NetworkDevice device2, Line line) {
        this.device1 = device1;
        this.device2 = device2;
        this.line = line;
        bindLineToDevices();
    }

    public NetworkDevice getDevice1() { return device1; }
    public NetworkDevice getDevice2() { return device2; }
    public Line getLine() { return line; }

    // Check if this connection involves a specific device
    public boolean involves(NetworkDevice device) {
        return device1 == device || device2 == device;
    }

    // Get the other device in the connection
    public NetworkDevice getOtherDevice(NetworkDevice device) {
        if (device1 == device) return device2;
        if (device2 == device) return device1;
        return null; // Should not happen if involves() is checked first
    }

    // Bind line ends to device centers
    private void bindLineToDevices() {
        // Assuming visual node size is around 50x50 for centering
        // You might need to adjust this offset based on actual node size
        double offsetX = 25;
        double offsetY = 25;

        line.startXProperty().bind(device1.xPositionProperty().add(offsetX));
        line.startYProperty().bind(device1.yPositionProperty().add(offsetY));
        line.endXProperty().bind(device2.xPositionProperty().add(offsetX));
        line.endYProperty().bind(device2.yPositionProperty().add(offsetY));
    }

    // Call this if a device involved in the connection is removed
    public void unbind() {
        line.startXProperty().unbind();
        line.startYProperty().unbind();
        line.endXProperty().unbind();
        line.endYProperty().unbind();
    }
}