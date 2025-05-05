package org.example.network_simulator.DragAndDrop;

import org.example.network_simulator.NetworkDevice;

public class Switch extends NetworkDevice {
    public Switch(double x, double y) {
        super("Switch", x, y);
    }

    @Override
    public String toString() {
        return getType() + getId();
    }
}
