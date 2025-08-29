package com.qtech.im.cache;

import com.qtech.im.cache.impl.SimpleKeyGenerator;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * since  :  2025/08/27
 */
public class KeyGeneratorTest {

    @Test
    public void testSimpleKeyGenerator() throws NoSuchMethodException {
        SimpleKeyGenerator generator = new SimpleKeyGenerator();
        Object target = new TestService();
        Method method = TestService.class.getMethod("testMethod", String.class, int.class);
        Object[] params = {"param1", 42};

        Object key = generator.generate(target, method, params);
        assertNotNull(key);
        assertTrue(key instanceof SimpleKeyGenerator.SimpleKey);

        // 测试键的equals和hashCode
        Object key2 = generator.generate(target, method, params);
        assertEquals(key, key2);
        assertEquals(key.hashCode(), key2.hashCode());

        // 测试不同的参数产生不同的键
        Object[] params2 = {"param1", 43};
        Object key3 = generator.generate(target, method, params2);
        assertNotEquals(key, key3);
    }

    static class TestService {
        public void testMethod(String param1, int param2) {
            // 测试方法
        }
    }
}
