package com.socialmediamanager.observer;

import com.socialmediamanager.model.Post;
import com.socialmediamanager.model.PublishingResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActivityLogListener implements PostEventListener {

    private final List<String> entries = new ArrayList<>();

    @Override
    public void onPublishingOutcome(Post post, PublishingResult result, String message) {
        String entry = "Post #" + post.getId() + " " + result
                + (message == null || message.isBlank() ? "" : " - " + message);
        entries.add(entry);
    }

    public List<String> getEntries() {
        return Collections.unmodifiableList(entries);
    }
}
