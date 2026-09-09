package com.socialmediamanager.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PublishingScheduler {

    private final PostService postService;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public PublishingScheduler(PostService postService) {
        this.postService = postService;
    }

    public void start() {
        executor.scheduleWithFixedDelay(this::publishDuePosts, 0, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        executor.shutdownNow();
    }

    private void publishDuePosts() {
        try {
            postService.publishDuePosts();
        } catch (Exception ignored) {
            // A failed attempt is recorded by PostService when publishing starts.
        }
    }
}