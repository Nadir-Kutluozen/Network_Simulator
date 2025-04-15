package org.example.network_simulator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;


import java.io.IOException;
import java.util.*;

public class NetworkController {

    @FXML private AnchorPane networkPane;
    @FXML private VBox palette;
    @FXML private Label infoLabel;

    // Model Data
    private final List<NetworkDevice> devices = new ArrayList<>();
    private final List<Connection> connections = new ArrayList<>();

    // UI Mappings
    private final Map<Node, NetworkDevice> nodeToDeviceMap = new HashMap<>();
    private final Map<NetworkDevice, Node> deviceToNodeMap = new HashMap<>();
    private final Map<PC, TerminalController> openTerminals = new HashMap<>();

    // State for connecting devices
    private boolean isConnecting = false;
    private NetworkDevice firstDeviceSelected = null;
    private Node firstNodeSelected = null;

    // State for dragging devices within the pane
    private double dragStartX, dragStartY;
    private Node draggedNode = null;


    @FXML
    public void initialize() {
        setupPaletteDrag();
        setupPaneDrop();
        setupPaneClick(); // For initiating connections
        infoLabel.setText("Drag icons to add devices.\nClick device, then another to connect.\nDouble-click PC to open terminal.");
    }

    // --- Drag and Drop from Palette ---

    private void setupPaletteDrag() {
        for (Node paletteIcon : palette.getChildren()) {
            if (paletteIcon instanceof ImageView) {
                paletteIcon.setOnDragDetected(this::handlePaletteDragDetected);
                // Add hover effect maybe?
                paletteIcon.setOnMouseEntered(e -> paletteIcon.setCursor(Cursor.HAND));
                paletteIcon.setOnMouseExited(e -> paletteIcon.setCursor(Cursor.DEFAULT));
            }
        }
    }

    private void handlePaletteDragDetected(MouseEvent event) {
        Node sourceNode = (Node) event.getSource();
        if (sourceNode instanceof ImageView && sourceNode.getUserData() != null) {
            Dragboard db = sourceNode.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            String deviceType = (String) sourceNode.getUserData();
            content.putString(deviceType); // Put device type identifier
            // Optional: set drag view
            // db.setDragView(((ImageView) sourceNode).getImage());
            db.setContent(content);
            event.consume();
            System.out.println("Drag detected: " + deviceType);
        }
    }

    // --- Drop onto Network Pane ---

