package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SceneNavigator {
    private static Stage primaryStage;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void switchTo(String fxmlPath) {
        try {
            URL fxmlUrl = SceneNavigator.class.getResource("/" + fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("FXML file not found: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("AnimoFound");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
