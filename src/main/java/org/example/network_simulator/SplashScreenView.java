package org.example.network_simulator;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class SplashScreenView {

    @FXML
    private ImageView loginImage;

    @FXML
    void onClick(MouseEvent event) throws IOException {
        HelloApplication.setRoot("login-view.fxml");
    }

}