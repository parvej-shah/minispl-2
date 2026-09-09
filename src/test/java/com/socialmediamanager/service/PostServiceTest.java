package com.socialmediamanager.service;

import com.socialmediamanager.dao.ContentDao;
import com.socialmediamanager.dao.PlatformDao;
import com.socialmediamanager.db.DatabaseManager;
import com.socialmediamanager.db.DatabaseSeeder;
import com.socialmediamanager.model.Content;
import com.socialmediamanager.model.ContentType;
import com.socialmediamanager.model.Platform;
import com.socialmediamanager.model.Post;
import com.socialmediamanager.model.PostStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PostServiceTest {

    private final PostService postService = new PostService();
    private int contentId;
    private int platformId;

    @BeforeEach
    void setUp() throws Exception {
        DatabaseManager.initializeSchema();
        DatabaseSeeder.seed();

        Content content = new Content();
        content.setTitle("Launch post");
        content.setBody("We are launching something new today.");
        content.setContentType(ContentType.TEXT);
        contentId = new ContentDao().create(content).getId();

        platformId = new PlatformDao().findAll().get(0).getId();
    }

    @Test
    void newPostStartsAsDraft() throws Exception {
        Post post = postService.createDraft(contentId, platformId);
        assertEquals(PostStatus.DRAFT, post.getStatus());
    }

    @Test
    void fullHappyPathTransitionsToScheduled() throws Exception {
        Post post = postService.createDraft(contentId, platformId);
        postService.markValidated(post.getId());
        postService.schedule(post.getId(), "2026-01-01T10:00");

        Post reloaded = postService.listAll().stream()
                .filter(p -> p.getId().equals(post.getId()))
                .findFirst().orElseThrow();
        assertEquals(PostStatus.SCHEDULED, reloaded.getStatus());
    }

    @Test
    void cannotScheduleADraftDirectly() throws Exception {
        Post post = postService.createDraft(contentId, platformId);
        assertThrows(IllegalStateException.class, () -> postService.schedule(post.getId(), "2026-01-01T10:00"));
    }

    @Test
    void validationRejectsContentThatBreaksPlatformRules() throws Exception {
        int instagramId = new PlatformDao().findAll().stream()
                .filter(p -> p.getName().equals("Instagram"))
                .findFirst().orElseThrow().getId();

        Content textOnly = new Content();
        textOnly.setTitle("No media");
        textOnly.setBody("Just words, no picture.");
        textOnly.setContentType(ContentType.TEXT);
        int textOnlyContentId = new ContentDao().create(textOnly).getId();

        Post post = postService.createDraft(textOnlyContentId, instagramId);

        assertThrows(IllegalArgumentException.class, () -> postService.markValidated(post.getId()));
    }

    @Test
    void cancellingARemovesItFromScheduledList() throws Exception {
        Post post = postService.createDraft(contentId, platformId);
        postService.markValidated(post.getId());
        postService.schedule(post.getId(), "2026-01-01T10:00");

        postService.cancel(post.getId());

        boolean stillScheduled = postService.listByStatus(PostStatus.SCHEDULED).stream()
                .anyMatch(p -> p.getId().equals(post.getId()));
        boolean nowCancelled = postService.listByStatus(PostStatus.CANCELLED).stream()
                .anyMatch(p -> p.getId().equals(post.getId()));

        assertEquals(false, stillScheduled);
        assertEquals(true, nowCancelled);
    }
}
