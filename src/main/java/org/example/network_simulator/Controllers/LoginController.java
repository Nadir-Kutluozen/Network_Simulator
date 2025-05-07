package org.example.network_simulator.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

// Import your real classes here
import org.example.network_simulator.db.DbOps;
import org.example.network_simulator.models.Session;
import org.example.network_simulator.models.User;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    protected void handleLogin(ActionEvent event) throws IOException {
        String name = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        User user = DbOps.authenticateUser(name, password);

        if (user != null) {
            Session.setUser(user);
            switchScene(event, "NetworkView.fxml");
        } else {
            System.out.println("Login failed!");
            // TODO: Show a popup/alert to the user instead of console
        }
    }

    @FXML
    protected void switchToRegister(ActionEvent event) throws IOException {
        switchScene(event, "register-view.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlFile) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}