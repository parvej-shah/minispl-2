package com.socialmediamanager.ui;

import com.socialmediamanager.dao.ContentDao;
import com.socialmediamanager.dao.PlatformDao;
import com.socialmediamanager.model.Platform;
import com.socialmediamanager.model.Post;
import com.socialmediamanager.model.PublishingHistoryEntry;
import com.socialmediamanager.model.PublishingResult;
import com.socialmediamanager.service.PostService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublishingHistoryController {

    @FXML
    private ListView<PublishingHistoryEntry> historyListView;
    @FXML
    private ComboBox<PublishingResult> resultFilterComboBox;
    @FXML
    private ComboBox<Platform> platformFilterComboBox;
    @FXML
    private Label messageLabel;
    @FXML
    private Label resultCountLabel;
    @FXML
    private NavBarController navBarController;

    private final PostService postService = new PostService();
    private final PlatformDao platformDao = new PlatformDao();
    private final ContentDao contentDao = new ContentDao();
    private final Map<Integer, String> postDescriptions = new HashMap<>();

    @FXML
    private void initialize() {
        navBarController.setActiveScreen("history");
        resultFilterComboBox.setItems(FXCollections.observableArrayList(PublishingResult.values()));

        try {
            platformFilterComboBox.setItems(FXCollections.observableArrayList(platformDao.findAll()));
            loadPostDescriptions();
        } catch (Exception e) {
            setMessage("Error loading filters: " + e.getMessage(), "message-error");
        }

        historyListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(PublishingHistoryEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty || entry == null) {
                    setText(null);
                    getStyleClass().removeAll("message-success", "message-error");
                    return;
                }
                setText(describe(entry));
                getStyleClass().removeAll("message-success", "message-error");
                getStyleClass().add(entry.getResult() == PublishingResult.SUCCESS
                        ? "message-success" : "message-error");
            }
        });

        refresh(null, null);
    }

    private void loadPostDescriptions() throws Exception {
        Map<Integer, String> contentTitles = new HashMap<>();
        contentDao.findAll().forEach(content -> contentTitles.put(content.getId(), content.getTitle()));
        Map<Integer, String> platformNames = new HashMap<>();
        platformDao.findAll().forEach(platform -> platformNames.put(platform.getId(), platform.getName()));

        postDescriptions.clear();
        for (Post post : postService.listAll()) {
            postDescriptions.put(post.getId(),
                    contentTitles.getOrDefault(post.getContentId(), "Unknown content")
                            + "  ·  " + platformNames.getOrDefault(post.getPlatformId(), "Unknown platform"));
        }
    }

    private String describe(PublishingHistoryEntry entry) {
        String post = postDescriptions.getOrDefault(entry.getPostId(), "Post #" + entry.getPostId());
        String line = post + "  ·  " + entry.getResult() + "  ·  " + entry.getOccurredAt();
        return entry.getMessage() == null || entry.getMessage().isBlank()
                ? line : line + "  —  " + entry.getMessage();
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
        resultFilterComboBox.setValue(null);
        platformFilterComboBox.setValue(null);
        refresh(null, null);
    }

    private void refresh(PublishingResult result, Integer platformId) {
        try {
            List<PublishingHistoryEntry> entries = postService.searchPublishingHistory(result, platformId);
            historyListView.setItems(FXCollections.observableArrayList(entries));
            resultCountLabel.setText(entries.size() + (entries.size() == 1 ? " attempt" : " attempts"));
            setMessage("", "message-info");
        } catch (Exception e) {
            setMessage("Error loading history: " + e.getMessage(), "message-error");
        }
    }

    private void setMessage(String text, String styleClass) {
        messageLabel.getStyleClass().removeAll("message-success", "message-error", "message-info");
        messageLabel.getStyleClass().add(styleClass);
        messageLabel.setText(text);
    }
}
