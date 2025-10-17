package com.im.qtech.service.config.thread;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 线程池配置映射类 - 增强版
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/04/25 10:48:54
 */
@Getter
@Configuration
@ConfigurationProperties(prefix = "thread-pool")
public class ThreadPoolProperties {

    private PoolConfig important;
    private PoolConfig normal;
    private PoolConfig low;
    private VirtualThreadConfig virtual = new VirtualThreadConfig();

    public void setImportant(PoolConfig important) {
        this.important = important;
    }

    public void setNormal(PoolConfig normal) {
        this.normal = normal;
    }

    public void setLow(PoolConfig low) {
        this.low = low;
    }

    public void setVirtual(VirtualThreadConfig virtual) {
        this.virtual = virtual;
    }

    @Getter
    public static class PoolConfig {
        private int corePoolSize = 5;
        private int maxPoolSize = 10;
        private int queueCapacity = 100;
        private int keepAliveSeconds = 60;
        private String threadNamePrefix = "task";

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public void setMaxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }

        public void setKeepAliveSeconds(int keepAliveSeconds) {
            this.keepAliveSeconds = keepAliveSeconds;
        }

        public void setThreadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }
    }

    @Getter
    public static class VirtualThreadConfig {
        private boolean enabled = false;
        private int permits = 100;
        private String threadNamePrefix = "virtual";

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void setPermits(int permits) {
            this.permits = permits;
        }

        public void setThreadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }
    }
}
