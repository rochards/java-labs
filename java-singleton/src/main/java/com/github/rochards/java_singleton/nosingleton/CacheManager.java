package com.github.rochards.java_singleton.nosingleton;

import java.util.HashMap;
import java.util.Map;

public class CacheManager {

    private final Map<String, String> cache;

    public CacheManager() {
        // The constructor is accessible to anyone, so you cannot guarantee that only one instance will exist in memory
        System.out.println("Creating instance...");
        cache = new HashMap<>();
    }

    public void put(String key, String value) {
        cache.put(key, value);
    }

    public String get(String key) {
        return cache.get(key);
    }
}
