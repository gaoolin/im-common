package com.im.inspection.dpp.data;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.im.common.exception.constants.ErrorCode;
import org.im.common.exception.type.data.NoDataFoundException;
import org.im.config.ConfigurationManager;
import org.im.exception.constants.ErrorMessage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.im.inspection.util.Constant.DRUID_TRANSFORM_SQL;
import static com.im.inspection.util.DebugModeDataShow.showText;

/**
 * @author zhilin.gao
 * @email gaoolin@gmail.com
 * @date 2022/1/4 11:41
 */
public class DataFetch {
    private static final Logger logger = LoggerFactory.getLogger(DataFetch.class);

    public static Dataset<Row> doFetch(SparkSession ss, String startTime, ConfigurationManager manager) {
        Boolean isDebugEnabled = manager.getBoolean("debug.mode.enabled");
        try {
            // 动态构建 WHERE 条件部分（核心改动在此）
            String whereCondition;
            if (isDebugEnabled) {
                String dtRange = manager.getString("debug.filter.dt_range", null);
                if (dtRange != null && !dtRange.trim().isEmpty()) {
                    String[] range = dtRange.trim().split("~");
                    if (range.length == 2) {
                        whereCondition = String.format("__time >= '%s' AND __time <= '%s'", range[0].trim(), range[1].trim());
                    } else {
                        logger.warn(">>>>> Invalid dtRange format: {}", dtRange);
                        whereCondition = String.format("__time > '%s'", startTime); // 回退
                    }
                } else {
                    whereCondition = String.format("__time > '%s'", startTime); // 默认回退
                }
            } else {
                whereCondition = String.format("__time > '%s'", startTime);
            }

            // 拼接 SQL 语句
            String baseSql = String.format(
                    "SELECT SIMID, ECount, receive_date, MachineType, __time, " +
                            "B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, Sendcount " +
                            "FROM t_dev_attrs WHERE %s AND receive_date IS NOT NULL " +
                            "AND MachineType IS NOT NULL AND device_type = 'WB' " +
                            "AND B1 IS NOT NULL AND B1 NOT LIKE 'DIE%%' AND B1 NOT LIKE 'LEAD%%' " +
                            "AND B1 NOT LIKE '%%j%%' AND B1 NOT LIKE '.0%%' AND B1 NOT LIKE '%%ln%%'",
                    whereCondition
            );

            // 追加 debug 模式的 filter
            if (isDebugEnabled) {
                String simIdFilter = manager.getString("debug.filter.sim_id", null);
                String mcIdFilter = manager.getString("debug.filter.module_id", null);
                String dtRange = manager.getString("debug.filter.dt_range", null);

                StringBuilder filterClauses = getFilterClauses(simIdFilter, mcIdFilter, dtRange);

                baseSql += filterClauses.toString();
            }

            showText(baseSql, isDebugEnabled);

            String driver = manager.getString("jdbc.druid.driver");
            String url = manager.getString("jdbc.druid.url");

            // 手动定义 schema（字段顺序必须与 SQL SELECT 一致）
            StructType schema = new StructType()
                    .add("SIMID", DataTypes.StringType)
                    .add("ECount", DataTypes.StringType)
                    .add("receive_date", DataTypes.StringType)
                    .add("MachineType", DataTypes.StringType)
                    .add("__time", DataTypes.TimestampType)
                    .add("B1", DataTypes.StringType)
                    .add("B2", DataTypes.StringType)
                    .add("B3", DataTypes.StringType)
                    .add("B4", DataTypes.StringType)
                    .add("B5", DataTypes.StringType)
                    .add("B6", DataTypes.StringType)
                    .add("B7", DataTypes.StringType)
                    .add("B8", DataTypes.StringType)
                    .add("B9", DataTypes.StringType)
                    .add("B10", DataTypes.StringType)
                    .add("Sendcount", DataTypes.StringType);

            Dataset<Row> tempDF = ss.read()
                    .format("jdbc")
                    .option("url", url)
                    .option("driver", driver)
                    .option("user", "")
                    .option("password", "")
                    .option("query", baseSql)
                    .option("fetchsize", "1000")  // JDBC 游标 的批量提取大小
                    .schema(schema)
                    .load()
                    .cache(); // 防止多次触发 JDBC 读取

            tempDF = tempDF
                    .withColumn("ECount", tempDF.col("ECount").cast(DataTypes.IntegerType))
                    .withColumn("receive_date", functions.to_timestamp(tempDF.col("receive_date"), "yyyy-MM-dd HH:mm:ss"))
                    .withColumn("Sendcount", tempDF.col("Sendcount").cast(DataTypes.IntegerType));

            // 注册为临时视图
            tempDF.createOrReplaceTempView("t_dev_attrs");

            // 执行 Spark SQL 转换
            Dataset<Row> finalDF = ss.sql(DRUID_TRANSFORM_SQL);

            // 使用 count() 避免 isEmpty() 引发的 JDBC schema 预探测
            if (finalDF == null || finalDF.count() == 0) {
                logger.error(">>>>> Druid has no data at the begin time: {}", startTime);
                throw new NoDataFoundException(ErrorCode.DB_NO_DATA_FOUND, ErrorMessage.DB_NO_DATA_FOUND);
            }

            return finalDF.filter("wire_id > 0");

        } catch (Exception e) {
            logger.error(">>>>> Error occurred in DataFetch.doFetch", e);
        }
        return null;
    }

    @NotNull
    private static StringBuilder getFilterClauses(String simIdFilter, String mcIdFilter, String dtRange) {
        StringBuilder filterClauses = new StringBuilder();

        if (simIdFilter != null && !simIdFilter.trim().isEmpty()) {
            filterClauses.append(String.format(" AND SIMID = '%s'", simIdFilter));
        }

        if (mcIdFilter != null && !mcIdFilter.trim().isEmpty()) {
            filterClauses.append(String.format(" AND MachineType = '%s'", mcIdFilter));
        }

        if (dtRange != null && !dtRange.trim().isEmpty()) {
            String[] range = dtRange.trim().split("~");
            if (range.length == 2) {
                filterClauses.append(String.format(" AND __time >= '%s' AND __time <= '%s'", range[0].trim(), range[1].trim()));
            } else {
                logger.warn(">>>>> Invalid dtRange format: {}", dtRange);
            }
        }

        return filterClauses;
    }
}