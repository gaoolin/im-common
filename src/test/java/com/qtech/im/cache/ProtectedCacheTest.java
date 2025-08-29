package com.qtech.im.cache;

import com.qtech.im.cache.impl.cache.ProtectedCache;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ProtectedCacheTest {

    @Test
    public void testProtectedCacheDelegation() {
        // 创建mock的委托缓存
        Cache<String, String> mockDelegate = mock(Cache.class);
        when(mockDelegate.get("testKey")).thenReturn("testValue");

        // 创建配置
        CacheConfig config = new CacheConfig("protectedTest");

        // 创建ProtectedCache
        ProtectedCache<String, String> protectedCache = new ProtectedCache<>(mockDelegate, config);

        // 测试委托调用
        String result = protectedCache.get("testKey");
        assertEquals("testValue", result);

        // 验证委托方法被调用
        verify(mockDelegate).get("testKey");
        verify(mockDelegate).put(anyString(), anyString());
    }
}
