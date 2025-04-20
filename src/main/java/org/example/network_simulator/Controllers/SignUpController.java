package org.example.network_simulator.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.example.network_simulator.ServerApplication;

import java.io.IOException;

public class SignUpController {

    @FXML
    private Button signInButton;

    @FXML
    private Button signUpButton;

    @FXML
    void onSignInClick(ActionEvent event) throws IOException {
        ServerApplication.setRoot("login-view.fxml");
    }

    @FXML
    void onSignUpClick(ActionEvent event) throws IOException {
        ServerApplication.setRoot("NetworkView.fxml");
    }
}