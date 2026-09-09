package com.socialmediamanager.model;

public class EngagementMetric {

    private Integer id;
    private int postId;
    private int likes;
    private int shares;
    private int comments;
    private int reach;
    private String recordedAt;

    public EngagementMetric() {
    }

    public EngagementMetric(Integer id, int postId, int likes, int shares, int comments,
                            int reach, String recordedAt) {
        this.id = id;
        this.postId = postId;
        this.likes = likes;
        this.shares = shares;
        this.comments = comments;
        this.reach = reach;
        this.recordedAt = recordedAt;
    }

    /** Total interactions of every kind. */
    public int getTotalInteractions() {
        return likes + shares + comments;
    }

    /** Share of the people reached who interacted, as a percentage. */
    public double getEngagementRate() {
        return reach == 0 ? 0.0 : (getTotalInteractions() * 100.0) / reach;
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

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getShares() {
        return shares;
    }

    public void setShares(int shares) {
        this.shares = shares;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public int getReach() {
        return reach;
    }

    public void setReach(int reach) {
        this.reach = reach;
    }

    public String getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(String recordedAt) {
        this.recordedAt = recordedAt;
    }
}
