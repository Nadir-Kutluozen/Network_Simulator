package org.example.network_simulator;

// PC.java
import java.util.Objects;

public class PC extends NetworkDevice {

    private String ipAddress;
    private String subnetMask = "255.255.255.0"; // Default mask

    public PC(double x, double y) {
        super("PC", x, y);
        // Assign a default IP based on ID (simple scheme)
        this.ipAddress = "192.168.1." + (100 + getId());
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

    // Override toString for better identification (used in ping target parsing)
    @Override
    public String toString() {
        // Consistent identifier like "PC1", "PC2"
        return getType() + getId();
    }

    // Useful for finding PCs by IP later if needed
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PC pc = (PC) o;
        return getId() == pc.getId(); // Assuming ID is unique
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}