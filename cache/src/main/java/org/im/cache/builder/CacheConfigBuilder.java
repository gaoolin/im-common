package org.im.cache.builder;


import org.im.cache.config.CacheConfig;
import org.im.cache.impl.support.BackendType;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/27
 */
public class CacheConfigBuilder {
    private final CacheConfig config;

    private CacheConfigBuilder() {
        this.config = new CacheConfig();
    }

    public static CacheConfigBuilder create() {
        return new CacheConfigBuilder();
    }

    public CacheConfigBuilder withName(String name) {
        config.setName(name);
        return this;
    }

    public CacheConfigBuilder withMaximumSize(int maximumSize) {
        config.setMaximumSize(maximumSize);
        return this;
    }

    public CacheConfigBuilder withExpireAfterWrite(long expireAfterWrite) {
        config.setExpireAfterWrite(expireAfterWrite);
        return this;
    }

    public CacheConfigBuilder withExpireAfterAccess(long expireAfterAccess) {
        config.setExpireAfterAccess(expireAfterAccess);
        return this;
    }

    public CacheConfigBuilder withStatsEnabled(boolean recordStats) {
        config.setRecordStats(recordStats);
        return this;
    }

    public CacheConfigBuilder withNullValueProtection(boolean enable) {
        config.setEnableNullValueProtection(enable);
        return this;
    }

    public CacheConfigBuilder withBreakdownProtection(boolean enable) {
        config.setEnableBreakdownProtection(enable);
        return this;
    }

    public CacheConfigBuilder withAvalancheProtection(boolean enable) {
        config.setEnableAvalancheProtection(enable);
        return this;
    }

    public CacheConfigBuilder withBackendType(BackendType backendType) {
        config.setBackendType(backendType);
        return this;
    }

    public CacheConfig build() {
        return config;
    }
}