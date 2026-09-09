package com.socialmediamanager;

import com.socialmediamanager.db.DatabaseManager;
import com.socialmediamanager.db.DatabaseSeeder;
import com.socialmediamanager.observer.EngagementRecorderListener;
import com.socialmediamanager.service.PublishingScheduler;
import com.socialmediamanager.service.PostService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private PublishingScheduler publishingScheduler;

    @Override
    public void init() throws Exception {
        DatabaseManager.initializeSchema();
        DatabaseSeeder.seed();
        DatabaseSeeder.seedAnalytics();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        PostService schedulerPostService = new PostService();
        schedulerPostService.addListener(new EngagementRecorderListener());
        publishingScheduler = new PublishingScheduler(schedulerPostService);
        publishingScheduler.start();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/dashboard.fxml"));
        Scene scene = new Scene(root, 1180, 700);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        primaryStage.setTitle("Social Media Manager");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1040);
        primaryStage.setMinHeight(560);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (publishingScheduler != null) {
            publishingScheduler.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
