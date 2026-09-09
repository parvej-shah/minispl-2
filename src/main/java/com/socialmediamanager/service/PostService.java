package com.socialmediamanager.service;

import com.socialmediamanager.dao.ContentDao;
import com.socialmediamanager.dao.PlatformDao;
import com.socialmediamanager.dao.PostDao;
import com.socialmediamanager.dao.PublishingHistoryDao;
import com.socialmediamanager.model.Content;
import com.socialmediamanager.model.Platform;
import com.socialmediamanager.model.Post;
import com.socialmediamanager.model.PostStatus;
import com.socialmediamanager.model.PublishingHistoryEntry;
import com.socialmediamanager.model.PublishingResult;
import com.socialmediamanager.observer.PostEventListener;
import com.socialmediamanager.state.PostLifecycle;
import com.socialmediamanager.strategy.PlatformRules;
import com.socialmediamanager.strategy.PlatformRulesRegistry;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Random;

public class PostService {

    private final PostDao postDao;
    private final ContentDao contentDao;
    private final PlatformDao platformDao;
    private final PublishingHistoryDao publishingHistoryDao;
    private final PostLifecycle postLifecycle;
    private final PlatformRulesRegistry platformRulesRegistry;
    private final List<PostEventListener> listeners = new ArrayList<>();

    public PostService() {
        this.postDao = new PostDao();
        this.contentDao = new ContentDao();
        this.platformDao = new PlatformDao();
        this.publishingHistoryDao = new PublishingHistoryDao();
        this.postLifecycle = new PostLifecycle();
        this.platformRulesRegistry = new PlatformRulesRegistry();
    }

    public PostService(PostDao postDao, ContentDao contentDao, PlatformDao platformDao,
                        PublishingHistoryDao publishingHistoryDao, PostLifecycle postLifecycle,
                        PlatformRulesRegistry platformRulesRegistry) {
        this.postDao = postDao;
        this.contentDao = contentDao;
        this.platformDao = platformDao;
        this.publishingHistoryDao = publishingHistoryDao;
        this.postLifecycle = postLifecycle;
        this.platformRulesRegistry = platformRulesRegistry;
    }

    public void addListener(PostEventListener listener) {
        listeners.add(listener);
    }

    public Post createDraft(int contentId, int platformId) throws Exception {
        if (postDao.existsForContentAndPlatform(contentId, platformId)) {
            throw new IllegalStateException("This content is already posted for the selected platform.");
        }
        Post post = new Post();
        post.setContentId(contentId);
        post.setPlatformId(platformId);
        post.setStatus(PostStatus.DRAFT);
        return postDao.create(post);
    }

    public void markValidated(int postId) throws Exception {
        Post post = postDao.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
        Content content = contentDao.findById(post.getContentId())
                .orElseThrow(() -> new IllegalStateException("Content not found for post " + postId));
        Platform platform = platformDao.findAll().stream()
                .filter(p -> p.getId() == post.getPlatformId())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Platform not found for post " + postId));

        PlatformRules rules = platformRulesRegistry.rulesFor(platform.getName());
        PlatformRules.ValidationResult result = rules.validate(content);
        if (!result.valid()) {
            throw new IllegalArgumentException(result.message());
        }

        moveTo(postId, PostStatus.VALIDATED, null);
    }

    public void schedule(int postId, String scheduledAt) throws Exception {
        moveTo(postId, PostStatus.SCHEDULED, scheduledAt);
    }

    public void publishDuePosts() throws Exception {
        List<Post> duePosts = postDao.findDueScheduled(LocalDateTime.now().toString());
        Random random = new Random();
        for (Post post : duePosts) {
            startPublishing(post.getId());
            if (random.nextInt(10) < 8) {
                markPublished(post.getId());
            } else {
                markFailed(post.getId(), "Simulated network error");
            }
        }
    }

    public void cancel(int postId) throws Exception {
        moveTo(postId, PostStatus.CANCELLED, null);
    }

    public void startPublishing(int postId) throws Exception {
        moveTo(postId, PostStatus.PUBLISHING, null);
    }

    public void markPublished(int postId) throws Exception {
        moveTo(postId, PostStatus.PUBLISHED, null);
        recordOutcome(postId, PublishingResult.SUCCESS, null);
    }

    public void markFailed(int postId, String reason) throws Exception {
        moveTo(postId, PostStatus.FAILED, null);
        recordOutcome(postId, PublishingResult.FAILURE, reason);
    }

    public List<Post> listAll() throws Exception {
        return postDao.findAll();
    }

    public List<Post> listByStatus(PostStatus status) throws Exception {
        return postDao.findByStatus(status);
    }

    public List<PublishingHistoryEntry> listPublishingHistory() throws Exception {
        return publishingHistoryDao.findAll();
    }

    public List<PublishingHistoryEntry> searchPublishingHistory(PublishingResult result, Integer platformId)
            throws Exception {
        return publishingHistoryDao.search(result, platformId);
    }

    private void recordOutcome(int postId, PublishingResult result, String message) throws Exception {
        Post post = postDao.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));

        PublishingHistoryEntry entry = new PublishingHistoryEntry();
        entry.setPostId(postId);
        entry.setResult(result);
        entry.setMessage(message);
        publishingHistoryDao.create(entry);

        for (PostEventListener listener : listeners) {
            listener.onPublishingOutcome(post, result, message);
        }
    }

    private void moveTo(int postId, PostStatus target, String scheduledAt) throws Exception {
        Post post = postDao.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
        postLifecycle.assertTransition(post.getStatus(), target);
        postDao.updateStatus(postId, target, scheduledAt);
    }
}
