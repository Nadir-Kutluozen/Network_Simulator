package org.example.network_simulator.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.network_simulator.ServerApplication;
import org.example.network_simulator.db.DbOps;

import java.io.IOException;

public class SignUpController {

    @FXML
    private Button signInButton;

    @FXML
    private Button signUpButton;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    void onSignInClick(ActionEvent event) throws IOException {
        ServerApplication.setRoot("login-view.fxml");
    }

    @FXML
    void onSignUpClick(ActionEvent event) throws IOException {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        boolean success = DbOps.registerUser(username, email, password); //sign up the user!!

        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful");
            alert.setHeaderText(null);
            alert.setContentText("You can now log in.");
            alert.showAndWait();
            ServerApplication.setRoot("login-view.fxml");
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Registration Failed");
            alert.setHeaderText("That email is already registered.");
            alert.setContentText("Try using a different email.");
            alert.showAndWait();
        }
    }
}
