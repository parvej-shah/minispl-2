package com.socialmediamanager.model;

public class Post {

    private Integer id;
    private int contentId;
    private int platformId;
    private PostStatus status;
    private String scheduledAt;
    private String createdAt;

    public Post() {
    }

    public Post(Integer id, int contentId, int platformId, PostStatus status, String scheduledAt, String createdAt) {
        this.id = id;
        this.contentId = contentId;
        this.platformId = platformId;
        this.status = status;
        this.scheduledAt = scheduledAt;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getContentId() {
        return contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
    }

    public int getPlatformId() {
        return platformId;
    }

    public void setPlatformId(int platformId) {
        this.platformId = platformId;
    }

    public PostStatus getStatus() {
        return status;
    }

    public void setStatus(PostStatus status) {
        this.status = status;
    }

    public String getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(String scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Post #" + id + " (" + status + ")";
    }
}
