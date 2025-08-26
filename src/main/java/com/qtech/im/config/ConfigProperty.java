package com.qtech.im.config;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/19 14:19:02
 * 配置属性接口
 * <p>
 * 定义配置属性的基本操作
 */
public interface ConfigProperty<T> {

    /**
     * 获取属性名称
     *
     * @return 属性名称
     */
    String getName();

    /**
     * 获取属性值
     *
     * @return 属性值
     */
    T getValue();

    /**
     * 获取默认值
     *
     * @return 默认值
     */
    T getDefaultValue();

    /**
     * 获取属性描述
     *
     * @return 属性描述
     */
    String getDescription();

    /**
     * 验证属性值是否有效
     *
     * @param value 待验证的值
     * @return 是否有效
     */
    boolean isValid(T value);
}