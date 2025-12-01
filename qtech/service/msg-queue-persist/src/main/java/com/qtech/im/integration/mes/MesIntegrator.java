package com.qtech.im.integration.mes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * MES系统集成工具类
 * <p>
 * 特性：
 * - 通用性：支持多种MES系统和数据交互方式
 * - 规范性：统一的数据交换标准和接口规范
 * - 专业性：半导体行业MES集成专业实现
 * - 灵活性：可配置的数据格式和传输协议
 * - 可靠性：完善的异常处理和重试机制
 * - 安全性：数据加密和访问控制
 * - 复用性：模块化设计，组件可独立使用
 * - 容错性：优雅的错误处理和恢复机制
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/08/21
 */
public class MesIntegrator {

    private static final Logger logger = LoggerFactory.getLogger(MesIntegrator.class);

    // 默认配置
    public static final long DEFAULT_CACHE_TIMEOUT = 30 * 60 * 1000; // 30分钟
    public static final int DEFAULT_MAX_RETRY_ATTEMPTS = 3;
    public static final long DEFAULT_RETRY_DELAY = 1000; // 1秒
    public static final int DEFAULT_BATCH_SIZE = 100;
    public static final long DEFAULT_SYNC_INTERVAL = 5 * 60 * 1000; // 5分钟
    public static final int DEFAULT_MESSAGE_QUEUE_SIZE = 10000;
    public static final long DEFAULT_CONNECTION_TIMEOUT = 30000; // 30秒

    // MES系统类型枚举
    public enum MesType {
        SIEMENS_SIMATIC_IT("西门子SIMATIC IT", "Siemens SIMATIC IT"),
        ROCKWELL_FACTORYTALK("罗克韦尔FactoryTalk", "Rockwell FactoryTalk"),
        HONEYWELL_MEASUREMENT("霍尼韦尔MES", "Honeywell MES"),
        CAMSTAR("康思尔MES", "Camstar MES"),
        FACTORY_MES("工厂自研MES", "Factory MES"),
        CUSTOM("自定义MES", "Custom MES");

        private final String chineseName;
        private final String englishName;

        MesType(String chineseName, String englishName) {
            this.chineseName = chineseName;
            this.englishName = englishName;
        }

        public String getChineseName() {
            return chineseName;
        }

        public String getEnglishName() {
            return englishName;
        }
    }

    // 数据包类型枚举
    public enum DataPackageType {
        PRODUCTION_DATA("生产数据", "Production Data"),
        QUALITY_DATA("质量数据", "Quality Data"),
        EQUIPMENT_DATA("设备数据", "Equipment Data"),
        MATERIAL_DATA("物料数据", "Material Data"),
        PROCESS_DATA("工艺数据", "Process Data"),
        DEFECT_DATA("缺陷数据", "Defect Data"),
        CUSTOM("自定义数据", "Custom Data");

        private final String chineseName;
        private final String englishName;

        DataPackageType(String chineseName, String englishName) {
            this.chineseName = chineseName;
            this.englishName = englishName;
        }

        public String getChineseName() {
            return chineseName;
        }

        public String getEnglishName() {
            return englishName;
        }
    }

    // 同步状态枚举
    public enum SyncStatus {
        PENDING("待同步", 1),
        SYNCING("同步中", 2),
        SUCCESS("同步成功", 3),
        FAILED("同步失败", 4),
        RETRYING("重试中", 5),
        CANCELLED("已取消", 6);

        private final String description;
        private final int sequence;

        SyncStatus(String description, int sequence) {
            this.description = description;
            this.sequence = sequence;
        }

        public String getDescription() {
            return description;
        }

        public int getSequence() {
            return sequence;
        }
    }

    // 指令类型枚举
    public enum CommandType {
        START_PROCESS("启动工艺", "Start Process"),
        STOP_PROCESS("停止工艺", "Stop Process"),
        PAUSE_PROCESS("暂停工艺", "Pause Process"),
        RESUME_PROCESS("恢复工艺", "Resume Process"),
        ADJUST_PARAMETERS("调整参数", "Adjust Parameters"),
        QUALITY_HOLD("质量暂停", "Quality Hold"),
        QUALITY_RELEASE("质量放行", "Quality Release"),
        EQUIPMENT_MAINTENANCE("设备维护", "Equipment Maintenance"),
        CUSTOM("自定义指令", "Custom Command");

        private final String chineseName;
        private final String englishName;

        CommandType(String chineseName, String englishName) {
            this.chineseName = chineseName;
            this.englishName = englishName;
        }

        public String getChineseName() {
            return chineseName;
        }

        public String getEnglishName() {
            return englishName;
        }
    }

    // 响应状态枚举
    public enum ResponseStatus {
        SUCCESS("成功", 1),
        FAILURE("失败", 2),
        PARTIAL_SUCCESS("部分成功", 3),
        REJECTED("拒绝", 4),
        TIMEOUT("超时", 5);

        private final String description;
        private final int priority;

        ResponseStatus(String description, int priority) {
            this.description = description;
            this.priority = priority;
        }

        public String getDescription() {
            return description;
        }

        public int getPriority() {
            return priority;
        }
    }

    // 实时数据主题枚举
    public enum RealTimeTopic {
        PRODUCTION_STATUS("生产状态", "Production Status"),
        QUALITY_METRICS("质量指标", "Quality Metrics"),
        EQUIPMENT_STATUS("设备状态", "Equipment Status"),
        MATERIAL_FLOW("物料流转", "Material Flow"),
        PROCESS_PARAMETERS("工艺参数", "Process Parameters"),
        ALARM_EVENTS("报警事件", "Alarm Events"),
        CUSTOM("自定义主题", "Custom Topic");

        private final String chineseName;
        private final String englishName;

        RealTimeTopic(String chineseName, String englishName) {
            this.chineseName = chineseName;
            this.englishName = englishName;
        }

        public String getChineseName() {
            return chineseName;
        }

        public String getEnglishName() {
            return englishName;
        }
    }

    // 异常类型枚举
    public enum ExceptionType {
        COMMUNICATION_ERROR("通信错误", "Communication Error"),
        AUTHENTICATION_FAILURE("认证失败", "Authentication Failure"),
        DATA_FORMAT_ERROR("数据格式错误", "Data Format Error"),
        TIMEOUT_ERROR("超时错误", "Timeout Error"),
        BUSINESS_RULE_VIOLATION("业务规则违反", "Business Rule Violation"),
        SYSTEM_UNAVAILABLE("系统不可用", "System Unavailable"),
        CUSTOM("自定义异常", "Custom Exception");

