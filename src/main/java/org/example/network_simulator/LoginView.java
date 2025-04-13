package org.example.network_simulator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;

public class LoginView {

    @FXML
    private Button signInButton;

    @FXML
    void onSignInClick(ActionEvent event) throws IOException {
        HelloApplication.setRoot("hello-view.fxml");
    }

}
