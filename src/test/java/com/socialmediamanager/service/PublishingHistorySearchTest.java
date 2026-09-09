package com.socialmediamanager.service;

import com.socialmediamanager.dao.ContentDao;
import com.socialmediamanager.dao.PlatformDao;
import com.socialmediamanager.db.DatabaseManager;
import com.socialmediamanager.db.DatabaseSeeder;
import com.socialmediamanager.model.Content;
import com.socialmediamanager.model.ContentType;
import com.socialmediamanager.model.Platform;
import com.socialmediamanager.model.Post;
import com.socialmediamanager.model.PublishingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PublishingHistorySearchTest {

    private final PostService postService = new PostService();
    private int facebookId;
    private int instagramId;
    private Post facebookPost;
    private Post instagramPost;

    @BeforeEach
    void setUp() throws Exception {
        DatabaseManager.initializeSchema();
        DatabaseSeeder.seed();

        List<Platform> platforms = new PlatformDao().findAll();
        facebookId = platforms.stream().filter(p -> p.getName().equals("Facebook")).findFirst().orElseThrow().getId();
        instagramId = platforms.stream().filter(p -> p.getName().equals("Instagram")).findFirst().orElseThrow().getId();

        Content textContent = new Content();
        textContent.setTitle("Facebook update");
        textContent.setBody("Some announcement text.");
        textContent.setContentType(ContentType.TEXT);
        int textContentId = new ContentDao().create(textContent).getId();

        Content imageContent = new Content();
        imageContent.setTitle("Instagram update");
        imageContent.setBody("Photo caption.");
        imageContent.setContentType(ContentType.IMAGE);
        int imageContentId = new ContentDao().create(imageContent).getId();

        facebookPost = postService.createDraft(textContentId, facebookId);
        postService.markValidated(facebookPost.getId());
        postService.schedule(facebookPost.getId(), "2026-01-01T09:00");
        postService.startPublishing(facebookPost.getId());
        postService.markPublished(facebookPost.getId());

        instagramPost = postService.createDraft(imageContentId, instagramId);
        postService.markValidated(instagramPost.getId());
        postService.schedule(instagramPost.getId(), "2026-01-01T09:00");
        postService.startPublishing(instagramPost.getId());
        postService.markFailed(instagramPost.getId(), "Simulated network error");
    }

    @Test
    void filtersByResultOnly() throws Exception {
        var failures = postService.searchPublishingHistory(PublishingResult.FAILURE, null);
        assertTrue(failures.stream().anyMatch(e -> e.getPostId() == instagramPost.getId()));
        assertTrue(failures.stream().noneMatch(e -> e.getPostId() == facebookPost.getId()));
    }

    @Test
    void filtersByPlatformOnly() throws Exception {
        var facebookEntries = postService.searchPublishingHistory(null, facebookId);
        assertTrue(facebookEntries.stream().anyMatch(e -> e.getPostId() == facebookPost.getId()));
        assertTrue(facebookEntries.stream().noneMatch(e -> e.getPostId() == instagramPost.getId()));
    }

    @Test
    void filtersByResultAndPlatformTogether() throws Exception {
        var results = postService.searchPublishingHistory(PublishingResult.FAILURE, instagramId);
        assertTrue(results.stream().anyMatch(e -> e.getPostId() == instagramPost.getId()));

        var facebookFailures = postService.searchPublishingHistory(PublishingResult.FAILURE, facebookId);
        assertTrue(facebookFailures.stream().noneMatch(e -> e.getPostId() == facebookPost.getId()));
    }
}
