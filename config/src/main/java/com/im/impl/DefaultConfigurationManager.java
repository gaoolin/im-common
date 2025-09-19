package com.im.impl;

import com.im.ConfigSource;
import com.im.ConfigurationListener;
import com.im.ConfigurationManager;
import com.im.source.SystemPropertyConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 默认配置管理器实现
 * <p>
 * 职责：
 * 1. 加载和管理配置属性
 * 2. 支持多环境配置
 * 3. 提供配置热更新能力
 * 4. 配置项验证和默认值处理
 * <p>
 * 特性：
 * - 线程安全的配置访问
 * - 支持多种配置源（properties、yaml、环境变量等）
 * - 配置变更监听机制
 * - 配置项缓存优化
 * <p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/08/19
 */
public class DefaultConfigurationManager implements ConfigurationManager {
    private static final Logger logger = LoggerFactory.getLogger(DefaultConfigurationManager.class);

    // 配置文件名称
    private static final String DEFAULT_CONFIG_FILE = "application.properties";

    // 配置属性缓存
    private final ConcurrentHashMap<String, String> configCache = new ConcurrentHashMap<>();

    // 配置源列表
    private final List<ConfigSource> configSources = new CopyOnWriteArrayList<>();

    // 配置监听器列表
    private final List<ConfigurationListener> listeners = new CopyOnWriteArrayList<>();

    // 环境标识
    private String activeProfile = "default";

    public DefaultConfigurationManager() {
        initializeDefaultConfigSources();
        loadConfiguration();
    }

    public DefaultConfigurationManager(String profile) {
        this.activeProfile = profile;
        initializeDefaultConfigSources();
        loadConfiguration();
    }

    /**
     * 初始化默认配置源
     */
    private void initializeDefaultConfigSources() {
        // 添加系统属性配置源
        configSources.add(new SystemPropertyConfigSource());

        // 添加默认的properties文件配置源
        addDefaultPropertiesConfigSource();
    }

    /**
     * 添加默认properties配置源
     */
    private void addDefaultPropertiesConfigSource() {
        try {
            String configFile = DEFAULT_CONFIG_FILE;
            if (!"default".equals(activeProfile)) {
                configFile = "application-" + activeProfile + ".properties";
            }

            // 检查资源是否存在
            InputStream input = getClass().getClassLoader().getResourceAsStream(configFile);
            if (input != null) {
                String finalConfigFile = configFile;
                ConfigSource defaultSource = new ConfigSource() {
                    private final Map<String, String> properties = new ConcurrentHashMap<>();

                    {
                        loadProperties();
                    }

                    private void loadProperties() {
                        properties.clear();
                        try (InputStream in = getClass().getClassLoader().getResourceAsStream(finalConfigFile)) {
                            if (in != null) {
                                Properties props = new Properties();
                                props.load(in);
                                props.forEach((key, value) -> properties.put((String) key, (String) value));
                            }
                        } catch (IOException e) {
                            logger.error("Failed to load configuration from classpath: {}", finalConfigFile, e);
                        }
                    }

                    @Override
                    public String getName() {
                        return "default-properties-" + activeProfile;
                    }

                    @Override
                    public String getProperty(String key) {
                        return properties.get(key);
                    }

                    @Override
                    public Map<String, String> getProperties() {
                        return new ConcurrentHashMap<>(properties);
                    }

                    @Override
                    public Set<String> getPropertyNames() {
                        return new HashSet<>(properties.keySet());
                    }

                    @Override
                    public void refresh() {
                        loadProperties();
                    }

                    @Override
                    public boolean isAvailable() {
                        return getClass().getClassLoader().getResourceAsStream(finalConfigFile) != null;
                    }

                    @Override
                    public int getPriority() {
                        return 50;
                    }
                };

                configSources.add(defaultSource);
                logger.info("Added default properties config source: {}", configFile);
            } else {
                logger.warn("Default configuration file not found in classpath: {}", configFile);
            }
        } catch (Exception e) {
            logger.error("Failed to initialize default properties config source", e);
        }
    }

