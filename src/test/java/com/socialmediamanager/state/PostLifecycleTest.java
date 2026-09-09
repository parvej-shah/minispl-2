package com.socialmediamanager.state;

import com.socialmediamanager.model.PostStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostLifecycleTest {

    private final PostLifecycle lifecycle = new PostLifecycle();

    @Test
    void draftCanMoveToValidated() {
        assertTrue(lifecycle.canTransition(PostStatus.DRAFT, PostStatus.VALIDATED));
    }

    @Test
    void draftCannotSkipStraightToPublished() {
        assertFalse(lifecycle.canTransition(PostStatus.DRAFT, PostStatus.PUBLISHED));
    }

    @Test
    void scheduledCanBeCancelled() {
        assertTrue(lifecycle.canTransition(PostStatus.SCHEDULED, PostStatus.CANCELLED));
    }

    @Test
    void publishedIsTerminal() {
        assertFalse(lifecycle.canTransition(PostStatus.PUBLISHED, PostStatus.DRAFT));
    }

    @Test
    void assertTransitionThrowsOnInvalidMove() {
        assertThrows(IllegalStateException.class,
                () -> lifecycle.assertTransition(PostStatus.DRAFT, PostStatus.SCHEDULED));
    }
}
