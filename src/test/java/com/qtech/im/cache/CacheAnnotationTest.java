package com.qtech.im.cache;

import com.qtech.im.cache.annotation.CacheEvict;
import com.qtech.im.cache.annotation.CachePut;
import com.qtech.im.cache.annotation.Cacheable;
import com.qtech.im.cache.impl.SimpleKeyGenerator;
import com.qtech.im.cache.impl.cache.CaffeineCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * since  :  2025/08/27
 */

/**
 * 缓存注解测试类
 */
public class CacheAnnotationTest {
    private Cache<String, String> cache;

    @Before
    public void setUp() {
        CacheConfig config = new CacheConfig("annotationTestCache");
        config.setMaximumSize(100);
        config.setExpireAfterWrite(5 * 60 * 1000);
        cache = new CaffeineCache<>(config);
    }

    @After
    public void tearDown() {
        if (cache != null) {
            cache.clear();
            cache.close();
        }
    }

    @Test
    public void testCacheableAnnotation() throws NoSuchMethodException {
        // 测试@Cacheable注解的属性
        Method method = TestService.class.getMethod("getCachedData", String.class);
        Cacheable cacheable = method.getAnnotation(Cacheable.class);

        assertNotNull("@Cacheable注解应该存在", cacheable);
        assertEquals("缓存名称应该匹配", "testCache", cacheable.cacheNames());
        assertEquals("键生成器应该匹配", SimpleKeyGenerator.class, cacheable.keyGenerator());
        assertEquals("TTL应该为默认值0", 0, cacheable.ttl());
        assertEquals("TTL单位应该为毫秒", TimeUnit.MILLISECONDS, cacheable.ttlUnit());
    }

    @Test
    public void testCachePutAnnotation() throws NoSuchMethodException {
        // 测试@CachePut注解的属性
        Method method = TestService.class.getMethod("updateCachedData", String.class, String.class);
        CachePut cachePut = method.getAnnotation(CachePut.class);

        assertNotNull("@CachePut注解应该存在", cachePut);
        assertEquals("缓存名称应该匹配", "testCache", cachePut.cacheNames());
        assertEquals("键生成器应该匹配", SimpleKeyGenerator.class, cachePut.keyGenerator());
        assertEquals("TTL应该为默认值0", 0, cachePut.ttl());
        assertEquals("TTL单位应该为毫秒", TimeUnit.MILLISECONDS, cachePut.ttlUnit());
    }

    @Test
    public void testCacheEvictAnnotation() throws NoSuchMethodException {
        // 测试@CacheEvict注解的属性
        Method method = TestService.class.getMethod("deleteCachedData", String.class);
        CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);

        assertNotNull("@CacheEvict注解应该存在", cacheEvict);
        assertEquals("缓存名称应该匹配", "testCache", cacheEvict.cacheNames());
        assertEquals("键生成器应该匹配", SimpleKeyGenerator.class, cacheEvict.keyGenerator());
        assertFalse("allEntries应该为false", cacheEvict.allEntries());
        assertFalse("beforeInvocation应该为false", cacheEvict.beforeInvocation());
    }

    @Test
    public void testSimpleKeyGenerator() throws NoSuchMethodException {
        // 测试SimpleKeyGenerator的功能
        SimpleKeyGenerator generator = new SimpleKeyGenerator();
        Object target = new TestService();
        Method method = TestService.class.getMethod("getCachedData", String.class);
        Object[] params = {"testParam"};

        Object key = generator.generate(target, method, params);
        assertNotNull("生成的键不应该为null", key);
        assertTrue("生成的键应该是SimpleKey类型", key instanceof SimpleKeyGenerator);

        // 测试键的equals和hashCode
        Object key2 = generator.generate(target, method, params);
        assertEquals("相同参数生成的键应该相等", key, key2);
        assertEquals("相同参数生成的键hashCode应该相等", key.hashCode(), key2.hashCode());

        // 测试不同参数生成不同键
        Object[] params2 = {"testParam2"};
        Object key3 = generator.generate(target, method, params2);
        assertNotEquals("不同参数应该生成不同键", key, key3);
    }

    @Test
    public void testSimpleKeyEquality() {
        // 测试SimpleKey的相等性
        Object[] elements1 = {"class1", "method1", "param1"};
        Object[] elements2 = {"class1", "method1", "param1"};
        Object[] elements3 = {"class1", "method1", "param2"};

        SimpleKeyGenerator.SimpleKey key1 = new SimpleKeyGenerator.SimpleKey(elements1);
        SimpleKeyGenerator.SimpleKey key2 = new SimpleKeyGenerator.SimpleKey(elements2);
        SimpleKeyGenerator.SimpleKey key3 = new SimpleKeyGenerator.SimpleKey(elements3);

        assertEquals("相同元素的键应该相等", key1, key2);
        assertNotEquals("不同元素的键不应该相等", key1, key3);

        // 测试与null的比较
        assertNotEquals("不应该与null相等", key1, null);

        // 测试与非SimpleKey对象的比较
        assertNotEquals("不应该与其他类型相等", key1, "string");
    }

    @Test
    public void testSimpleKeyToString() {
        // 测试SimpleKey的toString方法
        Object[] elements = {"class1", "method1", "param1"};
        SimpleKeyGenerator.SimpleKey key = new SimpleKeyGenerator.SimpleKey(elements);

        String keyString = key.toString();
        assertNotNull("toString结果不应该为null", keyString);
        assertTrue("toString应该包含SimpleKey", keyString.contains("SimpleKey"));
        assertTrue("toString应该包含元素内容", keyString.contains("class1"));
        assertTrue("toString应该包含元素内容", keyString.contains("method1"));
        assertTrue("toString应该包含元素内容", keyString.contains("param1"));
    }

    // 测试服务类
    static class TestService {

        @Cacheable(cacheNames = "testCache", key = "#id")
        public String getCachedData(String id) {
            return "data:" + id;
        }

        @CachePut(cacheNames = "testCache", key = "#id")
        public String updateCachedData(String id, String data) {
            return data;
        }

        @CacheEvict(cacheNames = "testCache", key = "#id")
        public void deleteCachedData(String id) {
            // 删除操作
        }

        @CacheEvict(cacheNames = "testCache", allEntries = true)
        public void clearAllCache() {
            // 清除所有缓存
        }
    }
}