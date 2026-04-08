package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import model.User;
import model.Database;


import java.io.IOException;

public class SignUpController {

    @FXML
    private ImageView dlsuLogo;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField idNumberField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button signupButton;

    @FXML
    private Button backToLoginButton;

    @FXML
    private void initialize() {
        try {
            Image logoImage = new Image(getClass().getResourceAsStream("/images/dlsulogo.png"));
            dlsuLogo.setImage(logoImage);
        } catch (Exception e) {
            System.out.println("Failed to load logo: " + e.getMessage());
        }
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        String fullName = fullNameField.getText();
        String idNumber = idNumberField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (fullName.isEmpty() || idNumber.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing Fields", "Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Password Mismatch", "Passwords do not match.");
            return;
        }

        // Load current users
        var users = Database.loadUsers();

        // Check if email or ID already exists
        boolean exists = users.stream().anyMatch(u ->
                u.getEmail().equalsIgnoreCase(email) || u.getIdNumber().equals(idNumber)
        );

        if (exists) {
            showAlert(Alert.AlertType.ERROR, "User Exists", "An account with that email or ID already exists.");
            return;
        }

        // Create and add new user
        User newUser = new User(fullName, idNumber, email, password);
        users.add(newUser);
        Database.saveUsers(users); // This creates or updates users.json

        showAlert(Alert.AlertType.INFORMATION, "Sign Up Successful", "Your account has been created.");

        // Redirect to login screen
        try {
            Parent loginPage = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loginPage));
            stage.setTitle("Login - Animo Found");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load login screen: " + e.getMessage());
        }
    }


    @FXML
    private void handleBackToLogin(ActionEvent event) {
        try {

            Parent loginPage = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loginPage));
            stage.setTitle("Login - Animo Found");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load login screen: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}