package com.qtech.im.cache;

import com.qtech.im.cache.impl.CaffeineCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class CaffeineCacheTest {
    private Cache<String, String> cache;
    private CacheConfig config;

    @Before
    public void setUp() {
        config = new CacheConfig("testCache");
        config.setMaximumSize(100);
        config.setExpireAfterWrite(5000);
        cache = new CaffeineCache<>(config);
    }

    @After
    public void tearDown() {
        if (cache != null) {
            cache.clear(); // 清理缓存
            cache.close();
        }
    }

    @Test
    public void testBasicOperations() {
        // 测试put和get
        cache.put("key1", "value1");
        assertEquals("value1", cache.get("key1"));

        // 测试containsKey
        assertTrue(cache.containsKey("key1"));
        assertFalse(cache.containsKey("nonexistent"));

        // 测试remove
        assertTrue(cache.remove("key1"));
        assertFalse(cache.containsKey("key1"));
        assertNull(cache.get("key1"));

        // 测试remove不存在的key
        assertFalse("Remove non-existent key should return false",
                cache.remove("nonexistent"));
    }

    @Test
    public void testBatchOperations() {
        // 准备测试数据
        Map<String, String> testData = new HashMap<>();
        testData.put("key1", "value1");
        testData.put("key2", "value2");
        testData.put("key3", "value3");

        // 测试putAll
        cache.putAll(testData);

        // 测试getAll
        Set<String> keys = new HashSet<>(testData.keySet());
        Map<String, String> result = cache.getAll(keys);
        assertEquals(testData.size(), result.size());
        assertEquals(testData.get("key1"), result.get("key1"));
        assertEquals(testData.get("key2"), result.get("key2"));
        assertEquals(testData.get("key3"), result.get("key3"));

        // 测试removeAll
        int removedCount = cache.removeAll(keys);
        assertEquals(testData.size(), removedCount);
        assertEquals(0, cache.size());
    }

    @Test
    public void testSizeAndClear() {
        // 添加数据
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        // 测试size
        assertEquals(2, cache.size());

        // 测试clear
        cache.clear();
        assertEquals(0, cache.size());
        assertFalse(cache.containsKey("key1"));
        assertFalse(cache.containsKey("key2"));
    }

    @Test
    public void testGetOrLoad() {
        String key = "testKey";

        // 测试getOrLoad
        String value = cache.getOrLoad(key, k -> "loadedValue:" + k);
        assertEquals("loadedValue:testKey", value);
        assertEquals("loadedValue:testKey", cache.get(key));

        // 再次获取应该从缓存中获取
        String value2 = cache.getOrLoad(key, k -> "anotherValue");
        assertEquals("loadedValue:testKey", value2);
    }

    @Test
    public void testExpiration() throws InterruptedException {
        CacheConfig expireConfig = new CacheConfig("expireTest");
        expireConfig.setExpireAfterWrite(100);
        Cache<String, String> expireCache = new CaffeineCache<>(expireConfig);

        try {
            expireCache.put("expiringKey", "expiringValue");
            assertEquals("expiringValue", expireCache.get("expiringKey"));

            // 等待过期
            Thread.sleep(150);

            // 检查是否已过期
            assertNull("Cache entry should be expired", expireCache.get("expiringKey"));
        } finally {
            expireCache.close();
        }
    }
}
