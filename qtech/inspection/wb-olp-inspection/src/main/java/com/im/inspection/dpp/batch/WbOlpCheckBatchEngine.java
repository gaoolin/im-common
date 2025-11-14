package com.im.inspection.dpp.batch;

import com.im.inspection.config.DppConfigManager;
import com.im.inspection.dpp.data.DataFetch;
import com.im.inspection.dpp.data.DataTransfer;
import com.im.inspection.middleware.KafkaCli;
import com.im.inspection.util.log.TaskTimerRecorder;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.storage.StorageLevel;
import org.im.common.batch.config.BatchConfig;
import org.im.common.batch.core.BatchJobStatus;
import org.im.common.batch.exception.JobExecutionException;
import org.im.config.ConfigurationManager;
import org.im.exception.constants.ErrorCode;
import org.im.exception.constants.ErrorMessage;
import org.im.exception.type.common.BusinessException;
import org.im.exception.type.data.NoDataFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static com.im.inspection.dpp.algorithm.Transfer.doChk;
import static com.im.inspection.util.Constant.*;
import static com.im.inspection.util.DatasetUtils.getNeedFilterModule;
import static com.im.inspection.util.DatasetUtils.getTpl;
import static com.im.inspection.util.DebugModeDataShow.showDataset;
import static org.apache.spark.sql.functions.count;
import static org.apache.spark.sql.functions.lit;
import static org.im.common.dt.Chronos.*;

/**
 * WbOlp检查批处理引擎
 * 业务逻辑类，负责执行具体的OLP检查任务
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2022/06/12 10:16:07
 */
public class WbOlpCheckBatchEngine extends SparkBatchEngine<Void> {
    private static final Logger logger = LoggerFactory.getLogger(WbOlpCheckBatchEngine.class);
    private static final ConfigurationManager configManager = DppConfigManager.getInstance();

    // 每个实例一个唯一 taskId
    private final String taskId = "job-" + UUID.randomUUID();

    // 数据库连接配置
    private String driver;
    private String url;
    private String user;
    private String pwd;

    /**
     * 构造函数
     *
     * @param appName      应用名称
     * @param sparkSession Spark会话
     * @param config       批处理配置
     */
    public WbOlpCheckBatchEngine(String appName, SparkSession sparkSession, BatchConfig config) {
        super(appName, sparkSession, config);
    }

    /**
     * 构造函数（使用默认配置）
     *
     * @param appName      应用名称
     * @param sparkSession Spark会话
     */
    public WbOlpCheckBatchEngine(String appName, SparkSession sparkSession) {
        super(appName, sparkSession);
    }

