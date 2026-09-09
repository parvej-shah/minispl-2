package com.socialmediamanager.ui;

import com.socialmediamanager.dao.ContentDao;
import com.socialmediamanager.dao.PlatformDao;
import com.socialmediamanager.model.Content;
import com.socialmediamanager.model.Platform;
import com.socialmediamanager.model.Post;
import com.socialmediamanager.model.PostStatus;
import com.socialmediamanager.observer.ActivityLogListener;
import com.socialmediamanager.service.PostService;
import com.socialmediamanager.state.PostLifecycle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class PostController {

    private final Random random = new Random();

    @FXML
    private ListView<Post> postListView;
    @FXML
    private ComboBox<Content> contentComboBox;
    @FXML
    private FlowPane platformCheckBoxes;
    @FXML
    private VBox createCard;
    @FXML
    private VBox actionCard;
    @FXML
    private Label step1Badge;
    @FXML
    private Label step2Badge;
    @FXML
    private VBox createBox;
    @FXML
    private Button toggleCreateButton;
    @FXML
    private Label createMessageLabel;
    @FXML
    private HBox stageStrip;
    @FXML
    private Button createDraftsButton;
    @FXML
    private Button goToContentButton;
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
    private Button retryButton;
    @FXML
    private Button editContentButton;
    @FXML
    private Label stageDraftLabel;
    @FXML
    private Label stageValidatedLabel;
    @FXML
    private Label stageScheduledLabel;
    @FXML
    private Label stagePublishedLabel;
    @FXML
    private Label nextStepLabel;
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
    private final PostLifecycle postLifecycle = new PostLifecycle();
    private final Map<Integer, String> contentTitles = new HashMap<>();
    private final Map<Integer, String> platformNames = new HashMap<>();

    @FXML
    private void initialize() {
        navBarController.setActiveScreen("posts");
        scheduleBox.setVisible(false);
        scheduleBox.setManaged(false);
        postService.addListener(activityLogListener);
        refreshOptions();

        // Two lines per row (title above, platform + status below) so a long content
        // title never forces the list to scroll sideways.
        postListView.setCellFactory(list -> new ListCell<>() {
            private final Label titleLabel = new Label();
            private final Label detailLabel = new Label();
            private final VBox box = new VBox(2, titleLabel, detailLabel);

            {
                titleLabel.getStyleClass().add("post-row-title");
                detailLabel.getStyleClass().add("post-row-detail");
                setGraphic(box);
            }

            @Override
            protected void updateItem(Post post, boolean empty) {
                super.updateItem(post, empty);
                if (empty || post == null) {
                    setGraphic(null);
                    return;
                }
                titleLabel.setText(contentTitles.getOrDefault(post.getContentId(), "Unknown content"));
                String detail = platformNames.getOrDefault(post.getPlatformId(), "Unknown platform")
                        + "  ·  " + post.getStatus();
                detailLabel.setText(post.getScheduledAt() == null
                        ? detail : detail + "  ·  " + post.getScheduledAt());
                setGraphic(box);
            }
        });

        postListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (oldValue != newValue) {
                setMessage("", "message-info");
            }
            updateSelectionState(newValue);
        });

        refreshList();
        // With no posts to pick, creating one is the only thing to do — start open.
        setCreateBoxExpanded(postListView.getItems().isEmpty());
        updateSelectionState(null);
    }

    @FXML
    private void handleToggleCreate() {
        setCreateBoxExpanded(!createBox.isVisible());
    }

    private void setCreateBoxExpanded(boolean expanded) {
        createBox.setVisible(expanded);
        createBox.setManaged(expanded);
        toggleCreateButton.setText(expanded ? "✕ Close new draft" : "＋ New draft");
    }

    private String describe(Post post) {
        String contentTitle = contentTitles.getOrDefault(post.getContentId(), "Unknown content");
        String platformName = platformNames.getOrDefault(post.getPlatformId(), "Unknown platform");
        String line = contentTitle + "  ·  " + platformName + "  ·  " + post.getStatus();
        return post.getScheduledAt() == null ? line : line + "  ·  " + post.getScheduledAt();
    }

    private void updateSelectionState(Post post) {
        boolean hasSelection = post != null;
        statusLabel.setText(hasSelection
                ? describe(post)
                : "Nothing selected — click a post in the list on the left to work on it.");

        PostStatus status = hasSelection ? post.getStatus() : null;
        validateButton.setDisable(status != PostStatus.DRAFT);
        scheduleButton.setDisable(status != PostStatus.VALIDATED && !scheduleBox.isVisible());
        cancelButton.setDisable(status != PostStatus.SCHEDULED);
        publishButton.setDisable(status != PostStatus.SCHEDULED && status != PostStatus.VALIDATED);
        retryButton.setDisable(status != PostStatus.FAILED);
        // Published and cancelled posts are final, so their text is no longer worth editing.
        editContentButton.setDisable(!hasSelection
                || status == PostStatus.PUBLISHED || status == PostStatus.CANCELLED);
        publishButton.setText(status == PostStatus.SCHEDULED ? "Publish Early" : "Publish Now");

        nextStepLabel.setText(hasSelection
                ? postLifecycle.nextStepHint(status)
                : "Pick a post from the list on the left to work on it.");
        updateStageStrip(status);

        // Highlight whichever step the user needs to act in next.
        actionCard.getStyleClass().remove("step-card-active");
        createCard.getStyleClass().remove("step-card-active");
        (hasSelection ? actionCard : createCard).getStyleClass().add("step-card-active");

        step1Badge.getStyleClass().remove("step-badge-muted");
        step2Badge.getStyleClass().remove("step-badge-muted");
        (hasSelection ? step1Badge : step2Badge).getStyleClass().add("step-badge-muted");
        stageStrip.setVisible(hasSelection);
        stageStrip.setManaged(hasSelection);
    }

    private void updateStageStrip(PostStatus status) {
        int reached = switch (status == null ? PostStatus.DRAFT : status) {
            case DRAFT, FAILED, CANCELLED -> 0;
            case VALIDATED -> 1;
            case SCHEDULED -> 2;
            case PUBLISHING, PUBLISHED -> 3;
        };
        boolean stopped = status == PostStatus.FAILED || status == PostStatus.CANCELLED;

        Label[] steps = {stageDraftLabel, stageValidatedLabel, stageScheduledLabel, stagePublishedLabel};
        for (int i = 0; i < steps.length; i++) {
            steps[i].getStyleClass().removeAll("stage-step-done", "stage-step-current", "stage-step-stopped");
            if (status == null) {
                continue;
            }
            if (i < reached) {
                steps[i].getStyleClass().add("stage-step-done");
            } else if (i == reached) {
                steps[i].getStyleClass().add(stopped ? "stage-step-stopped" : "stage-step-current");
            }
        }

        stageDraftLabel.setText(stopped ? status.name().charAt(0) + status.name().substring(1).toLowerCase() : "Draft");
    }

    @FXML
    private void handleGoToContent() {
        try {
            SceneNavigator.switchTo((Stage) createDraftsButton.getScene().getWindow(),
                    "/fxml/content.fxml");
        } catch (Exception e) {
            setCreateMessage("Could not open the Content page: " + e.getMessage(), "message-error");
        }
    }

    @FXML
    private void handleEditContent() {
        Post post = postListView.getSelectionModel().getSelectedItem();
        if (post == null) {
            return;
        }
        try {
            ContentController controller = SceneNavigator.switchToAndGetController(
                    (Stage) editContentButton.getScene().getWindow(), "/fxml/content.fxml");
            controller.selectContentById(post.getContentId());
        } catch (Exception e) {
            setMessage("Could not open the Content page: " + e.getMessage(), "message-error");
        }
    }

    @FXML
    private void handleRetry() {
        withSelectedPost(post -> {
            postService.reopenAsDraft(post.getId());
            showInfo("Post moved back to draft. Validate it to try again.");
        });
    }

    private void refreshOptions() {
        try {
            var contentItems = contentDao.findAll();
            contentComboBox.setItems(FXCollections.observableArrayList(contentItems));
            contentTitles.clear();
            contentItems.forEach(content -> contentTitles.put(content.getId(), content.getTitle()));

            boolean noContent = contentItems.isEmpty();
            contentComboBox.setPromptText(noContent
                    ? "No content available"
                    : "Choose content to post");
            contentComboBox.setDisable(noContent);
            createDraftsButton.setDisable(noContent);
            goToContentButton.setVisible(noContent);
            goToContentButton.setManaged(noContent);
            if (noContent) {
                setCreateMessage("You have no content yet. Create some on the Content page first.",
                        "message-info");
            }

            var platforms = platformDao.findAll();
            platformNames.clear();
            platforms.forEach(platform -> platformNames.put(platform.getId(), platform.getName()));

            platformCheckBoxes.getChildren().clear();
            for (Platform platform : platforms) {
                CheckBox checkBox = new CheckBox(platform.getName());
                checkBox.setUserData(platform.getId());
                platformCheckBoxes.getChildren().add(checkBox);
            }
        } catch (Exception e) {
            showError("Error loading options: " + e.getMessage());
        }
    }

    private List<Integer> selectedPlatformIds() {
        return platformCheckBoxes.getChildren().stream()
                .map(node -> (CheckBox) node)
                .filter(CheckBox::isSelected)
                .map(checkBox -> (Integer) checkBox.getUserData())
                .toList();
    }

    private void clearPlatformSelection() {
        platformCheckBoxes.getChildren().stream()
                .map(node -> (CheckBox) node)
                .forEach(checkBox -> checkBox.setSelected(false));
    }

    private String namesOf(List<Integer> platformIds) {
        return platformIds.stream()
                .map(id -> platformNames.getOrDefault(id, "Unknown"))
                .collect(Collectors.joining(", "));
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
        applyMessage(messageLabel, text, styleClass);
    }

    private void setCreateMessage(String text, String styleClass) {
        // The create box can be collapsed, which would hide its label — in that case
        // show the message on the always-visible Step 2 panel instead.
        applyMessage(createBox.isVisible() ? createMessageLabel : messageLabel, text, styleClass);
    }

    private void applyMessage(Label label, String text, String styleClass) {
        label.getStyleClass().removeAll("message-success", "message-error", "message-info");
        label.getStyleClass().add(styleClass);
        label.setText(text);
    }

    @FXML
    private void handleCreateDraft() {
        Content content = contentComboBox.getValue();
        List<Integer> platformIds = selectedPlatformIds();
        if (content == null) {
            setCreateMessage("Select content first.", "message-error");
            return;
        }
        if (platformIds.isEmpty()) {
            setCreateMessage("Tick at least one platform.", "message-error");
            return;
        }
        try {
            var result = postService.createDraftsForPlatforms(content.getId(), platformIds);
            refreshList();
            clearPlatformSelection();

            if (result.createdPlatformIds().isEmpty()) {
                setCreateMessage(content.getTitle() + " already has a post on "
                        + namesOf(result.skippedPlatformIds()) + ".", "message-error");
                return;
            }

            // Collapse the creator so the new drafts are visible in the list behind it.
            setCreateBoxExpanded(false);
            selectNewestDraftFor(content.getId(), result.createdPlatformIds().get(0));

            if (result.skippedPlatformIds().isEmpty()) {
                setCreateMessage("Created " + result.createdPlatformIds().size() + " draft(s): "
                        + namesOf(result.createdPlatformIds())
                        + ". First one selected below.", "message-success");
            } else {
                setCreateMessage("Created " + namesOf(result.createdPlatformIds())
                        + ". Skipped " + namesOf(result.skippedPlatformIds())
                        + " — already posted there.", "message-info");
            }
        } catch (Exception e) {
            setCreateMessage(e.getMessage(), "message-error");
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

    private void selectNewestDraftFor(int contentId, int platformId) {
        postListView.getItems().stream()
                .filter(p -> p.getContentId() == contentId && p.getPlatformId() == platformId)
                .findFirst()
                .ifPresent(p -> {
                    postListView.getSelectionModel().select(p);
                    postListView.scrollTo(p);
                });
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
