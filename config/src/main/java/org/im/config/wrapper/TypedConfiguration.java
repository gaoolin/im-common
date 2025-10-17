package org.im.config.wrapper;

import org.im.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 类型安全的配置访问包装器
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/16
 */
public class TypedConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(TypedConfiguration.class);
    private final ConfigurationManager configManager;

    public TypedConfiguration(ConfigurationManager configManager) {
        this.configManager = configManager;
    }

    public String getRequiredString(String key) {
        String value = configManager.getString(key);
        if (value == null) {
            throw new IllegalStateException("Required configuration missing: " + key);
        }
        return value;
    }

    public int getRequiredInt(String key) {
        Integer value = configManager.getInteger(key);
        if (value == null) {
            throw new IllegalStateException("Required configuration missing: " + key);
        }
        return value;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return configManager.getBoolean(key, defaultValue);
    }
}