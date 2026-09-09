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
        switchToAndGetController(stage, fxmlPath);
    }

    /**
     * Same as {@link #switchTo}, but hands back the new screen's controller so the caller
     * can put it into a specific state (for example, preselecting a row).
     */
    public static <T> T switchToAndGetController(Stage stage, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
        Parent root = loader.load();
        stage.getScene().setRoot(root);
        return loader.getController();
    }
}
