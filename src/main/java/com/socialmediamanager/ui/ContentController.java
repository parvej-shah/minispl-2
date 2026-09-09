package com.socialmediamanager.ui;

import com.socialmediamanager.model.Content;
import com.socialmediamanager.model.ContentType;
import com.socialmediamanager.model.PostStatus;
import com.socialmediamanager.service.ContentService;
import com.socialmediamanager.service.PostService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.Optional;

public class ContentController {

    @FXML
    private ListView<Content> contentListView;
    @FXML
    private TextField titleField;
    @FXML
    private ComboBox<ContentType> contentTypeComboBox;
    @FXML
    private TextArea bodyField;
    @FXML
    private Label statusLabel;
    @FXML
    private Label formTitleLabel;
    @FXML
    private Label characterCountLabel;
    @FXML
    private Button deleteButton;
    @FXML
    private NavBarController navBarController;

    private final ContentService contentService = new ContentService();
    private final PostService postService = new PostService();
    private Content selectedContent;

    @FXML
    private void initialize() {
        navBarController.setActiveScreen("content");
        contentTypeComboBox.setItems(FXCollections.observableArrayList(ContentType.values()));

        contentListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            deleteButton.setDisable(newValue == null);
            if (newValue != null) {
                loadIntoForm(newValue);
            }
        });

        bodyField.textProperty().addListener((obs, oldValue, newValue) -> updateCharacterCount(newValue));

        refreshList();
        handleNew();
    }

    /** Opens a specific piece of content for editing, used when arriving from a post. */
    public void selectContentById(int contentId) {
        contentListView.getItems().stream()
                .filter(item -> item.getId() != null && item.getId() == contentId)
                .findFirst()
                .ifPresent(item -> {
                    contentListView.getSelectionModel().select(item);
                    contentListView.scrollTo(item);
                });
    }

    @FXML
    private void handleNew() {
        selectedContent = null;
        titleField.clear();
        bodyField.clear();
        contentTypeComboBox.getSelectionModel().select(ContentType.TEXT);
        contentListView.getSelectionModel().clearSelection();
        formTitleLabel.setText("New content");
        deleteButton.setDisable(true);
        setMessage("", "message-info");
    }

    @FXML
    private void handleSave() {
        try {
            Content content = selectedContent != null ? selectedContent : new Content();
            content.setTitle(titleField.getText());
            content.setBody(bodyField.getText());
            content.setContentType(contentTypeComboBox.getValue());

            boolean isNew = content.getId() == null;
            int reset = 0;
            if (isNew) {
                contentService.createContent(content);
            } else {
                contentService.updateContent(content);
                reset = resetValidatedPosts(content.getId());
            }

            refreshList();
            handleNew();
            String message = isNew ? "Content saved." : "Content updated.";
            if (reset > 0) {
                message += " " + reset + (reset == 1 ? " post was" : " posts were")
                        + " sent back to draft — validate again before publishing.";
            }
            setMessage(message, "message-success");
        } catch (Exception e) {
            setMessage(e.getMessage(), "message-error");
        }
    }

    @FXML
    private void handleDelete() {
        Content content = contentListView.getSelectionModel().getSelectedItem();
        if (content == null) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + content.getTitle() + "\"? Any posts created from this content will also be removed.",
                ButtonType.CANCEL, ButtonType.OK);
        confirm.setHeaderText("Delete this content?");
        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isEmpty() || choice.get() != ButtonType.OK) {
            return;
        }

        try {
            contentService.deleteContent(content.getId());
            refreshList();
            handleNew();
            setMessage("Content deleted.", "message-success");
        } catch (Exception e) {
            setMessage(e.getMessage(), "message-error");
        }
    }

    private void loadIntoForm(Content content) {
        selectedContent = content;
        titleField.setText(content.getTitle());
        bodyField.setText(content.getBody());
        contentTypeComboBox.getSelectionModel().select(content.getContentType());
        formTitleLabel.setText("Edit content");
        showEditableDraftHint(content);
    }

    /**
     * A post validated against the old text is no longer trustworthy once the text changes,
     * so send it back to draft rather than let it publish on a stale approval.
     */
    private int resetValidatedPosts(int contentId) throws Exception {
        int reset = 0;
        for (Post post : postService.listAll()) {
            if (post.getContentId() == contentId && post.getStatus() == PostStatus.VALIDATED) {
                postService.reopenAsDraft(post.getId());
                reset++;
            }
        }
        return reset;
    }

    /**
     * Posts read their text from the content row when they publish, so editing content
     * here also changes any draft made from it. Tell the user when that applies.
     */
    private void showEditableDraftHint(Content content) {
        try {
            long editable = postService.listAll().stream()
                    .filter(post -> post.getContentId() == content.getId())
                    .filter(post -> post.getStatus() == PostStatus.DRAFT
                            || post.getStatus() == PostStatus.VALIDATED)
                    .count();
            if (editable > 0) {
                setMessage("Editing this will also update " + editable
                        + (editable == 1 ? " post" : " posts") + " still waiting to publish. "
                        + "Validate them again afterwards.", "message-info");
            } else {
                setMessage("", "message-info");
            }
        } catch (Exception e) {
            setMessage("", "message-info");
        }
    }

    private void updateCharacterCount(String body) {
        int length = body == null ? 0 : body.length();
        characterCountLabel.setText(length + " characters" + (length > 280 ? " — too long for X" : ""));
    }

    private void refreshList() {
        try {
            contentListView.setItems(FXCollections.observableArrayList(contentService.listContent()));
        } catch (Exception e) {
            setMessage("Error loading content: " + e.getMessage(), "message-error");
        }
    }

    private void setMessage(String text, String styleClass) {
        statusLabel.getStyleClass().removeAll("message-success", "message-error", "message-info");
        statusLabel.getStyleClass().add(styleClass);
        statusLabel.setText(text);
    }
}
