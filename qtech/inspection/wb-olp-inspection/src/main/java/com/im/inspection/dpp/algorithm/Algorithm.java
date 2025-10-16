package com.im.inspection.dpp.algorithm;

import com.im.inspection.config.DppConfigManager;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;
import org.im.config.ConfigurationManager;

import java.util.Arrays;

import static com.im.inspection.utils.Constants.*;
import static com.im.inspection.utils.DatasetUtils.unionDfToCheckDf;
import static com.im.inspection.utils.DatasetUtils.unionDfToFullDf;
import static com.im.inspection.utils.DebugModeDataShow.showDataset;
import static org.apache.spark.sql.functions.*;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2021/12/23 16:28
 */
public class Algorithm {

    private static final ConfigurationManager configManager = DppConfigManager.getInstance();
    private static final Boolean flag = configManager.getBoolean("debug.mode.enabled", false);

    public static Dataset<Row> noTemplate(Dataset<Row> df) {
        Column simId = col(SIM_ID);
        Column prodType = col(PROD_TYPE);
        Column dt = col(DT);
        return df.select(simId, prodType, dt)
                .groupBy(simId, prodType)
                .agg(min(DT))
                .withColumnRenamed("min(dt)", DT)
                .withColumn(CODE, lit(3))
                .withColumn(DESCRIPTION, lit("Missing Template."));
    }

    public static Dataset<Row> linkLnEstimate(Dataset<Row> df) {
        Column simId = col(SIM_ID);
        Column prodType = col(PROD_TYPE);
        Column piecesIndex = col(PIECES_INDEX);
        Column lineNo = col(LINE_NO);

        WindowSpec winOrdByPIdx = Window.partitionBy(df.col(SIM_ID), df.col(PROD_TYPE), df.col(PIECES_INDEX)).orderBy(df.col(LINE_NO));
        WindowSpec winOrdByLnAsc = Window.partitionBy(simId, prodType, piecesIndex).orderBy(lineNo.asc());
        WindowSpec winOrdByLnDesc = Window.partitionBy(simId, prodType, piecesIndex).orderBy(lineNo.desc());

        Dataset<Row> minMaxDF = df.withColumn(LINE_NO_ASC, rank().over(winOrdByLnAsc))
                .withColumn(LINE_NO_DESC, rank().over(winOrdByLnDesc))
                .filter(col(LINE_NO_ASC).isin(1, 2).or(col(LINE_NO_DESC).isin(1, 2)))
                .withColumn(WIRE_LABEL, when(lineNo.equalTo(1).or(lineNo.equalTo(2)), lit("doubleMin")).otherwise(lit("doubleMax")))
                .select(simId, prodType, piecesIndex, lineNo, col(WIRE_LABEL), col(WIRE_LEN));

        Dataset<Row> minMaxAggDF = minMaxDF.groupBy(simId, prodType, piecesIndex, col(WIRE_LABEL))
                .agg(sum(WIRE_LEN).alias(WIRE_LEN_TTL))
                .withColumn(WIRE_LEN_TTL_RANK, rank().over(Window.partitionBy(simId, prodType, piecesIndex)
                        .orderBy(col(WIRE_LEN_TTL).asc())))
                .filter(col(WIRE_LEN_TTL_RANK).equalTo(1))
                .select(simId, prodType, piecesIndex, col(WIRE_LABEL)).distinct();

        Dataset<Row> deleteLnMapDF = minMaxDF.join(minMaxAggDF,
                        minMaxDF.col(SIM_ID).equalTo(minMaxAggDF.col(SIM_ID))
                                .and(minMaxDF.col(PROD_TYPE).equalTo(minMaxAggDF.col(PROD_TYPE)))
                                .and(minMaxDF.col(PIECES_INDEX).equalTo(minMaxAggDF.col(PIECES_INDEX)))
                                .and(minMaxDF.col(WIRE_LABEL).equalTo(minMaxAggDF.col(WIRE_LABEL))), "inner")
                .withColumn(CONFIRM_LABEL, lit(0))
                .select(minMaxDF.col(SIM_ID), minMaxDF.col(PROD_TYPE), minMaxDF.col(PIECES_INDEX),
                        minMaxDF.col(LINE_NO), col(CONFIRM_LABEL));

        return df.join(deleteLnMapDF,
                        df.col(SIM_ID).equalTo(deleteLnMapDF.col(SIM_ID))
                                .and(df.col(PROD_TYPE).equalTo(deleteLnMapDF.col(PROD_TYPE)))
                                .and(df.col(PIECES_INDEX).equalTo(deleteLnMapDF.col(PIECES_INDEX)))
                                .and(df.col(LINE_NO).equalTo(deleteLnMapDF.col(LINE_NO))), "left")
                .filter(deleteLnMapDF.col(CONFIRM_LABEL).isNull())
                .withColumn(LINE_NO_MOCK, rank().over(winOrdByPIdx))
                .select(df.col(SIM_ID), df.col(PROD_TYPE), df.col(DT), col(FIRST_DRAW_TIME), col(LINE_NO_MOCK),
                        col(LEAD_X), col(LEAD_Y), col(PAD_X), col(PAD_Y), col(CHECK_PORT), df.col(PIECES_INDEX),
                        col(SUB_PROD_TYPE), col(CNT), col(WIRE_LEN))
                .withColumnRenamed(LINE_NO_MOCK, LINE_NO)
                .sort(col(SIM_ID), col(PROD_TYPE), col(FIRST_DRAW_TIME), col(LINE_NO));
    }

