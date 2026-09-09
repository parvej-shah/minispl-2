package com.socialmediamanager.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class NavBarController {

    private static final String ACTIVE_STYLE =
            "-fx-background-color: #d3e4ff; -fx-border-color: #4c9aff; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-weight: bold;";

    @FXML
    private HBox navBar;
    @FXML
    private Button contentButton;
    @FXML
    private Button postsButton;
    @FXML
    private Button scheduledButton;
    @FXML
    private Button historyButton;

    public void setActiveScreen(String screen) {
        contentButton.setStyle(screen.equals("content") ? ACTIVE_STYLE : "");
        postsButton.setStyle(screen.equals("posts") ? ACTIVE_STYLE : "");
        scheduledButton.setStyle(screen.equals("scheduled") ? ACTIVE_STYLE : "");
        historyButton.setStyle(screen.equals("history") ? ACTIVE_STYLE : "");
    }

    @FXML
    private void handleGoDashboard() {
        navigateTo("/fxml/dashboard.fxml");
    }

    @FXML
    private void handleGoContent() {
        navigateTo("/fxml/content.fxml");
    }

    @FXML
    private void handleGoPosts() {
        navigateTo("/fxml/post.fxml");
    }

    @FXML
    private void handleGoScheduled() {
        navigateTo("/fxml/scheduled_posts.fxml");
    }

    @FXML
    private void handleGoHistory() {
        navigateTo("/fxml/publishing_history.fxml");
    }

    private void navigateTo(String fxmlPath) {
        try {
            Stage stage = (Stage) navBar.getScene().getWindow();
            SceneNavigator.switchTo(stage, fxmlPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to navigate to " + fxmlPath, e);
        }
    }
}
