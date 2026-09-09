package com.socialmediamanager.observer;

import com.socialmediamanager.model.Post;
import com.socialmediamanager.model.PublishingResult;

public interface PostEventListener {

    void onPublishingOutcome(Post post, PublishingResult result, String message);
}
