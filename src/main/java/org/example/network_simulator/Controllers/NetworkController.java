package org.example.network_simulator.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.network_simulator.Connection;
import org.example.network_simulator.DragAndDrop.PC;
import org.example.network_simulator.DragAndDrop.Server;
import org.example.network_simulator.NetworkDevice;
import org.example.network_simulator.ServerApplication;
import org.example.network_simulator.db.DbOps;
import org.example.network_simulator.models.Device;
import org.example.network_simulator.models.Session;
import org.example.network_simulator.models.User;
import org.example.network_simulator.NetworkUtils;
import javafx.animation.TranslateTransition;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import java.io.IOException;
import java.util.*;

public class NetworkController {

    @FXML private AnchorPane networkPane;
    @FXML private VBox palette;
    @FXML private Label infoLabel;
    @FXML private Label userName;
    @FXML private Button clearBtn;

    private final List<NetworkDevice> devices = new ArrayList<>();
    private final List<Connection> connections = new ArrayList<>();
    private final Map<PC, TerminalController> openTerminals = new HashMap<>();

    private boolean isConnecting = false;
    private NetworkDevice firstDeviceSelected = null;
    private Node firstNodeSelected = null;

    //before reinitializing, we have to create a method to load all the devices from the saved user
    private void loadSavedDevices() {
        User currentUser = Session.getUser();
        if (currentUser != null) {
            List<Device> loadedDevices = DbOps.loadDevices(currentUser.getId());

            for (Device device : loadedDevices) {
                // Recreate your devices based on type
                NetworkDevice newDevice;
                if ("PC".equalsIgnoreCase(device.getType())) {
                    PC pc = new PC(device.getX(), device.getY());
                    pc.setIpAddress(device.getIpAddress());
                    pc.setPort(device.getPort());
                    newDevice = pc;
                } else if ("Server".equalsIgnoreCase(device.getType())) {
                    Server server = new Server(device.getX(), device.getY());
                    server.setIpAddress(device.getIpAddress());
                    server.setPort(device.getPort());
                    server.startServer(); // important to listen
                    newDevice = server;
                } else {
                    newDevice = NetworkUtils.createDevice(device.getType(), device.getX(), device.getY());
                }

                addDeviceToPane(newDevice.getType(), newDevice.getXPosition(), newDevice.getYPosition());

                addDevice(newDevice);
            }

            infoLabel.setText("Loaded saved devices for " + currentUser.getUsername());
        }
    }

    @FXML
    private Button saveButton;

    @FXML
    void onSaveClick(ActionEvent event) {
        User currentUser = Session.getUser();
        if (currentUser != null) {
            DbOps.saveDevices(currentUser.getId(), devices);  // Save all current devices
            infoLabel.setText("Network saved to database!");
        } else {
            infoLabel.setText("No user session found!");
        }
    }



    @FXML
    public void initialize() {
        User currentUser = Session.getUser();
        if (currentUser != null) {
            userName.setText(currentUser.getUsername());
        }
        setupPaletteDrag();
        setupPaneDrop();
        infoLabel.setText("Drag to add. Click devices to connect.");
        loadSavedDevices();
    }

    @FXML
    void clearConnections(ActionEvent event) {
        // 1. Remove all connection lines
        for (Connection connection : connections) {
            connection.unbind(); // Detach bindings
            networkPane.getChildren().remove(connection.getLine());
        }
        connections.clear();

        // 2. Remove all icons
        networkPane.getChildren().removeIf(node -> node instanceof ImageView);

        // 3. Clear the device list
        devices.clear();

        // 4. Reset connection-related state
        isConnecting = false;
        firstDeviceSelected = null;
        firstNodeSelected = null;

        // 5. Update info label
        infoLabel.setText("All devices and connections cleared.");
    }



    private void setupPaletteDrag() {
        for (Node paletteIcon : palette.getChildren()) {
            if (paletteIcon instanceof ImageView) {
                paletteIcon.setOnDragDetected(event -> handlePaletteDragDetected(event, (ImageView) paletteIcon));
            }
        }
    }

    private void handlePaletteDragDetected(MouseEvent event, ImageView icon) {
        String deviceType = (String) icon.getUserData();
        if (deviceType == null) return;

        Dragboard db = icon.startDragAndDrop(TransferMode.COPY);
        ClipboardContent content = new ClipboardContent();
        content.putString(deviceType);
        db.setContent(content);
        event.consume();
    }

    private void setupPaneDrop() {
        networkPane.setOnDragOver(event -> {
            if (event.getGestureSource() != networkPane && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        networkPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString()) {
                addDeviceToPane(db.getString(), event.getX(), event.getY());
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });
    }

    private void addDeviceToPane(String type, double x, double y) {
        final Delta dragDelta = new Delta();
        NetworkDevice device = NetworkUtils.createDevice(type, x, y);
        if (device == null) return;

        // Assign IP/Port and start server if it's a PC or Server
        if (device instanceof PC pc) {
            // Set IP and port manually (replace these with your static counters if needed)
            pc.setIpAddress("192.168.1." + new Random().nextInt(100) + 100); // avoid duplicates if not saved
            pc.setPort(6000 + new Random().nextInt(1000));                   // range: 6000–6999
            pc.startServer();
        }

        ImageView icon = NetworkUtils.createDeviceIcon(type, palette);
        if (icon == null) return;

        icon.setLayoutX(x - 25);
        icon.setLayoutY(y - 25);

        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(200));
        tooltip.setHideDelay(Duration.millis(100));

        // Compose the tooltip text
        StringBuilder tooltipText = new StringBuilder();
        tooltipText.append("Name: ").append(device.getName()).append("\n");

        if (device instanceof PC pc) {
            tooltipText.append("IP: ").append(pc.getIpAddress()).append("\n");
            tooltipText.append("Port: ").append(pc.getPort());
        }

        tooltip.setText(tooltipText.toString());
        Tooltip.install(icon, tooltip);


        icon.setOnMousePressed(event -> {
            dragDelta.x = event.getX();
            dragDelta.y = event.getY();
            icon.toFront();
        });

        icon.setOnMouseDragged(event -> {
            icon.setLayoutX(event.getSceneX() - dragDelta.x - networkPane.getLayoutX());
            icon.setLayoutY(event.getSceneY() - dragDelta.y - networkPane.getLayoutY());

            device.setXPosition(icon.getLayoutX());
            device.setYPosition(icon.getLayoutY());
        });

        icon.setOnMouseReleased(event -> {
            if (event.isStillSincePress()) {
                if (event.getClickCount() == 2 && device instanceof PC) {
                    openTerminal((PC) device);
                } else {
                    handleDeviceClick(icon, device);
                }
            }
        });


        networkPane.getChildren().add(icon);
        addDevice(device);
    }





