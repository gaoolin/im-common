package org.im.semiconductor.common.parameter.core;

/**
 * 标识性参数接口 - 提供参数的唯一标识和版本控制
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */

public interface IdentifiableParameter extends Parameter {
    /**
     * 获取参数的唯一标识符
     */
    String getUniqueId();

    void setUniqueId(String uniqueId);

    /**
     * 获取参数的版本号
     */
    String getVersion();

    void setVersion(String version);
}