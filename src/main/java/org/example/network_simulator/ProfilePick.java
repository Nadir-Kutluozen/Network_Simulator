package org.example.network_simulator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.example.network_simulator.db.DbOps;
import org.example.network_simulator.models.Session;
import org.example.network_simulator.models.User;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ProfilePick {

    @FXML
    private Button backBtn,continueBtn,editPhotoBtn;
    @FXML
    private ImageView image;


    //todo - initialize() here
    //todo - back btn (initialize) should go back
    //todo - continue btn (continueBtn) should take the pick, save to the database.
    //todo - edit photo (editPhotoBtn) btn should be able to re upload the photo.
    //todo - load the image (ImageView image) from the user database before switching to the new scene.
    @FXML
    private void onEditPhotoClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(editPhotoBtn.getScene().getWindow());

        if (selectedFile != null) {
            try {
                //Load the image into the ImageView
                Image newImage = new Image(selectedFile.toURI().toString());
                image.setImage(newImage);

                //Save the image to DB (convert to byte[])
                byte[] imageBytes = Files.readAllBytes(selectedFile.toPath());

                //Get the logged-in user ID from session
                User currentUser = Session.getUser();  // already store session!

                if (currentUser != null) {
                    boolean updated = DbOps.updateProfilePic(currentUser.getId(), imageBytes);
                    if (updated) {
                        System.out.println("Profile picture updated successfully!");
                    } else {
                        System.out.println("Failed to update profile picture.");
                    }
                } else {
                    System.out.println("No user session found!");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("File selection cancelled.");
        }
    }

    public void initialize() {

        editPhotoBtn.setOnAction(e -> onEditPhotoClicked());
        //load the previous pick using session, which is the current user.
        User currentUser = Session.getUser();
        if (currentUser != null && currentUser.getProfilePic() != null) {
            ByteArrayInputStream bis = new ByteArrayInputStream(currentUser.getProfilePic());
            Image existingImage = new Image(bis);
            image.setImage(existingImage);
        }

    }
    @FXML
    void onContinueClick(ActionEvent event) throws IOException {
        ServerApplication.setRoot("login-view.fxml");  // Go to the login screen
    }


}