package com.socialmediamanager.dao;

import com.socialmediamanager.db.DatabaseManager;
import com.socialmediamanager.model.Post;
import com.socialmediamanager.model.PostStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostDao {

    public Post create(Post post) throws Exception {
        String sql = "INSERT INTO post (content_id, platform_id, status, scheduled_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = DatabaseManager.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, post.getContentId());
            statement.setInt(2, post.getPlatformId());
            statement.setString(3, post.getStatus().name());
            statement.setString(4, post.getScheduledAt());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    post.setId(keys.getInt(1));
                }
            }
        }
        return post;
    }

    public Optional<Post> findById(int id) throws Exception {
        String sql = "SELECT * FROM post WHERE id = ?";
        try (PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public boolean existsForContentAndPlatform(int contentId, int platformId) throws Exception {
        String sql = "SELECT 1 FROM post WHERE content_id = ? AND platform_id = ? "
                + "AND status <> 'CANCELLED' LIMIT 1";
        try (PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, contentId);
            statement.setInt(2, platformId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public List<Post> findAll() throws Exception {
        List<Post> results = new ArrayList<>();
        String sql = "SELECT * FROM post ORDER BY created_at DESC";
        try (Statement statement = DatabaseManager.getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                results.add(mapRow(resultSet));
            }
        }
        return results;
    }

    public List<Post> findByStatus(PostStatus status) throws Exception {
        List<Post> results = new ArrayList<>();
        String sql = "SELECT * FROM post WHERE status = ? ORDER BY created_at DESC";
        try (PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, status.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(mapRow(resultSet));
                }
            }
        }
        return results;
    }

    public List<Post> findDueScheduled(String scheduledAt) throws Exception {
        List<Post> results = new ArrayList<>();
        String sql = "SELECT * FROM post WHERE status = ? AND scheduled_at <= ? "
                + "ORDER BY scheduled_at";
        try (PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, PostStatus.SCHEDULED.name());
            statement.setString(2, scheduledAt);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(mapRow(resultSet));
                }
            }
        }
        return results;
    }

    public void updateStatus(int postId, PostStatus status, String scheduledAt) throws Exception {
        String sql = "UPDATE post SET status = ?, scheduled_at = ? WHERE id = ?";
        try (PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setString(2, scheduledAt);
            statement.setInt(3, postId);
            statement.executeUpdate();
        }
    }

    private Post mapRow(ResultSet resultSet) throws Exception {
        return new Post(
                resultSet.getInt("id"),
                resultSet.getInt("content_id"),
                resultSet.getInt("platform_id"),
                PostStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("scheduled_at"),
                resultSet.getString("created_at")
        );
    }
}
