package com.im.qtech.common.dpp.conf;

import org.apache.hadoop.conf.Configuration;
import org.im.config.ConfigurationManager;

import java.util.Map;

/**
 * 统一的Hadoop配置管理类
 *
 * @author : gaozhilin
 * @email : gaoolin@gmail.com
 * @date : 2023/04/17 11:35:29
 */

public class UnifiedHadoopConfig {
    private static final ConfigurationManager configManager = BigDataConfigManager.getInstance();

    public static Configuration createHadoopConfiguration() {
        Configuration conf = new Configuration();

        // 设置Hadoop用户
        String hadoopUser = configManager.getString("hadoop.user.name", "zcgx");
        System.setProperty("HADOOP_USER_NAME", hadoopUser);

        // 应用Hadoop配置
        Map<String, String> hadoopConfigs = BigDataConfigManager.getHadoopConfigurations();
        hadoopConfigs.forEach(conf::set);

        return conf;
    }
}
