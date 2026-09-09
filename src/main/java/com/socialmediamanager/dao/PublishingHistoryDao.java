package com.socialmediamanager.dao;

import com.socialmediamanager.db.DatabaseManager;
import com.socialmediamanager.model.PublishingHistoryEntry;
import com.socialmediamanager.model.PublishingResult;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PublishingHistoryDao {

    public PublishingHistoryEntry create(PublishingHistoryEntry entry) throws Exception {
        String sql = "INSERT INTO publishing_history (post_id, result, message) VALUES (?, ?, ?)";
        try (PreparedStatement statement = DatabaseManager.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, entry.getPostId());
            statement.setString(2, entry.getResult().name());
            statement.setString(3, entry.getMessage());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    entry.setId(keys.getInt(1));
                }
            }
        }
        return entry;
    }

    public List<PublishingHistoryEntry> findAll() throws Exception {
        List<PublishingHistoryEntry> results = new ArrayList<>();
        String sql = "SELECT * FROM publishing_history ORDER BY occurred_at DESC";
        try (Statement statement = DatabaseManager.getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                results.add(mapRow(resultSet));
            }
        }
        return results;
    }

    public List<PublishingHistoryEntry> findByPostId(int postId) throws Exception {
        List<PublishingHistoryEntry> results = new ArrayList<>();
        String sql = "SELECT * FROM publishing_history WHERE post_id = ? ORDER BY occurred_at DESC";
        try (PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, postId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(mapRow(resultSet));
                }
            }
        }
        return results;
    }

    private PublishingHistoryEntry mapRow(ResultSet resultSet) throws Exception {
        return new PublishingHistoryEntry(
                resultSet.getInt("id"),
                resultSet.getInt("post_id"),
                PublishingResult.valueOf(resultSet.getString("result")),
                resultSet.getString("message"),
                resultSet.getString("occurred_at")
        );
    }
}
