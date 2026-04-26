package com.github.rochards.java_singleton.singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class CacheManagerV3 {

    /*
    * About AtomicReference: it allows us to read and update a shared reference in a thread-safe way without using
    * volatile directly on the field itself. In this example, AtomicReference is the object responsible for holding
    * the singleton instance and publishing it safely to other threads.
    *
    * Two important points:
    *   - synchronized protects the block where initialization happens.
    *   - AtomicReference safely stores and exposes the shared singleton instance.
    * */
    private static final AtomicReference<CacheManagerV3> ATOMIC_REFERENCE = new AtomicReference<>();

    private final Map<String, String> cache;

    private CacheManagerV3() {
        System.out.println("Creating instance...");
        cache = new HashMap<>();
    }

    public static CacheManagerV3 getInstance() {
        // First read without locking to avoid synchronization after the instance is created.
        var instance = ATOMIC_REFERENCE.get();
        if (instance == null) {
            pauseToExposeRaceCondition();

            synchronized (CacheManagerV3.class) {
                // Read the shared reference again because another thread may have initialized it already.
                instance = ATOMIC_REFERENCE.get();
                if (instance == null) {
                    // Only the first thread that still sees null creates and stores the singleton instance.
                    instance = new CacheManagerV3();
                    ATOMIC_REFERENCE.set(instance);
                }
            }
        }
        return ATOMIC_REFERENCE.get();
    }

    private static void pauseToExposeRaceCondition() {
        // Artificially widens the race window
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void put(String key, String value) {
        cache.put(key, value);
    }

    public String get(String key) {
        return cache.get(key);
    }
}
