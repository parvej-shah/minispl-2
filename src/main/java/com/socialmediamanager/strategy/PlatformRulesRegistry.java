package com.socialmediamanager.strategy;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PlatformRulesRegistry {

    private final Map<String, Supplier<PlatformRules>> rulesByPlatform;

    public PlatformRulesRegistry() {
        this.rulesByPlatform = Map.of(
                "Facebook", FacebookRules::new,
                "Instagram", InstagramRules::new,
                "X", XRules::new
        );
    }

    public PlatformRules rulesFor(String platformName) {
        Supplier<PlatformRules> factory = rulesByPlatform.get(platformName);
        if (factory == null) {
            throw new IllegalArgumentException("No validation rules registered for platform: " + platformName);
        }
        return factory.get();
    }

    public List<String> supportedPlatforms() {
        return List.copyOf(rulesByPlatform.keySet());
    }
}
