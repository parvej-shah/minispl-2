package com.socialmediamanager.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class NavBarController {

    private static final String ACTIVE_CLASS = "nav-button-active";

    @FXML
    private VBox navBar;
    @FXML
    private Button contentButton;
    @FXML
    private Button postsButton;
    @FXML
    private Button scheduledButton;
    @FXML
    private Button historyButton;

    public void setActiveScreen(String screen) {
        markActive(contentButton, screen.equals("content"));
        markActive(postsButton, screen.equals("posts"));
        markActive(scheduledButton, screen.equals("scheduled"));
        markActive(historyButton, screen.equals("history"));
    }

    private void markActive(Button button, boolean active) {
        button.getStyleClass().remove(ACTIVE_CLASS);
        if (active) {
            button.getStyleClass().add(ACTIVE_CLASS);
        }
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