    public static Dataset<Row> diff(Dataset<Row> df) {
        Column simId = col(SIM_ID);
        Column prodType = col(PROD_TYPE);
        Column firstDrawTime = col(FIRST_DRAW_TIME);
        Column lineNo = col(LINE_NO);

        WindowSpec w_len = Window.partitionBy(simId, prodType, firstDrawTime, col(PIECES_INDEX)).orderBy(lineNo.asc());

        Column leadX = col(LEAD_X);
        Column leadY = col(LEAD_Y);
        Column padX = col(PAD_X);
        Column padY = col(PAD_Y);

        Column leadXLag = lag(LEAD_X, 1).over(w_len);
        Column leadYLag = lag(LEAD_Y, 1).over(w_len);
        Column padXLag = lag(PAD_X, 1).over(w_len);
        Column padYLag = lag(PAD_Y, 1).over(w_len);

        return df.withColumn(LEAD_X_LAG, leadXLag)
                .withColumn(LEAD_Y_LAG, leadYLag)
                .withColumn(PAD_X_LAG, padXLag)
                .withColumn(PAD_Y_LAG, padYLag)
                .withColumn(LEAD_LEN, sqrt(pow(leadX.minus(leadXLag), 2).plus(pow(leadY.minus(leadYLag), 2))))
                .withColumn(PAD_LEN, sqrt(pow(padX.minus(padXLag), 2).plus(pow(padY.minus(padYLag), 2))))
                .select(simId, prodType, col(DT), firstDrawTime, lineNo, leadX, leadY, padX, padY,
                        col(LEAD_LEN), col(PAD_LEN), col(CHECK_PORT), col(PIECES_INDEX), col(SUB_PROD_TYPE), col(WIRE_LEN));
    }

    public static Dataset<Row> fullLnMkSta(Dataset<Row> df, Dataset<Row> stdModelDf) {
        Dataset<Row> stdModDF = stdModelDf.select(
                col(STD_SIM_ID), col(STD_LINE_NO), col(STD_LEAD_DIFF), col(STD_PAD_DIFF),
                col(LEAD_THRESHOLD), col(PAD_THRESHOLD), col(STD_WIRE_LEN));

        return df.join(stdModDF, df.col(SUB_PROD_TYPE).equalTo(col(STD_SIM_ID)).and(df.col(LINE_NO).equalTo(col(STD_LINE_NO))), "left")
                .withColumn(LEAD_OFFSET, col(LEAD_LEN).minus(col(STD_LEAD_DIFF)))
                .withColumn(PAD_OFFSET, col(PAD_LEN).minus(col(STD_PAD_DIFF)))
                .withColumn(CODE,
                        when(abs(col(LEAD_OFFSET)).gt(col(LEAD_THRESHOLD)).or(abs(col(PAD_OFFSET)).gt(col(PAD_THRESHOLD))), 1)
                                .otherwise(lit(0)))
                .select(
                        col(SIM_ID), col(PROD_TYPE), col(DT), col(FIRST_DRAW_TIME), col(LINE_NO),
                        col(LEAD_X), col(LEAD_Y), col(PAD_X), col(PAD_Y),
                        col(LEAD_LEN), col(PAD_LEN), col(CHECK_PORT), col(PIECES_INDEX),
                        col(SUB_PROD_TYPE), col(STD_LEAD_DIFF), col(STD_PAD_DIFF),
                        col(LEAD_THRESHOLD), col(PAD_THRESHOLD), col(LEAD_OFFSET), col(PAD_OFFSET), col(CODE));
    }

    public static Dataset<Row> fullLn2doc(Dataset<Row> df) {
        // 1. 构造 DESCRIPTION 字段
        Dataset<Row> withDesc = df.withColumn(
                DESCRIPTION,
                when(col(CODE).equalTo(1),
                        format_string("ln%sLeOf%.1fPaOf%.1f", col(LINE_NO), col(LEAD_OFFSET), col(PAD_OFFSET)))
                        .otherwise("qualified")
        );

        // 2. 构造 (LINE_NO, DESCRIPTION) 的 struct
        Dataset<Row> withStruct = withDesc.withColumn(
                "desc_struct",
                struct(col(LINE_NO), col(DESCRIPTION))
        );

        // 3. 分组聚合：最大 CODE，collect_list struct 并按 LINE_NO 排序，过滤 null，再拼接
        Dataset<Row> aggregated = withStruct.groupBy(col(SIM_ID), col(PROD_TYPE), col(FIRST_DRAW_TIME))
                .agg(
                        max(CODE).as(CODE),
                        expr("concat_ws(';', transform(filter(sort_array(collect_list(desc_struct), true), x -> x.DESCRIPTION != 'qualified'), x -> x.DESCRIPTION))")
                                .as(DESCRIPTION)
                );

        // 4. 如果全是 qualified 则拼成 "qualified"，并重命名列
        return aggregated
                .withColumn(DESCRIPTION, when(col(DESCRIPTION).equalTo(""), "qualified").otherwise(col(DESCRIPTION)))
                .withColumnRenamed(FIRST_DRAW_TIME, DT)
                .select(col(SIM_ID), col(PROD_TYPE), col(DT), col(CODE), col(DESCRIPTION));
    }

