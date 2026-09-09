package com.socialmediamanager.db;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseManagerTest {

    @Test
    void schemaCreatesAllExpectedTables() throws Exception {
        DatabaseManager.initializeSchema();

        Set<String> tables = new HashSet<>();
        try (Statement statement = DatabaseManager.getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table'")) {
            while (resultSet.next()) {
                tables.add(resultSet.getString("name"));
            }
        }

        assertTrue(tables.containsAll(Set.of("platform", "content", "post", "publishing_history")));
    }

    @Test
    void seedInsertsDefaultPlatformsExactlyOnce() throws Exception {
        DatabaseManager.initializeSchema();
        DatabaseSeeder.seed();
        DatabaseSeeder.seed();

        try (Statement statement = DatabaseManager.getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS total FROM platform")) {
            resultSet.next();
            assertTrue(resultSet.getInt("total") == 3);
        }
    }
}
