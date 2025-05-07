package org.example.network_simulator;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import org.example.network_simulator.db.DbOps;
import org.example.network_simulator.models.Session;
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
    private ImageView profilePick; // this will be changed according to the people entering.

    @FXML
    public void initialize() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        loadUsers();// Load users
        User currentUser = Session.getUser();

        if (currentUser != null && currentUser.getProfilePic() != null) {
            byte[] picBytes = currentUser.getProfilePic();
            javafx.scene.image.Image image = new javafx.scene.image.Image(new java.io.ByteArrayInputStream(picBytes)); // set the image!
            profilePick.setImage(image);
        } else {
            profilePick.setImage(null);
        }
    }

    private void loadUsers() {
        List<User> users = DbOps.getAllUsers();
        userTable.getItems().setAll(users);
    }


}
