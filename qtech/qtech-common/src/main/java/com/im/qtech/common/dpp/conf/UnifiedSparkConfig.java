package com.im.qtech.common.dpp.conf;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.SparkConf;
import org.im.config.ConfigurationManager;

import java.util.Map;

/**
 * 统一的Spark配置管理类
 *
 * @author : gaozhilin
 * @email : gaoolin@gmail.com
 * @date : 2023/04/17 11:32:12
 */

public class UnifiedSparkConfig {
    private final ConfigurationManager configManager = BigDataConfigManager.getInstance();
    private String appName;
    private String master;

    public UnifiedSparkConfig() {
    }

    public UnifiedSparkConfig(String appName) {
        this.appName = appName;
    }

    public UnifiedSparkConfig(String appName, String master) {
        this.appName = appName;
        this.master = master;
    }

    public UnifiedSparkConfig withAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public UnifiedSparkConfig withMaster(String master) {
        this.master = master;
        return this;
    }

    public SparkConf buildSparkConf() {
        SparkConf sparkConf = new SparkConf();

        // 设置应用名称
        if (StringUtils.isNotBlank(this.appName)) {
            sparkConf.setAppName(this.appName);
        }

        // 设置Master
        if (StringUtils.isNotBlank(this.master)) {
            sparkConf.setMaster(this.master);
        } else {
            String masterFromConfig = configManager.getString("spark.master");
            if (StringUtils.isNotBlank(masterFromConfig)) {
                sparkConf.setMaster(masterFromConfig);
            }
        }

        // 应用Hadoop配置
        applyHadoopConfigurations(sparkConf);

        // 应用其他Spark配置
        applySparkConfigurations(sparkConf);

        return sparkConf;
    }

    private void applyHadoopConfigurations(SparkConf sparkConf) {
        Map<String, String> hadoopConfigs = BigDataConfigManager.getHadoopConfigurations();
        for (Map.Entry<String, String> entry : hadoopConfigs.entrySet()) {
            sparkConf.set(entry.getKey(), entry.getValue());
            sparkConf.set("spark.hadoop." + entry.getKey(), entry.getValue());
        }
    }

    private void applySparkConfigurations(SparkConf sparkConf) {
        // 从配置管理器加载所有spark.前缀的配置
        configManager.getAllProperties().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("spark.") && !entry.getKey().startsWith("spark.hadoop."))
                .forEach(entry -> sparkConf.set(entry.getKey(), entry.getValue()));
    }
}