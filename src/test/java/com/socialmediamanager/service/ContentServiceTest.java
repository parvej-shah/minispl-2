package com.socialmediamanager.service;

import com.socialmediamanager.db.DatabaseManager;
import com.socialmediamanager.model.Content;
import com.socialmediamanager.model.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ContentServiceTest {

    private final ContentService contentService = new ContentService();

    @BeforeEach
    void setUp() throws Exception {
        DatabaseManager.initializeSchema();
    }

    @Test
    void createContentRejectsBlankTitle() {
        Content content = new Content();
        content.setTitle("  ");
        content.setContentType(ContentType.TEXT);

        assertThrows(IllegalArgumentException.class, () -> contentService.createContent(content));
    }

    @Test
    void createContentRejectsMissingType() {
        Content content = new Content();
        content.setTitle("Launch announcement");

        assertThrows(IllegalArgumentException.class, () -> contentService.createContent(content));
    }

    @Test
    void createContentPersistsAndAssignsId() throws Exception {
        Content content = new Content();
        content.setTitle("Launch announcement");
        content.setBody("We are live!");
        content.setContentType(ContentType.TEXT);

        Content created = contentService.createContent(content);

        assertNotNull(created.getId());
        assertEquals("Launch announcement", contentService.listContent().get(0).getTitle());
    }
}
