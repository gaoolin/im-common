package com.im.qtech.data.util;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/07/30 10:15:03
 */

public class Constants {
    public static final String ORACLE_URL = "jdbc:oracle:thin:@//10.170.6.144:1521/qtechIm";
    public static final String ORACLE_USER = "imBiz";
    public static final String ORACLE_PASSWORD = "M5F5JkfnQ";
    public static final String ORACLE_DRIVER_CLASS = "oracle.jdbc.OracleDriver";

    // 优化的 MERGE 语句，添加 ORDER BY 保证处理顺序一致
    public static final String ORACLE_SQL = String.join("\n",
            "MERGE /*+ PARALLEL(t, 4) */ INTO IM_EQ_ONLINE_STATUS t",
            "USING (SELECT ? AS DEVICE_ID,",
            "              ? AS RECEIVE_DATE,",
            "              ? AS DEVICE_TYPE,",
            "              ? AS LOT_NAME,",
            "              ? AS STATUS,",
            "              ? AS REMOTE_CONTROL,",
            "              ? AS LAST_UPDATE",
            "       FROM DUAL ORDER BY DEVICE_ID) s", // 添加 ORDER BY 确保顺序一致
            "ON (t.DEVICE_ID = s.DEVICE_ID)",
            "WHEN MATCHED THEN",
            "  UPDATE SET",
            "    t.RECEIVE_DATE = s.RECEIVE_DATE,",
            "    t.DEVICE_TYPE = s.DEVICE_TYPE,",
            "    t.LOT_NAME = s.LOT_NAME,",
            "    t.STATUS = s.STATUS,",
            "    t.REMOTE_CONTROL = s.REMOTE_CONTROL,",
            "    t.LAST_UPDATE = s.LAST_UPDATE",
            "WHEN NOT MATCHED THEN",
            "  INSERT (DEVICE_ID, RECEIVE_DATE, DEVICE_TYPE, LOT_NAME, STATUS, REMOTE_CONTROL, LAST_UPDATE)",
            "  VALUES (s.DEVICE_ID, s.RECEIVE_DATE, s.DEVICE_TYPE, s.LOT_NAME, s.STATUS, s.REMOTE_CONTROL, s.LAST_UPDATE)"
    );

    public static final int ORACLE_BATCH_SIZE = 200;         // 减小批量大小
    public static final int ORACLE_MAX_RETRIES = 5;          // 增加重试次数
    public static final long ORACLE_BATCH_INTERVAL_MS = 2000; // 缩短批量间隔
    public static final int ORACLE_MAX_POOL_SIZE = 10;
    // 连接池配置
    public static final int ORACLE_INITIAL_SIZE = 5;
    public static final int ORACLE_MIN_IDLE = 2;
    public static final int ORACLE_MAX_ACTIVE = 15;          // 减少最大连接数
    public static final String ORACLE_DATA_SOURCE_CLASS = "oracle.jdbc.pool.OracleDataSource";


    public static final String POSTGRES_URL = "jdbc:postgresql://10.170.6.142:5432/imbiz";
    public static final String POSTGRES_USER = "qtech";
    public static final String POSTGRES_PASSWORD = "Ee786549!";
    public static final String POSTGRES_DRIVER_CLASS = "org.postgresql.Driver";
    public static final String POSTGRES_SQL = String.join("\n",
            "INSERT INTO im_eq_online_status (device_id, receive_date, device_type, lot_name, status, remote_control, last_update)",
            "VALUES (?, ?, ?, ?, ?, ?, ?)",
            "ON CONFLICT (device_id)",
            "DO UPDATE SET",
            "    receive_date = EXCLUDED.receive_date,",
            "    device_type = EXCLUDED.device_type,",
            "    lot_name = EXCLUDED.lot_name,",
            "    status = EXCLUDED.status,",
            "    remote_control = EXCLUDED.remote_control,",
            "    last_update = EXCLUDED.last_update"
    );
    public static final int POSTGRES_BATCH_SIZE = 200;
    public static final int POSTGRES_MAX_RETRIES = 5;
    public static final long POSTGRES_BATCH_INTERVAL_MS = 2000;
    public static final int POSTGRES_MAX_POOL_SIZE = 10;
    public static final int POSTGRES_INITIAL_SIZE = 5;
    public static final int POSTGRES_MIN_IDLE = 2;
    public static final int POSTGRES_MAX_ACTIVE = 15;
    public static final String POSTGRES_DATA_SOURCE_CLASS = "org.postgresql.ds.PGSimpleDataSource";


    // 线程池管理
    // 默认线程数，轻量设计，避免资源浪费
    public static final int DEFAULT_THREAD_POOL_SIZE = 8;
    // 高并发场景线程数（CPU核心数的1-2倍）
    public static final int HIGH_CONCURRENCY_THREAD_POOL_SIZE = 16;
}
