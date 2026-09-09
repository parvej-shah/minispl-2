package com.socialmediamanager.strategy;

import com.socialmediamanager.model.Content;
import com.socialmediamanager.model.ContentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformRulesTest {

    private final PlatformRulesRegistry registry = new PlatformRulesRegistry();

    @Test
    void xRejectsPostsOverCharacterLimit() {
        Content content = new Content();
        content.setContentType(ContentType.TEXT);
        content.setBody("x".repeat(281));

        PlatformRules.ValidationResult result = registry.rulesFor("X").validate(content);

        assertFalse(result.valid());
    }

    @Test
    void xAcceptsPostsWithinCharacterLimit() {
        Content content = new Content();
        content.setContentType(ContentType.TEXT);
        content.setBody("Short update.");

        assertTrue(registry.rulesFor("X").validate(content).valid());
    }

    @Test
    void instagramRequiresMediaContentType() {
        Content textContent = new Content();
        textContent.setContentType(ContentType.TEXT);
        textContent.setBody("No media here.");

        assertFalse(registry.rulesFor("Instagram").validate(textContent).valid());

        Content imageContent = new Content();
        imageContent.setContentType(ContentType.IMAGE);
        imageContent.setBody("Has an image.");

        assertTrue(registry.rulesFor("Instagram").validate(imageContent).valid());
    }

    @Test
    void unknownPlatformThrows() {
        assertThrows(IllegalArgumentException.class, () -> registry.rulesFor("Threads"));
    }
}
