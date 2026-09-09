package com.socialmediamanager.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DatabaseSeeder {

    private static final List<String> DEFAULT_PLATFORMS = List.of("Facebook", "Instagram", "X");

    private DatabaseSeeder() {
    }

    public static void seed() throws SQLException {
        Connection connection = DatabaseManager.getConnection();

        if (!isEmpty(connection, "platform")) {
            return;
        }

        String insertPlatform = "INSERT INTO platform (name) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(insertPlatform)) {
            for (String platformName : DEFAULT_PLATFORMS) {
                statement.setString(1, platformName);
                statement.executeUpdate();
            }
        }
    }

    private static boolean isEmpty(Connection connection, String table) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            var resultSet = statement.executeQuery("SELECT COUNT(*) FROM " + table);
            resultSet.next();
            return resultSet.getInt(1) == 0;
        }
    }
}
