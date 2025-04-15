package org.example.network_simulator;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class TerminalController {

    @FXML private TextArea outputArea;
    @FXML private TextField inputField;
    @FXML private Button sendButton;

    private NetworkController networkController; // Reference to main controller
    private PC pcDevice; // Reference to the PC this terminal belongs to

    @FXML
    public void initialize() {
        // Initial prompt or message
        outputArea.appendText("Terminal for [PC Name Placeholder]\nReady.\n");
        // Ensure focus starts on the input field
        Platform.runLater(() -> inputField.requestFocus());
    }

    // Setters to be called by NetworkController after loading FXML
    public void setNetworkController(NetworkController controller) {
        this.networkController = controller;
    }

    public void setPcDevice(PC pc) {
        this.pcDevice = pc;
        // Update the initial text or window title now that we have the PC info
        outputArea.setText("Terminal for " + pc.toString() + "\nReady.\n");
        // Optionally update window title here if you have access to the Stage
        // ((Stage) outputArea.getScene().getWindow()).setTitle("Terminal - " + pc.toString());

    }

    void handleSendAction(ActionEvent event) {
        String inputText = inputField.getText().trim();
        if (inputText.isEmpty() || networkController == null || pcDevice == null) {
            inputField.requestFocus(); // Keep focus even if empty
            return;
        }

        // Display the command/message locally first
        appendMessage("> " + inputText); // Use ">" or "$" as a prompt indicator

        inputField.clear();

        // --- Command Parsing ---
        String[] parts = inputText.split("\\s+", 2); // Split into command and the rest
        String command = parts[0].toLowerCase(); // Case-insensitive command

        boolean commandHandled = false;
        if ("ipconfig".equals(command)) {
            if (parts.length == 1) { // ipconfig takes no arguments
                networkController.executeCommand(pcDevice, command, new String[]{});
                commandHandled = true;
            } else {
                appendMessage("Error: 'ipconfig' does not take arguments.");
                commandHandled = true; // Handled as an error
            }
        } else if ("ping".equals(command)) {
            if (parts.length == 2 && !parts[1].trim().isEmpty()) { // ping needs a target
                String[] args = {parts[1].trim()}; // Target is the argument
                networkController.executeCommand(pcDevice, command, args);
                commandHandled = true;
            } else {
                appendMessage("Error: 'ping' requires a target destination (e.g., ping PC2 or ping 192.168.1.101).");
                commandHandled = true; // Handled as an error
            }
        }
        // Add more 'else if' blocks here for future commands (e.g., traceroute)

        // --- --- --- --- --- ---

        // If it wasn't a recognized command, treat it as a chat message
        if (!commandHandled) {
            networkController.sendMessage(pcDevice, inputText);
        }

        inputField.requestFocus(); // Keep focus on input field
    }

    // Method for NetworkController to call when this terminal receives a message
    public void receiveMessage(String message) {
        // Ensure UI updates happen on the JavaFX Application Thread
        Platform.runLater(() -> displayOutput(message));
    }

    // Helper to append messages to the output area
    public void displayOutput(String message) {
        // Ensure UI updates happen on the JavaFX Application Thread
        Platform.runLater(() -> {
            outputArea.appendText(message + "\n");
            // Auto-scroll to the bottom (optional but good for terminals)
            outputArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    // Method specifically for receiving chat messages from other terminals
    public void receiveChatMessage(String message) {
        // Ensure UI updates happen on the JavaFX Application Thread
        Platform.runLater(() -> {
            outputArea.appendText(message + "\n");
            // Auto-scroll to the bottom
            outputArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    // Kept original appendMessage for compatibility if needed, but prefer displayOutput/receiveChatMessage
    public void appendMessage(String message) {
        displayOutput(message); // Route to displayOutput for consistency now
    }

    // Getter needed by NetworkController to get the stage/window
    public TextArea getOutputArea() {
        return outputArea;
    }
}