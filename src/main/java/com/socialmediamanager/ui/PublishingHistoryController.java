package com.socialmediamanager.ui;

import com.socialmediamanager.dao.PlatformDao;
import com.socialmediamanager.model.Platform;
import com.socialmediamanager.model.PublishingResult;
import com.socialmediamanager.service.PostService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class PublishingHistoryController {

    @FXML
    private ListView<String> historyListView;
    @FXML
    private ComboBox<PublishingResult> resultFilterComboBox;
    @FXML
    private ComboBox<Platform> platformFilterComboBox;
    @FXML
    private Label messageLabel;

    private final PostService postService = new PostService();
    private final PlatformDao platformDao = new PlatformDao();

    @FXML
    private void initialize() {
        resultFilterComboBox.setItems(FXCollections.observableArrayList(PublishingResult.values()));
        try {
            platformFilterComboBox.setItems(FXCollections.observableArrayList(platformDao.findAll()));
        } catch (Exception e) {
            messageLabel.setText("Error loading platforms: " + e.getMessage());
        }

        refresh(null, null);
    }

    @FXML
    private void handleApplyFilter() {
        PublishingResult result = resultFilterComboBox.getValue();
        Platform platform = platformFilterComboBox.getValue();
        refresh(result, platform == null ? null : platform.getId());
    }

    @FXML
    private void handleClearFilter() {
        resultFilterComboBox.getSelectionModel().clearSelection();
        platformFilterComboBox.getSelectionModel().clearSelection();
        refresh(null, null);
    }

    private void refresh(PublishingResult result, Integer platformId) {
        try {
            var entries = postService.searchPublishingHistory(result, platformId).stream()
                    .map(Object::toString)
                    .toList();
            historyListView.setItems(FXCollections.observableArrayList(entries));
        } catch (Exception e) {
            messageLabel.setText("Error loading history: " + e.getMessage());
        }
    }
}
