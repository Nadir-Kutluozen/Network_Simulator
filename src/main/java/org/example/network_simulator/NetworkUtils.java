package org.example.network_simulator;

import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.example.network_simulator.DragAndDrop.PC;
import org.example.network_simulator.DragAndDrop.Server;
import org.example.network_simulator.DragAndDrop.Switch;
import org.example.network_simulator.Controllers.TerminalController;

import java.util.List;
import java.util.Random;

public class NetworkUtils {

    public static NetworkDevice createDevice(String type, double x, double y) {
        return switch (type) {
            case "PC" -> new PC(x, y);
            case "Switch" -> new Switch(x, y);
            case "Server" -> new Server(x, y);
            default -> null;
        };
    }

    public static ImageView createDeviceIcon(String type, VBox palette) {
        ImageView icon = new ImageView();
        icon.setFitWidth(50);
        icon.setFitHeight(50);
        icon.setPreserveRatio(true);

        switch (type) {
            case "PC":
                icon.setImage(((ImageView) palette.lookup("#pcPaletteIcon")).getImage());
                break;
            case "Switch":
                icon.setImage(((ImageView) palette.lookup("#switchPaletteIcon")).getImage());
                break;
            case "Server":
                icon.setImage(((ImageView) palette.lookup("#serverPaletteIcon")).getImage());
                break;
            default:
                return null;
        }
        return icon;
    }

    public static boolean areDevicesConnected(List<Connection> connections, NetworkDevice d1, NetworkDevice d2) {
        return connections.stream().anyMatch(conn ->
                (conn.getDevice1() == d1 && conn.getDevice2() == d2) ||
                        (conn.getDevice1() == d2 && conn.getDevice2() == d1));
    }

    public static void showIpConfig(PC pc, TerminalController terminal) {
        terminal.displayOutput("\nIPv4 Address: " + pc.getIpAddress());
        terminal.displayOutput("Subnet Mask: " + pc.getSubnetMask());
        terminal.displayOutput("Port: " + pc.getPort());
    }

    public static void handlePing(PC source, PC target, TerminalController terminal) {
        terminal.displayOutput("\nPinging " + target + " [" + target.getIpAddress() + "] with 32 bytes of data:");
        for (int i = 0; i < 4; i++) {
            try {
                source.sendMessage(target.getIpAddress(), target.getPort(), "ping");
                terminal.displayOutput("Reply from " + target.getIpAddress() + ": bytes=32 time=" + (1 + new Random().nextInt(10)) + "ms TTL=128");
            } catch (Exception e) {
                terminal.displayOutput("Request timed out.");
            }
        }
    }
}
