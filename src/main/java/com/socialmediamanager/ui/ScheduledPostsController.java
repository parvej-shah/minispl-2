package com.socialmediamanager.ui;

import com.socialmediamanager.model.Post;
import com.socialmediamanager.model.PostStatus;
import com.socialmediamanager.service.PostService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class ScheduledPostsController {

    @FXML
    private ListView<Post> scheduledListView;
    @FXML
    private Label messageLabel;
    @FXML
    private NavBarController navBarController;

    private final PostService postService = new PostService();

    @FXML
    private void initialize() {
        navBarController.setActiveScreen("scheduled");
        refresh();
    }

    @FXML
    private void handleCancel() {
        Post post = scheduledListView.getSelectionModel().getSelectedItem();
        if (post == null) {
            messageLabel.setText("Select a scheduled post first.");
            return;
        }
        try {
            postService.cancel(post.getId());
            messageLabel.setText("Post cancelled.");
            refresh();
        } catch (Exception e) {
            messageLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        refresh();
    }

    private void refresh() {
        try {
            scheduledListView.setItems(FXCollections.observableArrayList(
                    postService.listByStatus(PostStatus.SCHEDULED)));
        } catch (Exception e) {
            messageLabel.setText("Error loading scheduled posts: " + e.getMessage());
        }
    }
}
