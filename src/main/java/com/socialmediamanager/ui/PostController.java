package com.socialmediamanager.ui;

import com.socialmediamanager.dao.ContentDao;
import com.socialmediamanager.dao.PlatformDao;
import com.socialmediamanager.model.Content;
import com.socialmediamanager.model.Platform;
import com.socialmediamanager.model.Post;
import com.socialmediamanager.service.PostService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.time.LocalDateTime;

public class PostController {

    @FXML
    private ListView<Post> postListView;
    @FXML
    private ComboBox<Content> contentComboBox;
    @FXML
    private ComboBox<Platform> platformComboBox;
    @FXML
    private Label statusLabel;
    @FXML
    private Label messageLabel;

    private final PostService postService = new PostService();
    private final ContentDao contentDao = new ContentDao();
    private final PlatformDao platformDao = new PlatformDao();

    @FXML
    private void initialize() {
        try {
            contentComboBox.setItems(FXCollections.observableArrayList(contentDao.findAll()));
            platformComboBox.setItems(FXCollections.observableArrayList(platformDao.findAll()));
        } catch (Exception e) {
            messageLabel.setText("Error loading options: " + e.getMessage());
        }

        postListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                statusLabel.setText("Status: " + newValue.getStatus());
            }
        });

        refreshList();
    }

    @FXML
    private void handleCreateDraft() {
        Content content = contentComboBox.getValue();
        Platform platform = platformComboBox.getValue();
        if (content == null || platform == null) {
            messageLabel.setText("Select content and platform first.");
            return;
        }
        try {
            postService.createDraft(content.getId(), platform.getId());
            messageLabel.setText("Draft created.");
            refreshList();
        } catch (Exception e) {
            messageLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleValidate() {
        withSelectedPost(post -> {
            postService.markValidated(post.getId());
            messageLabel.setText("Post validated.");
        });
    }

    @FXML
    private void handleSchedule() {
        withSelectedPost(post -> {
            postService.schedule(post.getId(), LocalDateTime.now().plusMinutes(5).toString());
            messageLabel.setText("Post scheduled.");
        });
    }

    @FXML
    private void handleCancel() {
        withSelectedPost(post -> {
            postService.cancel(post.getId());
            messageLabel.setText("Post cancelled.");
        });
    }

    private interface PostAction {
        void run(Post post) throws Exception;
    }

    private void withSelectedPost(PostAction action) {
        Post post = postListView.getSelectionModel().getSelectedItem();
        if (post == null) {
            messageLabel.setText("Select a post first.");
            return;
        }
        try {
            action.run(post);
            refreshList();
        } catch (Exception e) {
            messageLabel.setText("Error: " + e.getMessage());
        }
    }

    private void refreshList() {
        try {
            postListView.setItems(FXCollections.observableArrayList(postService.listAll()));
        } catch (Exception e) {
            messageLabel.setText("Error loading posts: " + e.getMessage());
        }
    }
}
