package org.example.network_simulator.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.network_simulator.ServerApplication;
import org.example.network_simulator.db.DbOps;
import org.example.network_simulator.models.Session;
import org.example.network_simulator.models.User;

import java.io.IOException;

public class LoginController {

    @FXML
    private Button signInButton;

    @FXML
    private Button signUpButton;

    @FXML
    private TextField userName;

    //todo
    @FXML
    private PasswordField passwordField;
    //todo, add the patter regex here.

    //todo - Khalid , when hover, it should the information about the specific pc object

    @FXML
    void onSignInClick(ActionEvent event) throws IOException {
        String name = userName.getText().trim();
        String password = passwordField.getText().trim();

        User user = DbOps.authenticateUser(name, password);

        if (user != null) {
            Session.setUser(user);
            ServerApplication.setRoot("NetworkView.fxml");
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Failed");
            alert.setHeaderText(null);
            alert.setContentText("Incorrect username or password. Please try again.");
            alert.showAndWait();
        }
    }

    @FXML
    void onSignUpClick(ActionEvent event) throws IOException {
        ServerApplication.setRoot("sign-up-view.fxml");

    }

}