package com.qtech.im.cache;

import com.qtech.im.cache.impl.cache.CaffeineCache;
import com.qtech.im.cache.support.CacheConfig;
import com.qtech.im.cache.support.CacheStats;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * since  :  2025/08/27
 */

public class CachePerformanceTest {

    @Test
    public void testSingleThreadPerformance() {
        CacheConfig config = new CacheConfig("performanceTest");
        config.setMaximumSize(10000);
        Cache<String, String> cache = new CaffeineCache<>(config);

        try {
            long startTime = System.currentTimeMillis();

            // 测试写入性能
            for (int i = 0; i < 10000; i++) {
                cache.put("key" + i, "value" + i);
            }

            long writeTime = System.currentTimeMillis() - startTime;
            System.out.println("写入10000条数据耗时: " + writeTime + "ms");

            startTime = System.currentTimeMillis();

            // 测试读取性能
            int hitCount = 0;
            for (int i = 0; i < 10000; i++) {
                String value = cache.get("key" + i);
                if (value != null) {
                    hitCount++;
                }
            }

            long readTime = System.currentTimeMillis() - startTime;
            System.out.println("读取10000条数据耗时: " + readTime + "ms");
            System.out.println("命中率: " + (hitCount / 10000.0 * 100) + "%");

            assertEquals(10000, hitCount);
        } finally {
            cache.close();
        }
    }

    @Test
    public void testConcurrentPerformance() throws InterruptedException {
        CacheConfig config = new CacheConfig("concurrentTest");
        config.setMaximumSize(10000);
        Cache<String, String> cache = new CaffeineCache<>(config);

        try {
            int threadCount = 10;
            int operationsPerThread = 1000;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicLong totalOperations = new AtomicLong(0);

            long startTime = System.currentTimeMillis();

            // 启动并发线程
            for (int t = 0; t < threadCount; t++) {
                final int threadId = t;
                executor.submit(() -> {
                    try {
                        for (int i = 0; i < operationsPerThread; i++) {
                            int keyIndex = threadId * operationsPerThread + i;
                            String key = "key" + keyIndex;
                            String value = "value" + keyIndex;

                            // 50%写入，50%读取
                            if (i % 2 == 0) {
                                cache.put(key, value);
                            } else {
                                cache.get(key);
                            }
                            totalOperations.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // 等待所有线程完成
            latch.await();
            long totalTime = System.currentTimeMillis() - startTime;

            executor.shutdown();

            long totalOps = totalOperations.get();
            double opsPerSecond = totalOps / (totalTime / 1000.0);

            System.out.println("并发测试结果:");
            System.out.println("总操作数: " + totalOps);
            System.out.println("总耗时: " + totalTime + "ms");
            System.out.println("每秒操作数: " + opsPerSecond);

            // 验证基本功能
            assertTrue(totalOps > 0);
            assertTrue(opsPerSecond > 0);
        } finally {
            cache.close();
        }
    }

    @Test
    public void testCacheHitRate() {
        CacheConfig config = new CacheConfig("hitRateTest");
        config.setMaximumSize(1000);
        config.setRecordStats(true);
        Cache<String, String> cache = new CaffeineCache<>(config);

        try {
            // 预热缓存
            for (int i = 0; i < 100; i++) {
                cache.put("key" + i, "value" + i);
            }

            // 混合读取（命中和未命中）
            for (int i = 0; i < 200; i++) {
                cache.get("key" + (i % 150)); // 前100个命中，后50个未命中
            }

            // 检查统计信息
            CacheStats stats = cache.getStats();
            assertNotNull(stats);

            System.out.println("缓存统计信息:");
            System.out.println("请求总数: " + stats.getRequestCount());
            System.out.println("命中次数: " + stats.getHitCount());
            System.out.println("未命中次数: " + stats.getMissCount());
            System.out.println("命中率: " + String.format("%.2f%%", stats.getHitRate() * 100));

            // 验证统计信息合理性
            assertEquals(200, stats.getRequestCount());
            assertTrue(stats.getHitCount() > 0);
            assertTrue(stats.getMissCount() > 0);
            assertTrue(stats.getHitRate() > 0);
        } finally {
            cache.close();
        }
    }
}
