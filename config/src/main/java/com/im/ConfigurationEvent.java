package com.im;

/**
 * 配置变更事件
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/19 14:22:32
 */
public class ConfigurationEvent {

    private final String key;
    private final String oldValue;
    private final String newValue;
    private final long timestamp;
    private final String source;

    public ConfigurationEvent(String key, String oldValue, String newValue, String source) {
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.source = source;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getKey() {
        return key;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "ConfigurationEvent{" +
                "key='" + key + '\'' +
                ", oldValue='" + oldValue + '\'' +
                ", newValue='" + newValue + '\'' +
                ", source='" + source + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
