package com.im.inspection.utils.log;

import com.im.inspection.config.DppConfigManager;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.im.inspection.utils.HadoopConfigBuilder.buildHaNoKerberosConfig;


/**
 * 支持日志输出 + HDFS 写入 + Kafka 上报 + 日志压缩归档 + 定时清理
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/05/14 11:38:07
 */
public class TaskTimerRecorder {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(TaskTimerRecorder.class);
    // 多实例支持
    private static final Map<String, TaskTimer> WATCH_REGISTRY = new ConcurrentHashMap<>();
    // 时间格式
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final boolean DEBUG_MODE = DppConfigManager.getInstance().getBoolean("debug.mode.enabled", false);
    // HDFS 配置
    private static boolean enableHdfsWrite = false;
    private static Path baseOutputPath;
    private static FileSystem fs;
    // Kafka 配置
    private static boolean enableKafkaWrite = false;
    private static KafkaReporter kafkaReporter;

    // 后台任务线程池
    private static ScheduledExecutorService scheduler;

    /**
     * 初始化 HDFS 支持（可选）
     */
    public static void initHdfs(Configuration conf, String basePath) throws IOException {
        if (DEBUG_MODE) return;

        fs = FileSystem.get(conf);
        baseOutputPath = new Path(basePath);
        if (!fs.exists(baseOutputPath)) {
            fs.mkdirs(baseOutputPath);
        }
        enableHdfsWrite = true;

        startBackgroundTasks();
    }


    public static void initHdfs(Configuration conf) {
        try {
            initHdfs(conf, "/tmp/task-timer-recorder");
        } catch (IOException e) {
            logger.error(">>>>> Error initializing HDFS.", e);
        }
    }

    public static void initHdfs() {
        initHdfs(buildHaNoKerberosConfig());
    }

    /**
     * 初始化 Kafka 上报模块（可选）
     */
    public static void initKafkaReporter(boolean enable, KafkaReporter reporter) {
        if (DEBUG_MODE) return;

        enableKafkaWrite = enable;
        kafkaReporter = reporter;
    }

    /**
     * 注册一个新的任务计时器
     */
    public static void register(String taskId) {
        String timestamp = now();
        WATCH_REGISTRY.put(taskId, new TaskTimer(taskId, timestamp));
        logInfo("【%s】开始计时", taskId);  // 使用类级别的 logger
        writeToFile(taskId, String.format("[%s] 【%s】 开始计时", timestamp, taskId));
    }

    /**
     * 记录一个阶段耗时
     */
    public static void logStep(String taskId, String stepName) {
        String timestamp = now();
        TaskTimer timer = WATCH_REGISTRY.get(taskId);
        if (timer != null && timer.isRunning()) {
            timer.logStep(stepName, timestamp);
            String message = timer.getLastLog();
            logInfo("%s.", message);
            writeToFile(taskId, message);

            if (enableKafkaWrite && kafkaReporter != null) {
                try {
                    kafkaReporter.report(timer.getLastJson());
                } catch (Exception e) {
                    logger.warn(">>>>> Failed to report to Kafka for task: {}", taskId, e);
                }
            }
        } else {
            register(taskId);
        }
    }

    /**
     * 记录总耗时并清理资源
     */
    public static void logTotalTime(String taskId) {
        String timestamp = now();
        TaskTimer timer = WATCH_REGISTRY.remove(taskId);
        if (timer != null) {
            timer.finish(timestamp);
            String message = timer.getTotalLog();
            logInfo("%s.", message);
            writeToFile(taskId, message);

            if (enableKafkaWrite && kafkaReporter != null) {
                try {
                    kafkaReporter.report(timer.getTotalJson());
                } catch (Exception e) {
                    logger.warn(">>>>> Failed to report total time to Kafka for task: {}", taskId, e);
                }
            }
        }
    }

    /**
     * 日志输出
     */
    private static void logInfo(String format, Object... args) {
        String msg = String.format(format, args);
        if (TaskTimerRecorder.logger != null) {
            TaskTimerRecorder.logger.info(msg);
        } else {
            System.out.println(msg);
        }
    }

