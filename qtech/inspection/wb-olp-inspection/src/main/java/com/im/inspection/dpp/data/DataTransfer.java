package com.im.inspection.dpp.data;

import com.im.inspection.config.DppConfigManager;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;
import org.apache.spark.sql.functions;
import org.im.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.im.inspection.util.Constant.*;
import static com.im.inspection.util.DebugModeDataShow.showDataset;
import static org.apache.spark.sql.functions.*;

/**
 * @author zhilin.gao
 * @email gaoolin@gmail.com
 * @date 2021/12/24 11:54
 */
public class DataTransfer {
    private static final Logger logger = LoggerFactory.getLogger(DataTransfer.class);
    private static final ConfigurationManager props = DppConfigManager.getInstance();

    public static Dataset<Row> doTransfer(Dataset<Row> rawDF, Dataset<Row> stdMdWireCnt) {
        try {
            String joinKeyRaw = props.getString("data.analysis.join.key.raw", NORM_MODULE_ID);
            String joinKeyStd = props.getString("data.analysis.join.key.tpl", TPL_MODULE_ID);
            String filterSimId = props.getString("data.analysis.filter.sim_id", null);

            WindowSpec winByPIdx = Window.partitionBy(col(SIM_ID), col(MODULE_ID), col(PIECES_INDEX));
            WindowSpec winMcByPIdx = Window.partitionBy(col(SIM_ID), col(PIECES_INDEX));

            Column subModuleIdCol = split(col(MODULE_ID), "#").getItem(0);
            Column dx = col(PAD_X).minus(col(LEAD_X));
            Column dy = col(PAD_Y).minus(col(LEAD_Y));
            Column wireLenCol = sqrt(functions.pow(dx, 2).plus(functions.pow(dy, 2)));

            Dataset<Row> df = rawDF
                    .withColumn(FIRST_BONDING_TIME, min(col(DT)).over(winByPIdx))
                    .withColumn(NORM_MODULE_ID, subModuleIdCol)
                    .withColumn(MODULES_BY_PIECES_INDEX, approx_count_distinct(NORM_MODULE_ID).over(winMcByPIdx))
                    .withColumn(WIRE_LEN, wireLenCol);

            // 过滤逻辑
            Dataset<Row> filteredDf = df
                    .filter(col(MODULES_BY_PIECES_INDEX).equalTo(1))
                    .filter(col(WIRE_ID).leq(col(CHECK_PORT)))
                    // 针对本次数据采集盒问题调整的逻辑 2025-05-16
                    .filter(col(PAD_X).isNotNull().or(col(PAD_X).notEqual(lit("null")))
                            .and(col(PAD_Y).isNotNull().or(col(PAD_Y).notEqual(lit("null"))))
                            .and(col(LEAD_X).isNotNull().or(col(LEAD_X).notEqual(lit("null"))))
                            .and(col(LEAD_Y).isNotNull().or(col(LEAD_Y).notEqual(lit("null")))))
                    .withColumn(CNT, count(WIRE_ID).over(winByPIdx));

            showDataset(filteredDf, "filteredDf");

            Dataset<Row> mergeStdMdDF = filteredDf.join(
                    stdMdWireCnt,
                    filteredDf.col(joinKeyRaw).equalTo(stdMdWireCnt.col(joinKeyStd)),
                    "left"
            );

            Dataset<Row> result = mergeStdMdDF.select(
                            col(SIM_ID),
                            col(MODULE_ID),
                            col(DT),
                            col(FIRST_BONDING_TIME),
                            col(WIRE_ID),
                            col(LEAD_X),
                            col(LEAD_Y),
                            col(PAD_X),
                            col(PAD_Y),
                            col(CHECK_PORT),
                            col(WIRE_LEN),
                            col(PIECES_INDEX),
                            col(NORM_MODULE_ID),
                            col(CNT),
                            col(MODULES_BY_PIECES_INDEX),
                            col(TPL_WIRE_CNT)
                    )
                    .sort(
                            col(SIM_ID),
                            col(MODULE_ID),
                            col(FIRST_BONDING_TIME),
                            col(WIRE_ID)
                    );

            if (filterSimId != null && !filterSimId.isEmpty()) {
                result = result.filter(col(SIM_ID).equalTo(filterSimId));
            }

            return result;

        } catch (Exception e) {
            logger.error(">>>>> Error occurred in DataTransfer.doProcess", e);
            throw e;
        }
    }
}