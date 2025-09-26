package org.im.semiconductor.common.parameter.core;


import org.im.semiconductor.common.security.AccessLevel;

/**
 * 访问控制参数接口 - 提供参数的访问权限控制
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public interface AccessControlParameter extends Parameter {
    /**
     * 检查参数是否为只读
     */
    boolean isReadOnly();

    void setReadOnly(boolean readOnly);

    /**
     * 获取参数的访问权限
     */
    AccessLevel getAccessLevel();

    void setAccessLevel(AccessLevel accessLevel);
}