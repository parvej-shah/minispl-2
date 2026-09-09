package com.socialmediamanager;

import com.socialmediamanager.db.DatabaseManager;
import com.socialmediamanager.db.DatabaseSeeder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;

public class App extends Application {

    @Override
    public void init() throws SQLException {
        DatabaseManager.initializeSchema();
        DatabaseSeeder.seed();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/dashboard.fxml"));
        primaryStage.setTitle("Social Media Manager");
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
