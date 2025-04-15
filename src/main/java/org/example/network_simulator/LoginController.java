package org.example.network_simulator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;

public class LoginController {

    @FXML
    private Button signInButton;

    @FXML
    private Button signUpButton;

    @FXML
    void onSignInClick(ActionEvent event) throws IOException {
        ServerApplication.setRoot("NetworkView.fxml");
    }

    @FXML
    void onSignUpClick(ActionEvent event) throws IOException {
        ServerApplication.setRoot("sign-up-view.fxml");

    }

}