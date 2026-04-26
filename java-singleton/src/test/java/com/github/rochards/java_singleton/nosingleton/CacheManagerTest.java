package com.github.rochards.java_singleton.nosingleton;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CacheManagerTest {

    @Test
    void shouldSaveAndRetrieveAValue() {
        var cache = new CacheManager();

        cache.put("peter", "spiderman");

        assertEquals("spiderman", cache.get("peter"));
    }

    @Test
    void shouldVerityIsNotASingleton() {
        var cache1 = new CacheManager();
        var cache2 = new CacheManager();

        assertNotSame(cache1, cache2);
    }
}