    /**
     * 初始化配置参数
     */
    private void initConfig() {
        driver = configManager.getString("jdbc.postgres.driver");
        url = configManager.getString("jdbc.postgres.url");
        user = configManager.getString("jdbc.postgres.user");
        pwd = configManager.getString("jdbc.postgres.pwd");
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
    protected void processSparkJob(Void input) throws Exception {
        long rawDataCount = 0L;
        Dataset<Row> rawDataDf = null;
        Dataset<Row> processedDf = null;

        try {
            TaskTimerRecorder.register(taskId);
            initTaskTimerRecorder();
            initConfig();

            logElapsedTime("***************************配置文件已加载***************************");

            Dataset<Row> stdModels = getTpl(getSparkSession(), driver, url, user, pwd);
            validateStdModels(stdModels);

            Dataset<Row> stdModWireCnt = stdModels.groupBy("tpl_module_id").agg(count("tpl_module_id").as("tpl_wire_cnt"));

            String startDt = addMinutes(now(), TIME_OFFSET_MINUTES).format(getFormatter("yyyy-MM-dd HH:mm:ss"));
            logElapsedTime(String.format(">>>>> spark job start dt %s", startDt));

            // 使用 debug 模式控制数据获取
            rawDataDf = fetchData(startDt);
            rawDataCount = rawDataDf.count();

            logElapsedTime(String.format(">>>>> raw data count %d", rawDataCount));
            logElapsedTime(">>>>> spark compute done");

            // 使用 debug 模式控制数据转换
            processedDf = transformData(rawDataDf, stdModWireCnt);
            logElapsedTime(String.format(">>>>> processed data count %d", processedDf.count()));
            showDataset(processedDf, "processedDf");

            Dataset<Row> ttlCheckResDf = performCheck(processedDf, stdModels);
            handleCheckResults(ttlCheckResDf);

            Dataset<Row> finalCheckResDf = generateFinalResults();
            outputResults(finalCheckResDf, rawDataDf, rawDataCount);

        } finally {
            cleanupResources(rawDataDf, processedDf);
            TaskTimerRecorder.logTotalTime(taskId);
        }
    }

    /**
     * 验证标准模型数据
     */
    private void validateStdModels(Dataset<Row> stdModels) throws NoDataFoundException {
        if (stdModels == null || stdModels.isEmpty()) {
            throw new NoDataFoundException(ErrorCode.DB_NO_DATA_FOUND, ErrorMessage.DB_NO_DATA_FOUND);
        }
    }

    /**
     * 获取原始数据
     */
    private Dataset<Row> fetchData(String startDt) throws Exception {
        return java.util.Objects.requireNonNull(DataFetch.doFetch(getSparkSession(), startDt, configManager))
                .persist(StorageLevel.MEMORY_AND_DISK());
    }

    /**
     * 转换数据
     */
    private Dataset<Row> transformData(Dataset<Row> rawDataDf, Dataset<Row> stdModWireCnt) {
        return DataTransfer.doTransfer(rawDataDf, stdModWireCnt)
                .persist(StorageLevel.MEMORY_AND_DISK());
    }

    /**
     * 执行检查
     */
    private Dataset<Row> performCheck(Dataset<Row> processedDf, Dataset<Row> stdModels) throws Exception {
        Dataset<Row> ttlCheckResDf = doChk(processedDf, stdModels);
        if (ttlCheckResDf != null) {
            ttlCheckResDf.createOrReplaceTempView("ttlCheckResDf");
        }
        showDataset(ttlCheckResDf, "ttlCheckResDf");
        return ttlCheckResDf;
    }

    /**
     * 处理检查结果
     */
    private void handleCheckResults(Dataset<Row> ttlCheckResDf) {
        if (ttlCheckResDf != null) {
            ttlCheckResDf.createOrReplaceTempView("ttlCheckResDf");
        }
        getNeedFilterModule(getSparkSession(), driver, url, user, pwd).createOrReplaceTempView("needFilterMcId");
    }

    /**
     * 生成最终结果
     */
    private Dataset<Row> generateFinalResults() {
        Dataset<Row> needModifyDf = getSparkSession().sql(NEED_FILTER_MODULE);
        Dataset<Row> stableDf = getSparkSession().sql(EXCLUDE_NEED_FILTER_MODULE);

        needModifyDf = needModifyDf.withColumn("code", lit(0)).withColumn("description", lit("olp invalidation"));

        return stableDf.union(needModifyDf)
                // .withColumnRenamed("module", "module")
                .withColumnRenamed("dt", "chk_dt")
                .withColumn("chk_dt", functions.date_format(functions.col("chk_dt"), "yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 输出结果
     */
    private void outputResults(Dataset<Row> finalCheckResDf, Dataset<Row> rawDataDf, long rawDataCount) throws Exception {
        logElapsedTime(">>>>> translation done");

        // 使用 debug 模式控制 Kafka 输出
        if (!configManager.getBoolean("debug.mode.enabled")) {
            sendToKafka(finalCheckResDf, rawDataDf);
        } else {
            logger.warn(">>>>> Running in DEBUG mode, skipping Kafka output.");
        }

        long finalCheckCount = finalCheckResDf.count();
        logger.info(">>>>> final check result count: {}", finalCheckCount);
        showDataset(finalCheckResDf, "finalCheckResDf");
        logger.info(">>>>> olp check detail count: {}", rawDataCount);
        getMetrics().setProcessedRecords(rawDataCount);
        showDataset(rawDataDf, "rawDataDf");
    }

    /**
     * 发送结果到Kafka
     */
    private void sendToKafka(Dataset<Row> finalCheckResDf, Dataset<Row> rawDataDf) {
        try {
            KafkaCli.sendReverseCtrlInfoToKafka(finalCheckResDf);
        } catch (Exception e) {
            logger.error(">>>>> Kafka 发送最终校验结果失败", e);
            throw new BusinessException(ErrorCode.EXT_SERVICE_ERROR, "Failed to send final check results to Kafka", e);
        }

        try {
            KafkaCli.sendWbOlpRawDataToKafka(rawDataDf);
        } catch (Exception e) {
            logger.error(">>>>> Kafka 发送原始数据失败", e);
            throw new BusinessException(ErrorCode.EXT_SERVICE_ERROR, "Failed to send raw data to Kafka", e);
        }
    }

    /**
     * 清理资源
     */
    private void cleanupResources(Dataset<Row> rawDataDf, Dataset<Row> processedDf) {
        if (getSparkSession() != null) {
            if (getSparkSession().catalog().tableExists("ttlCheckResDf")) {
                getSparkSession().catalog().dropTempView("ttlCheckResDf");
            }
            if (getSparkSession().catalog().tableExists("needFilterMcId")) {
                getSparkSession().catalog().dropTempView("needFilterMcId");
            }
        }

        if (rawDataDf != null) {
            rawDataDf.unpersist();
        }
        if (processedDf != null) {
            processedDf.unpersist();
        }
    }

    private void logElapsedTime(String stepName) {
        TaskTimerRecorder.logStep(taskId, stepName);
    }

    @Override
    public Void execute(Void input) throws Exception {
        try {
            return super.execute(input);
        } catch (Exception e) {
            logger.error(">>>>> Job execution failed: {}", getJobName(), e);
            throw new JobExecutionException(getJobId(), "Batch job execution failed", e);
        }
    }

    @Override
    public void start() {
        super.start();
        try {
            execute(null);
        } catch (Exception e) {
            logger.error(">>>>> Failed to execute job: {}", getJobName(), e);
            updateStatus(BatchJobStatus.FAILED);
            getMetrics().fail(e);
            throw new BusinessException(ErrorCode.EXT_SERVICE_ERROR, "Failed to start batch engine", e);
        }
    }

    @Override
    public void stop() {
        // 清理引擎内部资源
        super.stop();
        logger.info("WbOlpCheckBatchEngine stopped");
    }

    @Override
    public void restart() {
        // 重新启动引擎
        super.restart();
        logger.info("WbOlpCheckBatchEngine restarted");
    }
}
