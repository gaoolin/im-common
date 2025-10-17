package org.im.config.listener;

import org.im.config.ConfigurationEvent;
import org.im.config.ConfigurationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置变更日志监听器
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/16
 */

public class ConfigurationChangeLogger implements ConfigurationListener {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationChangeLogger.class);

    @Override
    public void onConfigurationChanged(ConfigurationEvent event) {
        logger.info("Configuration changed - Key: {}, Old Value: {}, New Value: {}, Source: {}",
                event.getKey(), event.getOldValue(), event.getNewValue(), event.getSource());
    }
}