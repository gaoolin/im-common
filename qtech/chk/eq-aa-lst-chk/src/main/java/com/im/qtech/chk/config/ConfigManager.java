package com.im.qtech.chk.config;

import org.im.config.ConfigurationManager;
import org.im.config.impl.DefaultConfigurationManager;
import org.im.config.source.PropertiesFileConfigSource;
import org.im.config.source.SystemPropertyConfigSource;
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
        DefaultConfigurationManager configManager = new DefaultConfigurationManager();

        // 添加配置源
        try {
            configManager.addConfigSource(new PropertiesFileConfigSource("application.properties"));
            configManager.addConfigSource(new SystemPropertyConfigSource());
        } catch (Exception e) {
            logger.warn("添加配置源时出错", e);
        }

        return configManager;
    }
}