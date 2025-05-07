package org.example.network_simulator;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.network_simulator.db.DbOps;
import org.example.network_simulator.models.User;

import java.util.List;

public class UsersView {

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    public void initialize() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        loadUsers();  // Load users
    }

    private void loadUsers() {
        List<User> users = DbOps.getAllUsers();
        userTable.getItems().setAll(users);
    }


}
