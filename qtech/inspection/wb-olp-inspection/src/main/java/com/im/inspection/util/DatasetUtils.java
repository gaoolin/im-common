package com.im.inspection.util;

import com.im.qtech.common.dpp.conf.UnifiedHadoopConfig;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static com.im.inspection.util.Constant.NEED_FILTER_MODULE_TPL_SQL;
import static com.im.inspection.util.Constant.TPL_SQL;
import static org.apache.spark.sql.functions.col;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/03 22:41:00
 */
public class DatasetUtils {

    // 定义公共列名常量，便于维护
    private static final String[] FULL_COLS = {
            "sim_id", "module", "dt", "first_draw_time",
            "wire_id", "lead_x", "lead_y", "pad_x", "pad_y",
            "check_port", "pieces_index", "norm_module", "cnt", "wire_len"
    };

    private static final String[] CHECK_COLS = {
            "sim_id", "module", "dt", "code", "description"
    };

    public static Dataset<Row> unionDfToFullDf(Dataset<Row> firstDf, Dataset<Row> secondDf) {
        Dataset<Row> alignFirstDf = firstDf.select(toColumns(FULL_COLS));
        Dataset<Row> alignSecondDf = secondDf.select(toColumns(FULL_COLS));
        return alignFirstDf.union(alignSecondDf)
                .sort(col("sim_id"), col("module"), col("first_draw_time"), col("pieces_index"), col("wire_id"));
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

    public static Dataset<Row> getNeedFilterModule(SparkSession ss, String driver, String url, String user, String pwd) {
        return ss.read().format("jdbc")
                .option("driver", driver)
                .option("url", url)
                .option("dbtable", "(" + NEED_FILTER_MODULE_TPL_SQL + ") tmp")
                .option("user", user)
                .option("password", pwd)
                .load();
    }

    public static Dataset<Row> getTpl(SparkSession ss, String driver, String url, String user, String pwd) {
        return ss.read().format("jdbc")
                .option("driver", driver)
                .option("url", url)
                .option("dbtable", "(" + TPL_SQL + ") tmp")
                .option("user", user)
                .option("password", pwd)
                .load();
    }

    public static void write2File(String dt, String path) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 0);

        try {
            FileSystem fileSystem = FileSystem.get(UnifiedHadoopConfig.createHadoopConfiguration());

            FSDataOutputStream outputStream = fileSystem.create(new Path(path));

            outputStream.write(dt.getBytes(StandardCharsets.UTF_8));

            fileSystem.close();
        } catch (IOException e) {
            // 处理异常
        }
    }
}

