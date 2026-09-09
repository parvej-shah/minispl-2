package com.socialmediamanager.dao;

import com.socialmediamanager.db.DatabaseManager;
import com.socialmediamanager.model.Content;
import com.socialmediamanager.model.ContentType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContentDao {

    public Content create(Content content) throws Exception {
        String sql = "INSERT INTO content (title, body, content_type) VALUES (?, ?, ?)";
        try (PreparedStatement statement = DatabaseManager.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, content.getTitle());
            statement.setString(2, content.getBody());
            statement.setString(3, content.getContentType().name());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    content.setId(keys.getInt(1));
                }
            }
        }
        return content;
    }

    public Optional<Content> findById(int id) throws Exception {
        String sql = "SELECT * FROM content WHERE id = ?";
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

    public List<Content> findAll() throws Exception {
        List<Content> results = new ArrayList<>();
        String sql = "SELECT * FROM content ORDER BY created_at DESC";
        try (Statement statement = DatabaseManager.getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                results.add(mapRow(resultSet));
            }
        }
        return results;
    }

    public void update(Content content) throws Exception {
        String sql = "UPDATE content SET title = ?, body = ?, content_type = ? WHERE id = ?";
        try (PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(sql)) {
            statement.setString(1, content.getTitle());
            statement.setString(2, content.getBody());
            statement.setString(3, content.getContentType().name());
            statement.setInt(4, content.getId());
            statement.executeUpdate();
        }
    }

    public void delete(int id) throws Exception {
        String sql = "DELETE FROM content WHERE id = ?";
        try (PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private Content mapRow(ResultSet resultSet) throws Exception {
        return new Content(
                resultSet.getInt("id"),
                resultSet.getString("title"),
                resultSet.getString("body"),
                ContentType.valueOf(resultSet.getString("content_type")),
                resultSet.getString("created_at")
        );
    }
}
