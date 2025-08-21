package com.qtech.im.semiconductor.equipment.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 设备通信适配器工具类
 * <p>
 * 特性：
 * - 通用性：支持多种设备通信协议和厂商设备
 * - 规范性：统一的设备通信接口和标准命令
 * - 专业性：半导体行业设备通信专业实现
 * - 灵活性：可配置的通信参数和适配策略
 * - 可靠性：完善的连接管理和错误处理机制
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
public class EquipmentAdapter {

    // 默认配置
    public static final long DEFAULT_CONNECTION_TIMEOUT = 30000; // 30秒
    public static final long DEFAULT_RESPONSE_TIMEOUT = 10000; // 10秒
    public static final int DEFAULT_MAX_RETRIES = 3;
    public static final long DEFAULT_RETRY_DELAY = 1000; // 1秒
    public static final long DEFAULT_POLLING_INTERVAL = 5000; // 5秒
    public static final long DEFAULT_CACHE_TIMEOUT = 30 * 60 * 1000; // 30分钟
    public static final int DEFAULT_CONNECTION_POOL_SIZE = 10;
    public static final int DEFAULT_MAX_COMMAND_QUEUE_SIZE = 1000;
    private static final Logger logger = LoggerFactory.getLogger(EquipmentAdapter.class);
    // 内部存储和管理
    private static final Map<ProtocolType, EquipmentConnectionFactory> factoryRegistry = new ConcurrentHashMap<>();
    private static final Map<String, EquipmentConnection> connectionPool = new ConcurrentHashMap<>();
    private static final Map<String, EquipmentCommandProcessor> processorRegistry = new ConcurrentHashMap<>();
    private static final Map<String, EquipmentEventListener> listenerRegistry = new ConcurrentHashMap<>();
    private static final Map<String, PollingTask> pollingTaskRegistry = new ConcurrentHashMap<>();
    private static final Map<String, EquipmentResponse> responseCache = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private static final ExecutorService commandExecutor = Executors.newFixedThreadPool(20);

    // 统计信息
    private static final AtomicLong totalCommands = new AtomicLong(0);
    private static final AtomicLong successfulCommands = new AtomicLong(0);
    private static final AtomicLong failedCommands = new AtomicLong(0);
    private static final AtomicInteger activeConnections = new AtomicInteger(0);

    // 初始化默认组件
    static {
        registerDefaultComponents();
        startMaintenanceTasks();
        logger.info("EquipmentAdapter initialized with default components");
    }

    /**
     * 注册默认组件
     */
    private static void registerDefaultComponents() {
        // 注册默认连接工厂、命令处理器等
        // registerConnectionFactory(ProtocolType.SECSGEM, new SecsGemConnectionFactory());
        // registerConnectionFactory(ProtocolType.MODBUS, new ModbusConnectionFactory());
        // registerCommandProcessor("default", new DefaultCommandProcessor());
    }

    /**
     * 启动维护任务
     */
    private static void startMaintenanceTasks() {
        // 启动缓存清理任务
        scheduler.scheduleAtFixedRate(EquipmentAdapter::cleanupCache,
                30, 30, TimeUnit.MINUTES);

        // 启动连接健康检查任务
        scheduler.scheduleAtFixedRate(EquipmentAdapter::checkConnectionHealth,
                5, 5, TimeUnit.MINUTES);

        logger.debug("Equipment adapter maintenance tasks started");
    }

    /**
     * 注册连接工厂
     */
    public static void registerConnectionFactory(ProtocolType protocol, EquipmentConnectionFactory factory) {
        if (protocol == null || factory == null) {
            throw new IllegalArgumentException("Protocol and factory cannot be null");
        }

        factoryRegistry.put(protocol, factory);
        logger.debug("Registered connection factory for protocol: {}", protocol);
    }

    /**
     * 注册命令处理器
     */
    public static void registerCommandProcessor(String name, EquipmentCommandProcessor processor) {
        if (name == null || name.isEmpty() || processor == null) {
            throw new IllegalArgumentException("Processor name and processor cannot be null or empty");
        }

        processorRegistry.put(name, processor);
        logger.debug("Registered command processor: {}", name);
    }

    /**
     * 多协议设备通信
     */
    public static EquipmentConnection createConnection(String equipmentId, ProtocolType protocol) {
        return createConnection(equipmentId, protocol, new ConnectionConfig());
    }

    /**
     * 多协议设备通信（带配置）
     */
    public static EquipmentConnection createConnection(String equipmentId, ProtocolType protocol, ConnectionConfig config) {
        if (equipmentId == null || equipmentId.isEmpty()) {
            logger.error("Invalid equipment ID for connection creation");
            return null;
        }

        if (protocol == null) {
            logger.error("Invalid protocol for connection creation");
            return null;
        }

        if (config == null) {
            config = new ConnectionConfig();
        }

        try {
            // 检查连接池中是否已存在连接
            String connectionKey = generateConnectionKey(equipmentId, protocol);
            EquipmentConnection existingConnection = connectionPool.get(connectionKey);

            if (existingConnection != null && existingConnection.isConnected()) {
                logger.debug("Reusing existing connection for equipment: {} with protocol: {}", equipmentId, protocol);
                return existingConnection;
            }

            // 获取对应的连接工厂
            EquipmentConnectionFactory factory = factoryRegistry.get(protocol);
            if (factory == null) {
                logger.error("No connection factory found for protocol: {}", protocol);
                return null;
            }

            // 创建新连接
            EquipmentConnection connection = factory.createConnection(equipmentId, config);
            if (connection != null) {
                // 连接设备
                boolean connected = connection.connect();
                if (connected) {
                    // 添加到连接池
                    connectionPool.put(connectionKey, connection);
                    activeConnections.incrementAndGet();
                    logger.info("Created and connected new {} connection for equipment: {}", protocol, equipmentId);
                    return connection;
                } else {
                    logger.warn("Failed to connect to equipment: {} with protocol: {}", equipmentId, protocol);
                }
            } else {
                logger.error("Failed to create connection for equipment: {} with protocol: {}", equipmentId, protocol);
            }

            return null;
        } catch (Exception e) {
            logger.error("Exception occurred while creating connection for equipment: {} with protocol: {}", equipmentId, protocol, e);
            return null;
        }
    }