    private void handleDeviceClick(Node clickedNode, NetworkDevice clickedDevice) {
        if (!isConnecting) {
            firstDeviceSelected = clickedDevice;
            firstNodeSelected = clickedNode;
            isConnecting = true;
            clickedNode.setStyle("-fx-effect: dropshadow(gaussian, blue, 10, 0.5, 0, 0);");
            infoLabel.setText("Now click another device to connect.");
            return;
        }

        if (clickedDevice == firstDeviceSelected) {
            firstNodeSelected.setStyle(null);
            resetConnectionState();
            infoLabel.setText("Connection cancelled.");
            return;
        }

        if (NetworkUtils.areDevicesConnected(connections, firstDeviceSelected, clickedDevice)) {
            infoLabel.setText("Devices already connected.");
            resetConnectionState();
            return;
        }

        // Use your Connection class to encapsulate line creation and binding
        Connection connection = new Connection(firstDeviceSelected, clickedDevice, new Line());
        networkPane.getChildren().add(0, connection.getLine());
        addConnection(connection);

        infoLabel.setText("Connected " + firstDeviceSelected + " to " + clickedDevice);
        firstNodeSelected.setStyle(null);
        resetConnectionState();
    }

    private void resetConnectionState() {
        isConnecting = false;
        firstDeviceSelected = null;
        firstNodeSelected = null;
    }

    private void openTerminal(PC pc) {
        if (openTerminals.containsKey(pc)) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/network_simulator/terminal-view.fxml"));
            Parent root = loader.load();
            TerminalController terminalController = loader.getController();

            terminalController.setNetworkController(this);
            terminalController.setPcDevice(pc);
            registerTerminal(pc, terminalController);

            Stage terminalStage = new Stage();
            terminalStage.setTitle("Terminal - " + pc.toString());
            terminalStage.setScene(new Scene(root));
            terminalStage.initModality(Modality.NONE);
            terminalStage.initOwner(networkPane.getScene().getWindow());
            terminalStage.setOnCloseRequest(e -> unregisterTerminal(pc));
            terminalStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void executeCommand(PC pcDevice, String command, String[] args) {
        TerminalController terminal = openTerminals.get(pcDevice);
        if (terminal == null) return;

        switch (command.toLowerCase()) {
            case "ipconfig" -> NetworkUtils.showIpConfig(pcDevice, terminal);
            case "ping" -> {
                if (args.length == 0) {
                    terminal.displayOutput("Usage: ping <target>");
                    return;
                }
                Optional<NetworkDevice> found = findDeviceByIdentifier(args[0]);
                if (found.isEmpty() || !(found.get() instanceof PC targetPC)) {
                    terminal.displayOutput("Ping failed: Target not found or not a PC");
                    return;
                }
                NetworkUtils.handlePing(pcDevice, targetPC, terminal);
            }
            default -> terminal.displayOutput("Unknown command: " + command);
        }
    }

    private Optional<NetworkDevice> findDeviceByIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) return Optional.empty();
        String targetId = identifier.trim();

        return devices.stream().filter(dev -> {
            if (dev.toString().equalsIgnoreCase(targetId)) return true;
            if (dev instanceof PC) return ((PC) dev).getIpAddress().equals(targetId);
            return false;
        }).findFirst();
    }

    public void animatePacket(NetworkDevice from, NetworkDevice to, Color color, Runnable onArrival) {
        Circle packet = new Circle(6, color);
        networkPane.getChildren().add(packet);

        double startX = from.getXPosition();
        double startY = from.getYPosition();
        double endX = to.getXPosition();
        double endY = to.getYPosition();

        packet.setLayoutX(startX);
        packet.setLayoutY(startY);

        TranslateTransition transition = new TranslateTransition(Duration.seconds(1), packet);
        transition.setToX(endX - startX);
        transition.setToY(endY - startY);
        transition.setOnFinished(e -> {
            networkPane.getChildren().remove(packet);
            if (onArrival != null) onArrival.run();
        });
        transition.play();
    }


    public void registerTerminal(PC pc, TerminalController controller) {
        openTerminals.put(pc, controller);
    }

    public void unregisterTerminal(PC pc) {
        openTerminals.remove(pc);
    }

    public void addDevice(NetworkDevice device) {
        devices.add(device);
    }

    public void addConnection(Connection conn) {
        connections.add(conn);
    }

    public List<NetworkDevice> getDevices() {
        return devices;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void shutdownAllPCs() {
        devices.stream()
                .filter(d -> d instanceof PC)
                .map(d -> (PC) d)
                .forEach(PC::shutdown);
    }

    @FXML
    public void goToMedia() throws IOException {
        ServerApplication.setRoot("users-view.fxml");
    }

}

class Delta {
    double x, y;
}


