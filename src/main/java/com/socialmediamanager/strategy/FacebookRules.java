package com.socialmediamanager.strategy;

import com.socialmediamanager.model.Content;

public class FacebookRules implements PlatformRules {

    private static final int MAX_LENGTH = 5000;

    @Override
    public String getPlatformName() {
        return "Facebook";
    }

    @Override
    public ValidationResult validate(Content content) {
        String body = content.getBody() == null ? "" : content.getBody();
        if (body.isBlank()) {
            return ValidationResult.fail("Facebook posts need some text.");
        }
        if (body.length() > MAX_LENGTH) {
            return ValidationResult.fail("Facebook posts must be at most " + MAX_LENGTH + " characters.");
        }
        return ValidationResult.ok();
    }
}
