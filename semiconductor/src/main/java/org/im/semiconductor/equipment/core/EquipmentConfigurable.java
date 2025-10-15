package org.im.semiconductor.equipment.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备配置接口
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */
public interface EquipmentConfigurable {
    EquipmentConfiguration getConfiguration();

    void setConfiguration(EquipmentConfiguration config);

    class EquipmentConfiguration {
        private final Map<String, Object> properties = new ConcurrentHashMap<>();

        public void setProperty(String key, Object value) {
            properties.put(key, value);
        }

        public <T> T getProperty(String key, T defaultValue) {
            return (T) properties.getOrDefault(key, defaultValue);
        }
    }
}