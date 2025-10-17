package com.im.inspection.utils;

import com.im.qtech.common.dpp.conf.UnifiedHadoopConfig;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

import static com.im.inspection.utils.Constants.NEED_FILTER_MODULE_TPL_SQL;
import static com.im.inspection.utils.Constants.TPL_SQL;
import static org.apache.spark.sql.functions.col;

/**
 * 工具类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2023/06/12 10:46:44
 */

public class DbUtil {
    private static final Logger logger = LoggerFactory.getLogger(DbUtil.class);

    public static void write2File(String dt, String path) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 0);

        try {
            FileSystem fileSystem = FileSystem.get(UnifiedHadoopConfig.createHadoopConfiguration());

            FSDataOutputStream outputStream = fileSystem.create(new Path(path));

            outputStream.write(dt.getBytes(StandardCharsets.UTF_8));

            fileSystem.close();
        } catch (IOException e) {
            logger.error("write2File error", e);
        }
    }

    public static void upsertOracleEqCtrl(Dataset<Row> df, String url, String user, String password) throws SQLException {

        Connection conn = DriverManager.getConnection(url, user, password);

        PreparedStatement updatePstm = conn.prepareStatement("UPDATE equipment_control_info SET dt = ?," +
                " code = ?, description =? WHERE sim_id = ? AND program_name = 'WB_AD'");

        PreparedStatement insertPstm = conn.prepareStatement("INSERT INTO equipment_control_info(sim_id, " +
                "program_name, dt, code, description) VALUES(?, ?, ?, ?, ?)");

        try {
            for (Row row : df.collectAsList()) {
                updatePstm.setString(1, row.getString(2));
                updatePstm.setString(2, String.valueOf(row.getInt(3)));
                updatePstm.setString(3, row.getString(4));
                updatePstm.setString(4, row.getString(0));
                updatePstm.execute();
                if (updatePstm.getUpdateCount() == 0) {
                    insertPstm.setString(1, row.getString(0));
                    insertPstm.setString(2, "WB_AD");
                    insertPstm.setString(3, row.getString(2));
                    insertPstm.setString(4, String.valueOf(row.getInt(3)));
                    insertPstm.setString(5, row.getString(4));
                    insertPstm.execute();
                }
            }
        } finally {
            conn.close();
            updatePstm.close();
            insertPstm.close();
        }
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

    public static Dataset<Row> getNeedFilterModule(SparkSession ss, String driver, String url, String user, String pwd) {
        return ss.read().format("jdbc")
                .option("driver", driver)
                .option("url", url)
                .option("dbtable", "(" + NEED_FILTER_MODULE_TPL_SQL + ") tmp")
                .option("user", user)
                .option("password", pwd)
                .load();
    }

    public static Dataset<Row> normalizeDfToRawDf(Dataset<Row> df) {
        return df.select(col("sim_id"), col("module"), col("dt"), col("wire_id"), col("lead_x"), col("lead_y"), col("pad_x"), col("pad_y"), col("check_port"),
                col("pieces_index"));
    }
}