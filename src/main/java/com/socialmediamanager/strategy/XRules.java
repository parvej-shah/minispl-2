package com.socialmediamanager.strategy;

import com.socialmediamanager.model.Content;

public class XRules implements PlatformRules {

    private static final int MAX_LENGTH = 280;

    @Override
    public String getPlatformName() {
        return "X";
    }

    @Override
    public ValidationResult validate(Content content) {
        String body = content.getBody() == null ? "" : content.getBody();
        if (body.isBlank()) {
            return ValidationResult.fail("X posts need some text.");
        }
        if (body.length() > MAX_LENGTH) {
            return ValidationResult.fail("X posts must be at most " + MAX_LENGTH + " characters.");
        }
        return ValidationResult.ok();
    }
}
