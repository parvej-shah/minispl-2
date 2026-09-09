package com.socialmediamanager.strategy;

import com.socialmediamanager.model.Content;

public interface PlatformRules {

    String getPlatformName();

    ValidationResult validate(Content content);

    record ValidationResult(boolean valid, String message) {
        public static ValidationResult ok() {
            return new ValidationResult(true, "Content is valid for this platform.");
        }

        public static ValidationResult fail(String message) {
            return new ValidationResult(false, message);
        }
    }
}
