package org.example.network_simulator.Controllers;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.example.network_simulator.ServerApplication;

import java.io.IOException;

public class SplashScreenController {

    @FXML
    private ImageView loginImage;

    @FXML
    void onClick(MouseEvent event) throws IOException {
        ServerApplication.setRoot("login-view.fxml");
    }

}