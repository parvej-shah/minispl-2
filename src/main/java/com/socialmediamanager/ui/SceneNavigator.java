package com.socialmediamanager.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneNavigator {

    private SceneNavigator() {
    }

    public static void switchTo(Stage stage, String fxmlPath) throws IOException {
        Parent root = FXMLLoader.load(SceneNavigator.class.getResource(fxmlPath));
        stage.getScene().setRoot(root);
    }
}
