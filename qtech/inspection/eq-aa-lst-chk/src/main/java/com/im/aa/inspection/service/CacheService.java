package com.im.aa.inspection.service;

import com.im.aa.inspection.entity.param.EqLstParsed;
import com.im.aa.inspection.entity.standard.EqLstTplDO;
import com.im.aa.inspection.entity.standard.EqLstTplInfoPO;
import com.im.aa.inspection.util.EqpLstRedisCacheConfig;
import org.im.cache.core.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

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

    // Redis缓存配置
    private final EqpLstRedisCacheConfig redisCacheConfig;

    private final Cache<String, EqLstTplDO> eqLstTplCache;
    private final Cache<String, EqLstTplInfoPO> eqLstTplInfoCache;

    /**
     * 构造函数
     * 初始化缓存配置和缓存实例
     *
     * @param redisCacheConfig Redis缓存配置
     */
    public CacheService(EqpLstRedisCacheConfig redisCacheConfig) {
        this.redisCacheConfig = Objects.requireNonNull(redisCacheConfig, "Redis缓存配置不能为空");
        this.eqLstTplCache = this.redisCacheConfig.getEqLstTplDOCache();
        this.eqLstTplInfoCache = this.redisCacheConfig.getEqLstTplInfoPOCache();
        logger.info("CacheService初始化完成，使用Redis缓存配置");
    }

    /**
     * 无参构造函数
     * 初始化默认的Redis缓存配置
     */
    public CacheService() {
        this(new EqpLstRedisCacheConfig());
    }

    /**
     * 生成缓存键
     *
     * @param result 设备参数
     * @return 缓存键
     */
    private String generateEqLstTplInfoCacheKey(EqLstTplInfoPO result) {
        return "parsed:" + result.getModule();
    }

    /**
     * 生成缓存键
     *
     * @param result 检查结果
     * @return 缓存键
     */
    private String generateEqLstTplCacheKey(EqLstParsed result) {
        return "inspection:" + result.getModule();
    }

    /**
     * 关闭缓存服务，释放资源
     */
    public void close() {
        try {
            if (redisCacheConfig != null && redisCacheConfig.getCacheManager() != null) {
                redisCacheConfig.getCacheManager().close();
                logger.info("Redis缓存服务已关闭");
            }
        } catch (Exception e) {
            logger.error("关闭Redis缓存服务时出错", e);
        }
    }

    /**
     * 获取Redis缓存配置实例
     *
     * @return Redis缓存配置
     */
    public EqpLstRedisCacheConfig getRedisCacheConfig() {
        return redisCacheConfig;
    }
}