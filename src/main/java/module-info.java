module org.example.network_simulator {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens org.example.network_simulator to javafx.fxml;
    exports org.example.network_simulator;
    exports org.example.network_simulator.Controllers;
    opens org.example.network_simulator.Controllers to javafx.fxml;
    exports org.example.network_simulator.DragAndDrop;
    opens org.example.network_simulator.DragAndDrop to javafx.fxml;
}