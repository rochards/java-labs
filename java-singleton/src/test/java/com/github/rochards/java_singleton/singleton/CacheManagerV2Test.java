package com.github.rochards.java_singleton.singleton;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CacheManagerV2Test {

    @Test
    void shouldSaveAndRetrieveAValue() {
        var cache = CacheManagerV2.getInstance();

        cache.put("peter", "spiderman");

        assertEquals("spiderman", cache.get("peter"));
    }

    @Test
    void shouldVerifyItIsASingleton() {
        var cache1 = CacheManagerV2.getInstance();
        var cache2 = CacheManagerV2.getInstance();

        assertSame(cache1, cache2);
    }


    @Test
    void shouldVerifyThereIsOneInstanceCreated() throws Exception {
        CacheManagerV2.resetInstance();

        int threadCount = 4;
        var start = new CountDownLatch(1); // Releases all workers at the same time.
        var done = new CountDownLatch(threadCount); // Tracks when every worker has finished.
        var instances = Collections.synchronizedSet(new HashSet<CacheManagerV2>());

        try (var executor = Executors.newFixedThreadPool(threadCount)) { // Runs the workers concurrently.
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> { // Schedules one task that races to call getInstance().
                    try {
                        start.await(); // Waits until the test releases all workers.

                        instances.add(CacheManagerV2.getInstance()); // Calls the synchronized lazy initializer.

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Restores the interrupted status.
                    } finally {
                        done.countDown(); // Signals that this worker is done.
                    }
                });
            }

            start.countDown(); // Starts the race window for all workers.
            assertTrue(done.await(1, TimeUnit.SECONDS), "Timed out waiting for worker threads"); // Waits for all workers to finish.
        }

        assertFalse(instances.size() > 1);
    }
}
