package org.im.semiconductor.production.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 工艺过程控制工具类
 * <p>
 * 特性：
 * - 通用性：支持多种工艺过程和控制方法
 * - 规范性：统一的过程控制标准和执行流程
 * - 专业性：半导体行业工艺过程专业控制实现
 * - 灵活性：可配置的过程参数和调整规则
 * - 可靠性：完善的异常处理和容错机制
 * - 安全性：过程数据保护和访问控制
 * - 复用性：模块化设计，组件可独立使用
 * - 容错性：优雅的错误处理和恢复机制
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/08/21
 */
public class ProcessController {

    // 默认配置
    public static final long DEFAULT_CACHE_TIMEOUT = 30 * 60 * 1000; // 30分钟
    public static final int DEFAULT_MAX_RETRY_ATTEMPTS = 3;
    public static final long DEFAULT_RETRY_DELAY = 1000; // 1秒
    public static final int DEFAULT_TRACE_BUFFER_SIZE = 1000;
    public static final long DEFAULT_PROCESS_TIMEOUT = 24 * 60 * 60 * 1000; // 24小时
    private static final Logger logger = LoggerFactory.getLogger(ProcessController.class);
    // 内部存储和管理
    private static final Map<ProcessType, ProcessControllerEngine> controllerRegistry = new ConcurrentHashMap<>();
    private static final Map<AdjustmentType, ParameterAdjuster> adjusterRegistry = new ConcurrentHashMap<>();
    private static final Map<ExceptionType, ExceptionHandler> handlerRegistry = new ConcurrentHashMap<>();
    private static final Map<String, TraceStorage> traceStorageRegistry = new ConcurrentHashMap<>();
    private static final Map<String, ExecutionContext> processContextCache = new ConcurrentHashMap<>();
    private static final Map<String, ProcessTrace> processTraceCache = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private static final AtomicLong processCounter = new AtomicLong(0);
    // 当前使用的组件
    private static volatile ProcessControllerEngine currentController;
    private static volatile ParameterAdjuster currentAdjuster;
    private static volatile ExceptionHandler currentHandler;
    private static volatile TraceStorage currentTraceStorage;

    // 初始化默认组件
    static {
        registerDefaultComponents();
        initializeCurrentComponents();
        startMaintenanceTasks();
        logger.info("ProcessController initialized with default components");
    }

    /**
     * 注册默认组件
     */
    private static void registerDefaultComponents() {
        // 注册默认控制器、调整器、处理器和追溯存储
        // registerController(new DefaultProcessControllerEngine());
        // registerAdjuster(new DefaultParameterAdjuster());
        // registerHandler(new DefaultExceptionHandler());
        // registerTraceStorage(new DefaultTraceStorage());
    }

    /**
     * 初始化当前组件
     */
    private static void initializeCurrentComponents() {
        // 初始化为第一个注册的组件
        if (!controllerRegistry.isEmpty()) {
            currentController = controllerRegistry.values().iterator().next();
        }

        if (!adjusterRegistry.isEmpty()) {
            currentAdjuster = adjusterRegistry.values().iterator().next();
        }

        if (!handlerRegistry.isEmpty()) {
            currentHandler = handlerRegistry.values().iterator().next();
        }

        if (!traceStorageRegistry.isEmpty()) {
            currentTraceStorage = traceStorageRegistry.values().iterator().next();
        }
    }

    /**
     * 启动维护任务
     */
    private static void startMaintenanceTasks() {
        // 启动缓存清理任务
        scheduler.scheduleAtFixedRate(ProcessController::cleanupCache,
                30, 30, TimeUnit.MINUTES);

        logger.debug("Process controller maintenance tasks started");
    }

    /**
     * 注册工艺控制器
     */
    public static void registerController(ProcessControllerEngine controller) {
        if (controller == null) {
            throw new IllegalArgumentException("Controller cannot be null");
        }

        for (ProcessType type : ProcessType.values()) {
            if (controller.supportsProcessType(type)) {
                controllerRegistry.put(type, controller);
                logger.debug("Registered controller {} for process type {}", controller.getControllerName(), type);
            }
        }
    }

    /**
     * 注册参数调整器
     */
    public static void registerAdjuster(ParameterAdjuster adjuster) {
        if (adjuster == null) {
            throw new IllegalArgumentException("Adjuster cannot be null");
        }

        for (AdjustmentType type : AdjustmentType.values()) {
            if (adjuster.supportsAdjustmentType(type)) {
                adjusterRegistry.put(type, adjuster);
                logger.debug("Registered adjuster {} for adjustment type {}", adjuster.getAdjusterName(), type);
            }
        }
    }

    /**
     * 注册异常处理器
     */
    public static void registerHandler(ExceptionHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("Handler cannot be null");
        }

