package com.im.inspection.util;

import com.im.inspection.config.DppConfigManager;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.im.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 实现debug模式下Dataset<Row>数据打印功能
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/05/16 09:17:50
 */
public class DebugModeDataShow {
    private static final Logger logger = LoggerFactory.getLogger(DebugModeDataShow.class);
    private static final ConfigurationManager configManager = DppConfigManager.getInstance();

    /**
     * 在debug模式下打印Dataset<Row>类型的数据到控制台（默认打印所有行）
     *
     * @param dataset 输入数据集
     */
    public static void showDataset(Dataset<Row> dataset, String dataName) {
        showDataset(dataset, 500, dataName); // 默认打印所有行 Integer.MAX_VALUE
    }

    /**
     * 在debug模式下打印Dataset<Row>类型的数据到控制台，支持限制最大行数
     *
     * @param dataset 输入数据集
     * @param limit   打印的最大行数
     */
    public static void showDataset(Dataset<Row> dataset, int limit, String dataName) {
        if (configManager.getBoolean("debug.mode.enabled") && dataset != null) {
            logger.info("\n=== Debug Mode - Dataset Start ===\n");
            logger.info(">>>>> " + dataName);
            dataset.dtypes();
            dataset.count(); // 强制触发计算
            dataset.show(limit, false); // 此时数据已经加载完成
            logger.info("\n=== Debug Mode - Dataset End ===\n");
        }
    }

    /**
     * 在debug模式下打印指定的文本字符串到控制台
     *
     * @param message 输入的文本字符串
     * @param isDebug 是否为debug模式
     */
    public static void showText(String message, boolean isDebug) {
        if (isDebug && message != null) {
            logger.info("\n=== Debug Mode - Text Start ===\n");
            logger.info(">>>>> " + message);
            logger.info("\n=== Debug Mode - Text End ===\n");
        }
    }
}