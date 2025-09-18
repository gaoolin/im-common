package com.qtech.im.config;

import java.util.List;
import java.util.Map;

/**
 * 配置管理器接口
 * <p>
 * 负责管理配置源、配置属性和配置更新
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/19 14:20:48
 */
public interface ConfigurationManager {
    /**
     * 获取配置属性值
     *
     * @param key 配置键
     * @return 配置值，如果不存在返回null
     */
    String getProperty(String key);

    /**
     * 获取配置属性值，支持默认值
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 配置值，如果不存在返回默认值
     */
    String getProperty(String key, String defaultValue);

    /**
     * 获取整型配置属性值
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 整型配置值
     */
    int getIntProperty(String key, int defaultValue);

    /**
     * 获取布尔型配置属性值
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 布尔型配置值
     */
    boolean getBooleanProperty(String key, boolean defaultValue);

    /**
     * 重新加载配置
     */
    void reloadConfiguration();

    /**
     * 设置激活的配置环境
     *
     * @param profile 环境标识（如：dev、test、prod）
     */
    void setActiveProfile(String profile);

    /**
     * 获取字符串类型配置值
     *
     * @param key 配置键
     * @return 配置值
     */
    String getString(String key);

    /**
     * 获取字符串类型配置值（带默认值）
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getString(String key, String defaultValue);

    /**
     * 获取整数类型配置值
     *
     * @param key 配置键
     * @return 配置值
     */
    Integer getInteger(String key);

    /**
     * 获取整数类型配置值（带默认值）
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    Integer getInteger(String key, Integer defaultValue);

    /**
     * 获取长整数类型配置值
     *
     * @param key 配置键
     * @return 配置值
     */
    Long getLong(String key);

    /**
     * 获取长整数类型配置值（带默认值）
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    Long getLong(String key, Long defaultValue);

    /**
     * 获取布尔类型配置值
     *
     * @param key 配置键
     * @return 配置值
     */
    Boolean getBoolean(String key);

    /**
     * 获取布尔类型配置值（带默认值）
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    Boolean getBoolean(String key, Boolean defaultValue);

    /**
     * 获取Double类型配置值
     *
     * @param key 配置键
     * @return 配置值
     */
    Double getDouble(String key);

    /**
     * 获取Double类型配置值（带默认值）
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    Double getDouble(String key, Double defaultValue);

    /**
     * 获取配置值（泛型）
     *
     * @param key  配置键
     * @param type 类型
     * @param <T>  泛型类型
     * @return 配置值
     */
    <T> T getValue(String key, Class<T> type);

    /**
     * 获取配置值（泛型，带默认值）
     *
     * @param key          配置键
     * @param type         类型
     * @param defaultValue 默认值
     * @param <T>          泛型类型
     * @return 配置值
     */
    <T> T getValue(String key, Class<T> type, T defaultValue);

    /**
     * 获取所有配置属性
     *
     * @return 配置属性映射
     */
    Map<String, String> getAllProperties();

    /**
     * 添加配置源
     *
     * @param source 配置源
     */
    void addConfigSource(ConfigSource source);

    /**
     * 移除配置源
     *
     * @param sourceName 配置源名称
     */
    void removeConfigSource(String sourceName);

    /**
     * 获取所有配置源
     *
     * @return 配置源列表
     */
    List<ConfigSource> getConfigSources();

    /**
     * 刷新所有配置源
     */
    void refresh();

    /**
     * 添加配置监听器
     *
     * @param listener 配置监听器
     */
    void addConfigurationListener(ConfigurationListener listener);

    /**
     * 移除配置监听器
     *
     * @param listener 配置监听器
     */
    void removeConfigurationListener(ConfigurationListener listener);
}