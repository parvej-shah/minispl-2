package com.socialmediamanager.model;

public class PublishingHistoryEntry {

    private Integer id;
    private int postId;
    private PublishingResult result;
    private String message;
    private String occurredAt;

    public PublishingHistoryEntry() {
    }

    public PublishingHistoryEntry(Integer id, int postId, PublishingResult result, String message, String occurredAt) {
        this.id = id;
        this.postId = postId;
        this.result = result;
        this.message = message;
        this.occurredAt = occurredAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public PublishingResult getResult() {
        return result;
    }

    public void setResult(PublishingResult result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(String occurredAt) {
        this.occurredAt = occurredAt;
    }

    @Override
    public String toString() {
        return "Post #" + postId + " - " + result + " (" + occurredAt + ")"
                + (message == null || message.isBlank() ? "" : ": " + message);
    }
}
