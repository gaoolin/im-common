package com.im.qtech.data.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/12/01
 */

public class DataSourceManager {
    private static final Map<String, HikariDataSource> dataSourceMap = new ConcurrentHashMap();

    public DataSourceManager() {
    }

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

        return (DataSource) dataSourceMap.get(key);
    }

    public static void closeAll() {
        Iterator var0 = dataSourceMap.values().iterator();

        while (var0.hasNext()) {
            HikariDataSource ds = (HikariDataSource) var0.next();
            ds.close();
        }

    }
}
