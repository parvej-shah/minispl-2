package com.socialmediamanager.ui;

import com.socialmediamanager.model.Post;
import com.socialmediamanager.model.PostStatus;
import com.socialmediamanager.service.ContentService;
import com.socialmediamanager.service.PostService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML
    private Label summaryLabel;

    @FXML
    private Label contentValueLabel;

    @FXML
    private Label totalValueLabel;

    @FXML
    private Label publishedValueLabel;

    @FXML
    private Label scheduledValueLabel;

    @FXML
    private Label failedValueLabel;

    @FXML
    private VBox manageContentButton;

    @FXML
    private VBox managePostsButton;

    @FXML
    private VBox scheduledPostsButton;

    @FXML
    private VBox publishingHistoryButton;

    @FXML
    private VBox analyticsButton;

    private final PostService postService = new PostService();
    private final ContentService contentService = new ContentService();

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

    @FXML
    private void handleOpenScheduledPosts() {
        try {
            Stage stage = (Stage) scheduledPostsButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/fxml/scheduled_posts.fxml");
        } catch (Exception e) {
            summaryLabel.setText("Error opening scheduled posts screen: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenPublishingHistory() {
        try {
            Stage stage = (Stage) publishingHistoryButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/fxml/publishing_history.fxml");
        } catch (Exception e) {
            summaryLabel.setText("Error opening publishing history screen: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenAnalytics() {
        try {
            Stage stage = (Stage) analyticsButton.getScene().getWindow();
            SceneNavigator.switchTo(stage, "/fxml/analytics.fxml");
        } catch (Exception e) {
            summaryLabel.setText("Error opening analytics screen: " + e.getMessage());
        }
    }

    private void refreshSummary() {
        try {
            List<Post> posts = postService.listAll();
            Map<PostStatus, Long> counts = posts.stream()
                    .collect(Collectors.groupingBy(Post::getStatus, Collectors.counting()));

            contentValueLabel.setText(String.valueOf(contentService.listContent().size()));
            totalValueLabel.setText(String.valueOf(posts.size()));
            publishedValueLabel.setText(String.valueOf(counts.getOrDefault(PostStatus.PUBLISHED, 0L)));
            scheduledValueLabel.setText(String.valueOf(counts.getOrDefault(PostStatus.SCHEDULED, 0L)));
            failedValueLabel.setText(String.valueOf(counts.getOrDefault(PostStatus.FAILED, 0L)));
        } catch (Exception e) {
            summaryLabel.setText("Error loading summary: " + e.getMessage());
        }
    }
}
