package com.github.rochards.java_singleton.singleton;

import java.util.HashMap;
import java.util.Map;

public class CacheManagerV1 {

    private static CacheManagerV1 instance;
    private final Map<String, String> cache;

    private CacheManagerV1() {
        System.out.println("Creating instance...");
        cache = new HashMap<>();
    }

    public static CacheManagerV1 getInstance() {
        /* This is a valid way to implement a singleton pattern here, but in a multithread environment there is not
        * guarantee that this instance will be instantiated only once. Therefore, it is only valid in a single-threaded
        * environment.
        * This called Lazy Initialization, because the instance will exist only if the getInstance method is invoked.
        * */
        if (instance == null) {
            pauseToExposeRaceCondition();

            instance = new CacheManagerV1();
        }
        return instance;
    }

    private static void pauseToExposeRaceCondition() {
        // Artificially widens the race window so the test can reproduce the problem more easily.
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

    static void resetInstance() {
        // for test purpose
        instance = null;
    }
}
