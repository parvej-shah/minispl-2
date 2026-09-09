package com.socialmediamanager.ui;

import com.socialmediamanager.service.PostService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class PublishingHistoryController {

    @FXML
    private ListView<String> historyListView;
    @FXML
    private Label messageLabel;

    private final PostService postService = new PostService();

    @FXML
    private void initialize() {
        try {
            var entries = postService.listPublishingHistory().stream()
                    .map(Object::toString)
                    .toList();
            historyListView.setItems(FXCollections.observableArrayList(entries));
        } catch (Exception e) {
            messageLabel.setText("Error loading history: " + e.getMessage());
        }
    }
}
