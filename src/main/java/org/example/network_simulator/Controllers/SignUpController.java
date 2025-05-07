package org.example.network_simulator.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.network_simulator.ServerApplication;
import org.example.network_simulator.db.DbOps;

import java.io.IOException;
import java.time.LocalDate;

public class SignUpController {

    @FXML private Button signInButton;
    @FXML private Button signUpButton;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private DatePicker dobField;

    @FXML
    void onSignInClick(ActionEvent event) throws IOException {
        ServerApplication.setRoot("login-view.fxml");
    }

    @FXML
    void onSignUpClick(ActionEvent event) throws IOException {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        LocalDate dob = dobField.getValue();

        if (dob == null) {
            showAlert(Alert.AlertType.ERROR, "Missing DOB", "Please enter your date of birth.");
            return;
        }

        if (dob.isAfter(LocalDate.now().minusYears(13))) {
            showAlert(Alert.AlertType.ERROR, "Invalid DOB", "You must be at least 13 years old to register.");
            return;
        }

        boolean success = DbOps.registerUser(username, email, password);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "You can now log in.");
            ServerApplication.setRoot("login-view.fxml");
        } else {
            showAlert(Alert.AlertType.ERROR, "Registration Failed", "That email is already registered. Try using a different email.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
