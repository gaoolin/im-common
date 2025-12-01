package com.qtech.msg.config.thread;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 配置映射类
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

    public void setImportant(PoolConfig important) {
        this.important = important;
    }

    public void setNormal(PoolConfig normal) {
        this.normal = normal;
    }

    public void setLow(PoolConfig low) {
        this.low = low;
    }

    @Getter
    public static class PoolConfig {
        private int corePoolSize;
        private int maxPoolSize;
        private int queueCapacity;
        private int keepAliveSeconds;
        private String threadNamePrefix;

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
}