    public static Dataset<Row> lackLn2doc(Dataset<Row> df) {
        Dataset<Row> groupDF = lackAndOverGroup(df);
        return groupDF.withColumn(CODE, lit(2))
                .withColumn(DESCRIPTION,
                        format_string("less than required: [actual/required: %s/%s]",
                                col(CHECK_PORT),
                                when(col(CHECK_PORT).lt(col(STD_MOD_LINE_CNT)), col(STD_MOD_LINE_CNT))
                                        .otherwise(col(STD_MOD_LINE_CNT).plus(2))))
                .select(col(SIM_ID), col(PROD_TYPE), col(DT), col(CODE), col(DESCRIPTION));
    }

    public static Dataset<Row> overLn2doc(Dataset<Row> df) {
        Dataset<Row> groupDF = lackAndOverGroup(df);
        return groupDF.withColumn(CODE, lit(4))
                .withColumn(DESCRIPTION,
                        format_string("more than required: [actual/required: %s/%s]",
                                col(CHECK_PORT),
                                when(col(CHECK_PORT).equalTo(col(STD_MOD_LINE_CNT).plus(1)), col(STD_MOD_LINE_CNT))
                                        .otherwise(col(STD_MOD_LINE_CNT).plus(2))))
                .select(col(SIM_ID), col(PROD_TYPE), col(DT), col(CODE), col(DESCRIPTION));
    }

    public static Dataset<Row> lackAndOverGroup(Dataset<Row> df) {
        return df.select(col(SIM_ID), col(PROD_TYPE), col(FIRST_DRAW_TIME), col(LINE_NO), col(CHECK_PORT), col(STD_MOD_LINE_CNT))
                .groupBy(col(SIM_ID), col(PROD_TYPE), col(FIRST_DRAW_TIME))
                .agg(max(col(CHECK_PORT)).as(CHECK_PORT), max(col(STD_MOD_LINE_CNT)).as(STD_MOD_LINE_CNT))
                .withColumnRenamed(FIRST_DRAW_TIME, DT);
    }

    public static Dataset<Row> doIntegrate(Dataset<Row> processedDf, Dataset<Row> stdModels) {
        Column stdModLineCnt = col(STD_MOD_LINE_CNT);
        Column checkPort = col(CHECK_PORT);
        Column cnt = col(CNT);

        // 无模板数据
        Dataset<Row> noTemplateDf = Algorithm.noTemplate(processedDf.where(stdModLineCnt.isNull()));
        showDataset(noTemplateDf, flag, "noTemplateDf");

        // 缺线数据
        Dataset<Row> lackLineData = processedDf.filter(
                checkPort.lt(stdModLineCnt)
                        .or(checkPort.equalTo(stdModLineCnt.plus(1)))
        );
        showDataset(lackLineData, flag, "lackLineData");

        // 全线数据（加上 cnt 校验）
        Dataset<Row> fullLnDa = processedDf.filter(
                checkPort.equalTo(stdModLineCnt)
                        .and(checkPort.equalTo(cnt))
        );
        showDataset(fullLnDa, flag, "fullLnDa");

        // 连线数据（加上 cnt 校验）
        Dataset<Row> linkLnDa = processedDf.filter(
                checkPort.equalTo(stdModLineCnt.plus(2))
                        .and(checkPort.equalTo(cnt))
        );
        showDataset(linkLnDa, flag, "linkLnDa");

        // 多线数据
        Dataset<Row> overLnDa = processedDf.filter(
                checkPort.equalTo(stdModLineCnt.plus(1))
                        .or(checkPort.gt(stdModLineCnt.plus(2)))
        );
        showDataset(overLnDa, flag, "overLnDa");

        // 转换为描述性文档
        Dataset<Row> lackLn2doc = Algorithm.lackLn2doc(lackLineData);
        Dataset<Row> overLn2doc = Algorithm.overLn2doc(overLnDa);
        Dataset<Row> linkedLineToFullLine = Algorithm.linkLnEstimate(linkLnDa);

        Dataset<Row> fullLineTotal = unionDfToFullDf(fullLnDa, linkedLineToFullLine);
        Dataset<Row> fullLnDiff = Algorithm.diff(fullLineTotal);
        Dataset<Row> fullLnSta = Algorithm.fullLnMkSta(fullLnDiff, stdModels);
        Dataset<Row> fullLn2doc = Algorithm.fullLn2doc(fullLnSta);

        showDataset(fullLnDiff, flag, "fullLnDiff");
        showDataset(fullLnSta, flag, "fullLnSta");

        return unionDfToCheckDf(Arrays.asList(noTemplateDf, lackLn2doc, fullLn2doc, overLn2doc));
    }
}
