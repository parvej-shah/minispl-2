package com.socialmediamanager.ui;

import com.socialmediamanager.model.Post;
import com.socialmediamanager.model.PostStatus;
import com.socialmediamanager.service.PostService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML
    private Label summaryLabel;

    @FXML
    private Button manageContentButton;

    @FXML
    private Button managePostsButton;

    private final PostService postService = new PostService();

    @FXML
    private void initialize() {
        refreshSummary();
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

    @FXML
    private void handleOpenPosts() {
        try {
            Stage stage = (Stage) managePostsButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/fxml/post.fxml");
        } catch (Exception e) {
            summaryLabel.setText("Error opening posts screen: " + e.getMessage());
        }
    }

    private void refreshSummary() {
        try {
            List<Post> posts = postService.listAll();
            Map<PostStatus, Long> counts = posts.stream()
                    .collect(Collectors.groupingBy(Post::getStatus, Collectors.counting()));
            summaryLabel.setText(String.format(
                    "Total: %d | Published: %d | Scheduled: %d | Failed: %d",
                    posts.size(),
                    counts.getOrDefault(PostStatus.PUBLISHED, 0L),
                    counts.getOrDefault(PostStatus.SCHEDULED, 0L),
                    counts.getOrDefault(PostStatus.FAILED, 0L)));
        } catch (Exception e) {
            summaryLabel.setText("Error loading summary: " + e.getMessage());
        }
    }
}
