package com.socialmediamanager.dao;

import com.socialmediamanager.db.DatabaseManager;
import com.socialmediamanager.model.EngagementMetric;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EngagementMetricDao {

    public EngagementMetric create(EngagementMetric metric) throws Exception {
        String sql = "INSERT INTO engagement_metric (post_id, likes, shares, comments, reach) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = DatabaseManager.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, metric.getPostId());
            statement.setInt(2, metric.getLikes());
            statement.setInt(3, metric.getShares());
            statement.setInt(4, metric.getComments());
            statement.setInt(5, metric.getReach());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    metric.setId(keys.getInt(1));
                }
            }
        }
        return metric;
    }

    public EngagementMetric findByPostId(int postId) throws Exception {
        String sql = "SELECT * FROM engagement_metric WHERE post_id = ?";
        try (PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, postId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    public List<EngagementMetric> findAll() throws Exception {
        List<EngagementMetric> results = new ArrayList<>();
        String sql = "SELECT * FROM engagement_metric ORDER BY recorded_at DESC";
        try (Statement statement = DatabaseManager.getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                results.add(mapRow(resultSet));
            }
        }
        return results;
    }

    public boolean existsForPost(int postId) throws Exception {
        String sql = "SELECT 1 FROM engagement_metric WHERE post_id = ?";
        try (PreparedStatement statement = DatabaseManager.getConnection().prepareStatement(sql)) {
            statement.setInt(1, postId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private EngagementMetric mapRow(ResultSet resultSet) throws Exception {
        return new EngagementMetric(
                resultSet.getInt("id"),
                resultSet.getInt("post_id"),
                resultSet.getInt("likes"),
                resultSet.getInt("shares"),
                resultSet.getInt("comments"),
                resultSet.getInt("reach"),
                resultSet.getString("recorded_at")
        );
    }
}
