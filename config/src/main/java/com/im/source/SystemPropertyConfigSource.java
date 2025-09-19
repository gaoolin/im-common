package com.im.source;

import com.im.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/19 14:23:24
 * desc   :  系统属性配置源
 */
public class SystemPropertyConfigSource implements ConfigSource {

    private static final Logger logger = LoggerFactory.getLogger(SystemPropertyConfigSource.class);

    private static final String SOURCE_NAME = "system-properties";
    private static final int PRIORITY = 100; // 较低优先级

    @Override
    public String getName() {
        return SOURCE_NAME;
    }

    @Override
    public String getProperty(String key) {
        try {
            return System.getProperty(key);
        } catch (SecurityException e) {
            logger.warn("无法访问系统属性: {}", key, e);
            return null;
        }
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<>();
        try {
            Properties systemProperties = System.getProperties();
            for (String key : systemProperties.stringPropertyNames()) {
                properties.put(key, systemProperties.getProperty(key));
            }
        } catch (SecurityException e) {
            logger.warn("无法获取系统属性", e);
        }
        return properties;
    }

    @Override
    public Set<String> getPropertyNames() {
        Set<String> propertyNames = new HashSet<>();
        try {
            propertyNames.addAll(System.getProperties().stringPropertyNames());
        } catch (SecurityException e) {
            logger.warn("无法获取系统属性名称", e);
        }
        return propertyNames;
    }

    @Override
    public void refresh() {
        // 系统属性通常不需要刷新
        logger.debug("系统属性配置源不需要刷新");
    }

    @Override
    public boolean isAvailable() {
        return true; // 系统属性总是可用的
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }
}