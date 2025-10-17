package com.im.inspection.utils;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.Arrays;
import java.util.List;

import static org.apache.spark.sql.functions.col;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/03 22:41:00
 */
public class DatasetUtils {

    // 定义公共列名常量，便于维护
    private static final String[] FULL_COLS = {
            "sim_id", "mc_id", "dt", "first_draw_time",
            "line_no", "lead_x", "lead_y", "pad_x", "pad_y",
            "check_port", "pieces_index", "sub_mc_id", "cnt", "wire_len"
    };

    private static final String[] CHECK_COLS = {
            "sim_id", "mc_id", "dt", "code", "description"
    };

    public static Dataset<Row> unionDfToFullDf(Dataset<Row> firstDf, Dataset<Row> secondDf) {
        Dataset<Row> alignFirstDf = firstDf.select(toColumns(FULL_COLS));
        Dataset<Row> alignSecondDf = secondDf.select(toColumns(FULL_COLS));
        return alignFirstDf.union(alignSecondDf)
                .sort(col("sim_id"), col("mc_id"), col("first_draw_time"), col("pieces_index"), col("line_no"));
    }

    public static Dataset<Row> unionDfToCheckDf(List<Dataset<Row>> seqDF) {
        if (seqDF.isEmpty()) {
            throw new IllegalArgumentException("The list of dataframes is empty in unionDfToCheckDf.");
        }

        Column[] cols = toColumns(CHECK_COLS);

        return seqDF.stream()
                .map(df -> df.select(cols))
                .reduce(Dataset::union)
                .orElseThrow(() -> new IllegalStateException("Union reduction failed unexpectedly."));
    }

    // 辅助方法：将字符串数组转换为 Column 数组
    private static Column[] toColumns(String[] columnNames) {
        return Arrays.stream(columnNames)
                .map(org.apache.spark.sql.functions::col)
                .toArray(Column[]::new);
    }
}

