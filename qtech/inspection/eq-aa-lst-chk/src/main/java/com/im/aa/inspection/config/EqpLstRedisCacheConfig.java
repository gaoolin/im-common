package com.im.aa.inspection.config;

import com.im.aa.inspection.entity.standard.EqLstTplDO;
import com.im.aa.inspection.entity.standard.EqLstTplInfoDO;
import org.im.cache.config.CacheConfig;
import org.im.cache.core.Cache;
import org.im.cache.core.CacheManager;
import org.im.cache.impl.manager.DefaultCacheManager;
import org.im.cache.impl.support.BackendType;
import org.im.config.ConfigurationManager;
import org.im.config.impl.DefaultConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import static com.im.qtech.common.constant.QtechImBizConstant.*;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/30
 */

public class EqpLstRedisCacheConfig {
    private static final Logger logger = LoggerFactory.getLogger(EqpLstRedisCacheConfig.class);

    // 使用你框架的配置管理模块读取配置
    private String redisClusterNodes;
    private String redisPassword;
    private String clientName;
    private int defaultMaxSize;
    private long defaultExpireMs;

    private CacheManager cacheManager;

    // 缓存实例
    private volatile Cache<String, String> defaultCache;
    private volatile Cache<String, String> eqLstTplInfoCache;
    private volatile Cache<String, String> eqLstTplCache;
    private volatile Cache<String, EqLstTplInfoDO> eqLstTplInfoDOCache;
    private volatile Cache<String, EqLstTplDO> eqLstTplDOCache;
    private volatile Cache<String, byte[]> eqLstByteCache;

    private volatile Cache<String, String> eqLstByteStringCache;

    public EqpLstRedisCacheConfig() {
        // 使用配置模块读取application.properties中的配置
        loadConfigFromFramework();
        init();
    }

    private void loadConfigFromFramework() {
        // 这里使用你的im-framework配置管理模块来读取配置
        ConfigurationManager configManager = new DefaultConfigurationManager();
        this.redisClusterNodes = configManager.getProperty("im.redis.cluster.nodes", "10.170.6.24:6379,10.170.6.25:6379,10.170.6.26:6379");
        this.redisPassword = configManager.getProperty("im.redis.password", "im@2024");
        this.clientName = configManager.getProperty("im.redis.client-name", "im-dto-service");
        this.defaultMaxSize = Integer.parseInt(configManager.getProperty("cache.default.max-size", "1000"));
        this.defaultExpireMs = Long.parseLong(configManager.getProperty("cache.default.expire", "1800000"));

        logger.info(">>>>> Loaded cache configuration from framework config module");
    }

    public void init() {
        try {
            cacheManager = new DefaultCacheManager();
            logger.info(">>>>> CacheManager initialized successfully");
        } catch (Exception e) {
            logger.error(">>>>> Failed to initialize CacheManager", e);
            throw new RuntimeException(">>>>> Failed to initialize CacheManager", e);
        }
    }

