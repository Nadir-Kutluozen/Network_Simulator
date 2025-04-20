package org.example.network_simulator.DragAndDrop;

import org.example.network_simulator.NetworkDevice;

import java.util.Objects;

public class PC extends NetworkDevice {

    private static int nextIpSuffix = 100; // Static counter to avoid duplicate IPs
    private String ipAddress;
    private String subnetMask = "255.255.255.0"; // Default subnet mask

    public PC(double x, double y) {
        super("PC", x, y);
        this.ipAddress = "192.168.1." + nextIpSuffix++;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getSubnetMask() {
        return subnetMask;
    }

    public void setSubnetMask(String subnetMask) {
        this.subnetMask = subnetMask;
    }

    /**
     * Returns a readable identifier, e.g., "PC1", "PC2"
     */
    @Override
    public String toString() {
        return getType() + getId();
    }

    /**
     * Checks equality based on unique ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PC pc)) return false;
        return getId() == pc.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
