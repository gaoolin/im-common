package org.im.cache.impl.manager;

import org.im.cache.core.CacheManager;
import org.im.cache.stats.CacheManagerStats;
import org.im.cache.stats.CacheStats;
import org.im.cache.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 抽象缓存管理器
 * <p>
 * 提供缓存管理器的基础实现
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/12/08
 */
public abstract class AbstractCacheManager implements CacheManager {
    private static final Logger logger = LoggerFactory.getLogger(AbstractCacheManager.class);

    /**
     * 缓存名称集合（线程安全）
     */
    protected final CopyOnWriteArraySet<String> cacheNames = new CopyOnWriteArraySet<>();

    /**
     * 缓存管理器统计信息
     */
    protected final CacheManagerStats stats = new CacheManagerStats() {
        @Override
        public int getCacheCount() {
            return cacheNames.size();
        }

        @Override
        public CacheStats getAggregatedStats() {
            CacheStats aggregated = new CacheStats();
            cacheNames.forEach(name -> aggregated.merge(getCache(name).getStats()));
            return aggregated;
        }
    };

    /**
     * 获取缓存名称列表
     *
     * @return 缓存名称列表
     */
    @Override
    public Collection<String> getCacheNames() {
        return cacheNames;
    }

    /**
     * 获取缓存管理器统计信息
     *
     * @return 缓存管理器统计信息
     */
    @Override
    public CacheManagerStats getStats() {
        return stats;
    }

    /**
     * 验证缓存名称
     *
     * @param name 缓存名称
     * @throws IllegalArgumentException 如果名称无效
     */
    protected void validateCacheName(String name) {
        ValidationUtils.checkNotEmpty(name, "Cache name cannot be null or empty");
    }

    /**
     * 记录缓存创建
     */
    protected void recordCacheCreate() {
        stats.recordCacheCreate();
    }

    /**
     * 记录缓存移除
     */
    protected void recordCacheRemove() {
        stats.recordCacheRemove();
    }

    /**
     * 记录缓存获取
     */
    protected void recordCacheGet() {
        stats.recordCacheGet();
    }

    /**
     * 关闭缓存管理器
     */
    @Override
    public void close() {
        logger.info("Closing cache manager");
        // 子类需要实现具体的关闭逻辑
    }
}