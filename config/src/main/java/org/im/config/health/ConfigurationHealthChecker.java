package org.im.config.health;

import org.im.config.ConfigSource;
import org.im.config.ConfigurationManager;

import java.util.List;

/**
 * 配置健康检查器
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/16
 */

public class ConfigurationHealthChecker {

    public static boolean isHealthy(ConfigurationManager configManager) {
        try {
            // 检查配置管理器是否能正常工作
            configManager.getAllProperties();

            // 检查所有配置源是否可用
            List<ConfigSource> sources = configManager.getConfigSources();
            for (ConfigSource source : sources) {
                if (!source.isAvailable()) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}