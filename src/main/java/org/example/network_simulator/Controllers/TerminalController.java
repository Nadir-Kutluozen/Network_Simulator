package org.example.network_simulator.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.network_simulator.DragAndDrop.PC;

public class TerminalController {

    @FXML private TextArea outputArea;
    @FXML private TextField inputField;
    @FXML private Button sendButton;

    private PC pcDevice;
    private NetworkController networkController;

    @FXML
    public void initialize() {
        // Execute command on Enter key
        inputField.setOnAction(event -> handleInput());
        sendButton.setOnAction(event -> handleInput());
    }

    private void handleInput() {
        String input = inputField.getText().trim();
        if (input.isEmpty()) return;

        displayOutput("> " + input); // Echo the command

        // Parse command
        String[] tokens = input.split("\\s+");
        String command = tokens[0];
        String[] args = new String[tokens.length - 1];
        System.arraycopy(tokens, 1, args, 0, args.length);

        // Send to NetworkController for processing
        if (networkController != null && pcDevice != null) {
            networkController.executeCommand(pcDevice, command, args);
        } else {
            displayOutput("Error: Terminal not properly initialized.");
        }

        inputField.clear();
    }

    // Allows NetworkController to pass in this PC reference
    public void setPcDevice(PC pc) {
        this.pcDevice = pc;
    }

    public void setNetworkController(NetworkController controller) {
        this.networkController = controller;
    }

    // Let the controller send output to the terminal
    public void displayOutput(String text) {
        outputArea.appendText(text + "\n");
    }

    // For chat-style messages
    public void receiveChatMessage(String message) {
        outputArea.appendText(message + "\n");
    }

    public TextArea getOutputArea() {
        return outputArea;
    }
}