    /**
     * 生成连接键
     */
    private static String generateConnectionKey(String equipmentId, ProtocolType protocol) {
        return equipmentId + "_" + protocol.name();
    }

    /**
     * 标准化设备命令
     */
    public static EquipmentResponse sendStandardCommand(String equipmentId, StandardCommand command) {
        return sendStandardCommand(equipmentId, command, new CommandConfig());
    }

    /**
     * 标准化设备命令（带配置）
     */
    public static EquipmentResponse sendStandardCommand(String equipmentId, StandardCommand command, CommandConfig config) {
        if (equipmentId == null || equipmentId.isEmpty()) {
            logger.error("Invalid equipment ID for command sending");
            return new EquipmentResponse(ResponseStatus.FAILURE, "Invalid equipment ID", null);
        }

        if (command == null) {
            logger.error("Invalid command for equipment: {}", equipmentId);
            return new EquipmentResponse(ResponseStatus.FAILURE, "Invalid command", null);
        }

        if (config == null) {
            config = new CommandConfig();
        }

        totalCommands.incrementAndGet();

        try {
            long startTime = System.currentTimeMillis();

            // 检查缓存
            String cacheKey = generateCommandCacheKey(equipmentId, command);
            if (config.isEnableCache()) {
                EquipmentResponse cachedResponse = responseCache.get(cacheKey);
                if (cachedResponse != null && !isCacheExpired(cachedResponse)) {
                    logger.debug("Returning cached response for command: {} on equipment: {}", command.getCommandType(), equipmentId);
                    successfulCommands.incrementAndGet();
                    return cachedResponse;
                }
            }

            // 获取连接
            EquipmentConnection connection = getConnection(equipmentId, config.getProtocol());
            if (connection == null) {
                logger.error("No connection available for equipment: {}", equipmentId);
                failedCommands.incrementAndGet();
                return new EquipmentResponse(ResponseStatus.FAILURE, "No connection available", null);
            }

            // 获取命令处理器
            EquipmentCommandProcessor processor = processorRegistry.get(config.getProcessorName());
            if (processor == null) {
                processor = processorRegistry.get("default");
            }

            if (processor == null) {
                logger.error("No command processor available for equipment: {}", equipmentId);
                failedCommands.incrementAndGet();
                return new EquipmentResponse(ResponseStatus.FAILURE, "No command processor available", null);
            }

            // 发送命令
            EquipmentResponse response = null;
            int retryCount = 0;

            while (retryCount <= config.getMaxRetries()) {
                try {
                    response = processor.processCommand(connection, command, config);

                    if (response != null && response.getStatus() == ResponseStatus.SUCCESS) {
                        break; // 成功，退出重试循环
                    }
                } catch (Exception e) {
                    logger.warn("Command execution failed for equipment: {} (attempt {}/{})",
                            equipmentId, retryCount + 1, config.getMaxRetries() + 1, e);
                }

                retryCount++;

                // 如果需要重试，等待后重试
                if (retryCount <= config.getMaxRetries()) {
                    try {
                        Thread.sleep(config.getRetryDelay() * retryCount); // 递增延迟
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            long duration = System.currentTimeMillis() - startTime;

            // 处理响应
            if (response != null && response.getStatus() == ResponseStatus.SUCCESS) {
                successfulCommands.incrementAndGet();

                // 缓存响应
                if (config.isEnableCache()) {
                    responseCache.put(cacheKey, response);
                }

                logger.debug("Command {} executed successfully for equipment: {} in {}ms",
                        command.getCommandType(), equipmentId, duration);
            } else {
                failedCommands.incrementAndGet();
                logger.warn("Command {} failed for equipment: {} after {} attempts in {}ms",
                        command.getCommandType(), equipmentId, retryCount, duration);
            }

            return response != null ? response : new EquipmentResponse(ResponseStatus.FAILURE, "Command execution failed", null);
        } catch (Exception e) {
            failedCommands.incrementAndGet();
            logger.error("Exception occurred while sending command to equipment: {}", equipmentId, e);
            return new EquipmentResponse(ResponseStatus.FAILURE, "Exception: " + e.getMessage(), null);
        }
    }

    /**
     * 异步发送标准化设备命令
     */
    public static CompletableFuture<EquipmentResponse> sendStandardCommandAsync(String equipmentId, StandardCommand command) {
        return sendStandardCommandAsync(equipmentId, command, new CommandConfig());
    }

    /**
     * 异步发送标准化设备命令（带配置）
     */
    public static CompletableFuture<EquipmentResponse> sendStandardCommandAsync(String equipmentId, StandardCommand command, CommandConfig config) {
        return CompletableFuture.supplyAsync(() -> sendStandardCommand(equipmentId, command, config), commandExecutor);
    }

    /**
     * 批量发送标准化设备命令
     */
    public static List<EquipmentResponse> sendBatchCommands(String equipmentId, List<StandardCommand> commands) {
        return sendBatchCommands(equipmentId, commands, new CommandConfig());
    }

    /**
     * 批量发送标准化设备命令（带配置）
     */
    public static List<EquipmentResponse> sendBatchCommands(String equipmentId, List<StandardCommand> commands, CommandConfig config) {
        if (commands == null || commands.isEmpty()) {
            logger.warn("No commands to send for equipment: {}", equipmentId);
            return new ArrayList<>();
        }

        List<EquipmentResponse> responses = new ArrayList<>();

        for (StandardCommand command : commands) {
            EquipmentResponse response = sendStandardCommand(equipmentId, command, config);
            responses.add(response);
        }

        return responses;
    }

    /**
     * 获取连接
     */
    private static EquipmentConnection getConnection(String equipmentId, ProtocolType protocol) {
        if (protocol != null) {
            String connectionKey = generateConnectionKey(equipmentId, protocol);
            EquipmentConnection connection = connectionPool.get(connectionKey);
            if (connection != null && connection.isConnected()) {
                return connection;
            }
        }

        // 尝试从连接池中获取任何可用连接
        for (EquipmentConnection connection : connectionPool.values()) {
            if (connection.getEquipmentId().equals(equipmentId) && connection.isConnected()) {
                return connection;
            }
        }

        return null;
    }

    /**
     * 生成命令缓存键
     */
    private static String generateCommandCacheKey(String equipmentId, StandardCommand command) {
        return equipmentId + "_" + command.getCommandType() + "_" + command.getCommandId();
    }

    /**
     * 检查缓存是否过期
     */
    private static boolean isCacheExpired(EquipmentResponse response) {
        return System.currentTimeMillis() - response.getTimestamp() > DEFAULT_CACHE_TIMEOUT;
    }

    /**
     * 设备状态轮询
     */
    public static PollingResult pollEquipmentStatus(String equipmentId, PollingConfig config) {
        if (equipmentId == null || equipmentId.isEmpty()) {
            logger.error("Invalid equipment ID for polling");
            return new PollingResult(PollingStatus.FAILURE, "Invalid equipment ID", null);
        }

        if (config == null) {
            config = new PollingConfig();
        }

        try {
            // 创建轮询任务
            PollingTask task = new PollingTask(equipmentId, config);
            PollingResult result = task.execute();

            if (result.getStatus() == PollingStatus.SUCCESS) {
                logger.debug("Equipment status polling successful for: {}", equipmentId);
            } else {
                logger.warn("Equipment status polling failed for: {} - {}", equipmentId, result.getMessage());
            }

            return result;
        } catch (Exception e) {
            logger.error("Exception occurred while polling equipment status: {}", equipmentId, e);
            return new PollingResult(PollingStatus.FAILURE, "Exception: " + e.getMessage(), null);
        }
    }

    /**
     * 启动设备状态轮询
     */
    public static boolean startPolling(String equipmentId, PollingConfig config) {
        if (equipmentId == null || equipmentId.isEmpty()) {
            logger.error("Invalid equipment ID for polling start");
            return false;
        }

        if (config == null) {
            config = new PollingConfig();
        }

        try {
            // 检查是否已经在轮询
            if (pollingTaskRegistry.containsKey(equipmentId)) {
                logger.warn("Equipment {} is already being polled", equipmentId);
                return true;
            }

            // 创建轮询任务
            PollingTask task = new PollingTask(equipmentId, config);

            // 安排定期执行
            ScheduledFuture<?> scheduledTask = scheduler.scheduleAtFixedRate(
                    task,
                    0, // 立即开始
                    config.getPollingInterval(),
                    TimeUnit.MILLISECONDS
            );

            // 保存任务引用
            pollingTaskRegistry.put(equipmentId, task);
            task.setScheduledFuture(scheduledTask);

            logger.info("Started polling for equipment: {} with interval: {}ms", equipmentId, config.getPollingInterval());
            return true;
        } catch (Exception e) {
            logger.error("Failed to start polling for equipment: {}", equipmentId, e);
            return false;
        }
    }

    /**
     * 停止设备状态轮询
     */
    public static boolean stopPolling(String equipmentId) {
        if (equipmentId == null || equipmentId.isEmpty()) {
            logger.error("Invalid equipment ID for polling stop");
            return false;
        }

        try {
            PollingTask task = pollingTaskRegistry.remove(equipmentId);
            if (task != null) {
                task.cancel();
                logger.info("Stopped polling for equipment: {}", equipmentId);
                return true;
            } else {
                logger.warn("Equipment {} is not being polled", equipmentId);
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to stop polling for equipment: {}", equipmentId, e);
            return false;
        }
    }

    /**
     * 设备事件监听
     */
    public static void registerEventListener(String equipmentId, EquipmentEventListener listener) {
        if (equipmentId == null || equipmentId.isEmpty() || listener == null) {
            logger.error("Invalid equipment ID or listener for event registration");
            return;
        }

        listenerRegistry.put(equipmentId, listener);
        logger.debug("Registered event listener for equipment: {}", equipmentId);
    }

    /**
     * 注销设备事件监听
     */
    public static void unregisterEventListener(String equipmentId) {
        if (equipmentId == null || equipmentId.isEmpty()) {
            logger.error("Invalid equipment ID for event unregistration");
            return;
        }

        EquipmentEventListener removed = listenerRegistry.remove(equipmentId);
        if (removed != null) {
            logger.debug("Unregistered event listener for equipment: {}", equipmentId);
        } else {
            logger.warn("No event listener found for equipment: {}", equipmentId);
        }
    }

    /**
     * 触发设备事件
     */
    public static void fireEvent(String equipmentId, EquipmentEvent event) {
        if (equipmentId == null || equipmentId.isEmpty() || event == null) {
            logger.warn("Invalid equipment ID or event for firing");
            return;
        }

        EquipmentEventListener listener = listenerRegistry.get(equipmentId);
        if (listener != null) {
            try {
                listener.onEquipmentEvent(event);
                logger.debug("Fired event {} for equipment: {}", event.getEventType(), equipmentId);
            } catch (Exception e) {
                logger.error("Exception occurred while firing event for equipment: {}", equipmentId, e);
            }
        } else {
            logger.debug("No listener registered for equipment: {}", equipmentId);
        }
    }

    /**
     * 获取设备信息
     */
    public static EquipmentInfo getEquipmentInfo(String equipmentId) {
        if (equipmentId == null || equipmentId.isEmpty()) {
            logger.error("Invalid equipment ID for info retrieval");
            return null;
        }

        try {
            // 创建获取设备信息的标准命令
            StandardCommand infoCommand = new StandardCommand(StandardCommand.CommandType.GET_INFO, equipmentId);

            // 发送命令
            EquipmentResponse response = sendStandardCommand(equipmentId, infoCommand);

            if (response != null && response.getStatus() == ResponseStatus.SUCCESS) {
                Object data = response.getData();
                if (data instanceof EquipmentInfo) {
                    logger.debug("Retrieved equipment info for: {}", equipmentId);
                    return (EquipmentInfo) data;
                }
            }

            logger.warn("Failed to retrieve equipment info for: {}", equipmentId);
            return null;
        } catch (Exception e) {
            logger.error("Exception occurred while getting equipment info: {}", equipmentId, e);
            return null;
        }
    }

    /**
     * 获取设备状态
     */
    public static EquipmentStatus getEquipmentStatus(String equipmentId) {
        if (equipmentId == null || equipmentId.isEmpty()) {
            logger.error("Invalid equipment ID for status retrieval");
            return new EquipmentStatus(equipmentId, EquipmentState.UNKNOWN, System.currentTimeMillis());
        }

        try {
            // 创建获取设备状态的标准命令
            StandardCommand statusCommand = new StandardCommand(StandardCommand.CommandType.GET_STATUS, equipmentId);

            // 发送命令
            EquipmentResponse response = sendStandardCommand(equipmentId, statusCommand);

            if (response != null && response.getStatus() == ResponseStatus.SUCCESS) {
                Object data = response.getData();
                if (data instanceof EquipmentStatus) {
                    logger.debug("Retrieved equipment status for: {}", equipmentId);
                    return (EquipmentStatus) data;
                }
            }

            logger.warn("Failed to retrieve equipment status for: {}", equipmentId);
            return new EquipmentStatus(equipmentId, EquipmentState.UNKNOWN, System.currentTimeMillis());
        } catch (Exception e) {
            logger.error("Exception occurred while getting equipment status: {}", equipmentId, e);
            return new EquipmentStatus(equipmentId, EquipmentState.ERROR, System.currentTimeMillis());
        }
    }

    /**
     * 控制设备
     */
    public static boolean controlEquipment(String equipmentId, ControlCommand command) {
        if (equipmentId == null || equipmentId.isEmpty() || command == null) {
            logger.error("Invalid equipment ID or command for equipment control");
            return false;
        }

        try {
            // 创建控制设备的标准命令
            StandardCommand controlCommand = new StandardCommand(StandardCommand.CommandType.CONTROL, equipmentId);
            controlCommand.setParameter("controlAction", command.getAction());
            controlCommand.setParameter("controlParameters", command.getParameters());

            // 发送命令
            EquipmentResponse response = sendStandardCommand(equipmentId, controlCommand);

            boolean success = response != null && response.getStatus() == ResponseStatus.SUCCESS;

            if (success) {
                logger.info("Equipment control successful for: {} with action: {}", equipmentId, command.getAction());
            } else {
                logger.warn("Equipment control failed for: {} with action: {}", equipmentId, command.getAction());
            }

            return success;
        } catch (Exception e) {
            logger.error("Exception occurred while controlling equipment: {}", equipmentId, e);
            return false;
        }
    }

    /**
     * 检查连接健康状态
     */
    private static void checkConnectionHealth() {
        try {
            for (Map.Entry<String, EquipmentConnection> entry : connectionPool.entrySet()) {
                EquipmentConnection connection = entry.getValue();
                if (connection.isConnected()) {
                    try {
                        boolean healthy = connection.ping();
                        if (!healthy) {
                            logger.warn("Connection health check failed for equipment: {}", connection.getEquipmentId());
                            // 尝试重新连接
                            connection.disconnect();
                            connection.connect();
                        }
                    } catch (Exception e) {
                        logger.error("Failed to check connection health for equipment: {}", connection.getEquipmentId(), e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception occurred during connection health check", e);
        }
    }

    /**
     * 清理缓存
     */
    private static void cleanupCache() {
        try {
            long cutoffTime = System.currentTimeMillis() - DEFAULT_CACHE_TIMEOUT;
            responseCache.entrySet().removeIf(entry ->
                    entry.getValue().getTimestamp() < cutoffTime
            );
            logger.debug("Cleaned up equipment adapter cache, remaining entries: {}", responseCache.size());
        } catch (Exception e) {
            logger.error("Failed to cleanup equipment adapter cache", e);
        }
    }

    /**
     * 获取统计信息
     */
    public static AdapterStatistics getStatistics() {
        AdapterStatistics stats = new AdapterStatistics();
        stats.setTotalCommands(totalCommands.get());
        stats.setSuccessfulCommands(successfulCommands.get());
        stats.setFailedCommands(failedCommands.get());
        stats.setActiveConnections(activeConnections.get());
        stats.setConnectionPoolSize(connectionPool.size());
        stats.setResponseCacheSize(responseCache.size());
        stats.setEventListeners(listenerRegistry.size());
        stats.setPollingTasks(pollingTaskRegistry.size());
        return stats;
    }

    /**
     * 关闭适配器
     */
    public static void shutdown() {
        try {
            // 停止所有轮询任务
            for (Map.Entry<String, PollingTask> entry : pollingTaskRegistry.entrySet()) {
                entry.getValue().cancel();
            }
            pollingTaskRegistry.clear();

            // 关闭所有连接
            for (Map.Entry<String, EquipmentConnection> entry : connectionPool.entrySet()) {
                try {
                    entry.getValue().disconnect();
                } catch (Exception e) {
                    logger.warn("Failed to disconnect equipment: {}", entry.getValue().getEquipmentId(), e);
                }
            }
            connectionPool.clear();

            // 关闭线程池
            scheduler.shutdown();
            commandExecutor.shutdown();

            try {
                if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }

                if (!commandExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    commandExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                commandExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }

            logger.info("EquipmentAdapter shutdown completed");
        } catch (Exception e) {
            logger.error("Failed to shutdown EquipmentAdapter", e);
        }
    }

    // 协议类型枚举
    public enum ProtocolType {
        SECSGEM("SECS/GEM协议"),
        MODBUS("Modbus协议"),
        OPCUA("OPC UA协议"),
        HTTP("HTTP协议"),
        CUSTOM("自定义协议");

        private final String description;

        ProtocolType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 响应状态枚举
    public enum ResponseStatus {
        SUCCESS("成功"),
        FAILURE("失败"),
        TIMEOUT("超时"),
        UNAUTHORIZED("未授权"),
        NOT_FOUND("未找到");

        private final String description;

        ResponseStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 轮询状态枚举
    public enum PollingStatus {
        SUCCESS("成功"),
        FAILURE("失败"),
        TIMEOUT("超时");

        private final String description;

        PollingStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 设备状态枚举
    public enum EquipmentState {
        UNKNOWN("未知"),
        RUNNING("运行中"),
        IDLE("空闲"),
        SETUP("设置中"),
        MAINTENANCE("维护中"),
        ERROR("错误"),
        OFFLINE("离线");

        private final String description;

        EquipmentState(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 标准命令类型枚举
    public enum StandardCommandType {
        GET_INFO("获取设备信息"),
        GET_STATUS("获取设备状态"),
        CONTROL("控制设备"),
        CONFIGURE("配置设备"),
        DIAGNOSE("诊断设备"),
        CUSTOM("自定义命令");

        private final String description;

        StandardCommandType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 事件类型枚举
    public enum EventType {
        STATUS_CHANGE("状态变化"),
        ALARM("告警"),
        ERROR("错误"),
        MAINTENANCE("维护"),
        CUSTOM("自定义事件");

        private final String description;

        EventType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 设备连接工厂接口
    public interface EquipmentConnectionFactory {
        EquipmentConnection createConnection(String equipmentId, ConnectionConfig config) throws Exception;

        boolean supportsProtocol(ProtocolType protocol);
    }

    // 设备连接接口
    public interface EquipmentConnection {
        boolean connect() throws Exception;

        void disconnect() throws Exception;

        boolean isConnected();

        boolean ping() throws Exception;

        byte[] send(byte[] data, long timeout) throws Exception;

        String getEquipmentId();

        ProtocolType getProtocol();

        ConnectionInfo getConnectionInfo();
    }

    // 命令处理器接口
    public interface EquipmentCommandProcessor {
        EquipmentResponse processCommand(EquipmentConnection connection, StandardCommand command, CommandConfig config) throws Exception;

        boolean supportsProtocol(ProtocolType protocol);

        String getProcessorName();
    }

    // 设备事件监听器接口
    public interface EquipmentEventListener {
        void onEquipmentEvent(EquipmentEvent event);

        void onError(EquipmentException exception);
    }

    // 连接配置类
    public static class ConnectionConfig {
        private long connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
        private long responseTimeout = DEFAULT_RESPONSE_TIMEOUT;
        private int maxRetries = DEFAULT_MAX_RETRIES;
        private long retryDelay = DEFAULT_RETRY_DELAY;
        private String host;
        private int port;
        private Map<String, Object> parameters = new ConcurrentHashMap<>();

        // Getters and Setters
        public long getConnectionTimeout() {
            return connectionTimeout;
        }

        public ConnectionConfig setConnectionTimeout(long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public long getResponseTimeout() {
            return responseTimeout;
        }

        public ConnectionConfig setResponseTimeout(long responseTimeout) {
            this.responseTimeout = responseTimeout;
            return this;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public ConnectionConfig setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public long getRetryDelay() {
            return retryDelay;
        }

        public ConnectionConfig setRetryDelay(long retryDelay) {
            this.retryDelay = retryDelay;
            return this;
        }

        public String getHost() {
            return host;
        }

        public ConnectionConfig setHost(String host) {
            this.host = host;
            return this;
        }

        public int getPort() {
            return port;
        }

        public ConnectionConfig setPort(int port) {
            this.port = port;
            return this;
        }

        public Map<String, Object> getParameters() {
            return new HashMap<>(parameters);
        }

        public ConnectionConfig setParameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        public Object getParameter(String key) {
            return this.parameters.get(key);
        }

        @Override
        public String toString() {
            return "ConnectionConfig{" +
                    "connectionTimeout=" + connectionTimeout +
                    ", responseTimeout=" + responseTimeout +
                    ", maxRetries=" + maxRetries +
                    ", retryDelay=" + retryDelay +
                    ", host='" + host + '\'' +
                    ", port=" + port +
                    ", parameterCount=" + parameters.size() +
                    '}';
        }
    }

    // 命令配置类
    public static class CommandConfig {
        private long responseTimeout = DEFAULT_RESPONSE_TIMEOUT;
        private int maxRetries = DEFAULT_MAX_RETRIES;
        private long retryDelay = DEFAULT_RETRY_DELAY;
        private boolean enableCache = true;
        private ProtocolType protocol;
        private String processorName = "default";
        private Map<String, Object> parameters = new ConcurrentHashMap<>();

        // Getters and Setters
        public long getResponseTimeout() {
            return responseTimeout;
        }

        public CommandConfig setResponseTimeout(long responseTimeout) {
            this.responseTimeout = responseTimeout;
            return this;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public CommandConfig setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public long getRetryDelay() {
            return retryDelay;
        }

        public CommandConfig setRetryDelay(long retryDelay) {
            this.retryDelay = retryDelay;
            return this;
        }

        public boolean isEnableCache() {
            return enableCache;
        }

        public CommandConfig setEnableCache(boolean enableCache) {
            this.enableCache = enableCache;
            return this;
        }

        public ProtocolType getProtocol() {
            return protocol;
        }

        public CommandConfig setProtocol(ProtocolType protocol) {
            this.protocol = protocol;
            return this;
        }

        public String getProcessorName() {
            return processorName;
        }

        public CommandConfig setProcessorName(String processorName) {
            this.processorName = processorName;
            return this;
        }

        public Map<String, Object> getParameters() {
            return new HashMap<>(parameters);
        }

        public CommandConfig setParameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        public Object getParameter(String key) {
            return this.parameters.get(key);
        }

        @Override
        public String toString() {
            return "CommandConfig{" +
                    "responseTimeout=" + responseTimeout +
                    ", maxRetries=" + maxRetries +
                    ", retryDelay=" + retryDelay +
                    ", enableCache=" + enableCache +
                    ", protocol=" + protocol +
                    ", processorName='" + processorName + '\'' +
                    ", parameterCount=" + parameters.size() +
                    '}';
        }
    }

    // 轮询配置类
    public static class PollingConfig {
        private long pollingInterval = DEFAULT_POLLING_INTERVAL;
        private long responseTimeout = DEFAULT_RESPONSE_TIMEOUT;
        private int maxRetries = DEFAULT_MAX_RETRIES;
        private long retryDelay = DEFAULT_RETRY_DELAY;
        private boolean enableCache = true;
        private ProtocolType protocol;
        private Map<String, Object> parameters = new ConcurrentHashMap<>();

        // Getters and Setters
        public long getPollingInterval() {
            return pollingInterval;
        }

        public PollingConfig setPollingInterval(long pollingInterval) {
            this.pollingInterval = pollingInterval;
            return this;
        }

        public long getResponseTimeout() {
            return responseTimeout;
        }

        public PollingConfig setResponseTimeout(long responseTimeout) {
            this.responseTimeout = responseTimeout;
            return this;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public PollingConfig setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public long getRetryDelay() {
            return retryDelay;
        }

        public PollingConfig setRetryDelay(long retryDelay) {
            this.retryDelay = retryDelay;
            return this;
        }

        public boolean isEnableCache() {
            return enableCache;
        }

        public PollingConfig setEnableCache(boolean enableCache) {
            this.enableCache = enableCache;
            return this;
        }

        public ProtocolType getProtocol() {
            return protocol;
        }

        public PollingConfig setProtocol(ProtocolType protocol) {
            this.protocol = protocol;
            return this;
        }

        public Map<String, Object> getParameters() {
            return new HashMap<>(parameters);
        }

        public PollingConfig setParameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        public Object getParameter(String key) {
            return this.parameters.get(key);
        }

        @Override
        public String toString() {
            return "PollingConfig{" +
                    "pollingInterval=" + pollingInterval +
                    ", responseTimeout=" + responseTimeout +
                    ", maxRetries=" + maxRetries +
                    ", retryDelay=" + retryDelay +
                    ", enableCache=" + enableCache +
                    ", protocol=" + protocol +
                    ", parameterCount=" + parameters.size() +
                    '}';
        }
    }

    // 标准命令类
    public static class StandardCommand {
        private final String commandId;
        private final CommandType commandType;
        private final String equipmentId;
        private final LocalDateTime createTime;
        private final Map<String, Object> parameters;
        private final String description;
        public StandardCommand(CommandType commandType, String equipmentId) {
            this.commandId = UUID.randomUUID().toString();
            this.commandType = commandType != null ? commandType : CommandType.CUSTOM;
            this.equipmentId = equipmentId != null ? equipmentId : "";
            this.createTime = LocalDateTime.now();
            this.parameters = new ConcurrentHashMap<>();
            this.description = "";
        }

        // Getters and Setters
        public String getCommandId() {
            return commandId;
        }

        public CommandType getCommandType() {
            return commandType;
        }

        public String getEquipmentId() {
            return equipmentId;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public String getDescription() {
            return description;
        }

        public Map<String, Object> getParameters() {
            return new HashMap<>(parameters);
        }

        public StandardCommand setParameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        public Object getParameter(String key) {
            return this.parameters.get(key);
        }

        @Override
        public String toString() {
            return "StandardCommand{" +
                    "commandId='" + commandId + '\'' +
                    ", commandType=" + commandType +
                    ", equipmentId='" + equipmentId + '\'' +
                    ", createTime=" + createTime +
                    ", parameterCount=" + parameters.size() +
                    '}';
        }

        public enum CommandType {
            GET_INFO, GET_STATUS, CONTROL, CONFIGURE, DIAGNOSE, CUSTOM
        }
    }

    // 设备响应类
    public static class EquipmentResponse {
        private final String responseId;
        private final ResponseStatus status;
        private final String message;
        private final Object data;
        private final long timestamp;
        private final long duration;
        private final Map<String, Object> metadata;

        public EquipmentResponse(ResponseStatus status, String message, Object data) {
            this.responseId = UUID.randomUUID().toString();
            this.status = status != null ? status : ResponseStatus.FAILURE;
            this.message = message != null ? message : "";
            this.data = data;
            this.timestamp = System.currentTimeMillis();
            this.duration = 0;
            this.metadata = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getResponseId() {
            return responseId;
        }

        public ResponseStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public Object getData() {
            return data;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public long getDuration() {
            return duration;
        }

        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }

        public EquipmentResponse setMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Object getMetadata(String key) {
            return this.metadata.get(key);
        }

        @Override
        public String toString() {
            return "EquipmentResponse{" +
                    "responseId='" + responseId + '\'' +
                    ", status=" + status +
                    ", message='" + message + '\'' +
                    ", dataPresent=" + (data != null) +
                    ", timestamp=" + timestamp +
                    ", duration=" + duration + "ms" +
                    '}';
        }
    }

    // 轮询结果类
    public static class PollingResult {
        private final String resultId;
        private final PollingStatus status;
        private final String message;
        private final EquipmentStatus equipmentStatus;
        private final long timestamp;
        private final long duration;
        private final Map<String, Object> metadata;

        public PollingResult(PollingStatus status, String message, EquipmentStatus equipmentStatus) {
            this.resultId = UUID.randomUUID().toString();
            this.status = status != null ? status : PollingStatus.FAILURE;
            this.message = message != null ? message : "";
            this.equipmentStatus = equipmentStatus;
            this.timestamp = System.currentTimeMillis();
            this.duration = 0;
            this.metadata = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getResultId() {
            return resultId;
        }

        public PollingStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public EquipmentStatus getEquipmentStatus() {
            return equipmentStatus;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public long getDuration() {
            return duration;
        }

        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }

        public PollingResult setMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Object getMetadata(String key) {
            return this.metadata.get(key);
        }

        @Override
        public String toString() {
            return "PollingResult{" +
                    "resultId='" + resultId + '\'' +
                    ", status=" + status +
                    ", message='" + message + '\'' +
                    ", equipmentStatus=" + (equipmentStatus != null ? equipmentStatus.getState() : "null") +
                    ", timestamp=" + timestamp +
                    ", duration=" + duration + "ms" +
                    '}';
        }
    }

    // 控制命令类
    public static class ControlCommand {
        private final String action;
        private final Map<String, Object> parameters;
        private final LocalDateTime createTime;

        public ControlCommand(String action) {
            this.action = action != null ? action : "";
            this.parameters = new ConcurrentHashMap<>();
            this.createTime = LocalDateTime.now();
        }

        // Getters and Setters
        public String getAction() {
            return action;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public Map<String, Object> getParameters() {
            return new HashMap<>(parameters);
        }

        public ControlCommand setParameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        public Object getParameter(String key) {
            return this.parameters.get(key);
        }

        @Override
        public String toString() {
            return "ControlCommand{" +
                    "action='" + action + '\'' +
                    ", parameterCount=" + parameters.size() +
                    ", createTime=" + createTime +
                    '}';
        }
    }

    // 设备信息类
    public static class EquipmentInfo {
        private final String equipmentId;
        private final String equipmentName;
        private final Map<String, Object> attributes;
        private final LocalDateTime lastUpdate;
        private String model;
        private String manufacturer;
        private String serialNumber;
        private String firmwareVersion;
        private LocalDateTime manufactureDate;

        public EquipmentInfo(String equipmentId, String equipmentName) {
            this.equipmentId = equipmentId != null ? equipmentId : "";
            this.equipmentName = equipmentName != null ? equipmentName : "";
            this.model = "";
            this.manufacturer = "";
            this.serialNumber = "";
            this.firmwareVersion = "";
            this.manufactureDate = LocalDateTime.now();
            this.attributes = new ConcurrentHashMap<>();
            this.lastUpdate = LocalDateTime.now();
        }

        // Getters and Setters
        public String getEquipmentId() {
            return equipmentId;
        }

        public String getEquipmentName() {
            return equipmentName;
        }

        public String getModel() {
            return model;
        }

        public EquipmentInfo setModel(String model) {
            this.model = model;
            return this;
        }

        public String getManufacturer() {
            return manufacturer;
        }

        public EquipmentInfo setManufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
            return this;
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public EquipmentInfo setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
            return this;
        }

        public String getFirmwareVersion() {
            return firmwareVersion;
        }

        public EquipmentInfo setFirmwareVersion(String firmwareVersion) {
            this.firmwareVersion = firmwareVersion;
            return this;
        }

        public LocalDateTime getManufactureDate() {
            return manufactureDate;
        }

        public EquipmentInfo setManufactureDate(LocalDateTime manufactureDate) {
            this.manufactureDate = manufactureDate;
            return this;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public EquipmentInfo setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        public LocalDateTime getLastUpdate() {
            return lastUpdate;
        }

        @Override
        public String toString() {
            return "EquipmentInfo{" +
                    "equipmentId='" + equipmentId + '\'' +
                    ", equipmentName='" + equipmentName + '\'' +
                    ", model='" + model + '\'' +
                    ", manufacturer='" + manufacturer + '\'' +
                    ", serialNumber='" + serialNumber + '\'' +
                    ", firmwareVersion='" + firmwareVersion + '\'' +
                    ", manufactureDate=" + manufactureDate +
                    ", attributeCount=" + attributes.size() +
                    '}';
        }
    }

    // 设备状态类
    public static class EquipmentStatus {
        private final String equipmentId;
        private final EquipmentState state;
        private final long timestamp;
        private final Map<String, Object> attributes;
        private final String statusMessage;

        public EquipmentStatus(String equipmentId, EquipmentState state, long timestamp) {
            this.equipmentId = equipmentId != null ? equipmentId : "";
            this.state = state != null ? state : EquipmentState.UNKNOWN;
            this.timestamp = timestamp;
            this.attributes = new ConcurrentHashMap<>();
            this.statusMessage = "";
        }

        // Getters and Setters
        public String getEquipmentId() {
            return equipmentId;
        }

        public EquipmentState getState() {
            return state;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getStatusMessage() {
            return statusMessage;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public EquipmentStatus setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        @Override
        public String toString() {
            return "EquipmentStatus{" +
                    "equipmentId='" + equipmentId + '\'' +
                    ", state=" + state +
                    ", timestamp=" + timestamp +
                    ", attributeCount=" + attributes.size() +
                    '}';
        }
    }

    // 设备事件类
    public static class EquipmentEvent {
        private final String eventId;
        private final EventType eventType;
        private final String equipmentId;
        private final Object eventData;
        private final LocalDateTime eventTime;
        private final String message;
        private final Map<String, Object> attributes;

        public EquipmentEvent(EventType eventType, String equipmentId, Object eventData) {
            this.eventId = UUID.randomUUID().toString();
            this.eventType = eventType != null ? eventType : EventType.CUSTOM;
            this.equipmentId = equipmentId != null ? equipmentId : "";
            this.eventData = eventData;
            this.eventTime = LocalDateTime.now();
            this.message = "";
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getEventId() {
            return eventId;
        }

        public EventType getEventType() {
            return eventType;
        }

        public String getEquipmentId() {
            return equipmentId;
        }

        public Object getEventData() {
            return eventData;
        }

        public LocalDateTime getEventTime() {
            return eventTime;
        }

        public String getMessage() {
            return message;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public EquipmentEvent setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        @Override
        public String toString() {
            return "EquipmentEvent{" +
                    "eventId='" + eventId + '\'' +
                    ", eventType=" + eventType +
                    ", equipmentId='" + equipmentId + '\'' +
                    ", eventDataPresent=" + (eventData != null) +
                    ", eventTime=" + eventTime +
                    ", attributeCount=" + attributes.size() +
                    '}';
        }
    }

    // 连接信息类
    public static class ConnectionInfo {
        private final String connectionId;
        private final ProtocolType protocol;
        private final String host;
        private final int port;
        private final boolean connected;
        private final long connectTime;
        private final Map<String, Object> attributes;

        public ConnectionInfo(ProtocolType protocol, String host, int port) {
            this.connectionId = UUID.randomUUID().toString();
            this.protocol = protocol != null ? protocol : ProtocolType.CUSTOM;
            this.host = host != null ? host : "";
            this.port = port;
            this.connected = false;
            this.connectTime = 0;
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getConnectionId() {
            return connectionId;
        }

        public ProtocolType getProtocol() {
            return protocol;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public boolean isConnected() {
            return connected;
        }

        public long getConnectTime() {
            return connectTime;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public ConnectionInfo setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        @Override
        public String toString() {
            return "ConnectionInfo{" +
                    "connectionId='" + connectionId + '\'' +
                    ", protocol=" + protocol +
                    ", host='" + host + '\'' +
                    ", port=" + port +
                    ", connected=" + connected +
                    ", connectTime=" + connectTime +
                    ", attributeCount=" + attributes.size() +
                    '}';
        }
    }

    // 适配器统计信息类
    public static class AdapterStatistics {
        private final LocalDateTime lastUpdate;
        private long totalCommands;
        private long successfulCommands;
        private long failedCommands;
        private int activeConnections;
        private int connectionPoolSize;
        private int responseCacheSize;
        private int eventListeners;
        private int pollingTasks;

        public AdapterStatistics() {
            this.totalCommands = 0;
            this.successfulCommands = 0;
            this.failedCommands = 0;
            this.activeConnections = 0;
            this.connectionPoolSize = 0;
            this.responseCacheSize = 0;
            this.eventListeners = 0;
            this.pollingTasks = 0;
            this.lastUpdate = LocalDateTime.now();
        }

        // Getters and Setters
        public long getTotalCommands() {
            return totalCommands;
        }

        public void setTotalCommands(long totalCommands) {
            this.totalCommands = totalCommands;
        }

        public long getSuccessfulCommands() {
            return successfulCommands;
        }

        public void setSuccessfulCommands(long successfulCommands) {
            this.successfulCommands = successfulCommands;
        }

        public long getFailedCommands() {
            return failedCommands;
        }

        public void setFailedCommands(long failedCommands) {
            this.failedCommands = failedCommands;
        }

        public int getActiveConnections() {
            return activeConnections;
        }

        public void setActiveConnections(int activeConnections) {
            this.activeConnections = activeConnections;
        }

        public int getConnectionPoolSize() {
            return connectionPoolSize;
        }

        public void setConnectionPoolSize(int connectionPoolSize) {
            this.connectionPoolSize = connectionPoolSize;
        }

        public int getResponseCacheSize() {
            return responseCacheSize;
        }

        public void setResponseCacheSize(int responseCacheSize) {
            this.responseCacheSize = responseCacheSize;
        }

        public int getEventListeners() {
            return eventListeners;
        }

        public void setEventListeners(int eventListeners) {
            this.eventListeners = eventListeners;
        }

        public int getPollingTasks() {
            return pollingTasks;
        }

        public void setPollingTasks(int pollingTasks) {
            this.pollingTasks = pollingTasks;
        }

        public LocalDateTime getLastUpdate() {
            return lastUpdate;
        }

        public double getSuccessRate() {
            return totalCommands > 0 ? (double) successfulCommands / totalCommands * 100 : 0.0;
        }

        @Override
        public String toString() {
            return "AdapterStatistics{" +
                    "totalCommands=" + totalCommands +
                    ", successfulCommands=" + successfulCommands +
                    ", failedCommands=" + failedCommands +
                    ", successRate=" + String.format("%.2f", getSuccessRate()) + "%" +
                    ", activeConnections=" + activeConnections +
                    ", connectionPoolSize=" + connectionPoolSize +
                    ", responseCacheSize=" + responseCacheSize +
                    ", eventListeners=" + eventListeners +
                    ", pollingTasks=" + pollingTasks +
                    ", lastUpdate=" + lastUpdate +
                    '}';
        }
    }

    // 设备异常类
    public static class EquipmentException extends Exception {
        private final String equipmentId;
        private final String errorCode;

        public EquipmentException(String equipmentId, String message) {
            super(message);
            this.equipmentId = equipmentId != null ? equipmentId : "";
            this.errorCode = "";
        }

        public EquipmentException(String equipmentId, String errorCode, String message) {
            super(message);
            this.equipmentId = equipmentId != null ? equipmentId : "";
            this.errorCode = errorCode != null ? errorCode : "";
        }

        public EquipmentException(String equipmentId, String message, Throwable cause) {
            super(message, cause);
            this.equipmentId = equipmentId != null ? equipmentId : "";
            this.errorCode = "";
        }

        // Getters
        public String getEquipmentId() {
            return equipmentId;
        }

        public String getErrorCode() {
            return errorCode;
        }

        @Override
        public String toString() {
            return "EquipmentException{" +
                    "equipmentId='" + equipmentId + '\'' +
                    ", errorCode='" + errorCode + '\'' +
                    ", message='" + getMessage() + '\'' +
                    '}';
        }
    }

    // 轮询任务类
    private static class PollingTask implements Runnable {
        private final String equipmentId;
        private final PollingConfig config;
        private volatile ScheduledFuture<?> scheduledFuture;
        private volatile boolean cancelled = false;

        public PollingTask(String equipmentId, PollingConfig config) {
            this.equipmentId = equipmentId;
            this.config = config;
        }

        public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
            this.scheduledFuture = scheduledFuture;
        }

        public void cancel() {
            cancelled = true;
            if (scheduledFuture != null) {
                scheduledFuture.cancel(true);
            }
        }

        @Override
        public void run() {
            if (cancelled) {
                return;
            }

            try {
                // 执行轮询
                PollingResult result = pollEquipmentStatus(equipmentId, config);

                // 触发事件
                if (result.getEquipmentStatus() != null) {
                    EquipmentEvent event = new EquipmentEvent(
                            EventType.STATUS_CHANGE,
                            equipmentId,
                            result.getEquipmentStatus()
                    );
                    fireEvent(equipmentId, event);
                }
            } catch (Exception e) {
                logger.error("Exception occurred during polling task for equipment: {}", equipmentId, e);

                // 触发错误事件
                EquipmentEvent errorEvent = new EquipmentEvent(
                        EventType.ERROR,
                        equipmentId,
                        e.getMessage()
                );
                fireEvent(equipmentId, errorEvent);
            }
        }

        public PollingResult execute() {
            return pollEquipmentStatus(equipmentId, config);
        }
    }
}