    @Override
    public String getProperty(String key) {
        return configCache.get(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return configCache.getOrDefault(key, defaultValue);
    }

    @Override
    public int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for key: {}", key);
            }
        }
        return defaultValue;
    }

    @Override
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    @Override
    public void reloadConfiguration() {
        loadConfiguration();
        notifyListeners();
    }

    @Override
    public void setActiveProfile(String profile) {
        this.activeProfile = profile;
        // 重新初始化配置源以加载对应环境的配置文件
        configSources.clear();
        listeners.clear();
        initializeDefaultConfigSources();
        reloadConfiguration();
    }

    @Override
    public String getString(String key) {
        return getProperty(key);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return getProperty(key, defaultValue);
    }

    @Override
    public Integer getInteger(String key) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for key: {}", key);
            }
        }
        return null;
    }

    @Override
    public Integer getInteger(String key, Integer defaultValue) {
        Integer value = getInteger(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public Long getLong(String key) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Long.valueOf(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid long value for key: {}", key);
            }
        }
        return null;
    }

    @Override
    public Long getLong(String key, Long defaultValue) {
        Long value = getLong(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public Boolean getBoolean(String key) {
        String value = getProperty(key);
        if (value != null) {
            return Boolean.valueOf(value);
        }
        return null;
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue) {
        Boolean value = getBoolean(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public Double getDouble(String key) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Double.valueOf(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid double value for key: {}", key);
            }
        }
        return null;
    }

    @Override
    public Double getDouble(String key, Double defaultValue) {
        Double value = getDouble(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public <T> T getValue(String key, Class<T> type) {
        String value = getProperty(key);
        if (value != null) {
            try {
                if (type == String.class) {
                    return type.cast(value);
                } else if (type == Integer.class) {
                    return type.cast(Integer.valueOf(value));
                } else if (type == Long.class) {
                    return type.cast(Long.valueOf(value));
                } else if (type == Boolean.class) {
                    return type.cast(Boolean.valueOf(value));
                } else if (type == Double.class) {
                    return type.cast(Double.valueOf(value));
                }
                // 可以添加更多类型转换
            } catch (Exception e) {
                logger.warn("Failed to convert value for key: {} to type: {}", key, type.getSimpleName());
            }
        }
        return null;
    }

    @Override
    public <T> T getValue(String key, Class<T> type, T defaultValue) {
        T value = getValue(key, type);
        return value != null ? value : defaultValue;
    }

    @Override
    public Map<String, String> getAllProperties() {
        return new ConcurrentHashMap<>(configCache);
    }

    @Override
    public void addConfigSource(ConfigSource source) {
        if (source != null) {
            configSources.add(source);
            // 根据优先级排序
            Collections.sort(configSources, (s1, s2) -> Integer.compare(s2.getPriority(), s1.getPriority()));
            reloadConfiguration();
        }
    }

    @Override
    public void removeConfigSource(String sourceName) {
        configSources.removeIf(source -> source.getName().equals(sourceName));
        reloadConfiguration();
    }

    @Override
    public List<ConfigSource> getConfigSources() {
        return new ArrayList<>(configSources);
    }

    @Override
    public void refresh() {
        // 刷新所有配置源
        for (ConfigSource source : configSources) {
            try {
                source.refresh();
            } catch (Exception e) {
                logger.warn("Failed to refresh config source: {}", source.getName(), e);
            }
        }
        reloadConfiguration();
    }

    @Override
    public void addConfigurationListener(ConfigurationListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeConfigurationListener(ConfigurationListener listener) {
        listeners.remove(listener);
    }

    /**
     * 加载所有配置源的配置
     */
    private void loadConfiguration() {
        configCache.clear();

        // 按优先级顺序加载所有配置源（优先级高的后加载，可以覆盖优先级低的配置）
        List<ConfigSource> sortedSources = new ArrayList<>(configSources);
        sortedSources.sort((s1, s2) -> Integer.compare(s2.getPriority(), s1.getPriority()));

        for (ConfigSource source : sortedSources) {
            if (source.isAvailable()) {
                try {
                    Map<String, String> properties = source.getProperties();
                    if (properties != null) {
                        configCache.putAll(properties);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to load configuration from source: {}", source.getName(), e);
                }
            }
        }

        logger.info("Configuration loaded from {} sources", sortedSources.size());
    }

    /**
     * 通知所有监听器配置已变更
     */
    private void notifyListeners() {
        // 注意：由于ConfigurationListener接口有变化，这里暂时不实现具体通知逻辑
        // 需要等待ConfigurationEvent类的实现
        for (ConfigurationListener listener : listeners) {
            try {
                // 这里需要修改，因为接口方法签名已更改
                // listener.onConfigurationChanged(event);
                logger.debug("Configuration changed, notifying listener: {}", listener.getClass().getSimpleName());
            } catch (Exception e) {
                logger.warn("Error notifying configuration listener", e);
            }
        }
    }
}
