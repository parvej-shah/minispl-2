package com.socialmediamanager.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class DashboardController {

    @FXML
    private Label summaryLabel;

    @FXML
    private Button manageContentButton;

    @FXML
    private void initialize() {
        summaryLabel.setText("No posts yet.");
    }

    @FXML
    private void handleOpenContent() {
        try {
            Stage stage = (Stage) manageContentButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/fxml/content.fxml");
        } catch (Exception e) {
            summaryLabel.setText("Error opening content screen: " + e.getMessage());
        }
    }
}
