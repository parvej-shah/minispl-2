package com.socialmediamanager.service;

import com.socialmediamanager.dao.ContentDao;
import com.socialmediamanager.model.Content;

import java.util.List;

public class ContentService {

    private final ContentDao contentDao;

    public ContentService() {
        this.contentDao = new ContentDao();
    }

    public ContentService(ContentDao contentDao) {
        this.contentDao = contentDao;
    }

    public Content createContent(Content content) throws Exception {
        validate(content);
        return contentDao.create(content);
    }

    public void updateContent(Content content) throws Exception {
        validate(content);
        if (content.getId() == null) {
            throw new IllegalArgumentException("Content id is required for update");
        }
        contentDao.update(content);
    }

    public void deleteContent(int id) throws Exception {
        contentDao.delete(id);
    }

    public List<Content> listContent() throws Exception {
        return contentDao.findAll();
    }

    private void validate(Content content) {
        if (content.getTitle() == null || content.getTitle().isBlank()) {
            throw new IllegalArgumentException("Content title is required");
        }
        if (content.getContentType() == null) {
            throw new IllegalArgumentException("Content type is required");
        }
    }
}
