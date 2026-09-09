package com.socialmediamanager.observer;

import com.socialmediamanager.dao.EngagementMetricDao;
import com.socialmediamanager.model.EngagementMetric;
import com.socialmediamanager.model.Post;
import com.socialmediamanager.model.PublishingResult;

import java.util.Random;

/**
 * Records engagement figures for a post once it publishes successfully.
 *
 * <p>Publishing in this app is simulated rather than sent to a real network, so the
 * figures are generated rather than measured. They are seeded from the post id, which
 * keeps a given post's numbers stable across runs instead of changing on every launch.
 */
public class EngagementRecorderListener implements PostEventListener {

    private final EngagementMetricDao engagementMetricDao;

    public EngagementRecorderListener() {
        this(new EngagementMetricDao());
    }

    public EngagementRecorderListener(EngagementMetricDao engagementMetricDao) {
        this.engagementMetricDao = engagementMetricDao;
    }

    @Override
    public void onPublishingOutcome(Post post, PublishingResult result, String message) {
        if (result != PublishingResult.SUCCESS || post.getId() == null) {
            return;
        }
        try {
            if (engagementMetricDao.existsForPost(post.getId())) {
                return;
            }
            engagementMetricDao.create(generateFor(post));
        } catch (Exception e) {
            // Engagement figures are a reporting extra: never fail a publish over them.
            System.err.println("Could not record engagement for post #" + post.getId()
                    + ": " + e.getMessage());
        }
    }

    private EngagementMetric generateFor(Post post) {
        Random seeded = new Random(post.getId());

        int reach = 500 + seeded.nextInt(4500);
        // Interactions are drawn as a share of reach so the rate stays believable.
        int likes = (int) (reach * (0.02 + seeded.nextDouble() * 0.06));
        int shares = (int) (likes * (0.05 + seeded.nextDouble() * 0.20));
        int comments = (int) (likes * (0.03 + seeded.nextDouble() * 0.15));

        EngagementMetric metric = new EngagementMetric();
        metric.setPostId(post.getId());
        metric.setReach(reach);
        metric.setLikes(likes);
        metric.setShares(shares);
        metric.setComments(comments);
        return metric;
    }
}
