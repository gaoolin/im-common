package com.im.aa.inspection.service;

import com.im.aa.inspection.context.CheckResult;
import com.im.aa.inspection.entity.param.EqLstParsed;
import org.im.cache.builder.CacheConfigBuilder;
import org.im.cache.config.CacheConfig;
import org.im.cache.core.Cache;
import org.im.cache.core.CacheManager;
import org.im.cache.impl.manager.DefaultCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 缓存服务类
 * <p>
 * 提供设备参数检查结果的缓存功能，充分利用im-framework缓存管理模块的功能
 * 包括缓存穿透保护、击穿保护、雪崩保护等高级特性
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/25
 */

public class CacheService {
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    // 检查结果缓存名称
    private static final String CHECK_RESULT_CACHE_NAME = "check-result-cache";

    // 缓存管理器
    private final CacheManager cacheManager;

    // 检查结果缓存实例
    private final Cache<String, CheckResult> checkResultCache;

    /**
     * 构造函数
     * 初始化缓存管理器和缓存实例
     *
     * @param cacheManager 缓存管理器
     */
    public CacheService(CacheManager cacheManager) {
        // 使用传入的缓存管理器
        this.cacheManager = cacheManager;

        // 配置检查结果缓存
        CacheConfig checkResultCacheConfig = CacheConfigBuilder.create()
                .withName(CHECK_RESULT_CACHE_NAME)
                .withMaximumSize(10000) // 最大缓存10000个结果
                .withExpireAfterWrite(TimeUnit.MINUTES.toMillis(15)) // 写入后15分钟过期
                .withStatsEnabled(true) // 启用统计
                .withNullValueProtection(true) // 启用缓存穿透保护
                .withBreakdownProtection(true) // 启用缓存击穿保护
                .withAvalancheProtection(true) // 启用缓存雪崩保护
                .build();

        // 获取或创建检查结果缓存实例
        this.checkResultCache = this.cacheManager.getOrCreateCache(
                CHECK_RESULT_CACHE_NAME,
                checkResultCacheConfig);

        logger.info("CacheService初始化完成，检查结果缓存已创建: {}", CHECK_RESULT_CACHE_NAME);
    }

    /**
     * 无参构造函数
     * 初始化缓存管理器和缓存实例
     */
    public CacheService() {
        // 初始化缓存管理器
        this(new DefaultCacheManager());
    }

    /**
     * 从缓存中获取检查结果
     *
     * @param param 设备参数
     * @return 检查结果，如果缓存中不存在则返回null
     */
    public CheckResult getCheckResult(EqLstParsed param) {
        String key = generateCacheKey(param);
        CheckResult result = checkResultCache.get(key);

        if (result != null) {
            logger.debug("从缓存中获取到检查结果: key={}", key);
        } else {
            logger.debug("缓存中未找到检查结果: key={}", key);
        }

        return result;
    }

    /**
     * 将检查结果缓存
     *
     * @param result 检查结果
     */
    public void cacheCheckResult(CheckResult result) {
        String key = generateCacheKey(result);
        checkResultCache.put(key, result);
        logger.debug("检查结果已缓存: key={}", key);
    }

    /**
     * 生成缓存键
     *
     * @param param 设备参数
     * @return 缓存键
     */
    private String generateCacheKey(EqLstParsed param) {
        return param.getSimId() + ":" + param.getReceivedTime();
    }

    /**
     * 生成缓存键
     *
     * @param result 检查结果
     * @return 缓存键
     */
    private String generateCacheKey(CheckResult result) {
        return result.getEquipmentId() + ":" + result.getParamType();
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息字符串
     */
    public String getCacheStats() {
        return "检查结果缓存统计: " + checkResultCache.getStats().toString();
    }

    /**
     * 清空检查结果缓存
     */
    public void clearCheckResultCache() {
        checkResultCache.clear();
        logger.info("检查结果缓存已清空");
    }

    /**
     * 关闭缓存服务，释放资源
     */
    public void close() {
        try {
            cacheManager.close();
            logger.info("缓存服务已关闭");
        } catch (Exception e) {
            logger.error("关闭缓存服务时出错", e);
        }
    }
}