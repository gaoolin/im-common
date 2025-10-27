package org.im.common.thread.config;

import org.im.config.ConfigurationManager;
import org.im.config.impl.DefaultConfigurationManager;

/**
 * 线程池框架配置属性
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/24
 */

public class ThreadPoolFrameworkProperties {

    private final ConfigurationManager configManager;
    private boolean virtualThreadEnabled;
    private VirtualThreadConfig virtual = new VirtualThreadConfig();
    private PoolConfig important = new PoolConfig();
    private PoolConfig normal = new PoolConfig();
    private PoolConfig low = new PoolConfig();
    private MonitorConfig monitor = new MonitorConfig();

    public ThreadPoolFrameworkProperties() {
        this.configManager = new DefaultConfigurationManager();
        loadConfiguration();
    }

    public ThreadPoolFrameworkProperties(ConfigurationManager configManager) {
        this.configManager = configManager;
        loadConfiguration();
    }

    private void loadConfiguration() {
        // 虚拟线程配置
        virtualThreadEnabled = configManager.getBoolean("thread.pool.virtual.enabled", virtualThreadEnabled);

        // 虚拟线程详细配置
        virtual.setMaxConcurrency(configManager.getIntProperty("thread.pool.virtual.max.concurrency", virtual.getMaxConcurrency()));
        virtual.setThreadNamePrefix(configManager.getString("thread.pool.virtual.thread.name.prefix", virtual.getThreadNamePrefix()));

        // 重要线程池配置
        important.setCorePoolSize(configManager.getIntProperty("thread.pool.important.core.size", important.getCorePoolSize()));
        important.setMaxPoolSize(configManager.getIntProperty("thread.pool.important.max.size", important.getMaxPoolSize()));
        important.setQueueCapacity(configManager.getIntProperty("thread.pool.important.queue.capacity", important.getQueueCapacity()));
        important.setKeepAliveSeconds(configManager.getIntProperty("thread.pool.important.keep.alive.seconds", important.getKeepAliveSeconds()));
        important.setThreadNamePrefix(configManager.getString("thread.pool.important.thread.name.prefix", important.getThreadNamePrefix()));

        // 普通线程池配置
        normal.setCorePoolSize(configManager.getIntProperty("thread.pool.normal.core.size", normal.getCorePoolSize()));
        normal.setMaxPoolSize(configManager.getIntProperty("thread.pool.normal.max.size", normal.getMaxPoolSize()));
        normal.setQueueCapacity(configManager.getIntProperty("thread.pool.normal.queue.capacity", normal.getQueueCapacity()));
        normal.setKeepAliveSeconds(configManager.getIntProperty("thread.pool.normal.keep.alive.seconds", normal.getKeepAliveSeconds()));
        normal.setThreadNamePrefix(configManager.getString("thread.pool.normal.thread.name.prefix", normal.getThreadNamePrefix()));

        // 低优先级线程池配置
        low.setCorePoolSize(configManager.getIntProperty("thread.pool.low.core.size", low.getCorePoolSize()));
        low.setMaxPoolSize(configManager.getIntProperty("thread.pool.low.max.size", low.getMaxPoolSize()));
        low.setQueueCapacity(configManager.getIntProperty("thread.pool.low.queue.capacity", low.getQueueCapacity()));
        low.setKeepAliveSeconds(configManager.getIntProperty("thread.pool.low.keep.alive.seconds", low.getKeepAliveSeconds()));
        low.setThreadNamePrefix(configManager.getString("thread.pool.low.thread.name.prefix", low.getThreadNamePrefix()));

        // 监控配置
        monitor.setEnabled(configManager.getBoolean("thread.pool.monitor.enabled", monitor.isEnabled()));
        monitor.setLogIntervalSeconds(configManager.getLong("thread.pool.monitor.log.interval.seconds", monitor.getLogIntervalSeconds()));
    }

    // Getters and setters
    public boolean isVirtualThreadEnabled() {
        return virtualThreadEnabled;
    }

    public void setVirtualThreadEnabled(boolean virtualThreadEnabled) {
        this.virtualThreadEnabled = virtualThreadEnabled;
    }

    public VirtualThreadConfig getVirtual() {
        return virtual;
    }

    public void setVirtual(VirtualThreadConfig virtual) {
        this.virtual = virtual;
    }

    public PoolConfig getImportant() {
        return important;
    }

    public void setImportant(PoolConfig important) {
        this.important = important;
    }

    public PoolConfig getNormal() {
        return normal;
    }

    public void setNormal(PoolConfig normal) {
        this.normal = normal;
    }

    public PoolConfig getLow() {
        return low;
    }

    public void setLow(PoolConfig low) {
        this.low = low;
    }

    public MonitorConfig getMonitor() {
        return monitor;
    }

    public void setMonitor(MonitorConfig monitor) {
        this.monitor = monitor;
    }

    public static class VirtualThreadConfig {
        private int maxConcurrency = 100;
        private String threadNamePrefix = "vt-";

        // Getters and setters
        public int getMaxConcurrency() {
            return maxConcurrency;
        }

        public void setMaxConcurrency(int maxConcurrency) {
            this.maxConcurrency = maxConcurrency;
        }

        public String getThreadNamePrefix() {
            return threadNamePrefix;
        }

        public void setThreadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }
    }

    public static class PoolConfig {
        private int corePoolSize = 5;
        private int maxPoolSize = 10;
        private int queueCapacity = 100;
        private int keepAliveSeconds = 60;
        private String threadNamePrefix = "tp-";

        // Getters and setters
        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }

        public int getKeepAliveSeconds() {
            return keepAliveSeconds;
        }

        public void setKeepAliveSeconds(int keepAliveSeconds) {
            this.keepAliveSeconds = keepAliveSeconds;
        }

        public String getThreadNamePrefix() {
            return threadNamePrefix;
        }

        public void setThreadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }
    }

    public static class MonitorConfig {
        private boolean enabled = true;
        private long logIntervalSeconds = 300;

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getLogIntervalSeconds() {
            return logIntervalSeconds;
        }

        public void setLogIntervalSeconds(long logIntervalSeconds) {
            this.logIntervalSeconds = logIntervalSeconds;
        }
    }
}