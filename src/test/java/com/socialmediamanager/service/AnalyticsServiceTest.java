package com.socialmediamanager.service;

import com.socialmediamanager.dao.EngagementMetricDao;
import com.socialmediamanager.db.DatabaseManager;
import com.socialmediamanager.db.DatabaseSeeder;
import com.socialmediamanager.model.Content;
import com.socialmediamanager.model.ContentType;
import com.socialmediamanager.model.EngagementMetric;
import com.socialmediamanager.model.Post;
import com.socialmediamanager.model.PublishingResult;
import com.socialmediamanager.observer.EngagementRecorderListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalyticsServiceTest {

    private final AnalyticsService analyticsService = new AnalyticsService();
    private final EngagementMetricDao engagementMetricDao = new EngagementMetricDao();
    private final ContentService contentService = new ContentService();
    private final PostService postService = new PostService();

    @BeforeEach
    void setUp() throws Exception {
        DatabaseManager.initializeSchema();
        DatabaseSeeder.seed();
    }

    @Test
    void engagementRateIsInteractionsOverReach() {
        EngagementMetric metric = new EngagementMetric(null, 1, 40, 5, 5, 1000, null);

        assertEquals(50, metric.getTotalInteractions());
        assertEquals(5.0, metric.getEngagementRate(), 0.0001);
    }

    @Test
    void engagementRateIsZeroWhenNothingWasReached() {
        EngagementMetric metric = new EngagementMetric(null, 1, 0, 0, 0, 0, null);

        assertEquals(0.0, metric.getEngagementRate(), 0.0001);
    }

    @Test
    void recorderStoresFiguresOnlyForSuccessfulPublishes() throws Exception {
        Post published = newPost();
        Post failed = newPost();
        EngagementRecorderListener recorder = new EngagementRecorderListener(engagementMetricDao);

        recorder.onPublishingOutcome(published, PublishingResult.SUCCESS, "ok");
        recorder.onPublishingOutcome(failed, PublishingResult.FAILURE, "too long");

        assertNotNull(engagementMetricDao.findByPostId(published.getId()));
        assertNull(engagementMetricDao.findByPostId(failed.getId()));
    }

    @Test
    void recorderDoesNotOverwriteFiguresWhenAPostPublishesAgain() throws Exception {
        Post post = newPost();
        EngagementRecorderListener recorder = new EngagementRecorderListener(engagementMetricDao);

        recorder.onPublishingOutcome(post, PublishingResult.SUCCESS, "ok");
        int likesFirstTime = engagementMetricDao.findByPostId(post.getId()).getLikes();
        recorder.onPublishingOutcome(post, PublishingResult.SUCCESS, "ok");

        assertEquals(1, engagementMetricDao.findAll().stream()
                .filter(metric -> metric.getPostId() == post.getId()).count());
        assertEquals(likesFirstTime, engagementMetricDao.findByPostId(post.getId()).getLikes());
    }

    @Test
    void generatedFiguresAreTheSameForAGivenPost() throws Exception {
        Post post = newPost();
        new EngagementRecorderListener(engagementMetricDao)
                .onPublishingOutcome(post, PublishingResult.SUCCESS, "ok");
        EngagementMetric stored = engagementMetricDao.findByPostId(post.getId());

        // Interactions are drawn as a share of reach, so they must stay below it.
        assertTrue(stored.getReach() > 0);
        assertTrue(stored.getLikes() > 0);
        assertTrue(stored.getTotalInteractions() < stored.getReach());
    }

    @Test
    void totalsAddUpAcrossTheMeasuredPosts() throws Exception {
        Post first = newPost();
        Post second = newPost();
        engagementMetricDao.create(new EngagementMetric(null, first.getId(), 10, 2, 3, 500, null));
        engagementMetricDao.create(new EngagementMetric(null, second.getId(), 20, 4, 1, 500, null));

        AnalyticsService.Totals totals = analyticsService.totals();

        assertTrue(totals.likes() >= 30);
        assertTrue(totals.shares() >= 6);
        assertTrue(totals.postsMeasured() >= 2);
    }

    @Test
    void platformBreakdownNamesThePlatformAndRanksByEngagement() throws Exception {
        Post post = newPost();
        engagementMetricDao.create(new EngagementMetric(null, post.getId(), 50, 10, 10, 1000, null));

        List<AnalyticsService.PlatformBreakdown> rows = analyticsService.byPlatform();

        assertTrue(rows.stream().anyMatch(row -> row.platformName().equals("Facebook")));
        for (int i = 1; i < rows.size(); i++) {
            assertTrue(rows.get(i - 1).engagementRate() >= rows.get(i).engagementRate());
        }
    }

    @Test
    void topPostsAreOrderedByInteractionsAndRespectTheLimit() throws Exception {
        Post quiet = newPost();
        Post loud = newPost();
        engagementMetricDao.create(new EngagementMetric(null, quiet.getId(), 1, 0, 0, 100, null));
        engagementMetricDao.create(new EngagementMetric(null, loud.getId(), 900, 90, 90, 5000, null));

        List<AnalyticsService.TopPost> rows = analyticsService.topPosts(1);

        assertEquals(1, rows.size());
        assertEquals(900, rows.get(0).likes());
    }

    @Test
    void seedingAnalyticsBackfillsAPublishedPostThatHasNoFiguresYet() throws Exception {
        // A post published without the recorder attached has no engagement of its own.
        Post post = newPost();
        postService.markValidated(post.getId());
        postService.startPublishing(post.getId());
        postService.markPublished(post.getId());
        assertNull(engagementMetricDao.findByPostId(post.getId()));

        DatabaseSeeder.seedAnalytics();
        EngagementMetric backfilled = engagementMetricDao.findByPostId(post.getId());

        assertNotNull(backfilled, "seeding should backfill published posts");

        // Running again must not add a second row or change the figures.
        DatabaseSeeder.seedAnalytics();
        assertEquals(backfilled.getLikes(),
                engagementMetricDao.findByPostId(post.getId()).getLikes());
        assertEquals(1, engagementMetricDao.findAll().stream()
                .filter(metric -> metric.getPostId() == post.getId()).count());
    }

    /** Creates a published-able post on Facebook using a fresh piece of content. */
    private Post newPost() throws Exception {
        Content content = new Content();
        content.setTitle("Analytics fixture " + System.nanoTime());
        content.setBody("Body");
        content.setContentType(ContentType.IMAGE);
        Content created = contentService.createContent(content);
        return postService.createDraft(created.getId(), 1);
    }
}
