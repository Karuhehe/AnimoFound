package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import model.Database;
import model.User;
import util.Session;

import java.io.IOException;
import java.util.List;

public class LoginController {

    @FXML
    private ImageView dlsuLogo;

    @FXML
    private TextField idNumberField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button forgotPasswordButton;

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
    private void handleLogin(ActionEvent event) {
        String idNumber = idNumberField.getText().trim();
        String password = passwordField.getText().trim();

        if (idNumber.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing Fields", "Please fill in all fields.");
            return;
        }

        // Check for admin login first
        if ("admin".equals(idNumber) && "admin123".equals(password)) {
            try {
                Parent adminDashboard = FXMLLoader.load(getClass().getResource("/view/AdminDashboard.fxml"));
                Scene scene = new Scene(adminDashboard);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Admin Dashboard - Animo Found");
                stage.setMaximized(true);
                stage.show();
                return;
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Loading Error", "Could not load admin dashboard: " + e.getMessage());
                return;
            }
        }

        // Regular user login
        List<User> users = Database.loadUsers();
        for (User user : users) {
            if (user.getIdNumber().equals(idNumber) && user.getPassword().equals(password)) {
                // Save logged-in user in session
                Session.setCurrentUser(user);

                try {
                    Parent dashboard = FXMLLoader.load(getClass().getResource("/view/dashboard.fxml"));
                    Scene scene = new Scene(dashboard);
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(scene);
                    stage.setTitle("Dashboard - Animo Found");
                    stage.setMaximized(true);
                    stage.show();
                    return;
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Loading Error", "Could not load dashboard: " + e.getMessage());
                    return;
                }
            }
        }

        showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid ID number or password.");
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Forgot Password", "Please contact DLSU IT support to recover your account.");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}