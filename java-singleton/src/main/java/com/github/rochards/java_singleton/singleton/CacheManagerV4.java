package com.github.rochards.java_singleton.singleton;

import java.util.HashMap;
import java.util.Map;

public class CacheManagerV4 {

    /*
    * This is called Eager Initialization, because the singleton instance is created immediately when the class is loaded,
    * and class loading in Java is thread-safe.
    * */
    private static final CacheManagerV4 INSTANCE = new CacheManagerV4();
    private final Map<String, String> cache;

    private CacheManagerV4() {
        System.out.println("Creating instance...");
        cache = new HashMap<>();
    }

    public static CacheManagerV4 getInstance() {
        return INSTANCE;
    }

    public void put(String key, String value) {
        cache.put(key, value);
    }

    public String get(String key) {
        return cache.get(key);
    }
}
