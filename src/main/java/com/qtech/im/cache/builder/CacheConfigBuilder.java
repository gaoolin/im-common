package com.qtech.im.cache.builder;

import com.qtech.im.cache.CacheConfig;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * since  :  2025/08/27
 */
public class CacheConfigBuilder {
    private CacheConfig config;

    private CacheConfigBuilder() {
        this.config = new CacheConfig();
    }

    public static CacheConfigBuilder newBuilder() {
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

    public CacheConfig build() {
        return config;
    }
}
