package com.socialmediamanager;

import com.socialmediamanager.db.DatabaseManager;
import com.socialmediamanager.db.DatabaseSeeder;
import com.socialmediamanager.service.PublishingScheduler;
import com.socialmediamanager.service.PostService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;

public class App extends Application {

    private PublishingScheduler publishingScheduler;

    @Override
    public void init() throws SQLException {
        DatabaseManager.initializeSchema();
        DatabaseSeeder.seed();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        publishingScheduler = new PublishingScheduler(new PostService());
        publishingScheduler.start();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/dashboard.fxml"));
        primaryStage.setTitle("Social Media Manager");
        primaryStage.setScene(new Scene(root, 900, 600));
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
