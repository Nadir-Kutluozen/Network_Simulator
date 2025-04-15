package org.example.network_simulator;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public abstract class NetworkDevice {
    private static int idCounter = 0;
    private final int id;
    private final String type;
    // JavaFX properties to allow binding with visual elements
    private final DoubleProperty xPosition = new SimpleDoubleProperty();
    private final DoubleProperty yPosition = new SimpleDoubleProperty();

    public NetworkDevice(String type, double x, double y) {
        this.id = ++idCounter;
        this.type = type;
        this.xPosition.set(x);
        this.yPosition.set(y);
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public double getXPosition() { return xPosition.get(); }
    public DoubleProperty xPositionProperty() { return xPosition; }
    public void setXPosition(double xPosition) { this.xPosition.set(xPosition); }

    public double getYPosition() { return yPosition.get(); }
    public DoubleProperty yPositionProperty() { return yPosition; }
    public void setYPosition(double yPosition) { this.yPosition.set(yPosition); }

    @Override
    public String toString() {
        // Provides a consistent way to identify devices, e.g., "PC1", "Switch2"
        return type + getId();
    }

    // Could add lists for connections later
}