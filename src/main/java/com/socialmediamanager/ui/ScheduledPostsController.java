package com.socialmediamanager.ui;

import com.socialmediamanager.dao.ContentDao;
import com.socialmediamanager.dao.PlatformDao;
import com.socialmediamanager.model.Post;
import com.socialmediamanager.model.PostStatus;
import com.socialmediamanager.service.PostService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.HashMap;
import java.util.Map;

public class ScheduledPostsController {

    @FXML
    private ListView<Post> scheduledListView;
    @FXML
    private Label messageLabel;
    @FXML
    private Button cancelButton;
    @FXML
    private NavBarController navBarController;

    private final PostService postService = new PostService();
    private final ContentDao contentDao = new ContentDao();
    private final PlatformDao platformDao = new PlatformDao();
    private final Map<Integer, String> contentTitles = new HashMap<>();
    private final Map<Integer, String> platformNames = new HashMap<>();

    @FXML
    private void initialize() {
        navBarController.setActiveScreen("scheduled");
        loadLookups();

        scheduledListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Post post, boolean empty) {
                super.updateItem(post, empty);
                setText(empty || post == null ? null : describe(post));
            }
        });

        scheduledListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldValue, newValue) -> cancelButton.setDisable(newValue == null));
        cancelButton.setDisable(true);

        refresh();
    }

    private void loadLookups() {
        try {
            contentDao.findAll().forEach(content -> contentTitles.put(content.getId(), content.getTitle()));
            platformDao.findAll().forEach(platform -> platformNames.put(platform.getId(), platform.getName()));
        } catch (Exception e) {
            setMessage("Error loading details: " + e.getMessage(), "message-error");
        }
    }

    private String describe(Post post) {
        String contentTitle = contentTitles.getOrDefault(post.getContentId(), "Unknown content");
        String platformName = platformNames.getOrDefault(post.getPlatformId(), "Unknown platform");
        return contentTitle + "  ·  " + platformName + "  ·  publishes " + post.getScheduledAt();
    }

    @FXML
    private void handleCancel() {
        Post post = scheduledListView.getSelectionModel().getSelectedItem();
        if (post == null) {
            setMessage("Select a scheduled post first.", "message-error");
            return;
        }
        try {
            postService.cancel(post.getId());
            setMessage("Post cancelled.", "message-success");
            refresh();
        } catch (Exception e) {
            setMessage(e.getMessage(), "message-error");
        }
    }

    @FXML
    private void handleRefresh() {
        refresh();
        setMessage("List refreshed.", "message-info");
    }

    private void refresh() {
        try {
            scheduledListView.setItems(FXCollections.observableArrayList(
                    postService.listByStatus(PostStatus.SCHEDULED)));
        } catch (Exception e) {
            setMessage("Error loading scheduled posts: " + e.getMessage(), "message-error");
        }
    }

    private void setMessage(String text, String styleClass) {
        messageLabel.getStyleClass().removeAll("message-success", "message-error", "message-info");
        messageLabel.getStyleClass().add(styleClass);
        messageLabel.setText(text);
    }
}
