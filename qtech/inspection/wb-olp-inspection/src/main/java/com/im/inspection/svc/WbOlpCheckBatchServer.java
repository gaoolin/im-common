package com.im.inspection.svc;

import com.im.inspection.config.DppConfigManager;
import com.im.inspection.dpp.batch.WbOlpCheckBatchEngine;
import com.im.qtech.common.dpp.SparkInitConf;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.im.config.ConfigurationManager;
import org.im.exception.type.data.NoDataFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhilin.gao
 * @email gaoolin@gmail.com
 * @date 2021/12/21 17:36
 */
public class WbOlpCheckBatchServer {
    private static final Logger logger = LoggerFactory.getLogger(WbOlpCheckBatchServer.class);
    private static final ConfigurationManager configManager = DppConfigManager.getInstance();
    private static SparkSession spark;

    public static void main(String[] args) {
        try {
            new WbOlpCheckBatchServer().run(args);
        } catch (Exception e) {
            logger.error(">>>>> WbOlpCheckBatchServer run error", e);
        } finally {
            if (spark != null) {
                spark.close();
            }
        }
    }

    private void initSpark() throws NoDataFoundException {
        SparkConf sparkConf = SparkInitConf.initSparkConfigs();
        sparkConf.setMaster("local[*]");
        sparkConf.setAppName("wb olp check")
                .set("spark.default.parallelism", "4")
                .set("spark.sql.caseSensitive", "false") // 字段大小写不敏感
                .set("spark.sql.analyzer.failAmbiguousSelfJoin", "false");
        spark = SparkSession.builder().config(sparkConf).getOrCreate();
    }

    public void run(String[] args) throws Exception {
        boolean isDebugEnabled = configManager.getBoolean("debug.mode.enabled", false);

        logger.info("=========================wb olp check开始运行===========================");
        initSpark();

        // 构造引擎并传入 debug 参数
        WbOlpCheckBatchEngine wbOlpCheckBatchEngine = new WbOlpCheckBatchEngine(spark, isDebugEnabled);
        wbOlpCheckBatchEngine.start();

        logger.info("=========================wb olp check运行结束===========================");
    }
}