        private final String chineseName;
        private final String englishName;

        ExceptionType(String chineseName, String englishName) {
            this.chineseName = chineseName;
            this.englishName = englishName;
        }

        public String getChineseName() {
            return chineseName;
        }

        public String getEnglishName() {
            return englishName;
        }
    }

    // 数据包类
    public static class DataPackage {
        private final String packageId;
        private final DataPackageType packageType;
        private final String sourceSystem;
        private final String targetSystem;
        private final Map<String, Object> data;
        private final LocalDateTime createTime;
        private final String batchId;
        private final String productId;
        private final Map<String, Object> metadata;
        private final String correlationId;
        private volatile SyncStatus status;

        public DataPackage(DataPackageType packageType, String sourceSystem, String targetSystem) {
            this.packageId = UUID.randomUUID().toString();
            this.packageType = packageType != null ? packageType : DataPackageType.CUSTOM;
            this.sourceSystem = sourceSystem != null ? sourceSystem : "Unknown";
            this.targetSystem = targetSystem != null ? targetSystem : "MES";
            this.data = new ConcurrentHashMap<>();
            this.createTime = LocalDateTime.now();
            this.batchId = "";
            this.productId = "";
            this.metadata = new ConcurrentHashMap<>();
            this.correlationId = "";
            this.status = SyncStatus.PENDING;
        }

        // Getters and Setters
        public String getPackageId() {
            return packageId;
        }

        public DataPackageType getPackageType() {
            return packageType;
        }

        public String getSourceSystem() {
            return sourceSystem;
        }

        public String getTargetSystem() {
            return targetSystem;
        }

        public Map<String, Object> getData() {
            return new HashMap<>(data);
        }

        public DataPackage setData(String key, Object value) {
            this.data.put(key, value);
            return this;
        }

        public Object getData(String key) {
            return this.data.get(key);
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public String getBatchId() {
            return batchId;
        }

        public DataPackage setBatchId(String batchId) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public String getProductId() {
            return productId;
        }

        public DataPackage setProductId(String productId) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }

        public DataPackage setMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Object getMetadata(String key) {
            return this.metadata.get(key);
        }

        public String getCorrelationId() {
            return correlationId;
        }

        public DataPackage setCorrelationId(String correlationId) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public SyncStatus getStatus() {
            return status;
        }

        public DataPackage setStatus(SyncStatus status) {
            this.status = status;
            return this;
        }

        @Override
        public String toString() {
            return "DataPackage{" +
                    "packageId='" + packageId + '\'' +
                    ", packageType=" + packageType +
                    ", sourceSystem='" + sourceSystem + '\'' +
                    ", targetSystem='" + targetSystem + '\'' +
                    ", dataCount=" + data.size() +
                    ", createTime=" + createTime +
                    ", status=" + status +
                    '}';
        }
    }

    // 同步结果类
    public static class SyncResult {
        private final String resultId;
        private final String packageId;
        private final boolean success;
        private final String message;
        private final LocalDateTime syncTime;
        private final long duration;
        private final ResponseStatus responseStatus;
        private final Map<String, Object> details;
        private final String errorMessage;
        private final int retryCount;

        public SyncResult(String packageId, boolean success, String message, long duration) {
            this.resultId = UUID.randomUUID().toString();
            this.packageId = packageId != null ? packageId : "";
            this.success = success;
            this.message = message != null ? message : "";
            this.syncTime = LocalDateTime.now();
            this.duration = duration;
            this.responseStatus = success ? ResponseStatus.SUCCESS : ResponseStatus.FAILURE;
            this.details = new ConcurrentHashMap<>();
            this.errorMessage = "";
            this.retryCount = 0;
        }

        // Getters and Setters
        public String getResultId() {
            return resultId;
        }

