package com.socialmediamanager.ui;

import com.socialmediamanager.model.Content;
import com.socialmediamanager.model.ContentType;
import com.socialmediamanager.service.ContentService;
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
            if (isNew) {
                contentService.createContent(content);
            } else {
                contentService.updateContent(content);
            }

            refreshList();
            handleNew();
            setMessage(isNew ? "Content saved." : "Content updated.", "message-success");
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
