package org.im.common.lifecycle;

/**
 * 通用生命周期管理接口
 * 定义组件生命周期的基本操作
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/29
 */

public interface Lifecycle {
    /**
     * 启动组件
     */
    void start();

    /**
     * 停止组件
     */
    void stop();

    /**
     * 检查组件是否正在运行
     */
    boolean isRunning();

    /**
     * 重启组件
     */
    void restart();
}
