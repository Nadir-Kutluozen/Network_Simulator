package org.example.network_simulator;

import javafx.scene.shape.Line;

import java.util.Objects;

public class Connection {
    private final NetworkDevice device1;
    private final NetworkDevice device2;
    private final Line line;

    public Connection(NetworkDevice device1, NetworkDevice device2, Line line) {
        this.device1 = device1;
        this.device2 = device2;
        this.line = line;
        bindLineToDevices();
    }

    public NetworkDevice getDevice1() {
        return device1;
    }

    public NetworkDevice getDevice2() {
        return device2;
    }

    public Line getLine() {
        return line;
    }

    public boolean involves(NetworkDevice device) {
        return device1 == device || device2 == device;
    }

    public NetworkDevice getOtherDevice(NetworkDevice device) {
        if (device1 == device) return device2;
        if (device2 == device) return device1;
        return null; // Should not happen if involves() is checked first
    }

    private void bindLineToDevices() {
        double offsetX = 25;
        double offsetY = 25;
        line.startXProperty().bind(device1.xPositionProperty().add(offsetX));
        line.startYProperty().bind(device1.yPositionProperty().add(offsetY));
        line.endXProperty().bind(device2.xPositionProperty().add(offsetX));
        line.endYProperty().bind(device2.yPositionProperty().add(offsetY));
    }

    public void unbind() {
        line.startXProperty().unbind();
        line.startYProperty().unbind();
        line.endXProperty().unbind();
        line.endYProperty().unbind();
    }

    public void highlight() {
        line.setStyle("-fx-stroke: red; -fx-stroke-width: 2;");
    }

    public void resetStyle() {
        line.setStyle("-fx-stroke: black;");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Connection other)) return false;
        return (device1 == other.device1 && device2 == other.device2)
                || (device1 == other.device2 && device2 == other.device1); // undirected check
    }

    @Override
    public int hashCode() {
        return Objects.hash(device1.getId() + device2.getId()); // simple hash
    }
}
