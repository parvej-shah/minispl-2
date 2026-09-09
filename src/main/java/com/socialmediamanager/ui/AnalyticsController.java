package com.socialmediamanager.ui;

import com.socialmediamanager.service.AnalyticsService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class AnalyticsController {

    @FXML
    private Label likesLabel;
    @FXML
    private Label sharesLabel;
    @FXML
    private Label commentsLabel;
    @FXML
    private Label engagementRateLabel;
    @FXML
    private Label coverageLabel;
    @FXML
    private Label messageLabel;

    @FXML
    private TableView<AnalyticsService.PlatformBreakdown> platformTable;
    @FXML
    private TableColumn<AnalyticsService.PlatformBreakdown, String> platformNameColumn;
    @FXML
    private TableColumn<AnalyticsService.PlatformBreakdown, Number> platformPostsColumn;
    @FXML
    private TableColumn<AnalyticsService.PlatformBreakdown, Number> platformLikesColumn;
    @FXML
    private TableColumn<AnalyticsService.PlatformBreakdown, Number> platformSharesColumn;
    @FXML
    private TableColumn<AnalyticsService.PlatformBreakdown, String> platformRateColumn;

    @FXML
    private TableView<AnalyticsService.TopPost> topPostTable;
    @FXML
    private TableColumn<AnalyticsService.TopPost, String> topContentColumn;
    @FXML
    private TableColumn<AnalyticsService.TopPost, String> topPlatformColumn;
    @FXML
    private TableColumn<AnalyticsService.TopPost, Number> topLikesColumn;
    @FXML
    private TableColumn<AnalyticsService.TopPost, String> topRateColumn;

    @FXML
    private NavBarController navBarController;

    private final AnalyticsService analyticsService = new AnalyticsService();

    @FXML
    private void initialize() {
        navBarController.setActiveScreen("analytics");

        platformNameColumn.setCellValueFactory(
                row -> new SimpleStringProperty(row.getValue().platformName()));
        platformPostsColumn.setCellValueFactory(
                row -> new SimpleObjectProperty<>(row.getValue().posts()));
        platformLikesColumn.setCellValueFactory(
                row -> new SimpleObjectProperty<>(row.getValue().likes()));
        platformSharesColumn.setCellValueFactory(
                row -> new SimpleObjectProperty<>(row.getValue().shares()));
        platformRateColumn.setCellValueFactory(
                row -> new SimpleStringProperty(percentage(row.getValue().engagementRate())));

        topContentColumn.setCellValueFactory(
                row -> new SimpleStringProperty(row.getValue().contentTitle()));
        topPlatformColumn.setCellValueFactory(
                row -> new SimpleStringProperty(row.getValue().platformName()));
        topLikesColumn.setCellValueFactory(
                row -> new SimpleObjectProperty<>(row.getValue().likes()));
        topRateColumn.setCellValueFactory(
                row -> new SimpleStringProperty(percentage(row.getValue().engagementRate())));

        refresh();
    }

    private void refresh() {
        try {
            AnalyticsService.Totals totals = analyticsService.totals();
            likesLabel.setText(String.format("%,d", totals.likes()));
            sharesLabel.setText(String.format("%,d", totals.shares()));
            commentsLabel.setText(String.format("%,d", totals.comments()));
            engagementRateLabel.setText(percentage(totals.engagementRate()));

            coverageLabel.setText(totals.postsMeasured() == 0
                    ? "No figures yet. Publish a post and its engagement will be recorded here."
                    : "Measured across " + totals.postsMeasured()
                            + (totals.postsMeasured() == 1 ? " published post" : " published posts")
                            + ", reaching " + String.format("%,d", totals.reach()) + " people.");

            platformTable.setItems(FXCollections.observableArrayList(analyticsService.byPlatform()));
            topPostTable.setItems(FXCollections.observableArrayList(analyticsService.topPosts(10)));
        } catch (Exception e) {
            messageLabel.getStyleClass().add("message-error");
            messageLabel.setText("Could not load analytics: " + e.getMessage());
        }
    }

    private String percentage(double value) {
        return String.format("%.1f%%", value);
    }
}
