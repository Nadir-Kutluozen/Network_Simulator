package org.example.network_simulator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NetworkSimulationApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(NetworkSimulationApplication.class.getResource("NetworkView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Network Simulator");
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}