package org.im.semiconductor.common.parameter.core;

/**
 * 生命周期参数接口 - 提供参数的初始化和状态管理
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public interface LifecycleParameter extends Parameter {
    /**
     * 检查参数是否已初始化
     */
    boolean isInitialized();

    /**
     * 初始化参数
     */
    void initialize();
}