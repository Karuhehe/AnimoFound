package main;

import javafx.application.Application;
import javafx.stage.Stage;
import util.SceneNavigator;

import java.net.URL;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        SceneNavigator.setStage(primaryStage);

        URL testURL = getClass().getResource("/view/signup.fxml");

        System.out.println("Found? " + (testURL != null));

        SceneNavigator.switchTo("view/signup.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