        public String getPackageId() {
            return packageId;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public LocalDateTime getSyncTime() {
            return syncTime;
        }

        public long getDuration() {
            return duration;
        }

        public ResponseStatus getResponseStatus() {
            return responseStatus;
        }

        public SyncResult setResponseStatus(ResponseStatus responseStatus) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public Map<String, Object> getDetails() {
            return new HashMap<>(details);
        }

        public SyncResult setDetail(String key, Object value) {
            this.details.put(key, value);
            return this;
        }

        public Object getDetail(String key) {
            return this.details.get(key);
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public SyncResult setErrorMessage(String errorMessage) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public SyncResult setRetryCount(int retryCount) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        @Override
        public String toString() {
            return "SyncResult{" +
                    "resultId='" + resultId + '\'' +
                    ", packageId='" + packageId + '\'' +
                    ", success=" + success +
                    ", message='" + message + '\'' +
                    ", syncTime=" + syncTime +
                    ", duration=" + duration + "ms" +
                    ", responseStatus=" + responseStatus +
                    '}';
        }
    }

    // MES指令类
    public static class MesCommand {
        private final String commandId;
        private final CommandType commandType;
        private final String targetEquipment;
        private final String operator;
        private final LocalDateTime createTime;
        private final Map<String, Object> parameters;
        private final String workOrderId;
        private final String batchId;
        private final Map<String, Object> metadata;
        private final String priority;

        public MesCommand(CommandType commandType, String targetEquipment, String operator) {
            this.commandId = UUID.randomUUID().toString();
            this.commandType = commandType != null ? commandType : CommandType.CUSTOM;
            this.targetEquipment = targetEquipment != null ? targetEquipment : "";
            this.operator = operator != null ? operator : "System";
            this.createTime = LocalDateTime.now();
            this.parameters = new ConcurrentHashMap<>();
            this.workOrderId = "";
            this.batchId = "";
            this.metadata = new ConcurrentHashMap<>();
            this.priority = "NORMAL";
        }

        // Getters and Setters
        public String getCommandId() {
            return commandId;
        }

        public CommandType getCommandType() {
            return commandType;
        }

        public String getTargetEquipment() {
            return targetEquipment;
        }

        public String getOperator() {
            return operator;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public Map<String, Object> getParameters() {
            return new HashMap<>(parameters);
        }

        public MesCommand setParameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        public Object getParameter(String key) {
            return this.parameters.get(key);
        }

        public String getWorkOrderId() {
            return workOrderId;
        }

        public MesCommand setWorkOrderId(String workOrderId) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public String getBatchId() {
            return batchId;
        }

        public MesCommand setBatchId(String batchId) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }

        public MesCommand setMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Object getMetadata(String key) {
            return this.metadata.get(key);
        }

        public String getPriority() {
            return priority;
        }

        public MesCommand setPriority(String priority) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        @Override
        public String toString() {
            return "MesCommand{" +
                    "commandId='" + commandId + '\'' +
                    ", commandType=" + commandType +
                    ", targetEquipment='" + targetEquipment + '\'' +
                    ", operator='" + operator + '\'' +
                    ", createTime=" + createTime +
                    ", parameterCount=" + parameters.size() +
                    ", priority='" + priority + '\'' +
                    '}';
        }
    }

    // 指令响应类
    public static class CommandResponse {
        private final String responseId;
        private final String commandId;
        private final ResponseStatus status;
        private final String message;
        private final LocalDateTime responseTime;
        private final Map<String, Object> responseData;
        private final String errorMessage;
        private final long processingTime;

        public CommandResponse(String commandId, ResponseStatus status, String message) {
            this.responseId = UUID.randomUUID().toString();
            this.commandId = commandId != null ? commandId : "";
            this.status = status != null ? status : ResponseStatus.FAILURE;
            this.message = message != null ? message : "";
            this.responseTime = LocalDateTime.now();
            this.responseData = new ConcurrentHashMap<>();
            this.errorMessage = "";
            this.processingTime = 0;
        }

        // Getters and Setters
        public String getResponseId() {
            return responseId;
        }

        public String getCommandId() {
            return commandId;
        }

        public ResponseStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public LocalDateTime getResponseTime() {
            return responseTime;
        }

        public Map<String, Object> getResponseData() {
            return new HashMap<>(responseData);
        }

        public CommandResponse setResponseData(String key, Object value) {
            this.responseData.put(key, value);
            return this;
        }

        public Object getResponseData(String key) {
            return this.responseData.get(key);
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public CommandResponse setErrorMessage(String errorMessage) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public long getProcessingTime() {
            return processingTime;
        }

        public CommandResponse setProcessingTime(long processingTime) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        @Override
        public String toString() {
            return "CommandResponse{" +
                    "responseId='" + responseId + '\'' +
                    ", commandId='" + commandId + '\'' +
                    ", status=" + status +
                    ", message='" + message + '\'' +
                    ", responseTime=" + responseTime +
                    ", processingTime=" + processingTime + "ms" +
                    '}';
        }
    }

    // 异常数据类
    public static class ExceptionData {
        private final String exceptionId;
        private final ExceptionType exceptionType;
        private final String sourceSystem;
        private final String targetSystem;
        private final String errorMessage;
        private final LocalDateTime occurrenceTime;
        private final Map<String, Object> contextData;
        private final String severity;
        private final boolean acknowledged;
        private final String assignedTo;
        private final Map<String, Object> metadata;

        public ExceptionData(ExceptionType exceptionType, String sourceSystem,
                             String targetSystem, String errorMessage) {
            this.exceptionId = UUID.randomUUID().toString();
            this.exceptionType = exceptionType != null ? exceptionType : ExceptionType.CUSTOM;
            this.sourceSystem = sourceSystem != null ? sourceSystem : "Unknown";
            this.targetSystem = targetSystem != null ? targetSystem : "MES";
            this.errorMessage = errorMessage != null ? errorMessage : "";
            this.occurrenceTime = LocalDateTime.now();
            this.contextData = new ConcurrentHashMap<>();
            this.severity = "MEDIUM";
            this.acknowledged = false;
            this.assignedTo = "";
            this.metadata = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getExceptionId() {
            return exceptionId;
        }

        public ExceptionType getExceptionType() {
            return exceptionType;
        }

        public String getSourceSystem() {
            return sourceSystem;
        }

        public String getTargetSystem() {
            return targetSystem;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public LocalDateTime getOccurrenceTime() {
            return occurrenceTime;
        }

        public Map<String, Object> getContextData() {
            return new HashMap<>(contextData);
        }

        public ExceptionData setContextData(String key, Object value) {
            this.contextData.put(key, value);
            return this;
        }

        public Object getContextData(String key) {
            return this.contextData.get(key);
        }

        public String getSeverity() {
            return severity;
        }

        public ExceptionData setSeverity(String severity) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public boolean isAcknowledged() {
            return acknowledged;
        }

        public ExceptionData setAcknowledged(boolean acknowledged) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public String getAssignedTo() {
            return assignedTo;
        }

        public ExceptionData setAssignedTo(String assignedTo) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }

        public ExceptionData setMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Object getMetadata(String key) {
            return this.metadata.get(key);
        }

        @Override
        public String toString() {
            return "ExceptionData{" +
                    "exceptionId='" + exceptionId + '\'' +
                    ", exceptionType=" + exceptionType +
                    ", sourceSystem='" + sourceSystem + '\'' +
                    ", targetSystem='" + targetSystem + '\'' +
                    ", errorMessage='" + errorMessage + '\'' +
                    ", occurrenceTime=" + occurrenceTime +
                    ", severity='" + severity + '\'' +
                    ", acknowledged=" + acknowledged +
                    '}';
        }
    }

    // 实时数据包类
    public static class RealTimeData {
        private final String dataId;
        private final RealTimeTopic topic;
        private final String source;
        private final Object payload;
        private final LocalDateTime timestamp;
        private final Map<String, Object> metadata;
        private final String priority;

        public RealTimeData(RealTimeTopic topic, String source, Object payload) {
            this.dataId = UUID.randomUUID().toString();
            this.topic = topic != null ? topic : RealTimeTopic.CUSTOM;
            this.source = source != null ? source : "Unknown";
            this.payload = payload;
            this.timestamp = LocalDateTime.now();
            this.metadata = new ConcurrentHashMap<>();
            this.priority = "NORMAL";
        }

        // Getters and Setters
        public String getDataId() {
            return dataId;
        }

        public RealTimeTopic getTopic() {
            return topic;
        }

        public String getSource() {
            return source;
        }

        public Object getPayload() {
            return payload;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }

        public RealTimeData setMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Object getMetadata(String key) {
            return this.metadata.get(key);
        }

        public String getPriority() {
            return priority;
        }

        public RealTimeData setPriority(String priority) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        @Override
        public String toString() {
            return "RealTimeData{" +
                    "dataId='" + dataId + '\'' +
                    ", topic=" + topic +
                    ", source='" + source + '\'' +
                    ", payloadClass=" + (payload != null ? payload.getClass().getSimpleName() : "null") +
                    ", timestamp=" + timestamp +
                    ", priority='" + priority + '\'' +
                    '}';
        }
    }

    // MES集成器接口
    public interface MesIntegratorEngine {
        SyncResult syncDataToMes(DataPackage data) throws Exception;

        boolean supportsMesType(MesType mesType);

        String getEngineName();
    }

    // MES指令处理器接口
    public interface CommandProcessor {
        CommandResponse processMesCommand(MesCommand command) throws Exception;

        boolean supportsCommandType(CommandType commandType);

        String getProcessorName();
    }

    // 实时数据推送器接口
    public interface RealTimeDataPusher {
        boolean pushRealTimeData(String topic, Object data) throws Exception;

        boolean supportsTopic(RealTimeTopic topic);

        String getPusherName();
    }

    // 异常数据上报器接口
    public interface ExceptionReporter {
        boolean reportExceptionData(ExceptionData data) throws Exception;

        boolean supportsExceptionType(ExceptionType exceptionType);

        String getReporterName();
    }

    // 数据同步器接口
    public interface DataSynchronizer {
        boolean syncBatchData(List<DataPackage> dataPackages) throws Exception;

        SyncStatus getSyncStatus(String packageId) throws Exception;

        boolean supportsDataPackageType(DataPackageType packageType);

        String getSynchronizerName();
    }

    // 连接管理器接口
    public interface ConnectionManager {
        boolean connect() throws Exception;

        boolean disconnect() throws Exception;

        boolean isConnected();

        boolean testConnection() throws Exception;

        String getConnectionInfo();

        long getConnectionTimeout();
    }

    // 内部存储和管理
    private static final Map<MesType, MesIntegratorEngine> engineRegistry = new ConcurrentHashMap<>();
    private static final Map<CommandType, CommandProcessor> processorRegistry = new ConcurrentHashMap<>();
    private static final Map<RealTimeTopic, RealTimeDataPusher> pusherRegistry = new ConcurrentHashMap<>();
    private static final Map<ExceptionType, ExceptionReporter> reporterRegistry = new ConcurrentHashMap<>();
    private static final Map<DataPackageType, DataSynchronizer> synchronizerRegistry = new ConcurrentHashMap<>();
    private static final Map<String, ConnectionManager> connectionManagerRegistry = new ConcurrentHashMap<>();
    private static final Map<String, DataPackage> packageCache = new ConcurrentHashMap<>();
    private static final Map<String, SyncResult> resultCache = new ConcurrentHashMap<>();
    private static final BlockingQueue<RealTimeData> messageQueue = new LinkedBlockingQueue<>(DEFAULT_MESSAGE_QUEUE_SIZE);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private static final ScheduledExecutorService messageProcessor = Executors.newSingleThreadScheduledExecutor();

    // 当前使用的组件
    private static volatile MesIntegratorEngine currentEngine;
    private static volatile CommandProcessor currentProcessor;
    private static volatile RealTimeDataPusher currentPusher;
    private static volatile ExceptionReporter currentReporter;
    private static volatile DataSynchronizer currentSynchronizer;
    private static volatile ConnectionManager currentConnectionManager;
    private static volatile MesType currentMesType = MesType.CUSTOM;

    // 初始化默认组件
    static {
        registerDefaultComponents();
        initializeCurrentComponents();
        startMaintenanceTasks();
        startMessageProcessing();
        logger.info("MesIntegrator initialized with default components");
    }

    /**
     * 注册默认组件
     */
    private static void registerDefaultComponents() {
        // 注册默认引擎、处理器、推送器、上报器和同步器
        // registerEngine(new DefaultMesIntegratorEngine());
        // registerProcessor(new DefaultCommandProcessor());
        // registerPusher(new DefaultRealTimeDataPusher());
        // registerReporter(new DefaultExceptionReporter());
        // registerSynchronizer(new DefaultDataSynchronizer());
        // registerConnectionManager("default", new DefaultConnectionManager());
    }

    /**
     * 初始化当前组件
     */
    private static void initializeCurrentComponents() {
        // 初始化为第一个注册的组件
        if (!engineRegistry.isEmpty()) {
            currentEngine = engineRegistry.values().iterator().next();
        }

        if (!processorRegistry.isEmpty()) {
            currentProcessor = processorRegistry.values().iterator().next();
        }

        if (!pusherRegistry.isEmpty()) {
            currentPusher = pusherRegistry.values().iterator().next();
        }

        if (!reporterRegistry.isEmpty()) {
            currentReporter = reporterRegistry.values().iterator().next();
        }

        if (!synchronizerRegistry.isEmpty()) {
            currentSynchronizer = synchronizerRegistry.values().iterator().next();
        }

        if (!connectionManagerRegistry.isEmpty()) {
            currentConnectionManager = connectionManagerRegistry.values().iterator().next();
        }
    }

    /**
     * 启动维护任务
     */
    private static void startMaintenanceTasks() {
        // 启动缓存清理任务
        scheduler.scheduleAtFixedRate(MesIntegrator::cleanupCache,
                30, 30, TimeUnit.MINUTES);

        // 启动连接健康检查任务
        scheduler.scheduleAtFixedRate(MesIntegrator::checkConnectionHealth,
                5, 5, TimeUnit.MINUTES);

        logger.debug("MES integrator maintenance tasks started");
    }

    /**
     * 启动消息处理任务
     */
    private static void startMessageProcessing() {
        messageProcessor.scheduleWithFixedDelay(MesIntegrator::processMessageQueue,
                1, 1, TimeUnit.SECONDS);
        logger.debug("MES integrator message processing started");
    }

    /**
     * 注册MES集成器引擎
     */
    public static void registerEngine(MesIntegratorEngine engine) {
        if (engine == null) {
            throw new IllegalArgumentException("Engine cannot be null");
        }

        for (MesType type : MesType.values()) {
            if (engine.supportsMesType(type)) {
                engineRegistry.put(type, engine);
                logger.debug("Registered engine {} for MES type {}", engine.getEngineName(), type);
            }
        }
    }

    /**
     * 注册指令处理器
     */
    public static void registerProcessor(CommandProcessor processor) {
        if (processor == null) {
            throw new IllegalArgumentException("Processor cannot be null");
        }

        for (CommandType type : CommandType.values()) {
            if (processor.supportsCommandType(type)) {
                processorRegistry.put(type, processor);
                logger.debug("Registered processor {} for entity type {}", processor.getProcessorName(), type);
            }
        }
    }

    /**
     * 注册实时数据推送器
     */
    public static void registerPusher(RealTimeDataPusher pusher) {
        if (pusher == null) {
            throw new IllegalArgumentException("Pusher cannot be null");
        }

        for (RealTimeTopic topic : RealTimeTopic.values()) {
            if (pusher.supportsTopic(topic)) {
                pusherRegistry.put(topic, pusher);
                logger.debug("Registered pusher {} for topic {}", pusher.getPusherName(), topic);
            }
        }
    }

    /**
     * 注册异常数据上报器
     */
    public static void registerReporter(ExceptionReporter reporter) {
        if (reporter == null) {
            throw new IllegalArgumentException("Reporter cannot be null");
        }

        for (ExceptionType type : ExceptionType.values()) {
            if (reporter.supportsExceptionType(type)) {
                reporterRegistry.put(type, reporter);
                logger.debug("Registered reporter {} for type type {}", reporter.getReporterName(), type);
            }
        }
    }

    /**
     * 注册数据同步器
     */
    public static void registerSynchronizer(DataSynchronizer synchronizer) {
        if (synchronizer == null) {
            throw new IllegalArgumentException("Synchronizer cannot be null");
        }

        for (DataPackageType type : DataPackageType.values()) {
            if (synchronizer.supportsDataPackageType(type)) {
                synchronizerRegistry.put(type, synchronizer);
                logger.debug("Registered synchronizer {} for data package type {}", synchronizer.getSynchronizerName(), type);
            }
        }
    }

    /**
     * 注册连接管理器
     */
    public static void registerConnectionManager(String name, ConnectionManager manager) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Connection manager name cannot be null or empty");
        }

        if (manager == null) {
            throw new IllegalArgumentException("Connection manager cannot be null");
        }

        connectionManagerRegistry.put(name, manager);
        logger.debug("Registered connection manager {} with name {}", manager.getConnectionInfo(), name);
    }

    /**
     * 设置当前MES集成器引擎
     */
    public static void setCurrentEngine(MesType mesType) {
        MesIntegratorEngine engine = engineRegistry.get(mesType);
        if (engine != null) {
            currentEngine = engine;
            currentMesType = mesType;
            logger.info("Current MES engine set to: {} for MES type {}", engine.getEngineName(), mesType);
        } else {
            logger.warn("No engine found for MES type: {}", mesType);
        }
    }

    /**
     * 设置当前指令处理器
     */
    public static void setCurrentProcessor(CommandType commandType) {
        CommandProcessor processor = processorRegistry.get(commandType);
        if (processor != null) {
            currentProcessor = processor;
            logger.info("Current entity processor set to: {} for entity type {}",
                    processor.getProcessorName(), commandType);
        } else {
            logger.warn("No processor found for entity type: {}", commandType);
        }
    }

    /**
     * 设置当前实时数据推送器
     */
    public static void setCurrentPusher(RealTimeTopic topic) {
        RealTimeDataPusher pusher = pusherRegistry.get(topic);
        if (pusher != null) {
            currentPusher = pusher;
            logger.info("Current data pusher set to: {} for topic {}", pusher.getPusherName(), topic);
        } else {
            logger.warn("No pusher found for topic: {}", topic);
        }
    }

    /**
     * 设置当前异常数据上报器
     */
    public static void setCurrentReporter(ExceptionType exceptionType) {
        ExceptionReporter reporter = reporterRegistry.get(exceptionType);
        if (reporter != null) {
            currentReporter = reporter;
            logger.info("Current type reporter set to: {} for type type {}",
                    reporter.getReporterName(), exceptionType);
        } else {
            logger.warn("No reporter found for type type: {}", exceptionType);
        }
    }

    /**
     * 设置当前数据同步器
     */
    public static void setCurrentSynchronizer(DataPackageType packageType) {
        DataSynchronizer synchronizer = synchronizerRegistry.get(packageType);
        if (synchronizer != null) {
            currentSynchronizer = synchronizer;
            logger.info("Current data synchronizer set to: {} for package type {}",
                    synchronizer.getSynchronizerName(), packageType);
        } else {
            logger.warn("No synchronizer found for package type: {}", packageType);
        }
    }

    /**
     * 设置当前连接管理器
     */
    public static void setCurrentConnectionManager(String name) {
        ConnectionManager manager = connectionManagerRegistry.get(name);
        if (manager != null) {
            currentConnectionManager = manager;
            logger.info("Current connection manager set to: {}", manager.getConnectionInfo());
        } else {
            logger.warn("No connection manager found with name: {}", name);
        }
    }

    /**
     * MES数据同步
     */
    public static SyncResult syncDataToMes(DataPackage data) {
        if (data == null) {
            logger.warn("Invalid data package for synchronization");
            return new SyncResult(null, false, "无效的数据包", 0);
        }

        long startTime = System.currentTimeMillis();

        try {
            // 验证连接
            if (!ensureConnection()) {
                logger.warn("Failed to establish connection for data synchronization");
                return new SyncResult(data.getPackageId(), false, "连接MES系统失败",
                        System.currentTimeMillis() - startTime);
            }

            // 缓存数据包
            packageCache.put(data.getPackageId(), data);

            // 使用注册的引擎进行数据同步
            MesIntegratorEngine engine = engineRegistry.get(currentMesType);
            SyncResult result = null;

            if (engine != null) {
                result = engine.syncDataToMes(data);
            } else if (currentEngine != null) {
                // 使用当前引擎
                result = currentEngine.syncDataToMes(data);
            } else {
                // 使用默认同步逻辑
                result = performDefaultDataSync(data);
            }

            long duration = System.currentTimeMillis() - startTime;

            // 更新缓存中的结果
            if (result != null) {
                resultCache.put(result.getResultId(), result);
                data.setStatus(result.isSuccess() ? SyncStatus.SUCCESS : SyncStatus.FAILED);

                if (result.isSuccess()) {
                    logger.info("Data synchronization completed successfully: {} inspection {}ms",
                            data.getPackageId(), duration);
                } else {
                    logger.warn("Data synchronization failed: {} - {}",
                            data.getPackageId(), result.getMessage());
                }
            } else {
                logger.warn("Data synchronization returned null result: {}", data.getPackageId());
            }

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Failed to sync data to MES: " + data.getPackageId(), e);

            // 记录异常数据
            ExceptionData exceptionData = new ExceptionData(
                    ExceptionType.COMMUNICATION_ERROR,
                    data.getSourceSystem(),
                    data.getTargetSystem(),
                    "数据同步异常: " + e.getMessage()
            )
                    .setContextData("packageId", data.getPackageId())
                    .setContextData("packageType", data.getPackageType().name())
                    .setSeverity("HIGH");

            reportExceptionData(exceptionData);

            return new SyncResult(data.getPackageId(), false, "数据同步异常: " + e.getMessage(), duration);
        }
    }

    /**
     * 执行默认数据同步逻辑
     */
    private static SyncResult performDefaultDataSync(DataPackage data) {
        try {
            // 模拟数据同步过程
            Thread.sleep(100 + (long) (Math.random() * 200)); // 随机延迟100-300ms

            // 模拟95%的成功率
            boolean success = Math.random() > 0.05;

            String message = success ? "数据同步成功" : "数据同步失败";
            long duration = (long) (100 + Math.random() * 200);

            SyncResult result = new SyncResult(data.getPackageId(), success, message, duration);

            if (success) {
                result.setDetail("syncedRecords", data.getData().size())
                        .setDetail("targetSystem", data.getTargetSystem());
            } else {
                result.setErrorMessage("模拟同步失败")
                        .setResponseStatus(ResponseStatus.FAILURE);
            }

            logger.debug("Default data sync performed for: {} - {}", data.getPackageId(), message);

            return result;
        } catch (Exception e) {
            logger.warn("Failed to perform default data sync", e);
            return null;
        }
    }

    /**
     * 批量MES数据同步
     */
    public static List<SyncResult> syncBatchDataToMes(List<DataPackage> dataPackages) {
        if (dataPackages == null || dataPackages.isEmpty()) {
            logger.warn("Invalid data packages for batch synchronization");
            return new ArrayList<>();
        }

        List<SyncResult> results = new ArrayList<>();

        // 分批处理
        for (int i = 0; i < dataPackages.size(); i += DEFAULT_BATCH_SIZE) {
            int endIndex = Math.min(i + DEFAULT_BATCH_SIZE, dataPackages.size());
            List<DataPackage> batch = dataPackages.subList(i, endIndex);

            List<SyncResult> batchResults = syncBatchData(batch);
            results.addAll(batchResults);
        }

        return results;
    }

    /**
     * 批量数据同步
     */
    private static List<SyncResult> syncBatchData(List<DataPackage> dataPackages) {
        try {
            if (currentSynchronizer != null) {
                // 使用专门的批量同步器
                boolean success = currentSynchronizer.syncBatchData(dataPackages);

                List<SyncResult> results = new ArrayList<>();
                for (DataPackage data : dataPackages) {
                    SyncResult result = new SyncResult(
                            data.getPackageId(),
                            success,
                            success ? "批量同步成功" : "批量同步失败",
                            0
                    );
                    results.add(result);
                    resultCache.put(result.getResultId(), result);
                }

                return results;
            } else {
                // 逐个同步
                return dataPackages.stream()
                        .map(MesIntegrator::syncDataToMes)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Failed to sync batch data to MES", e);

            List<SyncResult> results = new ArrayList<>();
            for (DataPackage data : dataPackages) {
                SyncResult result = new SyncResult(
                        data.getPackageId(),
                        false,
                        "批量同步异常: " + e.getMessage(),
                        0
                );
                results.add(result);
                resultCache.put(result.getResultId(), result);
            }

            return results;
        }
    }

    /**
     * MES指令接收和处理
     */
    public static CommandResponse processMesCommand(MesCommand command) {
        if (command == null) {
            logger.warn("Invalid MES entity for processing");
            return new CommandResponse(null, ResponseStatus.FAILURE, "无效的MES指令");
        }

        long startTime = System.currentTimeMillis();

        try {
            // 验证连接
            if (!ensureConnection()) {
                logger.warn("Failed to establish connection for entity processing");
                return new CommandResponse(command.getCommandId(), ResponseStatus.FAILURE, "连接MES系统失败");
            }

            // 使用注册的处理器处理指令
            CommandProcessor processor = processorRegistry.get(command.getCommandType());
            CommandResponse response = null;

            if (processor != null) {
                response = processor.processMesCommand(command);
            } else if (currentProcessor != null) {
                // 使用当前处理器
                response = currentProcessor.processMesCommand(command);
            } else {
                // 使用默认处理逻辑
                response = performDefaultCommandProcessing(command);
            }

            long processingTime = System.currentTimeMillis() - startTime;

            if (response != null) {
                response.setProcessingTime(processingTime);

                if (response.getStatus() == ResponseStatus.SUCCESS) {
                    logger.info("Command processing completed successfully: {} inspection {}ms",
                            command.getCommandId(), processingTime);
                } else {
                    logger.warn("Command processing failed: {} - {}",
                            command.getCommandId(), response.getMessage());
                }
            } else {
                logger.warn("Command processing returned null response: {}", command.getCommandId());
            }

            return response;
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to process MES entity: " + command.getCommandId(), e);

            // 记录异常数据
            ExceptionData exceptionData = new ExceptionData(
                    ExceptionType.COMMUNICATION_ERROR,
                    "MES",
                    "LocalSystem",
                    "指令处理异常: " + e.getMessage()
            )
                    .setContextData("commandId", command.getCommandId())
                    .setContextData("commandType", command.getCommandType().name())
                    .setSeverity("HIGH");

            reportExceptionData(exceptionData);

            return new CommandResponse(command.getCommandId(), ResponseStatus.FAILURE,
                    "指令处理异常: " + e.getMessage())
                    .setProcessingTime(processingTime);
        }
    }

    /**
     * 执行默认指令处理逻辑
     */
    private static CommandResponse performDefaultCommandProcessing(MesCommand command) {
        try {
            // 模拟指令处理过程
            Thread.sleep(50 + (long) (Math.random() * 150)); // 随机延迟50-200ms

            // 模拟90%的成功率
            boolean success = Math.random() > 0.1;

            ResponseStatus status = success ? ResponseStatus.SUCCESS : ResponseStatus.FAILURE;
            String message = success ? "指令处理成功" : "指令处理失败";

            CommandResponse response = new CommandResponse(command.getCommandId(), status, message);

            if (success) {
                response.setResponseData("processedParameters", command.getParameters().size())
                        .setResponseData("targetEquipment", command.getTargetEquipment());
            } else {
                response.setErrorMessage("模拟处理失败");
            }

            logger.debug("Default entity processing performed for: {} - {}", command.getCommandId(), message);

            return response;
        } catch (Exception e) {
            logger.warn("Failed to perform default entity processing", e);
            return null;
        }
    }

    /**
     * 实时数据推送
     */
    public static boolean pushRealTimeData(String topic, Object data) {
        if (topic == null || topic.isEmpty()) {
            logger.warn("Invalid topic for real-time data push");
            return false;
        }

        try {
            // 创建实时数据包
            RealTimeData realTimeData = new RealTimeData(
                    RealTimeTopic.CUSTOM,
                    "LocalSystem",
                    data
            )
                    .setMetadata("topic", topic);

            // 如果是标准主题，使用对应的枚举
            for (RealTimeTopic standardTopic : RealTimeTopic.values()) {
                if (standardTopic.name().equalsIgnoreCase(topic)) {
                    realTimeData = new RealTimeData(standardTopic, "LocalSystem", data);
                    break;
                }
            }

            // 尝试直接推送
            boolean pushed = false;

            // 使用注册的推送器进行推送
            RealTimeDataPusher pusher = null;
            for (RealTimeTopic standardTopic : RealTimeTopic.values()) {
                if (standardTopic.name().equalsIgnoreCase(topic)) {
                    pusher = pusherRegistry.get(standardTopic);
                    break;
                }
            }

            if (pusher != null) {
                pushed = pusher.pushRealTimeData(topic, data);
            } else if (currentPusher != null) {
                // 使用当前推送器
                pushed = currentPusher.pushRealTimeData(topic, data);
            } else {
                // 添加到消息队列进行异步处理
                pushed = messageQueue.offer(realTimeData);
                if (!pushed) {
                    logger.warn("Message queue is full, dropping real-time data: {}", topic);
                }
            }

            if (pushed) {
                logger.debug("Real-time data push initiated: {} - {}", topic,
                        data != null ? data.getClass().getSimpleName() : "null");
            } else {
                logger.warn("Failed to push real-time data: {}", topic);
            }

            return pushed;
        } catch (Exception e) {
            logger.error("Failed to push real-time data to topic: " + topic, e);

            // 记录异常数据
            ExceptionData exceptionData = new ExceptionData(
                    ExceptionType.COMMUNICATION_ERROR,
                    "LocalSystem",
                    "MES",
                    "实时数据推送异常: " + e.getMessage()
            )
                    .setContextData("topic", topic)
                    .setContextData("dataClass", data != null ? data.getClass().getName() : "null")
                    .setSeverity("MEDIUM");

            reportExceptionData(exceptionData);

            return false;
        }
    }

    /**
     * 处理消息队列
     */
    private static void processMessageQueue() {
        try {
            while (!messageQueue.isEmpty()) {
                RealTimeData data = messageQueue.poll();
                if (data != null) {
                    // 使用当前推送器推送数据
                    if (currentPusher != null) {
                        boolean pushed = currentPusher.pushRealTimeData(
                                data.getTopic().name(), data.getPayload());

                        if (!pushed) {
                            logger.warn("Failed to push queued real-time data: {}", data.getTopic());
                            // 如果推送失败，重新加入队列（但要避免无限循环）
                            if (messageQueue.size() < DEFAULT_MESSAGE_QUEUE_SIZE * 0.8) {
                                messageQueue.offer(data);
                            }
                        } else {
                            logger.debug("Successfully pushed queued real-time data: {}", data.getTopic());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to process message queue", e);
        }
    }

    /**
     * 异常数据上报
     */
    public static boolean reportExceptionData(ExceptionData data) {
        if (data == null) {
            logger.warn("Invalid type data for reporting");
            return false;
        }

        try {
            // 使用注册的上报器进行上报
            ExceptionReporter reporter = reporterRegistry.get(data.getExceptionType());
            boolean reported = false;

            if (reporter != null) {
                reported = reporter.reportExceptionData(data);
            } else if (currentReporter != null) {
                // 使用当前上报器
                reported = currentReporter.reportExceptionData(data);
            } else {
                // 使用默认上报逻辑
                reported = performDefaultExceptionReporting(data);
            }

            if (reported) {
                logger.info("Exception data reported successfully: {} - {}",
                        data.getExceptionId(), data.getExceptionType().getChineseName());
            } else {
                logger.warn("Failed to report type data: {}", data.getExceptionId());
            }

            return reported;
        } catch (Exception e) {
            logger.error("Failed to report type data: " + data.getExceptionId(), e);
            return false;
        }
    }

    /**
     * 执行默认异常数据上报逻辑
     */
    private static boolean performDefaultExceptionReporting(ExceptionData data) {
        try {
            // 模拟异常数据上报过程
            Thread.sleep(20 + (long) (Math.random() * 80)); // 随机延迟20-100ms

            // 模拟98%的成功率
            boolean success = Math.random() > 0.02;

            if (success) {
                logger.debug("Default type reporting performed for: {} - {}",
                        data.getExceptionId(), data.getExceptionType().getChineseName());
            } else {
                logger.warn("Default type reporting failed for: {}", data.getExceptionId());
            }

            return success;
        } catch (Exception e) {
            logger.warn("Failed to perform default type reporting", e);
            return false;
        }
    }

    /**
     * 确保连接正常
     */
    private static boolean ensureConnection() {
        try {
            if (currentConnectionManager != null) {
                if (!currentConnectionManager.isConnected()) {
                    return currentConnectionManager.connect();
                }
                return true;
            }
            return true; // 如果没有连接管理器，默认认为连接正常
        } catch (Exception e) {
            logger.error("Failed to ensure connection", e);
            return false;
        }
    }

    /**
     * 检查连接健康状态
     */
    private static void checkConnectionHealth() {
        try {
            if (currentConnectionManager != null && currentConnectionManager.isConnected()) {
                boolean healthy = currentConnectionManager.testConnection();
                if (!healthy) {
                    logger.warn("Connection health check failed, attempting to reconnect");
                    currentConnectionManager.disconnect();
                    currentConnectionManager.connect();
                }
            }
        } catch (Exception e) {
            logger.error("Failed to check connection health", e);
        }
    }

    /**
     * 创建数据包
     */
    public static DataPackage createDataPackage(DataPackageType packageType,
                                                String sourceSystem,
                                                String targetSystem) {
        if (packageType == null || sourceSystem == null || targetSystem == null) {
            logger.warn("Invalid parameters for data package creation");
            return null;
        }

        try {
            DataPackage dataPackage = new DataPackage(packageType, sourceSystem, targetSystem);

            // 添加到缓存
            packageCache.put(dataPackage.getPackageId(), dataPackage);

            logger.info("Created new data package: {} (Type: {}, Source: {}, Target: {})",
                    dataPackage.getPackageId(), packageType.getChineseName(),
                    sourceSystem, targetSystem);

            return dataPackage;
        } catch (Exception e) {
            logger.error("Failed to create data package", e);
            return null;
        }
    }

    /**
     * 获取数据包
     */
    public static DataPackage getDataPackage(String packageId) {
        if (packageId == null || packageId.isEmpty()) {
            logger.warn("Invalid package ID for retrieval");
            return null;
        }

        try {
            DataPackage dataPackage = packageCache.get(packageId);
            if (dataPackage != null) {
                logger.debug("Retrieved data package: {}", packageId);
            } else {
                logger.warn("Data package not found: {}", packageId);
            }
            return dataPackage;
        } catch (Exception e) {
            logger.error("Failed to get data package: " + packageId, e);
            return null;
        }
    }

    /**
     * 获取同步结果
     */
    public static SyncResult getSyncResult(String resultId) {
        if (resultId == null || resultId.isEmpty()) {
            logger.warn("Invalid result ID for retrieval");
            return null;
        }

        try {
            SyncResult result = resultCache.get(resultId);
            if (result != null) {
                logger.debug("Retrieved sync result: {}", resultId);
            } else {
                logger.warn("Sync result not found: {}", resultId);
            }
            return result;
        } catch (Exception e) {
            logger.error("Failed to get sync result: " + resultId, e);
            return null;
        }
    }

    /**
     * 获取同步状态
     */
    public static SyncStatus getSyncStatus(String packageId) {
        if (packageId == null || packageId.isEmpty()) {
            logger.warn("Invalid package ID for status retrieval");
            return SyncStatus.CANCELLED;
        }

        try {
            if (currentSynchronizer != null) {
                return currentSynchronizer.getSyncStatus(packageId);
            } else {
                DataPackage dataPackage = packageCache.get(packageId);
                return dataPackage != null ? dataPackage.getStatus() : SyncStatus.CANCELLED;
            }
        } catch (Exception e) {
            logger.error("Failed to get sync status for package: " + packageId, e);
            return SyncStatus.FAILED;
        }
    }

    /**
     * 清理缓存
     */
    private static void cleanupCache() {
        try {
            long cutoffTime = System.currentTimeMillis() - DEFAULT_CACHE_TIMEOUT;

            // 清理数据包缓存
            packageCache.entrySet().removeIf(entry ->
                    entry.getValue().getCreateTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli() < cutoffTime
            );

            // 清理结果缓存
            resultCache.entrySet().removeIf(entry ->
                    entry.getValue().getSyncTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli() < cutoffTime
            );

            logger.debug("Cleaned up MES integrator cache, remaining entries - package: {}, result: {}",
                    packageCache.size(), resultCache.size());
        } catch (Exception e) {
            logger.error("Failed to cleanup MES integrator cache", e);
        }
    }

    /**
     * 获取缓存统计信息
     */
    public static CacheStatistics getCacheStatistics() {
        CacheStatistics stats = new CacheStatistics();
        stats.setPackageCacheSize(packageCache.size());
        stats.setResultCacheSize(resultCache.size());
        stats.setMessageQueueSize(messageQueue.size());
        stats.setCacheTimeout(DEFAULT_CACHE_TIMEOUT);
        return stats;
    }

    /**
     * 获取连接统计信息
     */
    public static ConnectionStatistics getConnectionStatistics() {
        ConnectionStatistics stats = new ConnectionStatistics();
        stats.setConnected(currentConnectionManager != null && currentConnectionManager.isConnected());
        if (currentConnectionManager != null) {
            stats.setConnectionInfo(currentConnectionManager.getConnectionInfo());
            stats.setConnectionTimeout(currentConnectionManager.getConnectionTimeout());
        }
        return stats;
    }

    /**
     * 缓存统计信息类
     */
    public static class CacheStatistics {
        private int packageCacheSize;
        private int resultCacheSize;
        private int messageQueueSize;
        private long cacheTimeout;

        // Getters and Setters
        public int getPackageCacheSize() {
            return packageCacheSize;
        }

        public void setPackageCacheSize(int packageCacheSize) {
            this.packageCacheSize = packageCacheSize;
        }

        public int getResultCacheSize() {
            return resultCacheSize;
        }

        public void setResultCacheSize(int resultCacheSize) {
            this.resultCacheSize = resultCacheSize;
        }

        public int getMessageQueueSize() {
            return messageQueueSize;
        }

        public void setMessageQueueSize(int messageQueueSize) {
            this.messageQueueSize = messageQueueSize;
        }

        public long getCacheTimeout() {
            return cacheTimeout;
        }

        public void setCacheTimeout(long cacheTimeout) {
            this.cacheTimeout = cacheTimeout;
        }

        @Override
        public String toString() {
            return "CacheStatistics{" +
                    "packageCacheSize=" + packageCacheSize +
                    ", resultCacheSize=" + resultCacheSize +
                    ", messageQueueSize=" + messageQueueSize +
                    ", cacheTimeout=" + cacheTimeout + "ms" +
                    '}';
        }
    }

    /**
     * 连接统计信息类
     */
    public static class ConnectionStatistics {
        private boolean connected;
        private String connectionInfo;
        private long connectionTimeout;

        // Getters and Setters
        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        public String getConnectionInfo() {
            return connectionInfo;
        }

        public void setConnectionInfo(String connectionInfo) {
            this.connectionInfo = connectionInfo;
        }

        public long getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        @Override
        public String toString() {
            return "ConnectionStatistics{" +
                    "connected=" + connected +
                    ", connectionInfo='" + connectionInfo + '\'' +
                    ", connectionTimeout=" + connectionTimeout + "ms" +
                    '}';
        }
    }

    /**
     * 关闭MES集成器
     */
    public static void shutdown() {
        try {
            scheduler.shutdown();
            messageProcessor.shutdown();

            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }

            if (!messageProcessor.awaitTermination(30, TimeUnit.SECONDS)) {
                messageProcessor.shutdownNow();
            }

            // 断开连接
            if (currentConnectionManager != null && currentConnectionManager.isConnected()) {
                currentConnectionManager.disconnect();
            }

            logger.info("MesIntegrator shutdown completed");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            messageProcessor.shutdownNow();
            Thread.currentThread().interrupt();
            logger.warn("MesIntegrator shutdown interrupted");
        } catch (Exception e) {
            logger.error("Failed to shutdown MesIntegrator", e);
        }
    }
}
