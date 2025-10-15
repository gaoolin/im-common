package org.im.config.source;

import org.im.config.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 环境变量配置源
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/19 14:25:11
 */
public class EnvironmentVariableConfigSource implements ConfigSource {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentVariableConfigSource.class);

    private static final String SOURCE_NAME = "environment-variables";
    private static final int PRIORITY = 200; // 更低优先级

    @Override
    public String getName() {
        return SOURCE_NAME;
    }

    @Override
    public String getProperty(String key) {
        try {
            // 将点号分隔的键转换为下划线分隔的大写形式
            String envKey = key.replace('.', '_').toUpperCase();
            return System.getenv(envKey);
        } catch (SecurityException e) {
            logger.warn("无法访问环境变量: {}", key, e);
            return null;
        }
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<>();
        try {
            Map<String, String> env = System.getenv();
            for (Map.Entry<String, String> entry : env.entrySet()) {
                // 将下划线分隔的键转换为点号分隔的小写形式
                String key = entry.getKey().toLowerCase().replace('_', '.');
                properties.put(key, entry.getValue());
            }
        } catch (SecurityException e) {
            logger.warn("无法获取环境变量", e);
        }
        return properties;
    }

    @Override
    public Set<String> getPropertyNames() {
        Set<String> propertyNames = new HashSet<>();
        try {
            Set<String> envKeys = System.getenv().keySet();
            for (String envKey : envKeys) {
                String key = envKey.toLowerCase().replace('_', '.');
                propertyNames.add(key);
            }
        } catch (SecurityException e) {
            logger.warn("无法获取环境变量名称", e);
        }
        return propertyNames;
    }

    @Override
    public void refresh() {
        // 环境变量通常不需要刷新
        logger.debug("环境变量配置源不需要刷新");
    }

    @Override
    public boolean isAvailable() {
        return true; // 环境变量总是可用的
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }
}