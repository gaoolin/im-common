package com.im.aa.inspection.config;

import org.im.config.ConfigurationManager;
import org.im.config.impl.DefaultConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/25
 */

/**
 * 配置管理器
 */
public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);

    public static ConfigurationManager initialize() {
        // 直接使用默认配置管理器，它会自动加载 classpath 下的 application.properties
        DefaultConfigurationManager configManager = new DefaultConfigurationManager();

        // 如果需要添加额外的配置源，可以在这里添加
        // 但不要重复添加已经存在的配置源
        // 只在需要额外配置源时添加
        // try {
        // 使用完整路径或确保文件在正确位置
        //     configManager.addConfigSource(new PropertiesFileConfigSource("config/application.properties"));
        // } catch (Exception e) {
        //     logger.warn("添加额外配置源时出错", e);
        // }

        return configManager;
    }
}
