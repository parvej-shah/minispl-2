package com.socialmediamanager.model;

public class Content {

    private Integer id;
    private String title;
    private String body;
    private ContentType contentType;
    private String createdAt;

    public Content() {
    }

    public Content(Integer id, String title, String body, ContentType contentType, String createdAt) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.contentType = contentType;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return title + " (" + contentType + ")";
    }
}
