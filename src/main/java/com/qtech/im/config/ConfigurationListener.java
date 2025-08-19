package com.qtech.im.config;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/19 14:21:54
 * desc   :
 * 配置监听器接口
 <p>
 * 监听配置变化事件
 */
public interface ConfigurationListener {

    /**
     * 配置变更事件处理
     *
     * @param event 配置变更事件
     */
    void onConfigurationChanged(ConfigurationEvent event);
}