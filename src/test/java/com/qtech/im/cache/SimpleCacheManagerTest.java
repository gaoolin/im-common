package com.qtech.im.cache;

import com.qtech.im.cache.impl.manage.DefaultCacheManager;
import com.qtech.im.cache.support.CacheConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * since  :  2025/08/27
 */

public class SimpleCacheManagerTest {
    private CacheManager cacheManager;

    @Before
    public void setUp() {
        cacheManager = new DefaultCacheManager();
    }

    @After
    public void tearDown() {
        cacheManager.close();
    }

    @Test
    public void testCacheManagerOperations() {
        CacheConfig config = new CacheConfig("managerTestCache");
        config.setMaximumSize(100);

        // 测试getCache（目前返回null，因为createCache未实现）
        Cache<String, String> cache = cacheManager.getCache("managerTestCache");
        assertNull(cache);

        // 测试getCacheNames
        String[] cacheNames = cacheManager.getCacheNames().toArray(new String[0]);
        assertNotNull(cacheNames);
    }
}
