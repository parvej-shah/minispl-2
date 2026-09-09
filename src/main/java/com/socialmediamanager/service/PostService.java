package com.socialmediamanager.service;

import com.socialmediamanager.dao.ContentDao;
import com.socialmediamanager.dao.PlatformDao;
import com.socialmediamanager.dao.PostDao;
import com.socialmediamanager.model.Content;
import com.socialmediamanager.model.Platform;
import com.socialmediamanager.model.Post;
import com.socialmediamanager.model.PostStatus;
import com.socialmediamanager.state.PostLifecycle;
import com.socialmediamanager.strategy.PlatformRules;
import com.socialmediamanager.strategy.PlatformRulesRegistry;

import java.util.List;

public class PostService {

    private final PostDao postDao;
    private final ContentDao contentDao;
    private final PlatformDao platformDao;
    private final PostLifecycle postLifecycle;
    private final PlatformRulesRegistry platformRulesRegistry;

    public PostService() {
        this.postDao = new PostDao();
        this.contentDao = new ContentDao();
        this.platformDao = new PlatformDao();
        this.postLifecycle = new PostLifecycle();
        this.platformRulesRegistry = new PlatformRulesRegistry();
    }

    public PostService(PostDao postDao, ContentDao contentDao, PlatformDao platformDao,
                        PostLifecycle postLifecycle, PlatformRulesRegistry platformRulesRegistry) {
        this.postDao = postDao;
        this.contentDao = contentDao;
        this.platformDao = platformDao;
        this.postLifecycle = postLifecycle;
        this.platformRulesRegistry = platformRulesRegistry;
    }

    public Post createDraft(int contentId, int platformId) throws Exception {
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

    public void cancel(int postId) throws Exception {
        moveTo(postId, PostStatus.CANCELLED, null);
    }

    public void startPublishing(int postId) throws Exception {
        moveTo(postId, PostStatus.PUBLISHING, null);
    }

    public void markPublished(int postId) throws Exception {
        moveTo(postId, PostStatus.PUBLISHED, null);
    }

    public void markFailed(int postId) throws Exception {
        moveTo(postId, PostStatus.FAILED, null);
    }

    public List<Post> listAll() throws Exception {
        return postDao.findAll();
    }

    public List<Post> listByStatus(PostStatus status) throws Exception {
        return postDao.findByStatus(status);
    }

    private void moveTo(int postId, PostStatus target, String scheduledAt) throws Exception {
        Post post = postDao.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
        postLifecycle.assertTransition(post.getStatus(), target);
        postDao.updateStatus(postId, target, scheduledAt);
    }
}
