package com.socialmediamanager.service;

import com.socialmediamanager.dao.ContentDao;
import com.socialmediamanager.dao.EngagementMetricDao;
import com.socialmediamanager.dao.PlatformDao;
import com.socialmediamanager.dao.PostDao;
import com.socialmediamanager.model.Content;
import com.socialmediamanager.model.EngagementMetric;
import com.socialmediamanager.model.Platform;
import com.socialmediamanager.model.Post;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns the stored posts and their engagement figures into the summaries shown on the
 * Analytics screen.
 */
public class AnalyticsService {

    private final EngagementMetricDao engagementMetricDao;
    private final PostDao postDao;
    private final ContentDao contentDao;
    private final PlatformDao platformDao;

    public AnalyticsService() {
        this(new EngagementMetricDao(), new PostDao(), new ContentDao(), new PlatformDao());
    }

    public AnalyticsService(EngagementMetricDao engagementMetricDao, PostDao postDao,
                            ContentDao contentDao, PlatformDao platformDao) {
        this.engagementMetricDao = engagementMetricDao;
        this.postDao = postDao;
        this.contentDao = contentDao;
        this.platformDao = platformDao;
    }

    /** Totals across every post that has engagement recorded. */
    public Totals totals() throws Exception {
        List<EngagementMetric> metrics = engagementMetricDao.findAll();
        int likes = metrics.stream().mapToInt(EngagementMetric::getLikes).sum();
        int shares = metrics.stream().mapToInt(EngagementMetric::getShares).sum();
        int comments = metrics.stream().mapToInt(EngagementMetric::getComments).sum();
        int reach = metrics.stream().mapToInt(EngagementMetric::getReach).sum();
        double rate = reach == 0 ? 0.0 : ((likes + shares + comments) * 100.0) / reach;
        return new Totals(metrics.size(), likes, shares, comments, reach, rate);
    }

    /** Engagement broken down by platform, best average engagement rate first. */
    public List<PlatformBreakdown> byPlatform() throws Exception {
        Map<Integer, String> platformNames = new HashMap<>();
        for (Platform platform : platformDao.findAll()) {
            platformNames.put(platform.getId(), platform.getName());
        }
        Map<Integer, Integer> platformOfPost = new HashMap<>();
        for (Post post : postDao.findAll()) {
            platformOfPost.put(post.getId(), post.getPlatformId());
        }

        Map<Integer, List<EngagementMetric>> grouped = new HashMap<>();
        for (EngagementMetric metric : engagementMetricDao.findAll()) {
            Integer platformId = platformOfPost.get(metric.getPostId());
            if (platformId != null) {
                grouped.computeIfAbsent(platformId, key -> new ArrayList<>()).add(metric);
            }
        }

        List<PlatformBreakdown> rows = new ArrayList<>();
        for (Map.Entry<Integer, List<EngagementMetric>> entry : grouped.entrySet()) {
            List<EngagementMetric> metrics = entry.getValue();
            int likes = metrics.stream().mapToInt(EngagementMetric::getLikes).sum();
            int shares = metrics.stream().mapToInt(EngagementMetric::getShares).sum();
            int comments = metrics.stream().mapToInt(EngagementMetric::getComments).sum();
            int reach = metrics.stream().mapToInt(EngagementMetric::getReach).sum();
            double rate = reach == 0 ? 0.0 : ((likes + shares + comments) * 100.0) / reach;
            rows.add(new PlatformBreakdown(
                    platformNames.getOrDefault(entry.getKey(), "Unknown platform"),
                    metrics.size(), likes, shares, comments, reach, rate));
        }
        rows.sort(Comparator.comparingDouble(PlatformBreakdown::engagementRate).reversed());
        return rows;
    }

    /** The published posts with the most interactions, best first. */
    public List<TopPost> topPosts(int limit) throws Exception {
        Map<Integer, String> contentTitles = new HashMap<>();
        for (Content content : contentDao.findAll()) {
            contentTitles.put(content.getId(), content.getTitle());
        }
        Map<Integer, String> platformNames = new HashMap<>();
        for (Platform platform : platformDao.findAll()) {
            platformNames.put(platform.getId(), platform.getName());
        }
        Map<Integer, Post> postsById = new HashMap<>();
        for (Post post : postDao.findAll()) {
            postsById.put(post.getId(), post);
        }

        List<TopPost> rows = new ArrayList<>();
        for (EngagementMetric metric : engagementMetricDao.findAll()) {
            Post post = postsById.get(metric.getPostId());
            if (post == null) {
                continue;
            }
            rows.add(new TopPost(
                    contentTitles.getOrDefault(post.getContentId(), "Unknown content"),
                    platformNames.getOrDefault(post.getPlatformId(), "Unknown platform"),
                    metric.getLikes(), metric.getShares(), metric.getComments(),
                    metric.getReach(), metric.getEngagementRate()));
        }
        rows.sort(Comparator.comparingInt(
                (TopPost row) -> row.likes() + row.shares() + row.comments()).reversed());
        return rows.size() > limit ? rows.subList(0, limit) : rows;
    }

    public record Totals(int postsMeasured, int likes, int shares, int comments,
                         int reach, double engagementRate) {
    }

    public record PlatformBreakdown(String platformName, int posts, int likes, int shares,
                                    int comments, int reach, double engagementRate) {
    }

    public record TopPost(String contentTitle, String platformName, int likes, int shares,
                          int comments, int reach, double engagementRate) {
    }
}
