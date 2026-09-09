package com.socialmediamanager.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML
    private Label summaryLabel;

    @FXML
    private void initialize() {
        summaryLabel.setText("No posts yet.");
    }
}
