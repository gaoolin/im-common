package com.im.qtech.data.dpp;

import com.im.qtech.data.dpp.conf.UnifiedSparkConfig;
import org.apache.spark.SparkConf;
import org.im.common.exception.constants.ErrorCode;
import org.im.common.exception.type.common.BusinessException;

/**
 * 用于初始化SparkSession的配置
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/01/02 10:23:59
 */
public class SparkInitConf {

    /**
     * 初始化Spark配置（兼容旧版接口）
     *
     * @return SparkConf 配置对象
     */
    public static SparkConf initSparkConfigs() {
        return initSparkConfigs("QTechBigDataApp");
    }

    /**
     * 初始化Spark配置（兼容旧版接口）
     *
     * @param appName 应用名称
     * @return SparkConf 配置对象
     */
    public static SparkConf initSparkConfigs(String appName) {
        try {
            SparkConf conf = new UnifiedSparkConfig()
                    .withAppName(appName)
                    .buildSparkConf();

            if (conf.getAll().length == 0) {
                throw new BusinessException(ErrorCode.BIZ_UNKNOWN_ERROR,
                        "Spark 配置初始化出错，创建上下文的配置为空，请检查！");
            }

            return conf;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.BIZ_UNKNOWN_ERROR,
                    "Spark 配置初始化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 初始化Spark配置（带Master配置）
     *
     * @param appName 应用名称
     * @param master  Master配置
     * @return SparkConf 配置对象
     */
    public static SparkConf initSparkConfigs(String appName, String master) {
        try {
            SparkConf conf = new UnifiedSparkConfig()
                    .withAppName(appName)
                    .withMaster(master)
                    .buildSparkConf();

            if (conf.getAll().length == 0) {
                throw new BusinessException(ErrorCode.BIZ_UNKNOWN_ERROR,
                        "Spark 配置初始化出错，创建上下文的配置为空，请检查！");
            }

            return conf;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.BIZ_UNKNOWN_ERROR,
                    "Spark 配置初始化失败: " + e.getMessage(), e);
        }
    }
}
