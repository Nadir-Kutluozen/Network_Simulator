package org.example.network_simulator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.network_simulator.db.DbOps;

import java.io.IOException;
import java.util.Objects;

public class ServerApplication extends Application {
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ServerApplication.class.getResource("splash-screen-view.fxml"));
        scene = new Scene(fxmlLoader.load(), 950, 600);
        stage.setTitle("Silver Server");
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ServerApplication.class.getResource(fxml));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        //TODO, to launch test this!!
        //test email:nadir
        //test password:12345678
        // you can also register!!
        DbOps dbOps = new DbOps();
        dbOps.initializeDatabase();

        launch();

    }
}