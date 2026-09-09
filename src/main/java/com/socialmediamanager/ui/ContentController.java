package com.socialmediamanager.ui;

import com.socialmediamanager.model.Content;
import com.socialmediamanager.model.ContentType;
import com.socialmediamanager.service.ContentService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

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
    private NavBarController navBarController;

    private final ContentService contentService = new ContentService();
    private Content selectedContent;

    @FXML
    private void initialize() {
        navBarController.setActiveScreen("content");
        contentTypeComboBox.setItems(FXCollections.observableArrayList(ContentType.values()));
        contentListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                loadIntoForm(newValue);
            }
        });
        refreshList();
        handleNew();
    }

    @FXML
    private void handleNew() {
        selectedContent = null;
        titleField.clear();
        bodyField.clear();
        contentTypeComboBox.getSelectionModel().select(ContentType.TEXT);
        statusLabel.setText("");
    }

    @FXML
    private void handleSave() {
        try {
            Content content = selectedContent != null ? selectedContent : new Content();
            content.setTitle(titleField.getText());
            content.setBody(bodyField.getText());
            content.setContentType(contentTypeComboBox.getValue());

            if (content.getId() == null) {
                contentService.createContent(content);
            } else {
                contentService.updateContent(content);
            }

            statusLabel.setText("Saved.");
            refreshList();
            handleNew();
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Content content = contentListView.getSelectionModel().getSelectedItem();
        if (content == null) {
            return;
        }
        try {
            contentService.deleteContent(content.getId());
            refreshList();
            handleNew();
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void loadIntoForm(Content content) {
        selectedContent = content;
        titleField.setText(content.getTitle());
        bodyField.setText(content.getBody());
        contentTypeComboBox.getSelectionModel().select(content.getContentType());
    }

    private void refreshList() {
        try {
            contentListView.setItems(FXCollections.observableArrayList(contentService.listContent()));
        } catch (Exception e) {
            statusLabel.setText("Error loading content: " + e.getMessage());
        }
    }
}
