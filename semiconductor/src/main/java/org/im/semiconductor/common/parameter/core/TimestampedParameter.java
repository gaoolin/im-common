package org.im.semiconductor.common.parameter.core;

/**
 * 时间戳参数接口 - 提供参数的创建和更新时间
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */

public interface TimestampedParameter extends Parameter {
    /**
     * 获取参数的创建时间
     */
    long getCreateTime();

    void setCreateTime(long createTime);

    /**
     * 获取参数的更新时间
     */
    long getUpdateTime();

    void setUpdateTime(long updateTime);
}
