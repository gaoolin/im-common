package com.im.qtech.data.dpp.conf;

import org.im.config.ConfigurationManager;
import org.im.config.impl.DefaultConfigurationManager;
import org.im.config.source.PropertiesFileConfigSource;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一的配置管理器
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/16
 */
public class BigDataConfigManager {
    private static final Object lock = new Object();
    private static volatile ConfigurationManager instance;

    public static ConfigurationManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = createConfigurationManager();
                }
            }
        }
        return instance;
    }

    private static ConfigurationManager createConfigurationManager() {
        ConfigurationManager manager = new DefaultConfigurationManager();

        // 添加默认的Hadoop配置源（向后兼容）
        manager.addConfigSource(new PropertiesFileConfigSource("hadoop-default.properties") {
            @Override
            public Map<String, String> getProperties() {
                Map<String, String> defaultHadoopConfigs = new HashMap<>();
                defaultHadoopConfigs.put("hadoop.fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
                defaultHadoopConfigs.put("hadoop.fs.hdfs.impl.disable.cache", "true");
                defaultHadoopConfigs.put("hadoop.fs.defaultFS", "hdfs://cluster");
                defaultHadoopConfigs.put("hadoop.dfs.nameservices", "cluster");
                defaultHadoopConfigs.put("hadoop.dfs.ha.namenodes.cluster", "nn1,nn2");
                defaultHadoopConfigs.put("hadoop.dfs.namenode.rpc-address.cluster.nn1", "im01:8020");
                defaultHadoopConfigs.put("hadoop.dfs.namenode.rpc-address.cluster.nn2", "im02:8020");
                defaultHadoopConfigs.put("hadoop.dfs.client.failover.proxy.provider.cluster",
                        "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
                return defaultHadoopConfigs;
            }
        });

        return manager;
    }

    public static Map<String, String> getHadoopConfigurations() {
        Map<String, String> hadoopConfigs = new HashMap<>();
        getInstance().getAllProperties().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("hadoop."))
                .forEach(entry -> hadoopConfigs.put(
                        entry.getKey().substring("hadoop.".length()),
                        entry.getValue()));
        return hadoopConfigs;
    }
}