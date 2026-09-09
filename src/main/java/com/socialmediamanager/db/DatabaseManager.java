package com.socialmediamanager.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DEFAULT_DB_URL =
            "jdbc:sqlite:" + System.getProperty("user.home") + "/.social-media-manager/app.db";
    private static Connection connection;

    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = System.getProperty("db.url", DEFAULT_DB_URL);
            ensureParentDirectoryExists(url);
            connection = DriverManager.getConnection(url);
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }
        }
        return connection;
    }

    private static void ensureParentDirectoryExists(String url) {
        if (url.contains(":memory:")) {
            return;
        }
        Path parent = Path.of(url.substring("jdbc:sqlite:".length())).getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new IllegalStateException("Could not create database folder: " + parent, e);
            }
        }
    }

    public static void initializeSchema() throws SQLException {
        try (Statement statement = getConnection().createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS platform (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS content (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    body TEXT,
                    content_type TEXT NOT NULL,
                    created_at TEXT NOT NULL DEFAULT (datetime('now'))
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS post (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    content_id INTEGER NOT NULL,
                    platform_id INTEGER NOT NULL,
                    status TEXT NOT NULL DEFAULT 'DRAFT',
                    scheduled_at TEXT,
                    created_at TEXT NOT NULL DEFAULT (datetime('now')),
                    FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE,
                    FOREIGN KEY (platform_id) REFERENCES platform(id) ON DELETE RESTRICT
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS publishing_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    post_id INTEGER NOT NULL,
                    result TEXT NOT NULL,
                    message TEXT,
                    occurred_at TEXT NOT NULL DEFAULT (datetime('now')),
                    FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS engagement_metric (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    post_id INTEGER NOT NULL UNIQUE,
                    likes INTEGER NOT NULL DEFAULT 0,
                    shares INTEGER NOT NULL DEFAULT 0,
                    comments INTEGER NOT NULL DEFAULT 0,
                    reach INTEGER NOT NULL DEFAULT 0,
                    recorded_at TEXT NOT NULL DEFAULT (datetime('now')),
                    FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE
                )
                """);
        }
    }
}