        for (ExceptionType type : ExceptionType.values()) {
            if (handler.supportsExceptionType(type)) {
                handlerRegistry.put(type, handler);
                logger.debug("Registered handler {} for type type {}", handler.getHandlerName(), type);
            }
        }
    }

    /**
     * 注册追溯存储
     */
    public static void registerTraceStorage(TraceStorage traceStorage) {
        if (traceStorage == null) {
            throw new IllegalArgumentException("Trace storage cannot be null");
        }

        // 注册支持的所有追溯类型
        String[] traceTypes = {"database", "file", "memory", "cloud"}; // 简化实现
        for (String type : traceTypes) {
            if (traceStorage.supportsTraceType(type)) {
                traceStorageRegistry.put(type, traceStorage);
                logger.debug("Registered trace storage {} for type {}", traceStorage.getTraceStorageName(), type);
            }
        }
    }

    /**
     * 设置当前工艺控制器
     */
    public static void setCurrentController(ProcessType processType) {
        ProcessControllerEngine controller = controllerRegistry.get(processType);
        if (controller != null) {
            currentController = controller;
            logger.info("Current process controller set to: {} for process type {}",
                    controller.getControllerName(), processType);
        } else {
            logger.warn("No controller found for process type: {}", processType);
        }
    }

    /**
     * 设置当前参数调整器
     */
    public static void setCurrentAdjuster(AdjustmentType adjustmentType) {
        ParameterAdjuster adjuster = adjusterRegistry.get(adjustmentType);
        if (adjuster != null) {
            currentAdjuster = adjuster;
            logger.info("Current parameter adjuster set to: {} for adjustment type {}",
                    adjuster.getAdjusterName(), adjustmentType);
        } else {
            logger.warn("No adjuster found for adjustment type: {}", adjustmentType);
        }
    }

    /**
     * 设置当前异常处理器
     */
    public static void setCurrentHandler(ExceptionType exceptionType) {
        ExceptionHandler handler = handlerRegistry.get(exceptionType);
        if (handler != null) {
            currentHandler = handler;
            logger.info("Current type handler set to: {} for type type {}",
                    handler.getHandlerName(), exceptionType);
        } else {
            logger.warn("No handler found for type type: {}", exceptionType);
        }
    }

    /**
     * 设置当前追溯存储
     */
    public static void setCurrentTraceStorage(String traceStorageType) {
        TraceStorage traceStorage = traceStorageRegistry.get(traceStorageType);
        if (traceStorage != null) {
            currentTraceStorage = traceStorage;
            logger.info("Current trace storage set to: {}", traceStorage.getTraceStorageName());
        } else {
            logger.warn("No trace storage found for type: {}", traceStorageType);
        }
    }

    /**
     * 工艺步骤执行
     */
    public static ProcessStepResult executeStep(ProcessStep step, ExecutionContext context) {
        if (step == null) {
            logger.warn("Invalid process step for execution");
            return new ProcessStepResult(null, context != null ? context.getProcessId() : null,
                    false, "无效的工艺步骤", 0);
        }

        if (context == null) {
            logger.warn("Invalid execution context for step execution");
            return new ProcessStepResult(step.getStepId(), null, false, "无效的执行上下文", 0);
        }

        long startTime = System.currentTimeMillis();

        try {
            // 记录步骤开始事件
            recordStepStartEvent(step, context);

            // 使用注册的控制器执行步骤
            ProcessControllerEngine controller = controllerRegistry.get(context.getProcessType());
            ProcessStepResult result = null;

            if (controller != null) {
                result = controller.executeStep(step, context);
            } else if (currentController != null) {
                // 使用当前控制器
                result = currentController.executeStep(step, context);
            } else {
                // 使用默认执行逻辑
                result = performDefaultStepExecution(step, context);
            }

            long executionTime = System.currentTimeMillis() - startTime;

            // 记录步骤结束事件
            recordStepEndEvent(step, context, result);

            if (result != null) {
                logger.info("Step execution completed: {} - {} inspection {}ms",
                        step.getStepId(), step.getStepName(), executionTime);
            } else {
                logger.warn("Step execution failed: {} - {}", step.getStepId(), step.getStepName());
            }

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Failed to execute step: " + step.getStepId(), e);

            // 记录异常事件
            recordExceptionEvent(step, context, e);

            return new ProcessStepResult(step.getStepId(), context.getProcessId(),
                    false, "步骤执行异常: " + e.getMessage(), executionTime);
        }
    }

    /**
     * 记录步骤开始事件
     */
    private static void recordStepStartEvent(ProcessStep step, ExecutionContext context) {
        try {
            StepExecutionEvent event = new StepExecutionEvent(
                    step.getStepId(),
                    step.getStepName(),
                    true,
                    "步骤开始执行",
                    0
            );

            addTraceEvent(context.getProcessId(), event);
        } catch (Exception e) {
            logger.warn("Failed to record step start event", e);
        }
    }

    /**
     * 记录步骤结束事件
     */
    private static void recordStepEndEvent(ProcessStep step, ExecutionContext context, ProcessStepResult result) {
        try {
            if (result != null) {
                StepExecutionEvent event = new StepExecutionEvent(
                        step.getStepId(),
                        step.getStepName(),
                        result.isSuccess(),
                        result.getMessage(),
                        result.getExecutionTime()
                );

                addTraceEvent(context.getProcessId(), event);
            }
        } catch (Exception e) {
            logger.warn("Failed to record step end event", e);
        }
    }

    /**
     * 记录异常事件
     */
    private static void recordExceptionEvent(ProcessStep step, ExecutionContext context, Exception e) {
        try {
            ExceptionEvent event = new ExceptionEvent(
                    UUID.randomUUID().toString(),
                    ExceptionType.CUSTOM,
                    e.getMessage(),
                    5,
                    false
            );

            addTraceEvent(context.getProcessId(), event);
        } catch (Exception ex) {
            logger.warn("Failed to record type event", ex);
        }
    }

    /**
     * 执行默认步骤逻辑
     */
    private static ProcessStepResult performDefaultStepExecution(ProcessStep step, ExecutionContext context) {
        try {
            long startTime = System.currentTimeMillis();

            // 模拟步骤执行
            Thread.sleep(Math.max(100, step.getExpectedDuration() / 10)); // 简化实现

            long executionTime = System.currentTimeMillis() - startTime;

            // 检查约束条件
            boolean constraintsSatisfied = checkConstraints(step, context);

            ProcessStepResult result = new ProcessStepResult(
                    step.getStepId(),
                    context.getProcessId(),
                    constraintsSatisfied,
                    constraintsSatisfied ? "步骤执行成功" : "约束条件不满足",
                    executionTime
            );

            // 设置输出数据
            result.setOutputData("step_output", "default_output_value")
                    .setStatistic("execution_time_ms", executionTime)
                    .setStatistic("constraints_satisfied", constraintsSatisfied);

            return result;
        } catch (Exception e) {
            logger.warn("Failed to perform default step execution", e);
            return null;
        }
    }

    /**
     * 检查约束条件
     */
    private static boolean checkConstraints(ProcessStep step, ExecutionContext context) {
        // 简化实现，实际应用中需要根据具体约束进行检查
        return step.getConstraints().stream().allMatch(ProcessConstraint::isEnabled);
    }

    /**
     * 实时工艺参数调整
     */
    public static boolean adjustParameters(String processId, ParameterAdjustment adjustment) {
        if (processId == null || processId.isEmpty()) {
            logger.warn("Invalid process ID for parameter adjustment");
            return false;
        }

        if (adjustment == null) {
            logger.warn("Invalid parameter adjustment");
            return false;
        }

        try {
            // 使用注册的调整器进行参数调整
            ParameterAdjuster adjuster = adjusterRegistry.get(adjustment.getAdjustmentType());
            boolean adjusted = false;

            if (adjuster != null) {
                adjusted = adjuster.adjustParameters(processId, adjustment);
            } else if (currentAdjuster != null) {
                // 使用当前调整器
                adjusted = currentAdjuster.adjustParameters(processId, adjustment);
            } else {
                // 使用默认调整逻辑
                adjusted = performDefaultParameterAdjustment(processId, adjustment);
            }

            if (adjusted) {
                // 记录参数调整事件
                recordParameterAdjustmentEvent(processId, adjustment);
                logger.info("Parameter adjustment completed: {} - {} ({})",
                        processId, adjustment.getParameterName(), adjustment.getAdjustmentType());
            } else {
                logger.warn("Parameter adjustment failed: {} - {}", processId, adjustment.getParameterName());
            }

            return adjusted;
        } catch (Exception e) {
            logger.error("Failed to adjust parameters for process: " + processId, e);
            return false;
        }
    }

    /**
     * 记录参数调整事件
     */
    private static void recordParameterAdjustmentEvent(String processId, ParameterAdjustment adjustment) {
        try {
            ParameterAdjustmentEvent event = new ParameterAdjustmentEvent(
                    adjustment.getParameterName(),
                    adjustment.getOldValue(),
                    adjustment.getNewValue(),
                    adjustment.getAdjustmentType(),
                    adjustment.getReason()
            );

            addTraceEvent(processId, event);
        } catch (Exception e) {
            logger.warn("Failed to record parameter adjustment event", e);
        }
    }

    /**
     * 执行默认参数调整逻辑
     */
    private static boolean performDefaultParameterAdjustment(String processId, ParameterAdjustment adjustment) {
        try {
            // 获取执行上下文
            ExecutionContext context = processContextCache.get(processId);
            if (context == null) {
                logger.warn("Process context not found: {}", processId);
                return false;
            }

            // 更新全局参数
            context.setGlobalParameter(adjustment.getParameterName(), adjustment.getNewValue());

            // 更新运行时数据
            context.setRuntimeData(adjustment.getParameterName() + "_adjusted", System.currentTimeMillis());

            logger.debug("Default parameter adjustment applied: {} = {} for process {}",
                    adjustment.getParameterName(), adjustment.getNewValue(), processId);

            return true;
        } catch (Exception e) {
            logger.warn("Failed to perform default parameter adjustment", e);
            return false;
        }
    }

    /**
     * 工艺异常处理
     */
    public static ExceptionHandlingResult handleProcessException(ProcessException exception) {
        if (exception == null) {
            logger.warn("Invalid process type for handling");
            return new ExceptionHandlingResult(null, false, "无效的工艺异常");
        }

        try {
            // 使用注册的处理器处理异常
            ExceptionHandler handler = handlerRegistry.get(exception.getExceptionType());
            ExceptionHandlingResult result = null;

            if (handler != null) {
                result = handler.handleProcessException(exception);
            } else if (currentHandler != null) {
                // 使用当前处理器
                result = currentHandler.handleProcessException(exception);
            } else {
                // 使用默认处理逻辑
                result = performDefaultExceptionHandling(exception);
            }

            if (result != null) {
                // 记录异常处理事件
                recordExceptionHandlingEvent(exception, result);
                logger.info("Exception handling completed: {} - {} ({})",
                        exception.getExceptionId(), exception.getExceptionType(),
                        result.isHandled() ? "handled" : "not handled");
            } else {
                logger.warn("Exception handling failed: {}", exception.getExceptionId());
            }

            return result;
        } catch (Exception e) {
            logger.error("Failed to handle process type: " + exception.getExceptionId(), e);
            return new ExceptionHandlingResult(exception.getExceptionId(), false,
                    "异常处理过程中发生错误: " + e.getMessage());
        }
    }

    /**
     * 记录异常处理事件
     */
    private static void recordExceptionHandlingEvent(ProcessException exception, ExceptionHandlingResult result) {
        try {
            ExceptionEvent event = new ExceptionEvent(
                    exception.getExceptionId(),
                    exception.getExceptionType(),
                    exception.getMessage(),
                    exception.getSeverity(),
                    result.isHandled()
            );

            addTraceEvent(exception.getProcessId(), event);
        } catch (Exception e) {
            logger.warn("Failed to record type handling event", e);
        }
    }

    /**
     * 执行默认异常处理逻辑
     */
    private static ExceptionHandlingResult performDefaultExceptionHandling(ProcessException exception) {
        try {
            ExceptionHandlingResult result = new ExceptionHandlingResult(
                    exception.getExceptionId(),
                    true,
                    "默认异常处理"
            );

            // 根据异常类型添加恢复动作
            switch (exception.getExceptionType()) {
                case PARAMETER_OUT_OF_RANGE:
                    result.addRecoveryAction(new RecoveryAction("调整参数", "将参数调整到正常范围"));
                    break;
                case EQUIPMENT_FAILURE:
                    result.addRecoveryAction(new RecoveryAction("重启设备", "尝试重启相关设备"));
                    result.setProcessContinued(false);
                    break;
                case QUALITY_VIOLATION:
                    result.addRecoveryAction(new RecoveryAction("质量复检", "对产品进行质量复检"));
                    break;
                default:
                    result.addRecoveryAction(new RecoveryAction("通用恢复", "执行通用恢复程序"));
                    break;
            }

            logger.debug("Default type handling applied for: {}", exception.getExceptionId());

            return result;
        } catch (Exception e) {
            logger.warn("Failed to perform default type handling", e);
            return null;
        }
    }

    /**
     * 工艺过程追溯
     */
    public static ProcessTrace generateProcessTrace(String processId) {
        if (processId == null || processId.isEmpty()) {
            logger.warn("Invalid process ID for trace generation");
            return null;
        }

        try {
            // 首先检查缓存
            ProcessTrace cached = processTraceCache.get(processId);
            if (cached != null && !isTraceCacheExpired(cached)) {
                logger.debug("Returning cached process trace: {}", processId);
                return cached;
            }

            // 从追溯存储中获取
            ProcessTrace trace = null;
            if (currentTraceStorage != null) {
                trace = currentTraceStorage.loadProcessTrace(processId);
            }

            // 如果追溯存储中没有且缓存中有，则返回缓存的
            if (trace == null) {
                trace = cached;
            }

            // 如果都没有，创建新的追溯对象
            if (trace == null) {
                trace = new ProcessTrace(processId);
            }

            // 更新缓存
            if (trace != null) {
                processTraceCache.put(processId, trace);
            }

            if (trace != null) {
                logger.debug("Generated process trace: {}", processId);
            } else {
                logger.warn("Failed to generate process trace: {}", processId);
            }

            return trace;
        } catch (Exception e) {
            logger.error("Failed to generate process trace: " + processId, e);
            return null;
        }
    }

    /**
     * 检查追溯缓存是否过期
     */
    private static boolean isTraceCacheExpired(ProcessTrace trace) {
        return System.currentTimeMillis() - trace.getStartTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
                > DEFAULT_CACHE_TIMEOUT;
    }

    /**
     * 添加追溯事件
     */
    public static boolean addTraceEvent(String processId, TraceEvent event) {
        if (processId == null || processId.isEmpty() || event == null) {
            logger.warn("Invalid parameters for trace event addition");
            return false;
        }

        try {
            boolean added = false;
            if (currentTraceStorage != null) {
                added = currentTraceStorage.addTraceEvent(processId, event);
            } else {
                // 如果没有追溯存储，记录到缓存中的追溯对象
                ProcessTrace trace = processTraceCache.get(processId);
                if (trace != null) {
                    trace.addTraceEvent(event);
                    added = true;
                }
            }

            if (added) {
                logger.debug("Added trace event: {} for process: {}", event.getEventType(), processId);
            } else {
                logger.warn("Failed to add trace event for process: {}", processId);
            }

            return added;
        } catch (Exception e) {
            logger.error("Failed to add trace event for process: " + processId, e);
            return false;
        }
    }

    /**
     * 追溯事件查询
     */
    public static List<TraceEvent> queryTraceEvents(String processId, TraceQuery query) {
        if (processId == null || processId.isEmpty() || query == null) {
            logger.warn("Invalid parameters for trace event query");
            return new ArrayList<>();
        }

        try {
            if (currentTraceStorage != null) {
                return currentTraceStorage.queryTraceEvents(processId, query);
            } else {
                // 如果没有追溯存储，从缓存中查询
                return queryTraceEventsFromCache(processId, query);
            }
        } catch (Exception e) {
            logger.error("Failed to query trace events for process: " + processId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 从缓存中查询追溯事件
     */
    private static List<TraceEvent> queryTraceEventsFromCache(String processId, TraceQuery query) {
        ProcessTrace trace = processTraceCache.get(processId);
        if (trace == null) {
            return new ArrayList<>();
        }

        return trace.getTraceEvents().stream()
                .filter(event -> {
                    // 时间范围过滤
                    if (query.getStartDate() != null && event.getTimestamp().isBefore(query.getStartDate())) {
                        return false;
                    }

                    if (query.getEndDate() != null && event.getTimestamp().isAfter(query.getEndDate())) {
                        return false;
                    }

                    // 事件类型过滤
                    if (query.getEventType() != null && !query.getEventType().isEmpty() &&
                            !event.getEventType().equals(query.getEventType())) {
                        return false;
                    }

                    // 操作员过滤
                    if (query.getOperator() != null && !query.getOperator().isEmpty() &&
                            !event.getOperator().equals(query.getOperator())) {
                        return false;
                    }

                    return true;
                })
                .limit(query.getLimit())
                .collect(Collectors.toList());
    }

    /**
     * 创建执行上下文
     */
    public static ExecutionContext createExecutionContext(String processId, ProcessType processType,
                                                          String productId, String batchId) {
        if (processType == null) {
            logger.warn("Invalid process type for context creation");
            return null;
        }

        try {
            ExecutionContext context = new ExecutionContext(processId, processType, productId, batchId);

            // 添加到缓存
            processContextCache.put(context.getProcessId(), context);

            logger.info("Created execution context: {} for process type: {}",
                    context.getProcessId(), processType);

            return context;
        } catch (Exception e) {
            logger.error("Failed to create execution context", e);
            return null;
        }
    }

    /**
     * 更新执行上下文
     */
    public static boolean updateExecutionContext(ExecutionContext context) {
        if (context == null) {
            logger.warn("Invalid execution context for update");
            return false;
        }

        try {
            processContextCache.put(context.getProcessId(), context);
            logger.debug("Updated execution context: {}", context.getProcessId());
            return true;
        } catch (Exception e) {
            logger.error("Failed to update execution context: " + context.getProcessId(), e);
            return false;
        }
    }

    /**
     * 获取执行上下文
     */
    public static ExecutionContext getExecutionContext(String processId) {
        if (processId == null || processId.isEmpty()) {
            logger.warn("Invalid process ID for context retrieval");
            return null;
        }

        try {
            ExecutionContext context = processContextCache.get(processId);
            if (context != null) {
                logger.debug("Retrieved execution context: {}", processId);
            } else {
                logger.warn("Execution context not found: {}", processId);
            }
            return context;
        } catch (Exception e) {
            logger.error("Failed to get execution context: " + processId, e);
            return null;
        }
    }

    /**
     * 清理缓存
     */
    private static void cleanupCache() {
        try {
            long cutoffTime = System.currentTimeMillis() - DEFAULT_CACHE_TIMEOUT;

            // 清理执行上下文缓存
            processContextCache.entrySet().removeIf(entry ->
                    entry.getValue().getStartTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli() < cutoffTime
            );

            // 清理追溯信息缓存
            processTraceCache.entrySet().removeIf(entry ->
                    entry.getValue().getStartTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli() < cutoffTime
            );

            logger.debug("Cleaned up process controller cache, remaining entries - context: {}, trace: {}",
                    processContextCache.size(), processTraceCache.size());
        } catch (Exception e) {
            logger.error("Failed to cleanup process controller cache", e);
        }
    }

    /**
     * 获取缓存统计信息
     */
    public static CacheStatistics getCacheStatistics() {
        CacheStatistics stats = new CacheStatistics();
        stats.setContextCacheSize(processContextCache.size());
        stats.setTraceCacheSize(processTraceCache.size());
        stats.setCacheTimeout(DEFAULT_CACHE_TIMEOUT);
        return stats;
    }

    /**
     * 关闭工艺过程控制器
     */
    public static void shutdown() {
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            logger.info("ProcessController shutdown completed");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            logger.warn("ProcessController shutdown interrupted");
        }
    }

    // 工艺类型枚举
    public enum ProcessType {
        WIRE_BONDING("引线键合工艺", "Wire Bonding Process"),
        DIE_ATTACH("芯片贴装工艺", "Die Attach Process"),
        PLASTIC_PACKAGING("塑封工艺", "Plastic Packaging Process"),
        METAL_PACKAGING("金属封装工艺", "Metal Packaging Process"),
        CERAMIC_PACKAGING("陶瓷封装工艺", "Ceramic Packaging Process"),
        SOLDERING("焊接工艺", "Soldering Process"),
        CLEANING("清洗工艺", "Cleaning Process"),
        TESTING("测试工艺", "Testing Process"),
        ASSEMBLY("组装工艺", "Assembly Process"),
        CUSTOM("自定义工艺", "Custom Process");

        private final String chineseName;
        private final String englishName;

        ProcessType(String chineseName, String englishName) {
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

    // 工艺状态枚举
    public enum ProcessStatus {
        INITIALIZED("已初始化", 1),
        RUNNING("运行中", 2),
        PAUSED("已暂停", 3),
        COMPLETED("已完成", 4),
        FAILED("已失败", 5),
        CANCELLED("已取消", 6),
        SUSPENDED("已挂起", 7);

        private final String description;
        private final int sequence;

        ProcessStatus(String description, int sequence) {
            this.description = description;
            this.sequence = sequence;
        }

        public String getDescription() {
            return description;
        }

        public int getSequence() {
            return sequence;
        }

        public boolean isRunning() {
            return this == RUNNING || this == PAUSED || this == SUSPENDED;
        }

        public boolean isCompleted() {
            return this == COMPLETED || this == FAILED || this == CANCELLED;
        }
    }

    // 步骤类型枚举
    public enum StepType {
        PREPARATION("准备步骤", "Preparation Step"),
        PROCESSING("处理步骤", "Processing Step"),
        QUALITY_CHECK("质量检查", "Quality Check"),
        TRANSITION("过渡步骤", "Transition Step"),
        CLEANUP("清理步骤", "Cleanup Step"),
        CUSTOM("自定义步骤", "Custom Step");

        private final String chineseName;
        private final String englishName;

        StepType(String chineseName, String englishName) {
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
        EQUIPMENT_FAILURE("设备故障", "Equipment Failure"),
        PARAMETER_OUT_OF_RANGE("参数超限", "Parameter Out of Range"),
        QUALITY_VIOLATION("质量违规", "Quality Violation"),
        PROCESS_TIMEOUT("过程超时", "Process Timeout"),
        SAFETY_VIOLATION("安全违规", "Safety Violation"),
        COMMUNICATION_ERROR("通信错误", "Communication Error"),
        RESOURCE_UNAVAILABLE("资源不可用", "Resource Unavailable"),
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

    // 调整类型枚举
    public enum AdjustmentType {
        AUTOMATIC("自动调整", "Automatic Adjustment"),
        MANUAL("手动调整", "Manual Adjustment"),
        PREDICTIVE("预测调整", "Predictive Adjustment"),
        CORRECTIVE("纠正调整", "Corrective Adjustment");

        private final String chineseName;
        private final String englishName;

        AdjustmentType(String chineseName, String englishName) {
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

    // 工艺控制器接口
    public interface ProcessControllerEngine {
        ProcessStepResult executeStep(ProcessStep step, ExecutionContext context) throws Exception;

        boolean supportsProcessType(ProcessType processType);

        String getControllerName();
    }

    // 参数调整器接口
    public interface ParameterAdjuster {
        boolean adjustParameters(String processId, ParameterAdjustment adjustment) throws Exception;

        boolean supportsAdjustmentType(AdjustmentType adjustmentType);

        String getAdjusterName();
    }

    // 异常处理器接口
    public interface ExceptionHandler {
        ExceptionHandlingResult handleProcessException(ProcessException exception) throws Exception;

        boolean supportsExceptionType(ExceptionType exceptionType);

        String getHandlerName();
    }

    // 追溯存储接口
    public interface TraceStorage {
        boolean saveProcessTrace(ProcessTrace trace) throws Exception;

        ProcessTrace loadProcessTrace(String processId) throws Exception;

        boolean addTraceEvent(String processId, TraceEvent event) throws Exception;

        List<TraceEvent> queryTraceEvents(String processId, TraceQuery query) throws Exception;

        boolean supportsTraceType(String traceType);

        String getTraceStorageName();
    }

    // 工艺步骤类
    public static class ProcessStep {
        private final String stepId;
        private final String stepName;
        private final StepType stepType;
        private final int stepNumber;
        private final String description;
        private final Map<String, Object> parameters;
        private final long expectedDuration; // 毫秒
        private final Map<String, Object> conditions;
        private final List<String> dependencies;
        private final List<ProcessConstraint> constraints;
        private final Map<String, Object> metadata;

        public ProcessStep(String stepId, String stepName, StepType stepType, int stepNumber) {
            this.stepId = stepId != null ? stepId : UUID.randomUUID().toString();
            this.stepName = stepName != null ? stepName : "";
            this.stepType = stepType != null ? stepType : StepType.CUSTOM;
            this.stepNumber = stepNumber;
            this.description = "";
            this.parameters = new ConcurrentHashMap<>();
            this.expectedDuration = 0;
            this.conditions = new ConcurrentHashMap<>();
            this.dependencies = new ArrayList<>();
            this.constraints = new ArrayList<>();
            this.metadata = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getStepId() {
            return stepId;
        }

        public String getStepName() {
            return stepName;
        }

        public ProcessStep setStepName(String stepName) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public StepType getStepType() {
            return stepType;
        }

        public int getStepNumber() {
            return stepNumber;
        }

        public String getDescription() {
            return description;
        }

        public ProcessStep setDescription(String description) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public Map<String, Object> getParameters() {
            return new HashMap<>(parameters);
        }

        public ProcessStep setParameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        public Object getParameter(String key) {
            return this.parameters.get(key);
        }

        public long getExpectedDuration() {
            return expectedDuration;
        }

        public ProcessStep setExpectedDuration(long expectedDuration) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public Map<String, Object> getConditions() {
            return new HashMap<>(conditions);
        }

        public ProcessStep setCondition(String key, Object value) {
            this.conditions.put(key, value);
            return this;
        }

        public Object getCondition(String key) {
            return this.conditions.get(key);
        }

        public List<String> getDependencies() {
            return new ArrayList<>(dependencies);
        }

        public ProcessStep addDependency(String dependency) {
            this.dependencies.add(dependency);
            return this;
        }

        public List<ProcessConstraint> getConstraints() {
            return new ArrayList<>(constraints);
        }

        public ProcessStep addConstraint(ProcessConstraint constraint) {
            this.constraints.add(constraint);
            return this;
        }

        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }

        public ProcessStep setMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Object getMetadata(String key) {
            return this.metadata.get(key);
        }

        @Override
        public String toString() {
            return "ProcessStep{" +
                    "stepId='" + stepId + '\'' +
                    ", stepName='" + stepName + '\'' +
                    ", stepType=" + stepType +
                    ", stepNumber=" + stepNumber +
                    ", expectedDuration=" + expectedDuration + "ms" +
                    ", dependencyCount=" + dependencies.size() +
                    '}';
        }
    }

    // 工艺约束类
    public static class ProcessConstraint {
        private final String constraintId;
        private final String constraintName;
        private final String expression;
        private final String description;
        private final boolean enabled;
        private final Map<String, Object> attributes;

        public ProcessConstraint(String constraintId, String constraintName, String expression) {
            this.constraintId = constraintId != null ? constraintId : UUID.randomUUID().toString();
            this.constraintName = constraintName != null ? constraintName : "";
            this.expression = expression != null ? expression : "";
            this.description = "";
            this.enabled = true;
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getConstraintId() {
            return constraintId;
        }

        public String getConstraintName() {
            return constraintName;
        }

        public ProcessConstraint setConstraintName(String constraintName) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public String getExpression() {
            return expression;
        }

        public ProcessConstraint setExpression(String expression) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public String getDescription() {
            return description;
        }

        public ProcessConstraint setDescription(String description) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public ProcessConstraint setEnabled(boolean enabled) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public ProcessConstraint setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        @Override
        public String toString() {
            return "ProcessConstraint{" +
                    "constraintId='" + constraintId + '\'' +
                    ", constraintName='" + constraintName + '\'' +
                    ", expression='" + expression + '\'' +
                    ", enabled=" + enabled +
                    '}';
        }
    }

    // 执行上下文类
    public static class ExecutionContext {
        private final String processId;
        private final ProcessType processType;
        private final String productId;
        private final String batchId;
        private final Map<String, Object> globalParameters;
        private final Map<String, Object> runtimeData;
        private final String operator;
        private final String equipmentId;
        private final LocalDateTime startTime;
        private final Map<String, Object> metadata;
        private volatile ProcessStatus status;

        public ExecutionContext(String processId, ProcessType processType, String productId, String batchId) {
            this.processId = processId != null ? processId : UUID.randomUUID().toString();
            this.processType = processType != null ? processType : ProcessType.CUSTOM;
            this.productId = productId != null ? productId : "";
            this.batchId = batchId != null ? batchId : "";
            this.globalParameters = new ConcurrentHashMap<>();
            this.runtimeData = new ConcurrentHashMap<>();
            this.operator = "";
            this.equipmentId = "";
            this.startTime = LocalDateTime.now();
            this.status = ProcessStatus.INITIALIZED;
            this.metadata = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getProcessId() {
            return processId;
        }

        public ProcessType getProcessType() {
            return processType;
        }

        public String getProductId() {
            return productId;
        }

        public String getBatchId() {
            return batchId;
        }

        public Map<String, Object> getGlobalParameters() {
            return new HashMap<>(globalParameters);
        }

        public ExecutionContext setGlobalParameter(String key, Object value) {
            this.globalParameters.put(key, value);
            return this;
        }

        public Object getGlobalParameter(String key) {
            return this.globalParameters.get(key);
        }

        public Map<String, Object> getRuntimeData() {
            return new HashMap<>(runtimeData);
        }

        public ExecutionContext setRuntimeData(String key, Object value) {
            this.runtimeData.put(key, value);
            return this;
        }

        public Object getRuntimeData(String key) {
            return this.runtimeData.get(key);
        }

        public String getOperator() {
            return operator;
        }

        public ExecutionContext setOperator(String operator) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public String getEquipmentId() {
            return equipmentId;
        }

        public ExecutionContext setEquipmentId(String equipmentId) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public ProcessStatus getStatus() {
            return status;
        }

        public ExecutionContext setStatus(ProcessStatus status) {
            this.status = status;
            return this;
        }

        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }

        public ExecutionContext setMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Object getMetadata(String key) {
            return this.metadata.get(key);
        }

        @Override
        public String toString() {
            return "ExecutionContext{" +
                    "processId='" + processId + '\'' +
                    ", processType=" + processType +
                    ", productId='" + productId + '\'' +
                    ", status=" + status +
                    ", startTime=" + startTime +
                    '}';
        }
    }

    // 工艺步骤结果类
    public static class ProcessStepResult {
        private final String resultId;
        private final String stepId;
        private final String processId;
        private final boolean success;
        private final String message;
        private final long executionTime; // 毫秒
        private final Map<String, Object> outputData;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final List<ProcessEvent> events;
        private final Map<String, Object> statistics;

        public ProcessStepResult(String stepId, String processId, boolean success, String message, long executionTime) {
            this.resultId = UUID.randomUUID().toString();
            this.stepId = stepId != null ? stepId : "";
            this.processId = processId != null ? processId : "";
            this.success = success;
            this.message = message != null ? message : "";
            this.executionTime = executionTime;
            this.outputData = new ConcurrentHashMap<>();
            this.startTime = LocalDateTime.now().minusNanos(executionTime * 1000000); // 估算开始时间
            this.endTime = LocalDateTime.now();
            this.events = new CopyOnWriteArrayList<>();
            this.statistics = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getResultId() {
            return resultId;
        }

        public String getStepId() {
            return stepId;
        }

        public String getProcessId() {
            return processId;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public long getExecutionTime() {
            return executionTime;
        }

        public Map<String, Object> getOutputData() {
            return new HashMap<>(outputData);
        }

        public ProcessStepResult setOutputData(String key, Object value) {
            this.outputData.put(key, value);
            return this;
        }

        public Object getOutputData(String key) {
            return this.outputData.get(key);
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public List<ProcessEvent> getEvents() {
            return new ArrayList<>(events);
        }

        public void addEvent(ProcessEvent event) {
            this.events.add(event);
        }

        public Map<String, Object> getStatistics() {
            return new HashMap<>(statistics);
        }

        public ProcessStepResult setStatistic(String key, Object value) {
            this.statistics.put(key, value);
            return this;
        }

        public Object getStatistic(String key) {
            return this.statistics.get(key);
        }

        @Override
        public String toString() {
            return "ProcessStepResult{" +
                    "resultId='" + resultId + '\'' +
                    ", stepId='" + stepId + '\'' +
                    ", success=" + success +
                    ", message='" + message + '\'' +
                    ", executionTime=" + executionTime + "ms" +
                    ", eventCount=" + events.size() +
                    '}';
        }
    }

    // 参数调整类
    public static class ParameterAdjustment {
        private final String adjustmentId;
        private final String parameterName;
        private final Object oldValue;
        private final Object newValue;
        private final AdjustmentType adjustmentType;
        private final String reason;
        private final String operator;
        private final LocalDateTime adjustmentTime;
        private final Map<String, Object> metadata;

        public ParameterAdjustment(String parameterName, Object oldValue, Object newValue,
                                   AdjustmentType adjustmentType, String reason) {
            this.adjustmentId = UUID.randomUUID().toString();
            this.parameterName = parameterName != null ? parameterName : "";
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.adjustmentType = adjustmentType != null ? adjustmentType : AdjustmentType.AUTOMATIC;
            this.reason = reason != null ? reason : "";
            this.operator = "";
            this.adjustmentTime = LocalDateTime.now();
            this.metadata = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getAdjustmentId() {
            return adjustmentId;
        }

        public String getParameterName() {
            return parameterName;
        }

        public Object getOldValue() {
            return oldValue;
        }

        public Object getNewValue() {
            return newValue;
        }

        public AdjustmentType getAdjustmentType() {
            return adjustmentType;
        }

        public String getReason() {
            return reason;
        }

        public String getOperator() {
            return operator;
        }

        public ParameterAdjustment setOperator(String operator) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public LocalDateTime getAdjustmentTime() {
            return adjustmentTime;
        }

        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }

        public ParameterAdjustment setMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Object getMetadata(String key) {
            return this.metadata.get(key);
        }

        @Override
        public String toString() {
            return "ParameterAdjustment{" +
                    "adjustmentId='" + adjustmentId + '\'' +
                    ", parameterName='" + parameterName + '\'' +
                    ", adjustmentType=" + adjustmentType +
                    ", reason='" + reason + '\'' +
                    ", adjustmentTime=" + adjustmentTime +
                    '}';
        }
    }

    // 工艺异常类
    public static class ProcessException extends Exception {
        private final String exceptionId;
        private final ExceptionType exceptionType;
        private final String processId;
        private final String stepId;
        private final String equipmentId;
        private final Map<String, Object> contextData;
        private final LocalDateTime occurrenceTime;
        private final int severity; // 1-10, 10为最高严重性
        private final boolean recoverable;

        public ProcessException(ExceptionType exceptionType, String message, String processId, String stepId) {
            super(message);
            this.exceptionId = UUID.randomUUID().toString();
            this.exceptionType = exceptionType != null ? exceptionType : ExceptionType.CUSTOM;
            this.processId = processId != null ? processId : "";
            this.stepId = stepId != null ? stepId : "";
            this.equipmentId = "";
            this.contextData = new ConcurrentHashMap<>();
            this.occurrenceTime = LocalDateTime.now();
            this.severity = 5;
            this.recoverable = true;
        }

        // Getters and Setters
        public String getExceptionId() {
            return exceptionId;
        }

        public ExceptionType getExceptionType() {
            return exceptionType;
        }

        public String getProcessId() {
            return processId;
        }

        public String getStepId() {
            return stepId;
        }

        public String getEquipmentId() {
            return equipmentId;
        }

        public ProcessException setEquipmentId(String equipmentId) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public Map<String, Object> getContextData() {
            return new HashMap<>(contextData);
        }

        public ProcessException setContextData(String key, Object value) {
            this.contextData.put(key, value);
            return this;
        }

        public Object getContextData(String key) {
            return this.contextData.get(key);
        }

        public LocalDateTime getOccurrenceTime() {
            return occurrenceTime;
        }

        public int getSeverity() {
            return severity;
        }

        public ProcessException setSeverity(int severity) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public boolean isRecoverable() {
            return recoverable;
        }

        public ProcessException setRecoverable(boolean recoverable) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        @Override
        public String toString() {
            return "ProcessException{" +
                    "exceptionId='" + exceptionId + '\'' +
                    ", exceptionType=" + exceptionType +
                    ", processId='" + processId + '\'' +
                    ", stepId='" + stepId + '\'' +
                    ", message='" + getMessage() + '\'' +
                    ", occurrenceTime=" + occurrenceTime +
                    ", severity=" + severity +
                    ", recoverable=" + recoverable +
                    '}';
        }
    }

    // 异常处理结果类
    public static class ExceptionHandlingResult {
        private final String resultId;
        private final String exceptionId;
        private final boolean handled;
        private final String actionTaken;
        private final List<RecoveryAction> recoveryActions;
        private final LocalDateTime handlingTime;
        private final String handler;
        private final Map<String, Object> metadata;
        private boolean processContinued;

        public ExceptionHandlingResult(String exceptionId, boolean handled, String actionTaken) {
            this.resultId = UUID.randomUUID().toString();
            this.exceptionId = exceptionId != null ? exceptionId : "";
            this.handled = handled;
            this.actionTaken = actionTaken != null ? actionTaken : "";
            this.processContinued = false;
            this.recoveryActions = new ArrayList<>();
            this.handlingTime = LocalDateTime.now();
            this.handler = "";
            this.metadata = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getResultId() {
            return resultId;
        }

        public String getExceptionId() {
            return exceptionId;
        }

        public boolean isHandled() {
            return handled;
        }

        public String getActionTaken() {
            return actionTaken;
        }

        public boolean isProcessContinued() {
            return processContinued;
        }

        public ExceptionHandlingResult setProcessContinued(boolean processContinued) {
            this.processContinued = processContinued;
            return this;
        }

        public List<RecoveryAction> getRecoveryActions() {
            return new ArrayList<>(recoveryActions);
        }

        public void addRecoveryAction(RecoveryAction action) {
            this.recoveryActions.add(action);
        }

        public LocalDateTime getHandlingTime() {
            return handlingTime;
        }

        public String getHandler() {
            return handler;
        }

        public ExceptionHandlingResult setHandler(String handler) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }

        public ExceptionHandlingResult setMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Object getMetadata(String key) {
            return this.metadata.get(key);
        }

        @Override
        public String toString() {
            return "ExceptionHandlingResult{" +
                    "resultId='" + resultId + '\'' +
                    ", exceptionId='" + exceptionId + '\'' +
                    ", handled=" + handled +
                    ", actionTaken='" + actionTaken + '\'' +
                    ", processContinued=" + processContinued +
                    ", recoveryActionCount=" + recoveryActions.size() +
                    ", handlingTime=" + handlingTime +
                    '}';
        }
    }

    // 恢复动作类
    public static class RecoveryAction {
        private final String actionId;
        private final String actionName;
        private final String description;
        private final long estimatedDuration; // 毫秒
        private final boolean requiresManualIntervention;
        private final Map<String, Object> parameters;

        public RecoveryAction(String actionName, String description) {
            this.actionId = UUID.randomUUID().toString();
            this.actionName = actionName != null ? actionName : "";
            this.description = description != null ? description : "";
            this.estimatedDuration = 0;
            this.requiresManualIntervention = false;
            this.parameters = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getActionId() {
            return actionId;
        }

        public String getActionName() {
            return actionName;
        }

        public String getDescription() {
            return description;
        }

        public long getEstimatedDuration() {
            return estimatedDuration;
        }

        public RecoveryAction setEstimatedDuration(long estimatedDuration) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public boolean isRequiresManualIntervention() {
            return requiresManualIntervention;
        }

        public RecoveryAction setRequiresManualIntervention(boolean requiresManualIntervention) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public Map<String, Object> getParameters() {
            return new HashMap<>(parameters);
        }

        public RecoveryAction setParameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        public Object getParameter(String key) {
            return this.parameters.get(key);
        }

        @Override
        public String toString() {
            return "RecoveryAction{" +
                    "actionId='" + actionId + '\'' +
                    ", actionName='" + actionName + '\'' +
                    ", description='" + description + '\'' +
                    ", estimatedDuration=" + estimatedDuration + "ms" +
                    ", requiresManualIntervention=" + requiresManualIntervention +
                    '}';
        }
    }

    // 工艺追溯类
    public static class ProcessTrace {
        private final String traceId;
        private final String processId;
        private final List<TraceEvent> traceEvents;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final Map<String, Object> summary;
        private final boolean isComplete;
        private final Map<String, Object> metadata;

        public ProcessTrace(String processId) {
            this.traceId = UUID.randomUUID().toString();
            this.processId = processId != null ? processId : "";
            this.traceEvents = new CopyOnWriteArrayList<>();
            this.startTime = LocalDateTime.now();
            this.endTime = null;
            this.summary = new ConcurrentHashMap<>();
            this.isComplete = false;
            this.metadata = new ConcurrentHashMap<>();
        }

        // 完整构造函数
        public ProcessTrace(String processId, List<TraceEvent> traceEvents,
                            LocalDateTime startTime, LocalDateTime endTime) {
            this.traceId = UUID.randomUUID().toString();
            this.processId = processId != null ? processId : "";
            this.traceEvents = traceEvents != null ? new ArrayList<>(traceEvents) : new ArrayList<>();
            this.startTime = startTime;
            this.endTime = endTime;
            this.summary = new ConcurrentHashMap<>();
            this.isComplete = true;
            this.metadata = new ConcurrentHashMap<>();

            // 生成摘要
            generateSummary();
        }

        // 生成摘要
        private void generateSummary() {
            summary.put("eventCount", traceEvents.size());
            summary.put("duration", java.time.Duration.between(startTime, endTime).toMinutes() + "分钟");

            if (!traceEvents.isEmpty()) {
                // 统计各事件类型数量
                Map<String, Long> eventTypeCount = traceEvents.stream()
                        .collect(Collectors.groupingBy(
                                TraceEvent::getEventType,
                                Collectors.counting()
                        ));
                summary.put("eventTypeDistribution", eventTypeCount);

                // 统计步骤执行结果
                long successCount = traceEvents.stream()
                        .filter(event -> event instanceof StepExecutionEvent)
                        .map(event -> (StepExecutionEvent) event)
                        .filter(StepExecutionEvent::isSuccess)
                        .count();
                summary.put("successfulSteps", successCount);

                long failureCount = traceEvents.stream()
                        .filter(event -> event instanceof StepExecutionEvent)
                        .map(event -> (StepExecutionEvent) event)
                        .filter(event -> !event.isSuccess())
                        .count();
                summary.put("failedSteps", failureCount);
            }
        }

        // 添加追溯事件
        public void addTraceEvent(TraceEvent event) {
            if (traceEvents.size() < DEFAULT_TRACE_BUFFER_SIZE) {
                this.traceEvents.add(event);
            } else {
                logger.warn("Trace buffer full, dropping event: {}", event.getEventType());
            }
        }

        // Getters
        public String getTraceId() {
            return traceId;
        }

        public String getProcessId() {
            return processId;
        }

        public List<TraceEvent> getTraceEvents() {
            return new ArrayList<>(traceEvents);
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public Map<String, Object> getSummary() {
            return new HashMap<>(summary);
        }

        public boolean isComplete() {
            return isComplete;
        }

        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }

        public void setMetadata(String key, Object value) {
            this.metadata.put(key, value);
        }

        public Object getMetadata(String key) {
            return this.metadata.get(key);
        }

        @Override
        public String toString() {
            return "ProcessTrace{" +
                    "traceId='" + traceId + '\'' +
                    ", processId='" + processId + '\'' +
                    ", eventCount=" + traceEvents.size() +
                    ", startTime=" + startTime +
                    ", isComplete=" + isComplete +
                    '}';
        }
    }

    // 追溯事件基类
    public static abstract class TraceEvent {
        protected final String eventId;
        protected final String eventType;
        protected final String operator;
        protected final LocalDateTime timestamp;
        protected final Map<String, Object> attributes;

        public TraceEvent(String eventType, String operator) {
            this.eventId = UUID.randomUUID().toString();
            this.eventType = eventType != null ? eventType : "UNKNOWN";
            this.operator = operator != null ? operator : "System";
            this.timestamp = LocalDateTime.now();
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters
        public String getEventId() {
            return eventId;
        }

        public String getEventType() {
            return eventType;
        }

        public String getOperator() {
            return operator;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public void setAttribute(String key, Object value) {
            this.attributes.put(key, value);
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }
    }

    // 步骤执行事件
    public static class StepExecutionEvent extends TraceEvent {
        private final String stepId;
        private final String stepName;
        private final boolean success;
        private final String message;
        private final long executionTime; // 毫秒

        public StepExecutionEvent(String stepId, String stepName, boolean success, String message, long executionTime) {
            super("STEP_EXECUTION", "System");
            this.stepId = stepId != null ? stepId : "";
            this.stepName = stepName != null ? stepName : "";
            this.success = success;
            this.message = message != null ? message : "";
            this.executionTime = executionTime;
        }

        // Getters
        public String getStepId() {
            return stepId;
        }

        public String getStepName() {
            return stepName;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public long getExecutionTime() {
            return executionTime;
        }
    }

    // 参数调整事件
    public static class ParameterAdjustmentEvent extends TraceEvent {
        private final String parameterName;
        private final Object oldValue;
        private final Object newValue;
        private final AdjustmentType adjustmentType;
        private final String reason;

        public ParameterAdjustmentEvent(String parameterName, Object oldValue, Object newValue,
                                        AdjustmentType adjustmentType, String reason) {
            super("PARAMETER_ADJUSTMENT", "System");
            this.parameterName = parameterName != null ? parameterName : "";
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.adjustmentType = adjustmentType != null ? adjustmentType : AdjustmentType.AUTOMATIC;
            this.reason = reason != null ? reason : "";
        }

        // Getters
        public String getParameterName() {
            return parameterName;
        }

        public Object getOldValue() {
            return oldValue;
        }

        public Object getNewValue() {
            return newValue;
        }

        public AdjustmentType getAdjustmentType() {
            return adjustmentType;
        }

        public String getReason() {
            return reason;
        }
    }

    // 异常事件
    public static class ExceptionEvent extends TraceEvent {
        private final String exceptionId;
        private final ExceptionType exceptionType;
        private final String message;
        private final int severity;
        private final boolean handled;

        public ExceptionEvent(String exceptionId, ExceptionType exceptionType, String message,
                              int severity, boolean handled) {
            super("EXCEPTION", "System");
            this.exceptionId = exceptionId != null ? exceptionId : "";
            this.exceptionType = exceptionType != null ? exceptionType : ExceptionType.CUSTOM;
            this.message = message != null ? message : "";
            this.severity = severity;
            this.handled = handled;
        }

        // Getters
        public String getExceptionId() {
            return exceptionId;
        }

        public ExceptionType getExceptionType() {
            return exceptionType;
        }

        public String getMessage() {
            return message;
        }

        public int getSeverity() {
            return severity;
        }

        public boolean isHandled() {
            return handled;
        }
    }

    // 工艺事件类
    public static class ProcessEvent {
        private final String eventId;
        private final String eventType;
        private final String description;
        private final LocalDateTime timestamp;
        private final Map<String, Object> data;
        private final String source;

        public ProcessEvent(String eventType, String description) {
            this.eventId = UUID.randomUUID().toString();
            this.eventType = eventType != null ? eventType : "UNKNOWN";
            this.description = description != null ? description : "";
            this.timestamp = LocalDateTime.now();
            this.data = new ConcurrentHashMap<>();
            this.source = "";
        }

        // Getters and Setters
        public String getEventId() {
            return eventId;
        }

        public String getEventType() {
            return eventType;
        }

        public String getDescription() {
            return description;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public Map<String, Object> getData() {
            return new HashMap<>(data);
        }

        public ProcessEvent setData(String key, Object value) {
            this.data.put(key, value);
            return this;
        }

        public Object getData(String key) {
            return this.data.get(key);
        }

        public String getSource() {
            return source;
        }

        public ProcessEvent setSource(String source) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        @Override
        public String toString() {
            return "ProcessEvent{" +
                    "eventId='" + eventId + '\'' +
                    ", eventType='" + eventType + '\'' +
                    ", description='" + description + '\'' +
                    ", timestamp=" + timestamp +
                    ", source='" + source + '\'' +
                    '}';
        }
    }

    // 追溯查询类
    public static class TraceQuery {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String eventType;
        private String operator;
        private int limit = 100;

        // Getters and Setters
        public LocalDateTime getStartDate() {
            return startDate;
        }

        public TraceQuery setStartDate(LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public LocalDateTime getEndDate() {
            return endDate;
        }

        public TraceQuery setEndDate(LocalDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public String getEventType() {
            return eventType;
        }

        public TraceQuery setEventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public String getOperator() {
            return operator;
        }

        public TraceQuery setOperator(String operator) {
            this.operator = operator;
            return this;
        }

        public int getLimit() {
            return limit;
        }

        public TraceQuery setLimit(int limit) {
            this.limit = limit;
            return this;
        }
    }

    /**
     * 缓存统计信息类
     */
    public static class CacheStatistics {
        private int contextCacheSize;
        private int traceCacheSize;
        private long cacheTimeout;

        // Getters and Setters
        public int getContextCacheSize() {
            return contextCacheSize;
        }

        public void setContextCacheSize(int contextCacheSize) {
            this.contextCacheSize = contextCacheSize;
        }

        public int getTraceCacheSize() {
            return traceCacheSize;
        }

        public void setTraceCacheSize(int traceCacheSize) {
            this.traceCacheSize = traceCacheSize;
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
                    "contextCacheSize=" + contextCacheSize +
                    ", traceCacheSize=" + traceCacheSize +
                    ", cacheTimeout=" + cacheTimeout + "ms" +
                    '}';
        }
    }
}
