package com.im.inspection.svc;

import com.im.inspection.config.DppConfigManager;
import com.im.inspection.dpp.batch.WbOlpCheckBatchEngine;
import com.im.inspection.util.log.TaskTimerRecorder;
import com.im.qtech.data.dpp.SparkInitConf;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.im.common.batch.config.BatchConfig;
import org.im.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * WbOlp检查批处理服务
 * 负责初始化Spark环境和启动批处理作业
 *
 * @author zhilin.gao
 * @email gaoolin@gmail.com
 * @date 2021/12/21 17:36
 */
public class WbOlpCheckBatchServer {
    private static final Logger logger = LoggerFactory.getLogger(WbOlpCheckBatchServer.class);
    private static final ConfigurationManager configManager = DppConfigManager.getInstance();
    private SparkSession spark;
    private WbOlpCheckBatchEngine batchEngine;

    public static void main(String[] args) {
        WbOlpCheckBatchServer server = new WbOlpCheckBatchServer();
        try {
            server.start(args);
        } catch (Exception e) {
            logger.error(">>>>> WbOlpCheckBatchServer encountered an error", e);
        } finally {
            server.stop();
        }
    }

    /**
     * 初始化Spark环境
     */
    private SparkSession initSpark() {
        // 使用统一的Spark配置管理器
        SparkConf sparkConf = SparkInitConf.initSparkConfigs("wb olp check");

        // 根据local模式设置运行模式
        String localMode = configManager.getString("spark.mode", "cluster");

        if ("local".equals(localMode)) {
            sparkConf.setMaster("local[*]");
            // 设置临时目录避免权限问题
            sparkConf.set("spark.local.dir", "D:\\spark-tmp");
        } else {
            // 在dolphinscheduler上运行时使用yarn模式
            sparkConf.setMaster("yarn");
        }

        // 设置其他Spark配置
        sparkConf.set("spark.default.parallelism", "4")
                .set("spark.sql.caseSensitive", "false")
                .set("spark.sql.analyzer.failAmbiguousSelfJoin", "false");

        spark = SparkSession.builder().config(sparkConf).getOrCreate();
        spark.sparkContext().setLogLevel(Objects.equals(localMode, "local") ? "DEBUG" : "INFO");
        logger.info(">>>>> Spark session initialized with app name: {}", sparkConf.get("spark.app.name"));
        return spark;
    }

    /**
     * 启动服务
     */
    public void start(String[] args) throws Exception {
        logger.info("=========================wb olp check开始运行===========================");

        spark = initSpark();
        batchEngine = createBatchEngine(spark);

        // 启动批处理引擎
        batchEngine.start();

        logger.info("=========================wb olp check运行结束===========================");
        logger.info(">>>>> Job Status: {}", batchEngine.getStatus());
        logger.info(">>>>> Job Metrics: {}", batchEngine.getMetrics());
    }

    /**
     * 创建批处理引擎实例
     */
    private WbOlpCheckBatchEngine createBatchEngine(SparkSession sparkSession) {
        boolean isDebugEnabled = configManager.getBoolean("debug.mode.enabled", false);

        BatchConfig batchConfig = BatchConfig.builder()
                .property("debug.mode.enabled", isDebugEnabled)
                .continueOnError(false)
                .build();

        return new WbOlpCheckBatchEngine(
                "WbOlpCheckJob",
                sparkSession,
                batchConfig
        );
    }

    /**
     * 停止服务并释放资源
     */
    public void stop() {
        logger.info(">>>>> Shutting down WbOlpCheckBatchServer...");

        // 停止批处理引擎
        if (batchEngine != null && batchEngine.isRunning()) {
            batchEngine.stop();
        }

        // 关闭Spark会话
        if (spark != null) {
            spark.close();
            logger.info(">>>>> Spark session closed");
        }

        // 确保关闭所有后台线程池和其他资源
        try {
            // TaskTimerRecorder中有调度器，需要关闭它
            TaskTimerRecorder.shutdown();
        } catch (Exception e) {
            logger.warn(">>>>> Error shutting down background tasks", e);
        }

        logger.info(">>>>> WbOlpCheckBatchServer shutdown complete");
    }

    /**
     * 重启服务
     */
    public void restart() {
        try {
            stop();
            start(new String[0]);
        } catch (Exception e) {
            logger.error(">>>>> Failed to restart WbOlpCheckBatchServer", e);
        }
    }
}
