package org.im.cache.support;

import java.io.Serializable;
import java.util.Objects;

/**
 * 空值标记类
 * <p>
 * 用于缓存穿透保护，区分"键不存在"和"键存在但值为null"两种情况
 * 用于标记缓存中的空值，防止缓存穿透
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/27
 */
public final class NullValueMarker implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 单例实例
     */
    private static final NullValueMarker INSTANCE = new NullValueMarker();

    /**
     * 私有构造函数，防止外部实例化
     */
    private NullValueMarker() {
    }

    /**
     * 获取单例实例
     *
     * @return 单例实例
     */
    public static NullValueMarker getInstance() {
        return INSTANCE;
    }

    /**
     * 检查对象是否为空值标记
     *
     * @param obj 要检查的对象
     * @return 如果是空值标记返回true，否则返回false
     */
    public static boolean isNullValue(Object obj) {
        // 由于是单例，只需检查类型
        return obj instanceof NullValueMarker;
    }

    /**
     * 返回哈希码
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        // 所有实例返回相同的哈希码
        return Objects.hash("NULL_VALUE_MARKER");
    }

    /**
     * 检查对象是否相等
     *
     * @param obj 要比较的对象
     * @return 如果相等返回true，否则返回false
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof NullValueMarker;
    }

    /**
     * 返回字符串表示
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "NullValueMarker{singleton}";
    }

    /**
     * 防止反序列化创建新实例
     *
     * @return 单例实例
     */
    private Object readResolve() {
        return INSTANCE;
    }
}