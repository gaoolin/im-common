package org.im.config;

/**
 * 配置监听器接口
 * <p>
 * 监听配置变化事件
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/19 14:21:54
 */
public interface ConfigurationListener {

    /**
     * 配置变更事件处理
     *
     * @param event 配置变更事件
     */
    void onConfigurationChanged(ConfigurationEvent event);
}