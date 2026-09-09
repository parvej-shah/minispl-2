package com.socialmediamanager.dao;

import com.socialmediamanager.db.DatabaseManager;
import com.socialmediamanager.model.Platform;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PlatformDao {

    public List<Platform> findAll() throws Exception {
        List<Platform> results = new ArrayList<>();
        String sql = "SELECT * FROM platform ORDER BY name";
        try (Statement statement = DatabaseManager.getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                results.add(new Platform(resultSet.getInt("id"), resultSet.getString("name")));
            }
        }
        return results;
    }
}
