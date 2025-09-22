package org.im.semiconductor.equipment.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 设备状态监控工具类
 * <p>
 * 特性：
 * - 通用性：支持多种设备类型监控
 * - 规范性：统一的监控接口和数据结构
 * - 专业性：半导体设备专业监控算法
 * - 灵活性：支持自定义监控策略
 * - 可靠性：高可用监控架构
 * - 安全性：监控数据安全传输
 * - 复用性：监控组件可配置复用
 * - 容错性：监控失败自动恢复机制
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/08/20
 */
public class EquipmentMonitor {

    // 默认监控间隔（毫秒）
    public static final long DEFAULT_MONITOR_INTERVAL = 5000;
    // 默认预测窗口（毫秒）
    public static final long DEFAULT_PREDICTION_WINDOW = 300000; // 5分钟
    private static final Logger logger = LoggerFactory.getLogger(EquipmentMonitor.class);
    // 监控线程池
    private static final ScheduledExecutorService monitorExecutor =
            Executors.newScheduledThreadPool(10, new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "EquipmentMonitor-" + threadNumber.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                }
            });

    // 监控任务映射
    private static final Map<String, ScheduledFuture<?>> monitorTasks = new ConcurrentHashMap<>();

    /**
     * 开始监控设备
     *
     * @param equipmentId 设备ID
     * @param listener    状态监听器
     * @param config      监控配置
     * @return 是否启动成功
     */
    public static boolean startMonitoring(String equipmentId, StatusListener listener, MonitorConfig config) {
        if (equipmentId == null || equipmentId.isEmpty() || listener == null) {
            logger.error("Invalid equipment ID or listener");
            return false;
        }

        if (config == null) {
            config = new MonitorConfig();
        }

        // 检查是否已经在监控
        if (monitorTasks.containsKey(equipmentId)) {
            logger.warn("Equipment {} is already being monitored", equipmentId);
            return true;
        }

        try {
            // 创建监控任务
            ScheduledFuture<?> task = monitorExecutor.scheduleAtFixedRate(
                    new MonitorTask(equipmentId, listener, config),
                    0, // 立即开始
                    config.getInterval(),
                    TimeUnit.MILLISECONDS
            );

            // 保存任务引用
            monitorTasks.put(equipmentId, task);

            logger.info("Started monitoring equipment: {} with interval: {}ms",
                    equipmentId, config.getInterval());
            return true;
        } catch (Exception e) {
            logger.error("Failed to start monitoring for equipment: {}", equipmentId, e);
            return false;
        }
    }

    /**
     * 停止监控设备
     *
     * @param equipmentId 设备ID
     * @return 是否停止成功
     */
    public static boolean stopMonitoring(String equipmentId) {
        if (equipmentId == null || equipmentId.isEmpty()) {
            logger.error("Invalid equipment ID");
            return false;
        }

        ScheduledFuture<?> task = monitorTasks.remove(equipmentId);
        if (task != null) {
            task.cancel(true);
            logger.info("Stopped monitoring equipment: {}", equipmentId);
            return true;
        } else {
            logger.warn("Equipment {} is not being monitored", equipmentId);
            return false;
        }
    }

    /**
     * 获取当前设备状态
     *
     * @param equipmentId 设备ID
     * @return 设备状态
     */
    public static EquipmentStatus getCurrentStatus(String equipmentId) {
        if (equipmentId == null || equipmentId.isEmpty()) {
            logger.error("Invalid equipment ID");
            return null;
        }

        try {
            // 获取设备当前状态
            EquipmentStatus status = doGetCurrentStatus(equipmentId);

            logger.debug("Retrieved current status for equipment {}: {}", equipmentId, status.getState());
            return status;
        } catch (Exception e) {
            logger.error("Exception occurred while getting current status for equipment: {}", equipmentId, e);
            return new EquipmentStatus(equipmentId, EquipmentState.ERROR, System.currentTimeMillis());
        }
    }

    /**
     * 实际获取设备当前状态
     *
     * @param equipmentId 设备ID
     * @return 设备状态
     */
    private static EquipmentStatus doGetCurrentStatus(String equipmentId) {
        // 实现具体的设备状态获取逻辑
        // 可能涉及设备通信、数据库查询等
        return new EquipmentStatus(equipmentId, EquipmentState.RUNNING, System.currentTimeMillis());
    }

    /**
     * 计算设备健康度评分
     *
     * @param equipmentId 设备ID
     * @return 健康度评分
     */
    public static HealthScore calculateHealthScore(String equipmentId) {
        if (equipmentId == null || equipmentId.isEmpty()) {
            logger.error("Invalid equipment ID");
            return new HealthScore(equipmentId, 0.0, "Invalid equipment ID");
        }

        try {
            // 获取设备历史数据
            List<EquipmentStatus> history = getEquipmentHistory(equipmentId, 3600000); // 1小时

            if (history.isEmpty()) {
                logger.warn("No history data available for equipment: {}", equipmentId);
                return new HealthScore(equipmentId, 0.5, "No history data");
            }

            // 计算健康度评分
            HealthScore score = doCalculateHealthScore(history);

            logger.debug("Calculated health score for equipment {}: {} ({})",
                    equipmentId, String.format("%.2f", score.getScore() * 100), score.getDescription());
            return score;
        } catch (Exception e) {
            logger.error("Exception occurred while calculating health score for equipment: {}", equipmentId, e);
            return new HealthScore(equipmentId, 0.0, "Calculation error: " + e.getMessage());
        }
    }

    /**
     * 实际计算健康度评分
     *
     * @param history 历史状态数据
     * @return 健康度评分
     */
    private static HealthScore doCalculateHealthScore(List<EquipmentStatus> history) {
        if (history == null || history.isEmpty()) {
            return new HealthScore("", 0.0, "No data");
        }

        String equipmentId = history.get(0).getEquipmentId();
        int totalPoints = history.size();
        int healthyPoints = 0;

        // 统计健康状态点数
        for (EquipmentStatus status : history) {
            if (status.getState() == EquipmentState.RUNNING ||
                    status.getState() == EquipmentState.IDLE) {
                healthyPoints++;
            }
        }

        double score = totalPoints > 0 ? (double) healthyPoints / totalPoints : 0.0;
        String description = String.format("Healthy: %d/%d points", healthyPoints, totalPoints);

        return new HealthScore(equipmentId, score, description);
    }

    /**
     * 预测设备故障
     *
     * @param equipmentId 设备ID
     * @param config      监控配置
     * @return 故障预测结果
     */
    public static List<FaultPrediction> predictFaults(String equipmentId, MonitorConfig config) {
        if (equipmentId == null || equipmentId.isEmpty()) {
            logger.error("Invalid equipment ID");
            return new ArrayList<>();
        }

        if (config == null) {
            config = new MonitorConfig();
        }

        if (!config.isEnablePrediction()) {
            logger.debug("Fault prediction is disabled for equipment: {}", equipmentId);
            return new ArrayList<>();
        }

        try {
            // 获取设备历史数据
            long window = config.getPredictionWindow() > 0 ? config.getPredictionWindow() : DEFAULT_PREDICTION_WINDOW;
            List<EquipmentStatus> history = getEquipmentHistory(equipmentId, window);

            if (history == null || history.isEmpty()) {
                logger.warn("No history data available for fault prediction of equipment: {}", equipmentId);
                return new ArrayList<>();
            }

            // 执行故障预测
            List<FaultPrediction> predictions = doPredictFaults(history);

            logger.debug("Predicted {} potential faults for equipment: {}", predictions.size(), equipmentId);
            return predictions;
        } catch (Exception e) {
            logger.error("Exception occurred while predicting faults for equipment: {}", equipmentId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 实际执行故障预测
     *
     * @param history 历史状态数据
     * @return 故障预测结果
     */
    private static List<FaultPrediction> doPredictFaults(List<EquipmentStatus> history) {
        // 实现具体的故障预测算法
        // 可以使用机器学习、统计分析等方法
        List<FaultPrediction> predictions = new ArrayList<>();

        // 简单示例：如果最近有错误状态，则预测可能故障
        boolean hasRecentError = history.stream()
                .filter(status -> System.currentTimeMillis() - status.getTimestamp() < 300000) // 5分钟内
                .anyMatch(status -> status.getState() == EquipmentState.ERROR);

        if (hasRecentError) {
            predictions.add(new FaultPrediction(
                    "General",
                    0.7,
                    "Recent error detected",
                    System.currentTimeMillis() + 600000 // 预测10分钟后可能发生故障
            ));
        }

        return predictions;
    }

    /**
     * 获取设备历史状态
     *
     * @param equipmentId 设备ID
     * @param timeWindow  时间窗口（毫秒）
     * @return 历史状态列表
     */
    private static List<EquipmentStatus> getEquipmentHistory(String equipmentId, long timeWindow) {
        // 实现历史数据获取逻辑
        // 可能涉及数据库查询、缓存读取等
        List<EquipmentStatus> history = new ArrayList<>();

        // 添加一些示例数据
        long now = System.currentTimeMillis();
        history.add(new EquipmentStatus(equipmentId, EquipmentState.RUNNING, now));
        history.add(new EquipmentStatus(equipmentId, EquipmentState.IDLE, now - 60000));
        history.add(new EquipmentStatus(equipmentId, EquipmentState.RUNNING, now - 120000));

        return history;
    }

    /**
     * 生成设备利用率报告
     *
     * @param equipmentId 设备ID
     * @param timeRange   时间范围
     * @return 利用率报告
     */
    public static UtilizationReport generateUtilizationReport(String equipmentId, TimeRange timeRange) {
        if (equipmentId == null || equipmentId.isEmpty() || timeRange == null) {
            logger.error("Invalid equipment ID or time range");
            return null;
        }

        try {
            // 获取指定时间范围内的设备状态历史
            List<EquipmentStatus> history = getEquipmentHistoryInRange(equipmentId, timeRange);

            if (history == null || history.isEmpty()) {
                logger.warn("No history data available for utilization report of equipment: {}", equipmentId);
                return new UtilizationReport(equipmentId, timeRange, 0.0, new HashMap<>());
            }

            // 计算利用率
            UtilizationReport report = calculateUtilization(equipmentId, history, timeRange);

            logger.debug("Generated utilization report for equipment {}: {}%",
                    equipmentId, String.format("%.2f", report.getOverallUtilization() * 100));
            return report;
        } catch (Exception e) {
            logger.error("Exception occurred while generating utilization report for equipment: {}", equipmentId, e);
            return null;
        }
    }

    /**
     * 获取指定时间范围内的设备历史状态
     *
     * @param equipmentId 设备ID
     * @param timeRange   时间范围
     * @return 历史状态列表
     */
    private static List<EquipmentStatus> getEquipmentHistoryInRange(String equipmentId, TimeRange timeRange) {
        // 实现指定时间范围历史数据获取逻辑
        return getEquipmentHistory(equipmentId, timeRange.getEnd() - timeRange.getStart());
    }

    /**
     * 计算设备利用率
     *
     * @param equipmentId 设备ID
     * @param history     历史状态数据
     * @param timeRange   时间范围
     * @return 利用率报告
     */
    private static UtilizationReport calculateUtilization(String equipmentId, List<EquipmentStatus> history, TimeRange timeRange) {
        Map<EquipmentState, Long> stateDurations = new EnumMap<>(EquipmentState.class);
        long totalTime = timeRange.getEnd() - timeRange.getStart();

        // 初始化各状态持续时间
        for (EquipmentState state : EquipmentState.values()) {
            stateDurations.put(state, 0L);
        }

        // 计算各状态持续时间
        if (history != null && history.size() > 1) {
            for (int i = 0; i < history.size() - 1; i++) {
                EquipmentStatus current = history.get(i);
                EquipmentStatus next = history.get(i + 1);

                long duration = next.getTimestamp() - current.getTimestamp();
                stateDurations.merge(current.getState(), duration, Long::sum);
            }
        }

        // 计算生产状态时间（运行+设置）
        long productiveTime = stateDurations.getOrDefault(EquipmentState.RUNNING, 0L) +
                stateDurations.getOrDefault(EquipmentState.SETUP, 0L);

        double overallUtilization = totalTime > 0 ? (double) productiveTime / totalTime : 0.0;

        return new UtilizationReport(equipmentId, timeRange, overallUtilization, stateDurations);
    }

    /**
     * 关闭监控器
     */
    public static void shutdown() {
        // 停止所有监控任务
        for (Map.Entry<String, ScheduledFuture<?>> entry : monitorTasks.entrySet()) {
            entry.getValue().cancel(true);
        }
        monitorTasks.clear();

        // 关闭线程池
        monitorExecutor.shutdown();
        try {
            if (!monitorExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                monitorExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            monitorExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logger.info("Equipment monitor shutdown completed");
    }

    /**
     * 设备状态枚举
     */
    public enum EquipmentState {
        UNKNOWN,    // 未知状态
        RUNNING,    // 运行中
        IDLE,       // 空闲
        SETUP,      // 设置中
        MAINTENANCE,// 维护中
        ERROR,      // 错误
        OFFLINE     // 离线
    }

    /**
     * 状态监听器接口
     */
    public interface StatusListener {
        void onStatusUpdate(EquipmentStatus status);

        void onError(EquipmentMonitoringException exception);
    }

    /**
     * 监控配置类
     */
    public static class MonitorConfig {
        private long interval = DEFAULT_MONITOR_INTERVAL;
        private long predictionWindow = DEFAULT_PREDICTION_WINDOW;
        private boolean enablePrediction = true;
        private int maxRetries = 3;
        private long retryDelay = 1000;

        // Getters and Setters
        public long getInterval() {
            return interval;
        }

        public MonitorConfig setInterval(long interval) {
            this.interval = interval;
            return this;
        }

        public long getPredictionWindow() {
            return predictionWindow;
        }

        public MonitorConfig setPredictionWindow(long predictionWindow) {
            this.predictionWindow = predictionWindow;
            return this;
        }

        public boolean isEnablePrediction() {
            return enablePrediction;
        }

        public MonitorConfig setEnablePrediction(boolean enablePrediction) {
            this.enablePrediction = enablePrediction;
            return this;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public MonitorConfig setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public long getRetryDelay() {
            return retryDelay;
        }

        public MonitorConfig setRetryDelay(long retryDelay) {
            this.retryDelay = retryDelay;
            return this;
        }
    }

    /**
     * 监控任务类
     */
    private static class MonitorTask implements Runnable {
        private final String equipmentId;
        private final StatusListener listener;
        private final MonitorConfig config;
        private int retryCount = 0;

        public MonitorTask(String equipmentId, StatusListener listener, MonitorConfig config) {
            this.equipmentId = equipmentId;
            this.listener = listener;
            this.config = config;
        }

        @Override
        public void run() {
            try {
                // 获取设备状态
                EquipmentStatus status = doGetCurrentStatus(equipmentId);

                if (status != null) {
                    // 通知监听器
                    listener.onStatusUpdate(status);
                    retryCount = 0; // 重置重试计数
                } else {
                    handleFailure("Failed to get equipment status");
                }
            } catch (Exception e) {
                handleFailure("Exception in monitoring task: " + e.getMessage());
            }
        }

        private void handleFailure(String errorMessage) {
            logger.warn("Monitoring task failed for equipment {}: {}", equipmentId, errorMessage);

            if (retryCount < config.getMaxRetries()) {
                retryCount++;
                logger.info("Retrying monitoring task for equipment {} (attempt {}/{})",
                        equipmentId, retryCount, config.getMaxRetries());

                // 延迟重试
                try {
                    Thread.sleep(config.getRetryDelay() * retryCount);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                logger.error("Max retries exceeded for equipment: {}", equipmentId);
                listener.onError(new EquipmentMonitoringException(
                        "Max retries exceeded for equipment: " + equipmentId));
                retryCount = 0; // 重置重试计数
            }
        }
    }

    /**
     * 设备监控异常类
     */
    public static class EquipmentMonitoringException extends Exception {
        public EquipmentMonitoringException(String message) {
            super(message);
        }

        public EquipmentMonitoringException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 设备状态类
     */
    public static class EquipmentStatus {
        private final String equipmentId;
        private final EquipmentState state;
        private final long timestamp;
        private final Map<String, Object> attributes;

        public EquipmentStatus(String equipmentId, EquipmentState state, long timestamp) {
            this.equipmentId = equipmentId;
            this.state = state;
            this.timestamp = timestamp;
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters
        public String getEquipmentId() {
            return equipmentId;
        }

        public EquipmentState getState() {
            return state;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public void setAttribute(String key, Object value) {
            attributes.put(key, value);
        }

        public Object getAttribute(String key) {
            return attributes.get(key);
        }

        @Override
        public String toString() {
            return "EquipmentStatus{" +
                    "equipmentId='" + equipmentId + '\'' +
                    ", state=" + state +
                    ", timestamp=" + timestamp +
                    ", attributes=" + attributes.size() +
                    '}';
        }
    }

    /**
     * 健康度评分类
     */
    public static class HealthScore {
        private final String equipmentId;
        private final double score;
        private final String description;

        public HealthScore(String equipmentId, double score, String description) {
            this.equipmentId = equipmentId;
            this.score = Math.max(0.0, Math.min(1.0, score)); // 限制在0-1之间
            this.description = description != null ? description : "";
        }

        // Getters
        public String getEquipmentId() {
            return equipmentId;
        }

        public double getScore() {
            return score;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return "HealthScore{" +
                    "equipmentId='" + equipmentId + '\'' +
                    ", score=" + String.format("%.2f", score * 100) + "%" +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

    /**
     * 故障预测类
     */
    public static class FaultPrediction {
        private final String faultType;
        private final double probability;
        private final String reason;
        private final long predictedTime;

        public FaultPrediction(String faultType, double probability, String reason, long predictedTime) {
            this.faultType = faultType != null ? faultType : "Unknown";
            this.probability = Math.max(0.0, Math.min(1.0, probability)); // 限制在0-1之间
            this.reason = reason != null ? reason : "";
            this.predictedTime = predictedTime > 0 ? predictedTime : System.currentTimeMillis();
        }

        // Getters
        public String getFaultType() {
            return faultType;
        }

        public double getProbability() {
            return probability;
        }

        public String getReason() {
            return reason;
        }

        public long getPredictedTime() {
            return predictedTime;
        }

        @Override
        public String toString() {
            return "FaultPrediction{" +
                    "faultType='" + faultType + '\'' +
                    ", probability=" + String.format("%.2f", probability * 100) + "%" +
                    ", reason='" + reason + '\'' +
                    ", predictedTime=" + new Date(predictedTime) +
                    '}';
        }
    }

    /**
     * 利用率报告类
     */
    public static class UtilizationReport {
        private final String equipmentId;
        private final TimeRange timeRange;
        private final double overallUtilization;
        private final Map<EquipmentState, Long> stateDurations;

        public UtilizationReport(String equipmentId, TimeRange timeRange,
                                 double overallUtilization, Map<EquipmentState, Long> stateDurations) {
            this.equipmentId = equipmentId;
            this.timeRange = timeRange;
            this.overallUtilization = Math.max(0.0, Math.min(1.0, overallUtilization)); // 限制在0-1之间
            this.stateDurations = stateDurations != null ? new EnumMap<>(stateDurations) : new EnumMap<>(EquipmentState.class);
        }

        // Getters
        public String getEquipmentId() {
            return equipmentId;
        }

        public TimeRange getTimeRange() {
            return timeRange;
        }

        public double getOverallUtilization() {
            return overallUtilization;
        }

        public Map<EquipmentState, Long> getStateDurations() {
            return new EnumMap<>(stateDurations);
        }

        @Override
        public String toString() {
            return "UtilizationReport{" +
                    "equipmentId='" + equipmentId + '\'' +
                    ", timeRange=" + timeRange +
                    ", overallUtilization=" + String.format("%.2f", overallUtilization * 100) + "%" +
                    ", stateDurations=" + stateDurations +
                    '}';
        }
    }

    /**
     * 时间范围类
     */
    public static class TimeRange {
        private final long start;
        private final long end;

        public TimeRange(long start, long end) {
            if (start > end) {
                throw new IllegalArgumentException("Start time cannot be after end time");
            }
            this.start = start;
            this.end = end;
        }

        // Getters
        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }

        @Override
        public String toString() {
            return "TimeRange{" +
                    "start=" + new Date(start) +
                    ", end=" + new Date(end) +
                    '}';
        }
    }
}
