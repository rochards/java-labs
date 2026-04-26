package com.github.rochards.java_singleton.singleton;

import java.util.HashMap;
import java.util.Map;

public class CacheManagerV2 {

    /*
    * About the volatile keyword: A shared variable that includes the volatile modifier guarantees that all threads see
    * a consistent value for the shared variable. Any update to a volatile field updates the shared value of the field
    * immediately. In other words, a different thread cannot get an inconsistent value of the shared variable after its
    * value is updated.
    * source: https://www.baeldung.com/java-volatile
    *
    * Example:
    *   1. Thread A starts creating new CacheManagerV2().
    *   2. The reference may be assigned to instance before construction is fully visible to other threads.
    *   3. Thread B reads instance != null outside the synchronized block and returns it.
    *   4. Thread B may observe a partially constructed object.
    * volatile fixes that by giving instance safe publication semantics:
    *   - writes to instance become visible to other threads
    *   - the assignment cannot be reordered in a way that exposes a half-initialized object
    * So this: private static volatile CacheManagerV2 instance;
    * is required for this pattern to be correct.
    * Two important points:
    *   - synchronized protects the block where initialization happens.
    *   - volatile protects the unsynchronized reads outside that block.
    * */
    private static volatile CacheManagerV2 instance;

    private final Map<String, String> cache;

    private CacheManagerV2() {
        System.out.println("Creating instance...");
        cache = new HashMap<>();
    }

    public static CacheManagerV2 getInstance() {
        // This uses the double-checking looking
        if (instance == null) {
            pauseToExposeRaceCondition();

            synchronized (CacheManagerV2.class) {
                // This is a safe way to provide a singleton instance in a multi-thread environment. The synchronized
                // prevent more than one thread from executing this critical code simultaneously.
                if (instance == null) {
                    instance = new CacheManagerV2();
                }
            }
        }
        return instance;
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

    static void resetInstance() {
        // for test purpose
        instance = null;
    }
}
