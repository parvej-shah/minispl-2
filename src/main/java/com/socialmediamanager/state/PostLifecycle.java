package com.socialmediamanager.state;

import com.socialmediamanager.model.PostStatus;

import java.util.Map;
import java.util.Set;

public class PostLifecycle {

    private static final Map<PostStatus, Set<PostStatus>> ALLOWED_TRANSITIONS = Map.of(
            PostStatus.DRAFT, Set.of(PostStatus.VALIDATED, PostStatus.DRAFT),
            PostStatus.VALIDATED, Set.of(PostStatus.SCHEDULED, PostStatus.PUBLISHING, PostStatus.DRAFT),
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

    public String nextStepHint(PostStatus status) {
        return switch (status) {
            case DRAFT -> "Next: click Validate to check this post against the platform's rules.";
            case VALIDATED -> "Next: Publish Now to send it immediately, or Schedule it for later.";
            case SCHEDULED -> "Waiting for its scheduled time. It will publish automatically, or you can Cancel it.";
            case PUBLISHING -> "Publishing right now.";
            case PUBLISHED -> "Done. This post has been published.";
            case FAILED -> "Publishing failed. Click Retry to move it back to draft and try again.";
            case CANCELLED -> "This post was cancelled and will not be published.";
        };
    }
}
