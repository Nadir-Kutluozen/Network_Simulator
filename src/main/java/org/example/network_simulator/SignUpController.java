package org.example.network_simulator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

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
        ServerApplication.setRoot("main-view.fxml");
    }
}