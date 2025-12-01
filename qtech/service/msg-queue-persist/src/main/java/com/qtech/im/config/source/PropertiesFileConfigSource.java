package com.qtech.im.config.source;

import com.qtech.im.config.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/19 14:26:39
 * desc   :  Properties文件配置源
 */
public class PropertiesFileConfigSource implements ConfigSource {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesFileConfigSource.class);

    private final String sourceName;
    private final Path filePath;
    private final int priority;
    private final Map<String, String> properties = new ConcurrentHashMap<>();
    private volatile long lastModified = 0;

    public PropertiesFileConfigSource(String filePath) {
        this(filePath, 50); // 默认优先级
    }

    public PropertiesFileConfigSource(String filePath, int priority) {
        this.filePath = Paths.get(filePath);
        this.sourceName = "properties-file:" + filePath;
        this.priority = priority;
        loadProperties();
    }

    @Override
    public String getName() {
        return sourceName;
    }

    @Override
    public String getProperty(String key) {
        checkAndReloadIfNecessary();
        return properties.get(key);
    }

    @Override
    public Map<String, String> getProperties() {
        checkAndReloadIfNecessary();
        return new HashMap<>(properties);
    }

    @Override
    public Set<String> getPropertyNames() {
        checkAndReloadIfNecessary();
        return new HashSet<>(properties.keySet());
    }

    @Override
    public void refresh() {
        loadProperties();
    }

    @Override
    public boolean isAvailable() {
        return Files.exists(filePath) && Files.isReadable(filePath);
    }

    @Override
    public int getPriority() {
        return priority;
    }

    private void loadProperties() {
        if (!isAvailable()) {
            logger.warn("配置文件不存在或不可读: {}", filePath);
            return;
        }

        try {
            lastModified = Files.getLastModifiedTime(filePath).toMillis();
            Properties props = new Properties();

            try (InputStream in = Files.newInputStream(filePath)) {
                props.load(in);
            }

            properties.clear();
            for (String key : props.stringPropertyNames()) {
                properties.put(key, props.getProperty(key));
            }

            logger.info("成功加载配置文件: {}, 共加载 {} 个配置项", filePath, properties.size());
        } catch (Exception e) {
            logger.error("加载配置文件失败: {}", filePath, e);
        }
    }

    private void checkAndReloadIfNecessary() {
        try {
            if (Files.exists(filePath)) {
                long currentModified = Files.getLastModifiedTime(filePath).toMillis();
                if (currentModified > lastModified) {
                    logger.info("检测到配置文件变更，重新加载: {}", filePath);
                    loadProperties();
                }
            }
        } catch (Exception e) {
            logger.warn("检查配置文件变更失败: {}", filePath, e);
        }
    }
}
