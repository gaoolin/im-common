package com.im.inspection.config;

import org.im.config.ConfigurationManager;
import org.im.config.impl.DefaultConfigurationManager;

/**
 * 应用统一配置管理器
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/16
 */

public class DppConfigManager {
    private static final Object lock = new Object();
    private static volatile ConfigurationManager instance;

    public static ConfigurationManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new DefaultConfigurationManager();
                }
            }
        }
        return instance;
    }
}