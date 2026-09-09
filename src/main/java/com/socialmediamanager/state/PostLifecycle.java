package com.socialmediamanager.state;

import com.socialmediamanager.model.PostStatus;

import java.util.Map;
import java.util.Set;

public class PostLifecycle {

    private static final Map<PostStatus, Set<PostStatus>> ALLOWED_TRANSITIONS = Map.of(
            PostStatus.DRAFT, Set.of(PostStatus.VALIDATED, PostStatus.DRAFT),
            PostStatus.VALIDATED, Set.of(PostStatus.SCHEDULED),
            PostStatus.SCHEDULED, Set.of(PostStatus.PUBLISHING, PostStatus.CANCELLED),
            PostStatus.PUBLISHING, Set.of(PostStatus.PUBLISHED, PostStatus.FAILED),
            PostStatus.PUBLISHED, Set.of(),
            PostStatus.FAILED, Set.of(PostStatus.DRAFT),
            PostStatus.CANCELLED, Set.of()
    );

    public boolean canTransition(PostStatus from, PostStatus to) {
        return ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }

    public void assertTransition(PostStatus from, PostStatus to) {
        if (!canTransition(from, to)) {
            throw new IllegalStateException("Cannot move a post from " + from + " to " + to);
        }
    }
}
