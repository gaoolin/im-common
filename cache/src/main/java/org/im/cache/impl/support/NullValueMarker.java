package org.im.cache.impl.support;

import java.io.Serializable;
import java.util.Objects;

/**
 * 空值标记类
 * <p>
 * 用于缓存穿透保护，区分"键不存在"和"键存在但值为null"两种情况
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/27
 */
public final class NullValueMarker implements Serializable {
    /**
     * 全局单例实例
     */
    public static final NullValueMarker INSTANCE = new NullValueMarker();
    private static final long serialVersionUID = 1L;

    /**
     * 私有构造函数，防止外部实例化
     */
    private NullValueMarker() {
        // 私有构造函数确保单例
    }

    /**
     * 获取空值标记实例
     *
     * @return 空值标记实例
     */
    public static NullValueMarker getInstance() {
        return INSTANCE;
    }

    /**
     * 检查对象是否为空值标记
     *
     * @param obj 待检查对象
     * @return 如果是空值标记返回true，否则返回false
     */
    public static boolean isNullValueMarker(Object obj) {
        return obj instanceof NullValueMarker;
    }

    @Override
    public boolean equals(Object obj) {
        // 由于是单例，只需检查类型
        return obj instanceof NullValueMarker;
    }

    @Override
    public int hashCode() {
        // 所有实例返回相同的哈希码
        return Objects.hash("NULL_VALUE_MARKER");
    }

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
