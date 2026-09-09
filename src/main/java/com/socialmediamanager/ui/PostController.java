package com.socialmediamanager.ui;

import com.socialmediamanager.dao.ContentDao;
import com.socialmediamanager.dao.PlatformDao;
import com.socialmediamanager.model.Content;
import com.socialmediamanager.model.Platform;
import com.socialmediamanager.model.Post;
import com.socialmediamanager.model.PostStatus;
import com.socialmediamanager.observer.ActivityLogListener;
import com.socialmediamanager.service.PostService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
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
    private Button validateButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button publishButton;
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
    private final Map<Integer, String> contentTitles = new HashMap<>();
    private final Map<Integer, String> platformNames = new HashMap<>();

    @FXML
    private void initialize() {
        navBarController.setActiveScreen("posts");
        scheduleBox.setVisible(false);
        scheduleBox.setManaged(false);
        postService.addListener(activityLogListener);
        refreshOptions();

        postListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Post post, boolean empty) {
                super.updateItem(post, empty);
                setText(empty || post == null ? null : describe(post));
            }
        });

        postListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            updateSelectionState(newValue);
        });

        refreshList();
        updateSelectionState(null);
    }

    private String describe(Post post) {
        String contentTitle = contentTitles.getOrDefault(post.getContentId(), "Unknown content");
        String platformName = platformNames.getOrDefault(post.getPlatformId(), "Unknown platform");
        String line = contentTitle + "  ·  " + platformName + "  ·  " + post.getStatus();
        return post.getScheduledAt() == null ? line : line + "  ·  " + post.getScheduledAt();
    }

    private void updateSelectionState(Post post) {
        boolean hasSelection = post != null;
        statusLabel.setText(hasSelection ? "Selected: " + describe(post) : "No post selected.");

        PostStatus status = hasSelection ? post.getStatus() : null;
        validateButton.setDisable(status != PostStatus.DRAFT);
        scheduleButton.setDisable(status != PostStatus.VALIDATED && !scheduleBox.isVisible());
        cancelButton.setDisable(status != PostStatus.SCHEDULED);
        publishButton.setDisable(status != PostStatus.SCHEDULED && status != PostStatus.VALIDATED);
    }

    private void refreshOptions() {
        try {
            var contentItems = contentDao.findAll();
            contentComboBox.setItems(FXCollections.observableArrayList(contentItems));
            contentTitles.clear();
            contentItems.forEach(content -> contentTitles.put(content.getId(), content.getTitle()));
            if (contentItems.isEmpty()) {
                showInfo("No content yet — create some on the Content screen first.");
            }

            var platforms = platformDao.findAll();
            platformComboBox.setItems(FXCollections.observableArrayList(platforms));
            platformNames.clear();
            platforms.forEach(platform -> platformNames.put(platform.getId(), platform.getName()));
        } catch (Exception e) {
            showError("Error loading options: " + e.getMessage());
        }
    }

    private void showSuccess(String text) {
        setMessage(text, "message-success");
    }

    private void showError(String text) {
        setMessage(text, "message-error");
    }

    private void showInfo(String text) {
        setMessage(text, "message-info");
    }

    private void setMessage(String text, String styleClass) {
        messageLabel.getStyleClass().removeAll("message-success", "message-error", "message-info");
        messageLabel.getStyleClass().add(styleClass);
        messageLabel.setText(text);
    }

    @FXML
    private void handleCreateDraft() {
        Content content = contentComboBox.getValue();
        Platform platform = platformComboBox.getValue();
        if (content == null || platform == null) {
            showError("Select content and platform first.");
            return;
        }
        try {
            postService.createDraft(content.getId(), platform.getId());
            showSuccess("Draft created for " + content.getTitle() + " on " + platform.getName() + ".");
            refreshList();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleValidate() {
        withSelectedPost(post -> {
            postService.markValidated(post.getId());
            showSuccess("Post validated and ready to schedule.");
        });
    }

    @FXML
    private void handleSchedule() {
        if (!scheduleBox.isVisible()) {
            scheduleBox.setVisible(true);
            scheduleBox.setManaged(true);
            scheduleButton.setText("Confirm Schedule");
            showInfo("Choose a date and time, then click Confirm Schedule.");
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
            showSuccess("Post scheduled for " + scheduledAt + ".");
        });
    }

    @FXML
    private void handleCancel() {
        withSelectedPost(post -> {
            postService.cancel(post.getId());
            showSuccess("Post cancelled.");
        });
    }

    @FXML
    private void handleSimulatePublish() {
        withSelectedPost(post -> {
            postService.startPublishing(post.getId());
            if (random.nextInt(10) < 8) {
                postService.markPublished(post.getId());
                showSuccess("Post published.");
            } else {
                postService.markFailed(post.getId(), "Simulated network error");
                showError("Publishing failed: simulated network error.");
            }
        });
    }

    private interface PostAction {
        void run(Post post) throws Exception;
    }

    private void withSelectedPost(PostAction action) {
        Post post = postListView.getSelectionModel().getSelectedItem();
        if (post == null) {
            showError("Select a post first.");
            return;
        }
        try {
            action.run(post);
            refreshList();
            reselect(post.getId());
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void reselect(Integer postId) {
        postListView.getItems().stream()
                .filter(p -> p.getId().equals(postId))
                .findFirst()
                .ifPresent(p -> postListView.getSelectionModel().select(p));
    }

    private void refreshList() {
        try {
            postListView.setItems(FXCollections.observableArrayList(postService.listAll()));
        } catch (Exception e) {
            showError("Error loading posts: " + e.getMessage());
        }
    }
}
