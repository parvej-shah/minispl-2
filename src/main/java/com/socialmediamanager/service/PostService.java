package com.socialmediamanager.service;

import com.socialmediamanager.dao.PostDao;
import com.socialmediamanager.model.Post;
import com.socialmediamanager.model.PostStatus;
import com.socialmediamanager.state.PostLifecycle;

import java.util.List;

public class PostService {

    private final PostDao postDao;
    private final PostLifecycle postLifecycle;

    public PostService() {
        this.postDao = new PostDao();
        this.postLifecycle = new PostLifecycle();
    }

    public PostService(PostDao postDao, PostLifecycle postLifecycle) {
        this.postDao = postDao;
        this.postLifecycle = postLifecycle;
    }

    public Post createDraft(int contentId, int platformId) throws Exception {
        Post post = new Post();
        post.setContentId(contentId);
        post.setPlatformId(platformId);
        post.setStatus(PostStatus.DRAFT);
        return postDao.create(post);
    }

    public void markValidated(int postId) throws Exception {
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