    protected CacheConfig baseConfig(String name, int maxSize, long expireMs) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Cache name cannot be null or empty");
        }

        if (maxSize <= 0) {
            throw new IllegalArgumentException("Max size must be positive");
        }

        if (expireMs <= 0) {
            throw new IllegalArgumentException("Expire time must be positive");
        }

        CacheConfig config = new CacheConfig();
        config.setPrefix(name);
        config.setMaximumSize(maxSize);
        config.setExpireAfterWrite(expireMs);
        config.setBackendType(BackendType.REDIS);
        return config;
    }

    protected String buildRedisUri() throws UnsupportedEncodingException {
        if (redisClusterNodes == null || redisClusterNodes.isEmpty()) {
            throw new IllegalStateException("Redis cluster nodes are not configured properly. Current value: " + redisClusterNodes);
        }

        if (redisPassword == null) {
            throw new IllegalStateException("Redis password is not configured properly. Current value: " + redisPassword);
        }

        if (clientName == null || clientName.isEmpty()) {
            throw new IllegalStateException("Redis client name is not configured properly. Current value: " + clientName);
        }

        // 构建集群模式的Redis URI
        StringBuilder uris = new StringBuilder();
        String[] nodes = redisClusterNodes.split(",");
        for (int i = 0; i < nodes.length; i++) {
            if (i > 0) {
                uris.append(",");
            }
            String node = nodes[i].trim();
            if (!node.contains(":")) {
                throw new IllegalStateException("Invalid Redis cluster node format: " + node);
            }
            String encodedPassword = URLEncoder.encode(redisPassword, "UTF-8");
            uris.append(String.format("redis://%s:%s@%s?clientName=%s", "default", encodedPassword, node, clientName));
        }

        logger.debug(">>>>> Built Redis cluster URIs: {}", uris.toString());
        return uris.toString();
    }

    // ========= 默认分布式缓存 =========

    /**
     * @param
     * @return org.im.cache.core.Cache<java.lang.String, java.lang.Object>
     * @description 用于获取忽略反控的机台的盒子号（SIMID）
     */
    public Cache<String, String> getDefaultCache() {
        if (defaultCache == null) {
            synchronized (this) {
                if (defaultCache == null) {
                    try {
                        logger.info(">>>>> Creating defaultCache");

                        if (cacheManager == null) {
                            logger.error(">>>>> CacheManager is not initialized");
                            throw new IllegalStateException("CacheManager is not initialized");
                        }

                        logger.info(">>>>> Using defaultMaxSize: {}, defaultExpireMs: {}", defaultMaxSize, defaultExpireMs);
                        CacheConfig config = baseConfig(REDIS_KEY_PREFIX_EQP_REVERSE_IGNORE_SIM, defaultMaxSize, defaultExpireMs);
                        if (config == null) {
                            logger.error(">>>>> Failed to create base config for defaultCache");
                            throw new IllegalStateException("Failed to create base config for defaultCache");
                        }

                        String redisUri = buildRedisUri();
                        if (redisUri == null) {
                            logger.error(">>>>> Failed to build Redis URI");
                            throw new IllegalStateException("Failed to build Redis URI");
                        }

                        config.setRedisUri(redisUri);
                        defaultCache = cacheManager.getOrCreateCache("defaultCache", config);
                        logger.info(">>>>> defaultCache created successfully");
                    } catch (UnsupportedEncodingException e) {
                        logger.error(">>>>> Failed to create defaultCache", e);
                        throw new RuntimeException("Failed to create defaultCache", e);
                    }
                }
            }
        }
        return defaultCache;
    }

    // 字符串缓存
    public Cache<String, String> getEqLstTplInfoCache() {
        if (eqLstTplInfoCache == null) {
            synchronized (this) {
                if (eqLstTplInfoCache == null) {
                    try {
                        logger.info(">>>>> Creating eqLstTplInfoCache");

                        if (cacheManager == null) {
                            logger.error(">>>>> CacheManager is not initialized");
                            throw new IllegalStateException("CacheManager is not initialized");
                        }

                        CacheConfig config = baseConfig(REDIS_KEY_PREFIX_EQP_LST_TPL_INFO, 500, TimeUnit.MINUTES.toMillis(10));
                        if (config == null) {
                            logger.error(">>>>> Failed to create base config for eqLstTplInfoCache");
                            throw new IllegalStateException("Failed to create base config for eqLstTplInfoCache");
                        }

                        String redisUri = buildRedisUri();
                        if (redisUri == null) {
                            logger.error(">>>>> Failed to build Redis URI");
                            throw new IllegalStateException("Failed to build Redis URI");
                        }

                        config.setRedisUri(redisUri);
                        eqLstTplInfoCache = cacheManager.getOrCreateCache("eqLstTplInfoCache", config);
                        logger.info(">>>>> eqLstTplInfoCache created successfully");
                    } catch (UnsupportedEncodingException e) {
                        logger.error(">>>>> Failed to create eqLstTplInfoCache", e);
                        throw new RuntimeException("Failed to create eqLstTplInfoCache", e);
                    }
                }
            }
        }
        return eqLstTplInfoCache;
    }

    public Cache<String, String> getEqLstTplCache() {
        if (eqLstTplCache == null) {
            synchronized (this) {
                if (eqLstTplCache == null) {
                    try {
                        logger.info(">>>>> Creating eqLstTplCache");

                        if (cacheManager == null) {
                            logger.error(">>>>> CacheManager is not initialized");
                            throw new IllegalStateException("CacheManager is not initialized");
                        }

                        CacheConfig config = baseConfig(REDIS_KEY_PREFIX_EQP_LST_TPL, 500, TimeUnit.MINUTES.toMillis(10));
                        if (config == null) {
                            logger.error(">>>>> Failed to create base config for eqLstTplCache");
                            throw new IllegalStateException("Failed to create base config for eqLstTplCache");
                        }

                        String redisUri = buildRedisUri();
                        if (redisUri == null) {
                            logger.error(">>>>> Failed to build Redis URI");
                            throw new IllegalStateException("Failed to build Redis URI");
                        }

                        config.setRedisUri(redisUri);
                        eqLstTplCache = cacheManager.getOrCreateCache("eqLstTplCache", config);
                        logger.info(">>>>> eqLstTplCache created successfully");
                    } catch (UnsupportedEncodingException e) {
                        logger.error(">>>>> Failed to create eqLstTplCache", e);
                        throw new RuntimeException("Failed to create eqLstTplCache", e);
                    }
                }
            }
        }
        return eqLstTplCache;
    }

    // 对象缓存
    public Cache<String, EqLstTplInfoDO> getEqLstTplInfoDOCache() {
        if (eqLstTplInfoDOCache == null) {
            synchronized (this) {
                if (eqLstTplInfoDOCache == null) {
                    try {
                        logger.info(">>>>> Creating eqLstTplInfoDOCache");

                        if (cacheManager == null) {
                            logger.error(">>>>> CacheManager is not initialized");
                            throw new IllegalStateException("CacheManager is not initialized");
                        }

                        CacheConfig config = baseConfig(REDIS_KEY_PREFIX_EQP_LST_TPL_INFO, 500, TimeUnit.MINUTES.toMillis(10));
                        if (config == null) {
                            logger.error(">>>>> Failed to create base config for eqLstTplInfoDOCache");
                            throw new IllegalStateException("Failed to create base config for eqLstTplInfoDOCache");
                        }

                        String redisUri = buildRedisUri();
                        if (redisUri == null) {
                            logger.error(">>>>> Failed to build Redis URI");
                            throw new IllegalStateException("Failed to build Redis URI");
                        }

                        config.setRedisUri(redisUri);
                        eqLstTplInfoDOCache = cacheManager.getOrCreateCache("eqLstTplInfoDOCache", config, String.class, EqLstTplInfoDO.class);
                        logger.info(">>>>> eqLstTplInfoDOCache created successfully");
                    } catch (UnsupportedEncodingException e) {
                        logger.error(">>>>> Failed to create eqLstTplInfoDOCache", e);
                        throw new RuntimeException("Failed to create eqLstTplInfoDOCache", e);
                    }
                }
            }
        }
        return eqLstTplInfoDOCache;
    }

    public Cache<String, EqLstTplDO> getEqLstTplDOCache() {
        if (eqLstTplDOCache == null) {
            synchronized (this) {
                if (eqLstTplDOCache == null) {
                    try {
                        logger.info(">>>>> Creating eqLstTplDOCache");

                        if (cacheManager == null) {
                            logger.error(">>>>> CacheManager is not initialized");
                            throw new IllegalStateException("CacheManager is not initialized");
                        }

                        CacheConfig config = baseConfig(REDIS_KEY_PREFIX_EQP_LST_TPL, 500, TimeUnit.MINUTES.toMillis(10));
                        if (config == null) {
                            logger.error(">>>>> Failed to create base config for eqLstTplDOCache");
                            throw new IllegalStateException("Failed to create base config for eqLstTplDOCache");
                        }

                        String redisUri = buildRedisUri();
                        if (redisUri == null) {
                            logger.error(">>>>> Failed to build Redis URI");
                            throw new IllegalStateException("Failed to build Redis URI");
                        }

                        config.setRedisUri(redisUri);
                        eqLstTplDOCache = cacheManager.getOrCreateCache("eqLstTplDOCache", config, String.class, EqLstTplDO.class);
                        logger.info(">>>>> eqLstTplDOCache created successfully");
                    } catch (UnsupportedEncodingException e) {
                        logger.error(">>>>> Failed to create eqLstTplDOCache", e);
                        throw new RuntimeException(">>>>> Failed to create eqLstTplDOCache", e);
                    }
                }
            }
        }
        return eqLstTplDOCache;
    }

    public Cache<String, byte[]> getEqLstByteCache() {
        if (eqLstByteCache == null) {
            synchronized (this) {
                if (eqLstByteCache == null) {
                    CacheConfig config = baseConfig(REDIS_KEY_PREFIX_EQP_LST_TPL, 500, TimeUnit.MINUTES.toMillis(10));
                    try {
                        config.setRedisUri(buildRedisUri());
                    } catch (UnsupportedEncodingException e) {
                        logger.error(">>>>> Failed to create eqLstByteCache", e);
                        throw new RuntimeException(e);
                    }
                    eqLstByteCache = cacheManager.getOrCreateCache("eqLstByteCache", config, String.class, byte[].class);
                }
            }
        }
        return eqLstByteCache;
    }

    public Cache<String, String> getEqLstByteStringCache() {
        if (eqLstByteStringCache == null) {
            synchronized (this) {
                if (eqLstByteStringCache == null) {
                    try {
                        logger.info(">>>>> Creating eqLstByteStringCache");

                        if (cacheManager == null) {
                            logger.error(">>>>> CacheManager is not initialized");
                            throw new IllegalStateException("CacheManager is not initialized");
                        }

                        CacheConfig config = baseConfig(REDIS_KEY_PREFIX_EQP_LST_TPL, 500, TimeUnit.MINUTES.toMillis(10));
                        if (config == null) {
                            logger.error(">>>>> Failed to create base config for eqLstByteStringCache");
                            throw new IllegalStateException("Failed to create base config for eqLstByteStringCache");
                        }

                        String redisUri = buildRedisUri();
                        if (redisUri == null) {
                            logger.error(">>>>> Failed to build Redis URI");
                            throw new IllegalStateException("Failed to build Redis URI");
                        }

                        config.setRedisUri(redisUri);
                        eqLstByteStringCache = cacheManager.getOrCreateCache("eqLstByteStringCache", config);
                        logger.info(">>>>> eqLstByteStringCache created successfully");
                    } catch (UnsupportedEncodingException e) {
                        logger.error(">>>>> Failed to create eqLstByteStringCache", e);
                        throw new RuntimeException("Failed to create eqLstByteStringCache", e);
                    }
                }
            }
        }
        return eqLstByteStringCache;
    }

    // Getter methods for cache instances
    public Cache<String, String> defaultCache() {
        return getDefaultCache();
    }

    public Cache<String, String> eqLstTplInfo() {
        return getEqLstTplInfoCache();
    }

    public Cache<String, String> eqLstTpl() {
        return getEqLstTplCache();
    }

    // Configuration getters and setters
    public String getRedisClusterNodes() {
        return redisClusterNodes;
    }

    public void setRedisClusterNodes(String redisClusterNodes) {
        this.redisClusterNodes = redisClusterNodes;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public int getDefaultMaxSize() {
        return defaultMaxSize;
    }

    public void setDefaultMaxSize(int defaultMaxSize) {
        this.defaultMaxSize = defaultMaxSize;
    }

    public long getDefaultExpireMs() {
        return defaultExpireMs;
    }

    public void setDefaultExpireMs(long defaultExpireMs) {
        this.defaultExpireMs = defaultExpireMs;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }
}
