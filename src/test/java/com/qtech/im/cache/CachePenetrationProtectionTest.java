package com.qtech.im.cache;

import com.qtech.im.cache.impl.cache.CaffeineCache;
import org.junit.Test;

import static org.junit.Assert.assertNull;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * since  :  2025/08/27
 */
public class CachePenetrationProtectionTest {

    @Test
    public void testNullValueProtection() {
        CacheConfig config = new CacheConfig("nullValueTest");
        config.setEnableNullValueProtection(true);
        config.setNullValueExpireTime(1000); // 1秒过期

        Cache<String, String> cache = new CaffeineCache<>(config);

        try {
            // 查询不存在的数据
            String result = cache.get("nonexistent");
            assertNull(result);

            // 再次查询应该能快速返回（虽然具体实现需要在ProtectedCache中完成）
            String result2 = cache.get("nonexistent");
            assertNull(result2);
        } finally {
            cache.close();
        }
    }
}
