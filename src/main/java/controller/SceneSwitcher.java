package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneSwitcher {

    public static void switchTo(javafx.event.Event event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(SceneSwitcher.class.getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true); // Always maintain maximized state
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void switchToMaximized(javafx.event.Event event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(SceneSwitcher.class.getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setMaximized(true); // Always maintain maximized state
            if (title != null && !title.isEmpty()) {
                stage.setTitle(title);
            }
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}