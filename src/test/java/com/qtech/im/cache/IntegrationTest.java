package com.qtech.im.cache;

import com.qtech.im.cache.impl.cache.CaffeineCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * since  :  2025/08/27
 */


public class IntegrationTest {
    private Cache<String, TestData> cache;

    @Before
    public void setUp() {
        CacheConfig config = new CacheConfig("integrationTest");
        config.setMaximumSize(1000);
        config.setExpireAfterWrite(30000); // 30秒
        config.setRecordStats(true);
        config.setEnableNullValueProtection(true);
        config.setEnableBreakdownProtection(true);

        cache = new CaffeineCache<>(config);
    }

    @After
    public void tearDown() {
        if (cache != null) {
            cache.close();
        }
    }

    @Test
    public void testCompleteWorkflow() {
        // 1. 创建测试数据
        TestData data1 = new TestData("1", "Test Data 1");
        TestData data2 = new TestData("2", "Test Data 2");

        // 2. 放入缓存
        cache.put("data1", data1);
        cache.put("data2", data2);

        // 3. 从缓存获取
        TestData cachedData1 = cache.get("data1");
        TestData cachedData2 = cache.get("data2");

        assertNotNull(cachedData1);
        assertNotNull(cachedData2);
        assertEquals(data1, cachedData1);
        assertEquals(data2, cachedData2);

        // 4. 测试自动加载
        TestData loadedData = cache.getOrLoad("data3", key -> new TestData("3", "Loaded Data 3"));
        assertNotNull(loadedData);
        assertEquals("3", loadedData.getId());
        assertEquals("Loaded Data 3", loadedData.getName());

        // 5. 验证缓存中已存在加载的数据
        TestData cachedLoadedData = cache.get("data3");
        assertEquals(loadedData, cachedLoadedData);

        // 6. 检查统计信息
        com.qtech.im.cache.CacheStats stats = cache.getStats();
        assertTrue(stats.getRequestCount() > 0);
        assertTrue(stats.getHitCount() > 0);
        assertTrue(stats.getLoadSuccessCount() > 0);
    }

    // 测试数据类
    static class TestData {
        private String id;
        private String name;

        public TestData() {
        }

        public TestData(String id, String name) {
            this.id = id;
            this.name = name;
        }

        // getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestData testData = (TestData) o;
            return id.equals(testData.id) && name.equals(testData.name);
        }

        @Override
        public int hashCode() {
            return id.hashCode() + name.hashCode();
        }
    }
}
