package com.socialmediamanager.ui;

import com.socialmediamanager.dao.ContentDao;
import com.socialmediamanager.dao.PlatformDao;
import com.socialmediamanager.model.Content;
import com.socialmediamanager.model.Platform;
import com.socialmediamanager.model.Post;
import com.socialmediamanager.observer.ActivityLogListener;
import com.socialmediamanager.service.PostService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;

public class PostController {

    private final Random random = new Random();

    @FXML
    private ListView<Post> postListView;
    @FXML
    private ComboBox<Content> contentComboBox;
    @FXML
    private ComboBox<Platform> platformComboBox;
    @FXML
    private DatePicker scheduleDatePicker;
    @FXML
    private TextField scheduleTimeField;
    @FXML
    private VBox scheduleBox;
    @FXML
    private Button scheduleButton;
    @FXML
    private Label statusLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private NavBarController navBarController;

    private final PostService postService = new PostService();
    private final ContentDao contentDao = new ContentDao();
    private final PlatformDao platformDao = new PlatformDao();
    private final ActivityLogListener activityLogListener = new ActivityLogListener();

    @FXML
    private void initialize() {
        navBarController.setActiveScreen("posts");
        scheduleBox.setVisible(false);
        scheduleBox.setManaged(false);
        postService.addListener(activityLogListener);
        refreshOptions();

        postListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                statusLabel.setText("Status: " + newValue.getStatus());
            }
        });

        refreshList();
    }

    private void refreshOptions() {
        try {
            var contentItems = contentDao.findAll();
            contentComboBox.setItems(FXCollections.observableArrayList(contentItems));
            if (contentItems.isEmpty()) {
                messageLabel.setText("No content yet — create some on the Content screen first.");
            }

            platformComboBox.setItems(FXCollections.observableArrayList(platformDao.findAll()));
        } catch (Exception e) {
            messageLabel.setText("Error loading options: " + e.getMessage());
        }
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
        if (!scheduleBox.isVisible()) {
            scheduleBox.setVisible(true);
            scheduleBox.setManaged(true);
            scheduleButton.setText("Confirm Schedule");
            messageLabel.setText("Choose a date and time, then click Confirm Schedule.");
            return;
        }
        withSelectedPost(post -> {
            if (scheduleDatePicker.getValue() == null) {
                throw new IllegalArgumentException("Select a schedule date.");
            }
            LocalTime scheduleTime;
            try {
                scheduleTime = LocalTime.parse(scheduleTimeField.getText());
            } catch (Exception e) {
                throw new IllegalArgumentException("Enter time as HH:mm, for example 14:30.");
            }
            LocalDateTime scheduledAt = LocalDateTime.of(scheduleDatePicker.getValue(), scheduleTime);
            if (!scheduledAt.isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("Schedule time must be in the future.");
            }
            postService.schedule(post.getId(), scheduledAt.toString());
            scheduleBox.setVisible(false);
            scheduleBox.setManaged(false);
            scheduleButton.setText("Schedule");
            scheduleDatePicker.setValue(null);
            scheduleTimeField.clear();
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

    @FXML
    private void handleSimulatePublish() {
        withSelectedPost(post -> {
            postService.startPublishing(post.getId());
            if (random.nextInt(10) < 8) {
                postService.markPublished(post.getId());
                messageLabel.setText("Post published.");
            } else {
                postService.markFailed(post.getId(), "Simulated network error");
                messageLabel.setText("Publishing failed.");
            }
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
