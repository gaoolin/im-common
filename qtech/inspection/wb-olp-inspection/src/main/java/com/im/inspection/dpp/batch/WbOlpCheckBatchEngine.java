package com.im.inspection.dpp.batch;

import com.im.inspection.config.DppConfigManager;
import com.im.inspection.dpp.data.DataFetch;
import com.im.inspection.dpp.data.DataTransfer;
import com.im.inspection.utils.KafkaCli;
import com.im.inspection.utils.log.TaskTimerRecorder;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.storage.StorageLevel;
import org.im.config.ConfigurationManager;
import org.im.exception.constants.ErrorCode;
import org.im.exception.constants.ErrorMessage;
import org.im.exception.type.data.NoDataFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static com.im.inspection.dpp.algorithm.Transfer.doChk;
import static com.im.inspection.utils.Constants.*;
import static com.im.inspection.utils.DbUtil.getNeedFilterModule;
import static com.im.inspection.utils.DbUtil.getTpl;
import static com.im.inspection.utils.DebugModeDataShow.showDataset;
import static org.apache.spark.sql.functions.count;
import static org.apache.spark.sql.functions.lit;
import static org.im.common.dt.Chronos.*;

/**
 * 业务逻辑类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2022/06/12 10:16:07
 */
public class WbOlpCheckBatchEngine extends BatchEngine {
    private static final Logger logger = LoggerFactory.getLogger(WbOlpCheckBatchEngine.class);
    private static final ConfigurationManager configManager = DppConfigManager.getInstance();

    private final SparkSession spark;
    private final boolean isDebugEnabled;
    // 移除了 PropertiesManager props 字段

    // 每个实例一个唯一 taskId
    private final String taskId = "job-" + UUID.randomUUID();

    public WbOlpCheckBatchEngine(SparkSession spark, boolean isDebugEnabled) { // 移除了 PropertiesManager 参数
        this.spark = spark;
        this.isDebugEnabled = isDebugEnabled;
        initTaskTimerRecorder();
    }

    /**
     * 初始化 TaskTimerRecorder（启用 HDFS 写入）
     */
    private void initTaskTimerRecorder() {
        try {
            // 初始化 HDFS 配置（默认路径为 /tmp/task-timer-recorder）
            TaskTimerRecorder.initHdfs();
        } catch (Exception e) {
            logger.warn(">>>>> Failed to initialize TaskTimerRecorder with HDFS support.", e);
        }
    }

    @Override
    protected void svcProcessData() throws Exception {
        long rawDataCount = 0L;
        Dataset<Row> rawDataDf = null;
        Dataset<Row> processedDf = null;

        try {
            TaskTimerRecorder.register(taskId);
            logElapsedTime("***************************配置文件已加载***************************");

            // 使用配置管理器
            String driver = configManager.getString("jdbc.oracle.driver");
            String url = configManager.getString("jdbc.oracle.url");
            String user = configManager.getString("jdbc.oracle.user");
            String pwd = configManager.getString("jdbc.oracle.pwd");

            Dataset<Row> stdModels = getTpl(spark, driver, url, user, pwd);
            if (stdModels == null || stdModels.isEmpty()) {
                throw new NoDataFoundException(ErrorCode.DB_NO_DATA_FOUND, ErrorMessage.DB_NO_DATA_FOUND);
            }

            Dataset<Row> stdModWireCnt = stdModels.groupBy("std_mc_id").agg(count("std_mc_id").as("std_mod_line_cnt"));

            String startDt = addMinutes(now(), TIME_OFFSET_MINUTES).format(getFormatter("yyyy-MM-dd HH:mm:ss"));
            logElapsedTime(String.format(">>>>> spark job start dt %s", startDt));

            // 使用 debug 模式控制数据获取
            rawDataDf = DataFetch.doFetch(spark, startDt, isDebugEnabled, null) // 移除了 props 参数
                    .persist(StorageLevel.MEMORY_AND_DISK());
            rawDataCount = rawDataDf.count();

            logElapsedTime(String.format(">>>>> raw data count %d", rawDataCount));
            logElapsedTime(">>>>> spark compute done");

            // 使用 debug 模式控制数据转换
            processedDf = DataTransfer.doTransfer(rawDataDf, stdModWireCnt)
                    .persist(StorageLevel.MEMORY_AND_DISK());
            logElapsedTime(String.format(">>>>> processed data count %d", processedDf.count()));
            showDataset(processedDf, isDebugEnabled, "processedDf");

            Dataset<Row> ttlCheckResDf = doChk(processedDf, stdModels);
            if (ttlCheckResDf != null) {
                ttlCheckResDf.createOrReplaceTempView("ttlCheckResDf");
            }
            showDataset(ttlCheckResDf, isDebugEnabled, "ttlCheckResDf");

            getNeedFilterModule(spark, driver, url, user, pwd).createOrReplaceTempView("needFilterMcId");

            Dataset<Row> needModifyDf = spark.sql(NEED_FILTER_MC_ID);
            Dataset<Row> stableDf = spark.sql(EXCLUDE_NEED_FILTER_MC_ID);

            needModifyDf = needModifyDf.withColumn("code", lit(0)).withColumn("description", lit("olp invalidation"));

            Dataset<Row> finalCheckResDf = stableDf.union(needModifyDf)
                    .withColumnRenamed("mc_id", "module")
                    .withColumnRenamed("dt", "chk_dt")
                    .withColumn("chk_dt", functions.date_format(functions.col("chk_dt"), "yyyy-MM-dd HH:mm:ss"));

            logElapsedTime(">>>>> translation done");

            // 使用 debug 模式控制 Kafka 输出
            if (!isDebugEnabled) {
                try {
                    KafkaCli.sendReverseCtrlInfoToKafka(finalCheckResDf);
                } catch (Exception e) {
                    logger.error(">>>>> Kafka 发送最终校验结果失败", e);
                }

                try {
                    KafkaCli.sendWbOlpRawDataToKafka(rawDataDf);
                } catch (Exception e) {
                    logger.error(">>>>> Kafka 发送原始数据失败", e);
                }
            } else {
                logger.warn(">>>>> Running in DEBUG mode, skipping Kafka output.");
            }

            long finalCheckCount = finalCheckResDf.count();
            logger.info(">>>>> final check result count: {}", finalCheckCount);
            showDataset(finalCheckResDf, isDebugEnabled, "finalCheckResDf");
            logger.info(">>>>> olp check detail count: {}", rawDataCount);
            showDataset(rawDataDf, isDebugEnabled, "rawDataDf");

        } finally {
            if (spark.catalog().tableExists("ttlCheckResDf")) {
                spark.catalog().dropTempView("ttlCheckResDf");
            }
            if (spark.catalog().tableExists("needFilterMcId")) {
                spark.catalog().dropTempView("needFilterMcId");
            }

            if (rawDataDf != null) {
                rawDataDf.unpersist();
            }
            if (processedDf != null) {
                processedDf.unpersist();
            }

            TaskTimerRecorder.logTotalTime(taskId);
        }
    }

    private void logElapsedTime(String stepName) {
        TaskTimerRecorder.logStep(taskId, stepName);
    }
}