    /**
     * 写入 HDFS 文件
     */
    private static void writeToFile(String taskId, String content) {
        if (DEBUG_MODE || !enableHdfsWrite) return;

        try {
            Path taskDir = new Path(baseOutputPath, taskId);
            if (!fs.exists(taskDir)) {
                fs.mkdirs(taskDir);
            }

            String today = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            Path filePath = new Path(taskDir, "timing-" + today + ".log");

            // 使用 append 模式打开文件，若不存在则创建
            boolean appendable = fs.exists(filePath);
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(fs.create(filePath, appendable)))) {  // 注意：create 支持 append 模式
                writer.write(content);
                writer.newLine();
            }

        } catch (IOException e) {
            logger.error(">>>>> Error writing to HDFS file.", e);
        }
    }

    /**
     * 启动后台任务：压缩 & 清理
     */
    private static void startBackgroundTasks() {
        if (DEBUG_MODE) return;
        scheduler = Executors.newSingleThreadScheduledExecutor();

        // 每小时检查一次是否需要压缩
        scheduler.scheduleAtFixedRate(() -> {
            if (enableHdfsWrite) {
                LogArchiver.archiveLogs(fs, baseOutputPath);
            }
        }, 1, 1, TimeUnit.HOURS);

        // 每天凌晨清理 N 天前的日志
        scheduler.scheduleAtFixedRate(() -> {
            if (enableHdfsWrite) {
                LogFileCleaner.cleanupOldFiles(fs, baseOutputPath, 7); // 保留最近 7 天
            }
        }, 24, 24, TimeUnit.HOURS);
    }

    /**
     * 关闭后台线程池
     */
    public static void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    /**
     * 时间单位转换
     */
    public static double convert(long duration, ChronoUnit from, ChronoUnit to) {
        return (double) (duration * from.getDuration().toMillis()) / to.getDuration().toMillis();
    }

    private static String now() {
        return LocalDateTime.now().format(formatter);
    }

    /**
     * Kafka 上报接口（可自定义实现）
     */
    public interface KafkaReporter {
        void report(Map<String, Object> jsonMap);
    }

    /**
     * 单个任务计时器（封装细节）
     */
    private static class TaskTimer {
        private final String taskId;
        private final StopWatch watch;
        private final List<Step> steps = new ArrayList<>();
        private String lastLog = "";
        private Map<String, Object> lastJson = new HashMap<>();

        public TaskTimer(String taskId, String timestamp) {
            this.taskId = taskId;
            this.watch = new StopWatch();
            this.watch.start();
        }

        public boolean isRunning() {
            return this.watch.isStarted();
        }

        public void logStep(String stepName, String timestamp) {
            watch.split(); // 记录上一个 split 到现在的时间间隔
            long timeMs = watch.getSplitTime(); // 获取最近一次 split 的时间差
            double timeSec = TaskTimerRecorder.convert(timeMs, ChronoUnit.MILLIS, ChronoUnit.SECONDS);
            Step step = new Step(stepName, timeMs, timeSec);
            steps.add(step);

            lastLog = String.format("[%s] 【%s】 %s: %.3f 秒", timestamp, taskId, stepName, timeSec);
            lastJson = new HashMap<>();
            lastJson.put("taskId", taskId);
            lastJson.put("step", stepName);
            lastJson.put("time_ms", timeMs);
            lastJson.put("time_sec", timeSec);
        }

        public String getLastLog() {
            return lastLog;
        }

        public Map<String, Object> getLastJson() {
            return lastJson;
        }

        public void finish(String timestamp) {
            watch.stop();
            double totalTimeSec = TaskTimerRecorder.convert(watch.getTime(), ChronoUnit.MILLIS, ChronoUnit.SECONDS);
            lastLog = String.format("[%s] 【%s】 总耗时: %.3f 秒", timestamp, taskId, totalTimeSec);

            lastJson = new HashMap<>();
            lastJson.put("taskId", taskId);
            lastJson.put("step", "total_time");
            lastJson.put("time_ms", watch.getTime());
            lastJson.put("time_sec", totalTimeSec);
        }

        public String getTotalLog() {
            return lastLog;
        }

        public Map<String, Object> getTotalJson() {
            return lastJson;
        }

        public double getTotalTimeSec() {
            return steps.stream().mapToDouble(s -> s.timeSec).sum();
        }

        private static class Step {
            String name;
            long timeMs;
            double timeSec;

            Step(String name, long timeMs, double timeSec) {
                this.name = name;
                this.timeMs = timeMs;
                this.timeSec = timeSec;
            }
        }
    }
}