    private void setupPaneDrop() {
        networkPane.setOnDragOver(event -> {
            if (event.getGestureSource() != networkPane && event.getDragboard().hasString()) {
                // Allow for moving devices already on the pane OR copying from palette
                if(draggedNode != null) { // We are moving an existing node
                    event.acceptTransferModes(TransferMode.MOVE);
                } else { // We are copying from palette
                    event.acceptTransferModes(TransferMode.COPY);
                }
            }
            event.consume();
        });

        networkPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                // Check if it's a move operation originating from the pane itself
                if (draggedNode != null) {
                    NetworkDevice device = nodeToDeviceMap.get(draggedNode);
                    if (device != null) {
                        // Calculate new position based on drop point relative to drag start
                        double newX = event.getX() - dragStartX;
                        double newY = event.getY() - dragStartY;

                        // Keep within bounds (optional)
                        newX = Math.max(0, Math.min(networkPane.getWidth() - ((ImageView)draggedNode).getFitWidth(), newX));
                        newY = Math.max(0, Math.min(networkPane.getHeight()- ((ImageView)draggedNode).getFitHeight(), newY));


                        // Update model position (which updates node via binding)
                        device.setXPosition(newX);
                        device.setYPosition(newY);

                        // Ensure node's layout properties are also set directly
                        // Sometimes binding needs a nudge or direct setting is clearer for initial placement/move
                        draggedNode.setLayoutX(newX);
                        draggedNode.setLayoutY(newY);


                        System.out.println("Moved " + device.getType() + " to (" + newX + ", " + newY + ")");
                        success = true;
                    }
                } else {
                    // It's a drop from the palette (COPY operation)
                    String deviceType = db.getString();
                    System.out.println("Dropped: " + deviceType);
                    addDeviceToPane(deviceType, event.getX(), event.getY());
                    success = true;
                }
            }
            event.setDropCompleted(success);
            // Clear dragged node state *after* drop is completed
            draggedNode = null;
            networkPane.setCursor(Cursor.DEFAULT);
            event.consume();
        });

        // Handle drag exiting the pane during a move operation
        networkPane.setOnDragExited(event -> {
            if (draggedNode != null) {
                // Optional: reset cursor or visual feedback if needed
                networkPane.setCursor(Cursor.DEFAULT); // Reset cursor if it was changed
            }
        });
        // Handle drag entering the pane during a move operation
        networkPane.setOnDragEntered(event -> {
            // Check if we are dragging a node that originated from the pane
            if (event.getGestureSource() != networkPane && draggedNode != null) {
                networkPane.setCursor(Cursor.MOVE); // Indicate move possibility
            } else if (event.getGestureSource() != networkPane && event.getDragboard().hasString()) {
                networkPane.setCursor(Cursor.OPEN_HAND); // Indicate copy possibility
            }
            event.consume();
        });

    }


    private void addDeviceToPane(String type, double x, double y) {
        NetworkDevice device = null;
        ImageView deviceIcon = new ImageView();
        deviceIcon.setFitHeight(50.0); // Match palette size or define elsewhere
        deviceIcon.setFitWidth(50.0);
        deviceIcon.setPreserveRatio(true);

        // Center the icon on the drop point (adjusting for icon size)
        double placeX = x - deviceIcon.getFitWidth() / 2;
        double placeY = y - deviceIcon.getFitHeight() / 2;

        // Prevent placing outside bounds (simple)
        placeX = Math.max(0, Math.min(networkPane.getWidth() - deviceIcon.getFitWidth(), placeX));
        placeY = Math.max(0, Math.min(networkPane.getHeight() - deviceIcon.getFitHeight(), placeY));

        switch (type) {
            case "PC":
                device = new PC(placeX, placeY);
                // Use the actual icon image used in the palette
                deviceIcon.setImage(((ImageView) palette.lookup("#pcPaletteIcon")).getImage());
                deviceIcon.getStyleClass().add("pc-icon"); // Add CSS class
                break;
            case "Switch":
                device = new Switch(placeX, placeY);
                deviceIcon.setImage(((ImageView) palette.lookup("#switchPaletteIcon")).getImage());
                deviceIcon.getStyleClass().add("switch-icon");
                break;
            case "Router":
                device = new Router(placeX, placeY);
                deviceIcon.setImage(((ImageView) palette.lookup("#routerPaletteIcon")).getImage());
                deviceIcon.getStyleClass().add("router-icon");
                break;
            default:
                System.err.println("Unknown device type dropped: " + type);
                return;
        }

        if (device != null) {
            deviceIcon.setLayoutX(placeX);
            deviceIcon.setLayoutY(placeY);

            // Bind visual node position to model position
            // deviceIcon.layoutXProperty().bind(device.xPositionProperty());
            // deviceIcon.layoutYProperty().bind(device.yPositionProperty());
            // Direct setting might be initially less complex than pure binding for drag/drop
            // If using binding, ensure the model updates trigger UI updates reliably.

            devices.add(device);
            networkPane.getChildren().add(deviceIcon);
            nodeToDeviceMap.put(deviceIcon, device);
            deviceToNodeMap.put(device, deviceIcon);

            // Add event handlers for the newly created device icon
            setupDeviceNodeEvents(deviceIcon, device);
        }
    }

    // --- Event Handlers for Devices on the Pane ---

    private void setupDeviceNodeEvents(Node node, NetworkDevice device) {
        // 1. Clicking for Connection
        node.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (event.getClickCount() == 1) {
                    handleDeviceClickForConnection(node, device);
                } else if (event.getClickCount() == 2) {
                    // Double-click to open terminal (only for PCs)
                    handleDeviceDoubleClick(device);
                }
                event.consume(); // Consume to prevent pane click handler firing
            }
        });

        // 2. Drag and Move within the Pane
        node.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                dragStartX = event.getX(); // Position relative to node's top-left
                dragStartY = event.getY();
                draggedNode = node; // Mark this node as the one being dragged
                node.setCursor(Cursor.MOVE);
                node.toFront(); // Bring to front while dragging
                event.consume();
            }
        });

        node.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY && draggedNode == node) {
                // Calculate new top-left position in the parent pane's coordinates
                double newX = node.getLayoutX() + (event.getX() - dragStartX);
                double newY = node.getLayoutY() + (event.getY() - dragStartY);

                // --- Keep within bounds ---
                double iconWidth = node.getBoundsInLocal().getWidth();
                double iconHeight = node.getBoundsInLocal().getHeight();
                newX = Math.max(0, Math.min(networkPane.getWidth() - iconWidth, newX));
                newY = Math.max(0, Math.min(networkPane.getHeight() - iconHeight, newY));
                // --- --- --- --- --- --- ---

                // Update model position first
                device.setXPosition(newX);
                device.setYPosition(newY);

                // Update the node's layout properties directly
                node.setLayoutX(newX);
                node.setLayoutY(newY);

                // Note: Drag start coords don't need update here, they are relative to the node
                event.consume();
            }
        });

        node.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY && draggedNode == node) {
                node.setCursor(Cursor.HAND); // Or Cursor.DEFAULT
                draggedNode = null; // Stop dragging this node
                event.consume();
            }
        });


        // 3. Hover effects
        node.setOnMouseEntered(event -> node.setCursor(Cursor.HAND));
        node.setOnMouseExited(event -> {
            // Only reset to default if not currently being dragged
            if (draggedNode != node) {
                node.setCursor(Cursor.DEFAULT);
            }
        });
    }

    // --- Reset drag state if mouse released over the pane background ---
    private void setupPaneClick() {
        networkPane.setOnMouseReleased(event -> {
            if (draggedNode != null) {
                // If a drag was in progress but mouse released over the pane bg
                draggedNode.setCursor(Cursor.HAND); // Reset cursor
                draggedNode = null;
            }
            // Also reset connection state if clicking on the background
            if (isConnecting) {
                resetConnectionState();
            }
            event.consume();
        });
    }


    // --- Device Connection Logic ---

    private void handleDeviceClickForConnection(Node clickedNode, NetworkDevice clickedDevice) {
        System.out.println("Clicked on device: " + clickedDevice);
        if (!isConnecting) {
            // Start connection process
            firstDeviceSelected = clickedDevice;
            firstNodeSelected = clickedNode;
            isConnecting = true;
            // Optional: Visual feedback (e.g., highlight the selected node)
            clickedNode.setStyle("-fx-effect: dropshadow(three-pass-box, blue, 10, 0.5, 0, 0);");
            infoLabel.setText("Connecting... Click second device.");
            System.out.println("Starting connection from: " + clickedDevice);
        } else {
            // Complete connection process
            if (clickedDevice != firstDeviceSelected) { // Ensure not connecting to itself
                NetworkDevice secondDeviceSelected = clickedDevice;
                System.out.println("Connecting " + firstDeviceSelected + " to " + secondDeviceSelected);

                // Check if connection already exists (basic check)
                boolean alreadyConnected = connections.stream().anyMatch(conn ->
                        (conn.getDevice1() == firstDeviceSelected && conn.getDevice2() == secondDeviceSelected) ||
                                (conn.getDevice1() == secondDeviceSelected && conn.getDevice2() == firstDeviceSelected)
                );

                if (!alreadyConnected) {
                    // Create visual line
                    Line line = new Line();
                    line.setStroke(Color.BLACK);
                    line.setStrokeWidth(2);
                    line.setMouseTransparent(true); // Line shouldn't intercept mouse events

                    // Add line to the pane (ensure it's behind devices)
                    networkPane.getChildren().add(0, line); // Add at index 0 to be in the back

                    // Create Connection model object (this also binds the line ends)
                    Connection newConnection = new Connection(firstDeviceSelected, secondDeviceSelected, line);
                    connections.add(newConnection);

                    System.out.println("Connection created.");
                } else {
                    System.out.println("Devices already connected.");
                    infoLabel.setText("Devices already connected.");
                }

                resetConnectionState();

            } else {
                // Clicked the same device again - cancel connection
                System.out.println("Connection cancelled (clicked same device).");
                resetConnectionState();
            }
        }
    }

    private void resetConnectionState() {
        if (firstNodeSelected != null) {
            firstNodeSelected.setStyle(""); // Remove visual feedback
        }
        isConnecting = false;
        firstDeviceSelected = null;
        firstNodeSelected = null;
        infoLabel.setText("Drag icons to add. Click devices to connect.");
    }

    // --- Terminal Handling ---

    private void handleDeviceDoubleClick(NetworkDevice device) {
        if (device instanceof PC) {
            PC pc = (PC) device;
            System.out.println("Double-clicked on PC: " + pc);

            // Check if terminal is already open
            if (openTerminals.containsKey(pc)) {
                // Bring existing terminal to front
                Stage stage = (Stage) openTerminals.get(pc).getOutputArea().getScene().getWindow();
                if (stage != null) {
                    stage.toFront();
                }
                System.out.println("Terminal already open for " + pc);
                return;
            }

            // Open new terminal window
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("TerminalView.fxml"));
                Parent root = loader.load();
                TerminalController terminalController = loader.getController();

                // Pass necessary references to the terminal controller
                terminalController.setNetworkController(this);
                terminalController.setPcDevice(pc);

                Stage terminalStage = new Stage();
                terminalStage.setTitle("Terminal - " + pc.toString());
                terminalStage.setScene(new Scene(root));
                terminalStage.initModality(Modality.NONE); // Allow interaction with main window
                terminalStage.initOwner(networkPane.getScene().getWindow()); // Optional: Associate with main window

                // Keep track of open terminal
                openTerminals.put(pc, terminalController);

                // Handle terminal window closing
                terminalStage.setOnCloseRequest(event -> {
                    openTerminals.remove(pc);
                    System.out.println("Closed terminal for " + pc);
                });


                terminalStage.show();
                System.out.println("Opened terminal for " + pc);

            } catch (IOException e) {
                System.err.println("Error loading TerminalView.fxml: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // --- Helper Method to Find Device by Identifier (Name or IP) ---
    private Optional<NetworkDevice> findDeviceByIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return Optional.empty();
        }
        String targetId = identifier.trim(); //.toUpperCase(); // Make case-insensitive if desired

        return devices.stream()
                .filter(dev -> {
                    // Check against toString() representation (e.g., "PC1", "Switch2")
                    if (dev.toString().equalsIgnoreCase(targetId)) {
                        return true;
                    }
                    // If it's a PC, also check against its IP address
                    if (dev instanceof PC) {
                        return ((PC) dev).getIpAddress().equals(targetId);
                    }
                    return false;
                })
                .findFirst();
    }


    // --- Command Execution Logic ---
    public void executeCommand(PC sourcePc, String command, String[] args) {
        TerminalController sourceTerminal = openTerminals.get(sourcePc);
        if (sourceTerminal == null) {
            System.err.println("Command executed for PC with no open terminal: " + sourcePc);
            return; // Should not happen if called from TerminalController
        }

        switch (command.toLowerCase()) {
            case "ipconfig":
                handleIpConfig(sourcePc, sourceTerminal);
                break;
            case "ping":
                handlePing(sourcePc, args, sourceTerminal);
                break;
            // Add more command cases here
            default:
                sourceTerminal.displayOutput("Error: Unknown command '" + command + "'");
                break;
        }
    }

    private void handleIpConfig(PC pc, TerminalController terminal) {
        StringBuilder output = new StringBuilder();
        output.append("\nEthernet adapter Local Area Connection:\n\n");
        output.append("   IPv4 Address. . . . . . . . . . . : ").append(pc.getIpAddress()).append("\n");
        output.append("   Subnet Mask . . . . . . . . . . . : ").append(pc.getSubnetMask()).append("\n");
        // output.append("   Default Gateway . . . . . . . . . : ").append(pc.getGateway() != null ? pc.getGateway() : "").append("\n"); // Add gateway later if needed
        output.append("\n");
        terminal.displayOutput(output.toString());
    }

    private void handlePing(PC sourcePc, String[] args, TerminalController terminal) {
        if (args.length < 1 || args[0].isEmpty()) {
            terminal.displayOutput("Usage: ping <target_identifier>");
            return;
        }
        String targetIdentifier = args[0];

        // Find the target device
        Optional<NetworkDevice> targetOpt = findDeviceByIdentifier(targetIdentifier);

        if (!targetOpt.isPresent()) {
            terminal.displayOutput("\nPinging " + targetIdentifier + " [" + targetIdentifier + "] with 32 bytes of data:");
            terminal.displayOutput("Request timed out.");
            terminal.displayOutput("Request timed out.");
            terminal.displayOutput("Request timed out.");
            terminal.displayOutput("Request timed out.");
            terminal.displayOutput("\nPing statistics for " + targetIdentifier + ":");
            terminal.displayOutput("    Packets: Sent = 4, Received = 0, Lost = 4 (100% loss),");
            return;
        }

        NetworkDevice targetDevice = targetOpt.get();

        // --- Basic Reachability Check (Direct Connection Only for now) ---
        boolean directlyConnected = connections.stream().anyMatch(conn ->
                (conn.getDevice1() == sourcePc && conn.getDevice2() == targetDevice) ||
                        (conn.getDevice1() == targetDevice && conn.getDevice2() == sourcePc)
        );
        // TODO: Enhance reachability to check via switches/routers later

        // --- Simulate Ping Results ---
        String targetIp = (targetDevice instanceof PC) ? ((PC) targetDevice).getIpAddress() : targetDevice.toString(); // Use IP if PC, else ID
        terminal.displayOutput("\nPinging " + targetDevice.toString() + " [" + targetIp + "] with 32 bytes of data:");

        if (directlyConnected) {
            // Simulate successful pings (instantaneous for now)
            for (int i = 0; i < 4; i++) {
                int time = (int) (Math.random() * 10) + 1; // Simulate 1-10 ms delay
                terminal.displayOutput("Reply from " + targetIp + ": bytes=32 time=" + time + "ms TTL=128");
                // Optional: Add a small delay visually if desired, but complicates things
                // try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            terminal.displayOutput("\nPing statistics for " + targetIp + ":");
            terminal.displayOutput("    Packets: Sent = 4, Received = 4, Lost = 0 (0% loss),");
            // Approximate round trip times (can be more elaborate)
            terminal.displayOutput("Approximate round trip times in milli-seconds:");
            terminal.displayOutput("    Minimum = 1ms, Maximum = 10ms, Average = " + (int)(Math.random()*5 + 3) +"ms"); // Fake average

        } else {
            // Simulate failure
            for (int i = 0; i < 4; i++) {
                // Could vary message: "Request timed out." or "Destination host unreachable."
                terminal.displayOutput("Request timed out.");
                // Optional delay
                // try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            terminal.displayOutput("\nPing statistics for " + targetIp + ":");
            terminal.displayOutput("    Packets: Sent = 4, Received = 0, Lost = 4 (100% loss),");
        }
    }


    // --- Communication Simulation (Chat - Update to use receiveChatMessage) ---
    public void sendMessage(PC senderPc, String message) {
        System.out.println("Attempting to send CHAT from " + senderPc + ": " + message);

        boolean messageDelivered = false;
        for (Connection connection : connections) {
            NetworkDevice recipientDevice = null;
            if (connection.getDevice1() == senderPc) {
                recipientDevice = connection.getDevice2();
            } else if (connection.getDevice2() == senderPc) {
                recipientDevice = connection.getDevice1();
            }

            if (recipientDevice instanceof PC) {
                PC recipientPc = (PC) recipientDevice;
                if (openTerminals.containsKey(recipientPc)) {
                    TerminalController recipientTerminal = openTerminals.get(recipientPc);
                    String formattedMessage = "[" + senderPc.toString() + "]: " + message; // Use toString() for consistent ID
                    // *** CHANGE THIS CALL ***
                    recipientTerminal.receiveChatMessage(formattedMessage); // Use the new method for chat
                    System.out.println("Chat message delivered to " + recipientPc);
                    messageDelivered = true; // Mark that at least one recipient got it
                } else {
                    System.out.println("Recipient " + recipientPc + " has no open terminal for chat.");
                }
            } else if (recipientDevice != null) {
                System.out.println("Chat message reached non-PC device: " + recipientDevice);
            }
        }

        // Optional: Inform sender if message couldn't be delivered anywhere
        if (!messageDelivered && openTerminals.containsKey(senderPc)) {
            // Check if it was *intended* for a specific PC that wasn't connected/open
            // For now, just a generic message if no PC terminal received it.
            // openTerminals.get(senderPc).displayOutput("(Message could not be delivered to any open PC terminals)");
        }
    }

    // --- Communication Simulation ---


    // --- Utility for removing devices (More complex - requires handling connections) ---
    // TODO: Implement device removal (right-click context menu?)
    // - Remove device from devices list
    // - Remove node from pane
    // - Remove mappings (nodeToDeviceMap, deviceToNodeMap)
    // - Find and remove all connections involving the device (unbind lines!)
    // - Close terminal if open
}