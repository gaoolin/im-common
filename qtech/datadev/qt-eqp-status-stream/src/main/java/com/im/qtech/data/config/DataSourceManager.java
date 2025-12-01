package com.im.qtech.data.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单例，线程安全
 * <p>
 * 重复初始化：多个并行 Sink 任务不再各自建 HikariCP 池，改为共享一个全局池。
 * 序列化问题：DataSource 不需要序列化，Sink 只持有一个 DataSource key。
 * 启动卡住：池只初始化一次，Flink Task 部署更快。
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/04 13:41:21
 */
public class DataSourceManager {

    private static final Map<String, HikariDataSource> dataSourceMap = new ConcurrentHashMap<>();

    public static synchronized DataSource getDataSource(String key, String url, String user, String password, String driverClass) {
        if (!dataSourceMap.containsKey(key)) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);
            config.setDriverClassName(driverClass);
            config.setMaximumPoolSize(20);
            config.setMinimumIdle(5);
            config.setConnectionTimeout(30000L);
            config.setIdleTimeout(600000L);
            config.setMaxLifetime(1800000L);
            config.setLeakDetectionThreshold(60000L);
            config.setValidationTimeout(5000L);
            HikariDataSource ds = new HikariDataSource(config);
            dataSourceMap.put(key, ds);
        }
        return dataSourceMap.get(key);
    }

    public static void closeAll() {
        for (HikariDataSource ds : dataSourceMap.values()) {
            ds.close();
        }
    }
}