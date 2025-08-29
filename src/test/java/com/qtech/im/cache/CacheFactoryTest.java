package com.qtech.im.cache;

import com.qtech.im.cache.builder.CacheConfigBuilder;
import com.qtech.im.cache.builder.CacheFactory;
import com.qtech.im.cache.impl.cache.SimpleMemoryCache;
import com.qtech.im.cache.impl.manage.DefaultCacheManager;
import com.qtech.im.cache.support.BackendType;
import org.junit.Test;

import static org.junit.Assert.*;

public class CacheFactoryTest {

    @Test
    public void testCreateAndRegisterCache() {
        DefaultCacheManager manager = new DefaultCacheManager();
        CacheFactory factory = new CacheFactory(manager);
        CacheConfig config = CacheConfigBuilder.create()
                .withName("testCache")
                .withBackendType(BackendType.MEMORY)
                .withNullValueProtection(false) // 禁用保护机制
                .withBreakdownProtection(false)
                .withAvalancheProtection(false)
                .build();
        Cache<String, String> cache = factory.create(config);
        assertTrue(cache instanceof SimpleMemoryCache); // 现在应通过
        assertEquals(cache, manager.getCache("testCache"));
        cache.put("key", "value");
        assertEquals("value", cache.get("key"));
    }

    @Test
    public void testRegisterCache() {
        DefaultCacheManager manager = new DefaultCacheManager();
        CacheFactory factory = new CacheFactory(manager);
        CacheConfig config = CacheConfigBuilder.create()
                .withName("testCache")
                .withBackendType(BackendType.MEMORY)
                .withNullValueProtection(false)
                .withBreakdownProtection(false)
                .withAvalancheProtection(false)
                .build();
        Cache<String, String> cache = new SimpleMemoryCache<>(config);
        manager.registerCache("testCache", cache);
        assertEquals(cache, manager.getCache("testCache"));
    }

    @Test
    public void testGetOrCreateCache() {
        DefaultCacheManager manager = new DefaultCacheManager();
        CacheConfig config = CacheConfigBuilder.create()
                .withName("testCache")
                .withBackendType(BackendType.MEMORY)
                .withNullValueProtection(false)
                .withBreakdownProtection(false)
                .withAvalancheProtection(false)
                .build();
        Cache<String, String> cache1 = manager.getOrCreateCache("testCache", config);
        Cache<String, String> cache2 = manager.getOrCreateCache("testCache", config);
        assertSame(cache1, cache2); // 确保返回同一实例
    }

    @Test(expected = IllegalStateException.class)
    public void testRegisterCacheDuplicate() {
        DefaultCacheManager manager = new DefaultCacheManager();
        CacheFactory factory = new CacheFactory(manager);
        CacheConfig config = CacheConfigBuilder.create()
                .withName("testCache")
                .withBackendType(BackendType.MEMORY)
                .build();
        Cache<String, String> cache1 = factory.create(config);
        Cache<String, String> cache2 = new SimpleMemoryCache<>(config);
        manager.registerCache("testCache", cache2); // 应抛异常
    }
}