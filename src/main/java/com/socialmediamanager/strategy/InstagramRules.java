package com.socialmediamanager.strategy;

import com.socialmediamanager.model.Content;
import com.socialmediamanager.model.ContentType;

import java.util.Set;

public class InstagramRules implements PlatformRules {

    private static final Set<ContentType> MEDIA_TYPES = Set.of(
            ContentType.IMAGE, ContentType.VIDEO, ContentType.PROMOTIONAL);

    @Override
    public String getPlatformName() {
        return "Instagram";
    }

    @Override
    public ValidationResult validate(Content content) {
        if (content.getContentType() == null || !MEDIA_TYPES.contains(content.getContentType())) {
            return ValidationResult.fail("Instagram posts must include an image, video, or promotional media.");
        }
        return ValidationResult.ok();
    }
}
