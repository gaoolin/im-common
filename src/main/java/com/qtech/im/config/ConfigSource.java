package com.qtech.im.config;

import java.util.Map;
import java.util.Set;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/19 14:19:56
 * 配置源接口
 * <p>
 * 定义配置源的基本操作
 */
public interface ConfigSource {

    /**
     * 配置源名称
     *
     * @return 配置源名称
     */
    String getName();

    /**
     * 获取配置属性值
     *
     * @param key 配置键
     * @return 配置值
     */
    String getProperty(String key);

    /**
     * 获取所有配置属性
     *
     * @return 配置属性映射
     */
    Map<String, String> getProperties();

    /**
     * 获取所有配置键
     *
     * @return 配置键集合
     */
    Set<String> getPropertyNames();

    /**
     * 刷新配置源
     */
    void refresh();

    /**
     * 检查配置源是否可用
     *
     * @return 是否可用
     */
    boolean isAvailable();

    /**
     * 获取配置源优先级
     *
     * @return 优先级（数值越小优先级越高）
     */
    int getPriority();
}
