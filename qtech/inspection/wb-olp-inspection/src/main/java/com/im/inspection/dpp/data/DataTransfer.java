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

import static com.im.inspection.utils.Constants.*;
import static com.im.inspection.utils.DebugModeDataShow.showDataset;
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
            String joinKeyRaw = props.getString("data.analysis.join.key.raw", "sub_mc_id");
            String joinKeyStd = props.getString("data.analysis.join.key.std", "std_mc_id");
            String filterSimId = props.getString("data.analysis.filter.sim_id", null);

            WindowSpec winByPIdx = Window.partitionBy(col(SIM_ID), col(PROD_TYPE), col(PIECES_INDEX));
            WindowSpec winMcByPIdx = Window.partitionBy(col(SIM_ID), col(PIECES_INDEX));

            Column subMcIdCol = split(col(PROD_TYPE), "#").getItem(0);
            Column dx = col(PAD_X).minus(col(LEAD_X));
            Column dy = col(PAD_Y).minus(col(LEAD_Y));
            Column wireLenCol = sqrt(functions.pow(dx, 2).plus(functions.pow(dy, 2)));

            Dataset<Row> df = rawDF
                    .withColumn(FIRST_DRAW_TIME, min(col(DT)).over(winByPIdx))
                    .withColumn(SUB_PROD_TYPE, subMcIdCol)
                    .withColumn(MCS_BY_PIECES_INDEX, approx_count_distinct(SUB_PROD_TYPE).over(winMcByPIdx))
                    .withColumn(WIRE_LEN, wireLenCol);

            // 过滤逻辑
            Dataset<Row> filteredDf = df
                    .filter(col(MCS_BY_PIECES_INDEX).equalTo(1))
                    .filter(col(LINE_NO).leq(col(CHECK_PORT)))
                    // 针对本次数据采集盒问题调整的逻辑 2025-05-16
                    .filter(col(PAD_X).isNotNull().or(col(PAD_X).notEqual(lit("null")))
                            .and(col(PAD_Y).isNotNull().or(col(PAD_Y).notEqual(lit("null"))))
                            .and(col(LEAD_X).isNotNull().or(col(LEAD_X).notEqual(lit("null"))))
                            .and(col(LEAD_Y).isNotNull().or(col(LEAD_Y).notEqual(lit("null")))))
                    .withColumn(CNT, count(LINE_NO).over(winByPIdx));

            showDataset(filteredDf, props.getBoolean("debug.mode.enabled", false), "filteredDf");

            Dataset<Row> mergeStdMdDF = filteredDf.join(
                    stdMdWireCnt,
                    filteredDf.col(joinKeyRaw).equalTo(stdMdWireCnt.col(joinKeyStd)),
                    "left"
            );

            Dataset<Row> result = mergeStdMdDF.select(
                            col(SIM_ID),
                            col(PROD_TYPE),
                            col(DT),
                            col(FIRST_DRAW_TIME),
                            col(LINE_NO),
                            col(LEAD_X),
                            col(LEAD_Y),
                            col(PAD_X),
                            col(PAD_Y),
                            col(CHECK_PORT),
                            col(WIRE_LEN),
                            col(PIECES_INDEX),
                            col(SUB_PROD_TYPE),
                            col(CNT),
                            col(MCS_BY_PIECES_INDEX),
                            col(STD_MOD_LINE_CNT)
                    )
                    .sort(
                            col(SIM_ID),
                            col(PROD_TYPE),
                            col(FIRST_DRAW_TIME),
                            col(LINE_NO)
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