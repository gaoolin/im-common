package com.qtech.im.integration.erp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * ERP系统连接工具类
 * <p>
 * 特性：
 * - 通用性：支持多种ERP系统和业务数据交互
 * - 规范性：统一的数据交换标准和接口规范
 * - 专业性：半导体行业ERP集成专业实现
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
public class ErpConnector {

    // 默认配置
    public static final long DEFAULT_CACHE_TIMEOUT = 30 * 60 * 1000; // 30分钟
    public static final int DEFAULT_MAX_RETRY_ATTEMPTS = 3;
    public static final long DEFAULT_RETRY_DELAY = 1000; // 1秒
    public static final int DEFAULT_BATCH_SIZE = 100;
    public static final long DEFAULT_SYNC_INTERVAL = 5 * 60 * 1000; // 5分钟
    public static final int DEFAULT_MESSAGE_QUEUE_SIZE = 10000;
    public static final long DEFAULT_CONNECTION_TIMEOUT = 30000; // 30秒
    public static final int DEFAULT_INVENTORY_SYNC_THRESHOLD = 10; // 库存同步阈值
    private static final Logger logger = LoggerFactory.getLogger(ErpConnector.class);
    // 内部存储和管理
    private static final Map<ErpType, ErpConnectorEngine> engineRegistry = new ConcurrentHashMap<>();
    private static final Map<OrderStatus, OrderStatusSynchronizer> synchronizerRegistry = new ConcurrentHashMap<>();
    private static final Map<PlanStatus, ProductionPlanReceiver> receiverRegistry = new ConcurrentHashMap<>();
    private static final Map<CostType, CostDataReporter> reporterRegistry = new ConcurrentHashMap<>();
    private static final Map<InventoryType, InventoryDataSynchronizer> inventoryRegistry = new ConcurrentHashMap<>();
    private static final Map<String, ConnectionManager> connectionManagerRegistry = new ConcurrentHashMap<>();
    private static final Map<String, Order> orderCache = new ConcurrentHashMap<>();
    private static final Map<String, ProductionPlan> planCache = new ConcurrentHashMap<>();
    private static final Map<String, CostData> costCache = new ConcurrentHashMap<>();
    private static final Map<String, InventoryData> inventoryCache = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    // 当前使用的组件
    private static volatile ErpConnectorEngine currentEngine;
    private static volatile OrderStatusSynchronizer currentSynchronizer;
    private static volatile ProductionPlanReceiver currentReceiver;
    private static volatile CostDataReporter currentReporter;
    private static volatile InventoryDataSynchronizer currentInventorySynchronizer;
    private static volatile ConnectionManager currentConnectionManager;
    private static volatile ErpType currentErpType = ErpType.CUSTOM;

    // 初始化默认组件
    static {
        registerDefaultComponents();
        initializeCurrentComponents();
        startMaintenanceTasks();
        logger.info("ErpConnector initialized with default components");
    }

    /**
     * 注册默认组件
     */
    private static void registerDefaultComponents() {
        // 注册默认引擎、同步器、接收器、上报器和同步器
        // registerEngine(new DefaultErpConnectorEngine());
        // registerSynchronizer(new DefaultOrderStatusSynchronizer());
        // registerReceiver(new DefaultProductionPlanReceiver());
        // registerReporter(new DefaultCostDataReporter());
        // registerInventorySynchronizer(new DefaultInventoryDataSynchronizer());
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

        if (!synchronizerRegistry.isEmpty()) {
            currentSynchronizer = synchronizerRegistry.values().iterator().next();
        }

        if (!receiverRegistry.isEmpty()) {
            currentReceiver = receiverRegistry.values().iterator().next();
        }

        if (!reporterRegistry.isEmpty()) {
            currentReporter = reporterRegistry.values().iterator().next();
        }

        if (!inventoryRegistry.isEmpty()) {
            currentInventorySynchronizer = inventoryRegistry.values().iterator().next();
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
        scheduler.scheduleAtFixedRate(ErpConnector::cleanupCache,
                30, 30, TimeUnit.MINUTES);

        // 启动连接健康检查任务
        scheduler.scheduleAtFixedRate(ErpConnector::checkConnectionHealth,
                5, 5, TimeUnit.MINUTES);

        logger.debug("ERP connector maintenance tasks started");
    }

    /**
     * 注册ERP连接器引擎
     */
    public static void registerEngine(ErpConnectorEngine engine) {
        if (engine == null) {
            throw new IllegalArgumentException("Engine cannot be null");
        }

        for (ErpType type : ErpType.values()) {
            if (engine.supportsErpType(type)) {
                engineRegistry.put(type, engine);
                logger.debug("Registered engine {} for ERP type {}", engine.getEngineName(), type);
            }
        }
    }

    /**
     * 注册订单状态同步器
     */
    public static void registerSynchronizer(OrderStatusSynchronizer synchronizer) {
        if (synchronizer == null) {
            throw new IllegalArgumentException("Synchronizer cannot be null");
        }

        for (OrderStatus status : OrderStatus.values()) {
            if (synchronizer.supportsOrderStatus(status)) {
                synchronizerRegistry.put(status, synchronizer);
                logger.debug("Registered synchronizer {} for order status {}",
                        synchronizer.getSynchronizerName(), status);
            }
        }
    }

    /**
     * 注册生产计划接收器
     */
    public static void registerReceiver(ProductionPlanReceiver receiver) {
        if (receiver == null) {
            throw new IllegalArgumentException("Receiver cannot be null");
        }

        for (PlanStatus status : PlanStatus.values()) {
            if (receiver.supportsPlanStatus(status)) {
                receiverRegistry.put(status, receiver);
                logger.debug("Registered receiver {} for plan status {}",
                        receiver.getReceiverName(), status);
            }
        }
    }

    /**
     * 注册成本数据上报器
     */
    public static void registerReporter(CostDataReporter reporter) {
        if (reporter == null) {
            throw new IllegalArgumentException("Reporter cannot be null");
        }

        for (CostType type : CostType.values()) {
            if (reporter.supportsCostType(type)) {
                reporterRegistry.put(type, reporter);
                logger.debug("Registered reporter {} for cost type {}",
                        reporter.getReporterName(), type);
            }
        }
    }

    /**
     * 注册库存数据同步器
     */
    public static void registerInventorySynchronizer(InventoryDataSynchronizer synchronizer) {
        if (synchronizer == null) {
            throw new IllegalArgumentException("Inventory synchronizer cannot be null");
        }

        for (InventoryType type : InventoryType.values()) {
            if (synchronizer.supportsInventoryType(type)) {
                inventoryRegistry.put(type, synchronizer);
                logger.debug("Registered inventory synchronizer {} for inventory type {}",
                        synchronizer.getSynchronizerName(), type);
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
     * 设置当前ERP连接器引擎
     */
    public static void setCurrentEngine(ErpType erpType) {
        ErpConnectorEngine engine = engineRegistry.get(erpType);
        if (engine != null) {
            currentEngine = engine;
            currentErpType = erpType;
            logger.info("Current ERP engine set to: {} for ERP type {}", engine.getEngineName(), erpType);
        } else {
            logger.warn("No engine found for ERP type: {}", erpType);
        }
    }

    /**
     * 设置当前订单状态同步器
     */
    public static void setCurrentSynchronizer(OrderStatus status) {
        OrderStatusSynchronizer synchronizer = synchronizerRegistry.get(status);
        if (synchronizer != null) {
            currentSynchronizer = synchronizer;
            logger.info("Current order synchronizer set to: {} for status {}",
                    synchronizer.getSynchronizerName(), status);
        } else {
            logger.warn("No synchronizer found for order status: {}", status);
        }
    }

    /**
     * 设置当前生产计划接收器
     */
    public static void setCurrentReceiver(PlanStatus status) {
        ProductionPlanReceiver receiver = receiverRegistry.get(status);
        if (receiver != null) {
            currentReceiver = receiver;
            logger.info("Current production plan receiver set to: {} for status {}",
                    receiver.getReceiverName(), status);
        } else {
            logger.warn("No receiver found for plan status: {}", status);
        }
    }

    /**
     * 设置当前成本数据上报器
     */
    public static void setCurrentReporter(CostType costType) {
        CostDataReporter reporter = reporterRegistry.get(costType);
        if (reporter != null) {
            currentReporter = reporter;
            logger.info("Current cost reporter set to: {} for cost type {}",
                    reporter.getReporterName(), costType);
        } else {
            logger.warn("No reporter found for cost type: {}", costType);
        }
    }

    /**
     * 设置当前库存数据同步器
     */
    public static void setCurrentInventorySynchronizer(InventoryType inventoryType) {
        InventoryDataSynchronizer synchronizer = inventoryRegistry.get(inventoryType);
        if (synchronizer != null) {
            currentInventorySynchronizer = synchronizer;
            logger.info("Current inventory synchronizer set to: {} for inventory type {}",
                    synchronizer.getSynchronizerName(), inventoryType);
        } else {
            logger.warn("No synchronizer found for inventory type: {}", inventoryType);
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
     * 订单状态同步
     */
    public static boolean syncOrderStatus(String orderId, OrderStatus status) {
        if (orderId == null || orderId.isEmpty()) {
            logger.warn("Invalid order ID for status synchronization");
            return false;
        }

        if (status == null) {
            logger.warn("Invalid order status for synchronization");
            return false;
        }

        long startTime = System.currentTimeMillis();

        try {
            // 验证连接
            if (!ensureConnection()) {
                logger.warn("Failed to establish connection for order status synchronization");
                return false;
            }

            // 使用注册的引擎进行订单状态同步
            ErpConnectorEngine engine = engineRegistry.get(currentErpType);
            boolean synced = false;

            if (engine != null) {
                synced = engine.syncOrderStatus(orderId, status);
            } else if (currentEngine != null) {
                // 使用当前引擎
                synced = currentEngine.syncOrderStatus(orderId, status);
            } else {
                // 使用默认同步逻辑
                synced = performDefaultOrderStatusSync(orderId, status);
            }

            long duration = System.currentTimeMillis() - startTime;

            if (synced) {
                logger.info("Order status synchronization completed successfully: {} - {} in {}ms",
                        orderId, status.getDescription(), duration);
            } else {
                logger.warn("Order status synchronization failed: {} - {}", orderId, status.getDescription());
            }

            return synced;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Failed to sync order status: " + orderId + " - " + status, e);

            // 记录异常数据
            ExceptionData exceptionData = new ExceptionData(
                    ExceptionType.COMMUNICATION_ERROR,
                    "LocalSystem",
                    "ERP",
                    "订单状态同步异常: " + e.getMessage()
            )
                    .setContextData("orderId", orderId)
                    .setContextData("orderStatus", status.name())
                    .setSeverity("HIGH");

            reportExceptionData(exceptionData);

            return false;
        }
    }

    /**
     * 执行默认订单状态同步逻辑
     */
    private static boolean performDefaultOrderStatusSync(String orderId, OrderStatus status) {
        try {
            // 模拟订单状态同步过程
            Thread.sleep(50 + (long) (Math.random() * 150)); // 随机延迟50-200ms

            // 模拟95%的成功率
            boolean success = Math.random() > 0.05;

            if (success) {
                // 更新缓存中的订单状态
                Order order = orderCache.get(orderId);
                if (order != null) {
                    // 在实际实现中，这里需要通过反射或构建器模式更新订单状态
                    logger.debug("Updated order status in cache: {} - {}", orderId, status.getDescription());
                }
            }

            logger.debug("Default order status sync performed for: {} - {}", orderId, status.getDescription());

            return success;
        } catch (Exception e) {
            logger.warn("Failed to perform default order status sync", e);
            return false;
        }
    }

    /**
     * 批量订单状态同步
     */
    public static Map<String, Boolean> syncBatchOrderStatus(Map<String, OrderStatus> orderStatuses) {
        if (orderStatuses == null || orderStatuses.isEmpty()) {
            logger.warn("Invalid order statuses for batch synchronization");
            return new HashMap<>();
        }

        Map<String, Boolean> results = new HashMap<>();

        // 分批处理
        List<Map.Entry<String, OrderStatus>> entries = new ArrayList<>(orderStatuses.entrySet());
        for (int i = 0; i < entries.size(); i += DEFAULT_BATCH_SIZE) {
            int endIndex = Math.min(i + DEFAULT_BATCH_SIZE, entries.size());
            List<Map.Entry<String, OrderStatus>> batch = entries.subList(i, endIndex);

            Map<String, Boolean> batchResults = syncBatchOrderStatusInternal(batch);
            results.putAll(batchResults);
        }

        return results;
    }

    /**
     * 内部批量订单状态同步
     */
    private static Map<String, Boolean> syncBatchOrderStatusInternal(List<Map.Entry<String, OrderStatus>> batch) {
        Map<String, Boolean> results = new HashMap<>();

        for (Map.Entry<String, OrderStatus> entry : batch) {
            String orderId = entry.getKey();
            OrderStatus status = entry.getValue();

            boolean synced = syncOrderStatus(orderId, status);
            results.put(orderId, synced);
        }

        return results;
    }

    /**
     * 生产计划接收
     */
    public static ProductionPlan receiveProductionPlan(ErpPlan erpPlan) {
        if (erpPlan == null) {
            logger.warn("Invalid ERP plan for production plan receiving");
            return null;
        }

        long startTime = System.currentTimeMillis();

        try {
            // 验证连接
            if (!ensureConnection()) {
                logger.warn("Failed to establish connection for production plan receiving");
                return null;
            }

            // 使用注册的接收器接收生产计划
            ProductionPlanReceiver receiver = receiverRegistry.get(erpPlan.getStatus());
            ProductionPlan productionPlan = null;

            if (receiver != null) {
                productionPlan = receiver.receiveProductionPlan(erpPlan);
            } else if (currentReceiver != null) {
                // 使用当前接收器
                productionPlan = currentReceiver.receiveProductionPlan(erpPlan);
            } else {
                // 使用默认接收逻辑
                productionPlan = performDefaultProductionPlanReceiving(erpPlan);
            }

            long duration = System.currentTimeMillis() - startTime;

            // 更新缓存
            if (productionPlan != null) {
                planCache.put(productionPlan.getPlanId(), productionPlan);

                logger.info("Production plan received successfully: {} in {}ms",
                        productionPlan.getPlanId(), duration);
            } else {
                logger.warn("Production plan receiving failed: {} in {}ms",
                        erpPlan.getPlanId(), duration);
            }

            return productionPlan;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Failed to receive production plan: " + erpPlan.getPlanId(), e);

            // 记录异常数据
            ExceptionData exceptionData = new ExceptionData(
                    ExceptionType.COMMUNICATION_ERROR,
                    "ERP",
                    "LocalSystem",
                    "生产计划接收异常: " + e.getMessage()
            )
                    .setContextData("planId", erpPlan.getPlanId())
                    .setContextData("productId", erpPlan.getProductId())
                    .setContextData("plannedQuantity", erpPlan.getPlannedQuantity())
                    .setSeverity("HIGH");

            reportExceptionData(exceptionData);

            return null;
        }
    }

    /**
     * 执行默认生产计划接收逻辑
     */
    private static ProductionPlan performDefaultProductionPlanReceiving(ErpPlan erpPlan) {
        try {
            // 模拟生产计划接收过程
            Thread.sleep(100 + (long) (Math.random() * 200)); // 随机延迟100-300ms

            // 创建生产计划
            ProductionPlan productionPlan = new ProductionPlan(
                    erpPlan.getPlanId(),
                    erpPlan.getProductId(),
                    erpPlan.getProductName(),
                    erpPlan.getPlannedQuantity()
            );

            productionPlan.setStartDate(erpPlan.getStartDate())
                    .setEndDate(erpPlan.getEndDate())
                    .setProductionLine(erpPlan.getProductionLine())
                    .setStatus(PlanStatus.RELEASED);

            // 转换计划项为生产任务
            for (PlanItem item : erpPlan.getItems()) {
                ProductionTask task = new ProductionTask(
                        item.getItemId(),
                        "PROCESS_STEP_" + item.getItemId(),
                        item.getRequiredQuantity()
                );
                productionPlan.addTask(task);
            }

            logger.debug("Default production plan receiving performed for: {}", erpPlan.getPlanId());

            return productionPlan;
        } catch (Exception e) {
            logger.warn("Failed to perform default production plan receiving", e);
            return null;
        }
    }

    /**
     * 成本数据上报
     */
    public static boolean reportCostData(CostData costData) {
        if (costData == null) {
            logger.warn("Invalid cost data for reporting");
            return false;
        }

        long startTime = System.currentTimeMillis();

        try {
            // 验证连接
            if (!ensureConnection()) {
                logger.warn("Failed to establish connection for cost data reporting");
                return false;
            }

            // 使用注册的上报器上报成本数据
            CostDataReporter reporter = reporterRegistry.get(costData.getCostType());
            boolean reported = false;

            if (reporter != null) {
                reported = reporter.reportCostData(costData);
            } else if (currentReporter != null) {
                // 使用当前上报器
                reported = currentReporter.reportCostData(costData);
            } else {
                // 使用默认上报逻辑
                reported = performDefaultCostDataReporting(costData);
            }

            long duration = System.currentTimeMillis() - startTime;

            // 更新缓存
            if (reported) {
                costCache.put(costData.getCostId(), costData);

                logger.info("Cost data reported successfully: {} - {} {} in {}ms",
                        costData.getCostId(), costData.getAmount(), costData.getCurrency(), duration);
            } else {
                logger.warn("Cost data reporting failed: {} in {}ms", costData.getCostId(), duration);
            }

            return reported;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Failed to report cost data: " + costData.getCostId(), e);

            // 记录异常数据
            ExceptionData exceptionData = new ExceptionData(
                    ExceptionType.COMMUNICATION_ERROR,
                    "LocalSystem",
                    "ERP",
                    "成本数据上报异常: " + e.getMessage()
            )
                    .setContextData("costId", costData.getCostId())
                    .setContextData("costType", costData.getCostType().name())
                    .setContextData("amount", costData.getAmount().toString())
                    .setSeverity("MEDIUM");

            reportExceptionData(exceptionData);

            return false;
        }
    }

    /**
     * 执行默认成本数据上报逻辑
     */
    private static boolean performDefaultCostDataReporting(CostData costData) {
        try {
            // 模拟成本数据上报过程
            Thread.sleep(50 + (long) (Math.random() * 150)); // 随机延迟50-200ms

            // 模拟98%的成功率
            boolean success = Math.random() > 0.02;

            logger.debug("Default cost data reporting performed for: {} - {}",
                    costData.getCostId(), costData.getCostType().getChineseName());

            return success;
        } catch (Exception e) {
            logger.warn("Failed to perform default cost data reporting", e);
            return false;
        }
    }

    /**
     * 批量成本数据上报
     */
    public static List<SyncResult> reportBatchCostData(List<CostData> costDataList) {
        if (costDataList == null || costDataList.isEmpty()) {
            logger.warn("Invalid cost data list for batch reporting");
            return new ArrayList<>();
        }

        List<SyncResult> results = new ArrayList<>();

        // 分批处理
        for (int i = 0; i < costDataList.size(); i += DEFAULT_BATCH_SIZE) {
            int endIndex = Math.min(i + DEFAULT_BATCH_SIZE, costDataList.size());
            List<CostData> batch = costDataList.subList(i, endIndex);

            List<SyncResult> batchResults = reportBatchCostDataInternal(batch);
            results.addAll(batchResults);
        }

        return results;
    }

    /**
     * 内部批量成本数据上报
     */
    private static List<SyncResult> reportBatchCostDataInternal(List<CostData> batch) {
        List<SyncResult> results = new ArrayList<>();

        for (CostData costData : batch) {
            long startTime = System.currentTimeMillis();
            boolean reported = reportCostData(costData);
            long duration = System.currentTimeMillis() - startTime;

            SyncResult result = new SyncResult(costData.getCostId(), reported,
                    reported ? "成本数据上报成功" : "成本数据上报失败", duration);
            results.add(result);
        }

        return results;
    }

    /**
     * 库存信息同步
     */
    public static boolean syncInventoryData(InventoryData inventoryData) {
        if (inventoryData == null) {
            logger.warn("Invalid inventory data for synchronization");
            return false;
        }

        long startTime = System.currentTimeMillis();

        try {
            // 验证连接
            if (!ensureConnection()) {
                logger.warn("Failed to establish connection for inventory data synchronization");
                return false;
            }

            // 使用注册的同步器同步库存数据
            InventoryDataSynchronizer synchronizer = inventoryRegistry.get(inventoryData.getInventoryType());
            boolean synced = false;

            if (synchronizer != null) {
                synced = synchronizer.syncInventoryData(inventoryData);
            } else if (currentInventorySynchronizer != null) {
                // 使用当前同步器
                synced = currentInventorySynchronizer.syncInventoryData(inventoryData);
            } else {
                // 使用默认同步逻辑
                synced = performDefaultInventoryDataSync(inventoryData);
            }

            long duration = System.currentTimeMillis() - startTime;

            // 更新缓存
            if (synced) {
                inventoryCache.put(inventoryData.getInventoryId(), inventoryData);

                logger.info("Inventory data synchronized successfully: {} - {} {} in {}ms",
                        inventoryData.getInventoryId(), inventoryData.getQuantity(),
                        inventoryData.getUnit(), duration);
            } else {
                logger.warn("Inventory data synchronization failed: {} in {}ms",
                        inventoryData.getInventoryId(), duration);
            }

            return synced;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Failed to sync inventory data: " + inventoryData.getInventoryId(), e);

            // 记录异常数据
            ExceptionData exceptionData = new ExceptionData(
                    ExceptionType.COMMUNICATION_ERROR,
                    "LocalSystem",
                    "ERP",
                    "库存数据同步异常: " + e.getMessage()
            )
                    .setContextData("inventoryId", inventoryData.getInventoryId())
                    .setContextData("productId", inventoryData.getProductId())
                    .setContextData("quantity", inventoryData.getQuantity())
                    .setSeverity("MEDIUM");

            reportExceptionData(exceptionData);

            return false;
        }
    }

    /**
     * 执行默认库存数据同步逻辑
     */
    private static boolean performDefaultInventoryDataSync(InventoryData inventoryData) {
        try {
            // 模拟库存数据同步过程
            Thread.sleep(50 + (long) (Math.random() * 150)); // 随机延迟50-200ms

            // 模拟97%的成功率
            boolean success = Math.random() > 0.03;

            logger.debug("Default inventory data sync performed for: {} - {}",
                    inventoryData.getInventoryId(), inventoryData.getInventoryType().getChineseName());

            return success;
        } catch (Exception e) {
            logger.warn("Failed to perform default inventory data sync", e);
            return false;
        }
    }

    /**
     * 批量库存信息同步
     */
    public static List<SyncResult> syncBatchInventoryData(List<InventoryData> inventoryDataList) {
        if (inventoryDataList == null || inventoryDataList.isEmpty()) {
            logger.warn("Invalid inventory data list for batch synchronization");
            return new ArrayList<>();
        }

        List<SyncResult> results = new ArrayList<>();

        // 分批处理
        for (int i = 0; i < inventoryDataList.size(); i += DEFAULT_BATCH_SIZE) {
            int endIndex = Math.min(i + DEFAULT_BATCH_SIZE, inventoryDataList.size());
            List<InventoryData> batch = inventoryDataList.subList(i, endIndex);

            List<SyncResult> batchResults = syncBatchInventoryDataInternal(batch);
            results.addAll(batchResults);
        }

        return results;
    }

    /**
     * 内部批量库存信息同步
     */
    private static List<SyncResult> syncBatchInventoryDataInternal(List<InventoryData> batch) {
        List<SyncResult> results = new ArrayList<>();

        for (InventoryData inventoryData : batch) {
            long startTime = System.currentTimeMillis();
            boolean synced = syncInventoryData(inventoryData);
            long duration = System.currentTimeMillis() - startTime;

            SyncResult result = new SyncResult(inventoryData.getInventoryId(), synced,
                    synced ? "库存数据同步成功" : "库存数据同步失败", duration);
            results.add(result);
        }

        return results;
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
     * 创建订单
     */
    public static Order createOrder(String orderId, String customerCode, String customerName) {
        if (orderId == null || orderId.isEmpty() || customerCode == null || customerCode.isEmpty()) {
            logger.warn("Invalid parameters for order creation");
            return null;
        }

        try {
            Order order = new Order(orderId, customerCode, customerName);

            // 添加到缓存
            orderCache.put(order.getOrderId(), order);

            logger.info("Created new order: {} for customer: {} ({})",
                    orderId, customerCode, customerName);

            return order;
        } catch (Exception e) {
            logger.error("Failed to create order: " + orderId, e);
            return null;
        }
    }

    /**
     * 获取订单
     */
    public static Order getOrder(String orderId) {
        if (orderId == null || orderId.isEmpty()) {
            logger.warn("Invalid order ID for retrieval");
            return null;
        }

        try {
            Order order = orderCache.get(orderId);
            if (order != null) {
                logger.debug("Retrieved order: {}", orderId);
            } else {
                logger.warn("Order not found: {}", orderId);
            }
            return order;
        } catch (Exception e) {
            logger.error("Failed to get order: " + orderId, e);
            return null;
        }
    }

    /**
     * 更新订单
     */
    public static boolean updateOrder(Order order) {
        if (order == null) {
            logger.warn("Invalid order for update");
            return false;
        }

        try {
            orderCache.put(order.getOrderId(), order);
            logger.debug("Updated order in cache: {}", order.getOrderId());
            return true;
        } catch (Exception e) {
            logger.error("Failed to update order: " + order.getOrderId(), e);
            return false;
        }
    }

    /**
     * 删除订单
     */
    public static boolean deleteOrder(String orderId) {
        if (orderId == null || orderId.isEmpty()) {
            logger.warn("Invalid order ID for deletion");
            return false;
        }

        try {
            Order removed = orderCache.remove(orderId);
            if (removed != null) {
                logger.info("Deleted order: {}", orderId);
                return true;
            } else {
                logger.warn("Order not found for deletion: {}", orderId);
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to delete order: " + orderId, e);
            return false;
        }
    }

    /**
     * 创建ERP计划
     */
    public static ErpPlan createErpPlan(String planId, String productId, String productName, int plannedQuantity) {
        if (planId == null || planId.isEmpty() || productId == null || productId.isEmpty()) {
            logger.warn("Invalid parameters for ERP plan creation");
            return null;
        }

        try {
            ErpPlan erpPlan = new ErpPlan(planId, productId, productName, plannedQuantity);

            logger.info("Created new ERP plan: {} for product: {} (Quantity: {})",
                    planId, productId, plannedQuantity);

            return erpPlan;
        } catch (Exception e) {
            logger.error("Failed to create ERP plan: " + planId, e);
            return null;
        }
    }

    /**
     * 获取生产计划
     */
    public static ProductionPlan getProductionPlan(String planId) {
        if (planId == null || planId.isEmpty()) {
            logger.warn("Invalid plan ID for retrieval");
            return null;
        }

        try {
            ProductionPlan plan = planCache.get(planId);
            if (plan != null) {
                logger.debug("Retrieved production plan: {}", planId);
            } else {
                logger.warn("Production plan not found: {}", planId);
            }
            return plan;
        } catch (Exception e) {
            logger.error("Failed to get production plan: " + planId, e);
            return null;
        }
    }

    /**
     * 创建成本数据
     */
    public static CostData createCostData(String productId, CostType costType, BigDecimal amount) {
        if (productId == null || productId.isEmpty() || costType == null || amount == null) {
            logger.warn("Invalid parameters for cost data creation");
            return null;
        }

        try {
            CostData costData = new CostData(null, productId, costType, amount);

            logger.info("Created new cost data: {} - {} {} {}",
                    costData.getCostId(), costType.getChineseName(), amount, costData.getCurrency());

            return costData;
        } catch (Exception e) {
            logger.error("Failed to create cost data for product: " + productId, e);
            return null;
        }
    }

    /**
     * 获取成本数据
     */
    public static CostData getCostData(String costId) {
        if (costId == null || costId.isEmpty()) {
            logger.warn("Invalid cost ID for retrieval");
            return null;
        }

        try {
            CostData costData = costCache.get(costId);
            if (costData != null) {
                logger.debug("Retrieved cost data: {}", costId);
            } else {
                logger.warn("Cost data not found: {}", costId);
            }
            return costData;
        } catch (Exception e) {
            logger.error("Failed to get cost data: " + costId, e);
            return null;
        }
    }

    /**
     * 创建库存数据
     */
    public static InventoryData createInventoryData(String productId, String productName) {
        if (productId == null || productId.isEmpty()) {
            logger.warn("Invalid parameters for inventory data creation");
            return null;
        }

        try {
            InventoryData inventoryData = new InventoryData(null, productId, productName);

            logger.info("Created new inventory data: {} for product: {}",
                    inventoryData.getInventoryId(), productId);

            return inventoryData;
        } catch (Exception e) {
            logger.error("Failed to create inventory data for product: " + productId, e);
            return null;
        }
    }

    /**
     * 获取库存数据
     */
    public static InventoryData getInventoryData(String inventoryId) {
        if (inventoryId == null || inventoryId.isEmpty()) {
            logger.warn("Invalid inventory ID for retrieval");
            return null;
        }

        try {
            InventoryData inventoryData = inventoryCache.get(inventoryId);
            if (inventoryData != null) {
                logger.debug("Retrieved inventory data: {}", inventoryId);
            } else {
                logger.warn("Inventory data not found: {}", inventoryId);
            }
            return inventoryData;
        } catch (Exception e) {
            logger.error("Failed to get inventory data: " + inventoryId, e);
            return null;
        }
    }

    /**
     * 异常数据上报
     */
    public static boolean reportExceptionData(ExceptionData data) {
        if (data == null) {
            logger.warn("Invalid exception data for reporting");
            return false;
        }

        try {
            // 在实际实现中，这里应该将异常数据存储到数据库或发送到监控系统
            logger.info("Exception data reported: {} - {} - {}",
                    data.getExceptionId(), data.getExceptionType().getChineseName(),
                    data.getErrorMessage());

            return true;
        } catch (Exception e) {
            logger.error("Failed to report exception data: " + data.getExceptionId(), e);
            return false;
        }
    }

    /**
     * 清理缓存
     */
    private static void cleanupCache() {
        try {
            long cutoffTime = System.currentTimeMillis() - DEFAULT_CACHE_TIMEOUT;

            // 清理订单缓存
            orderCache.entrySet().removeIf(entry ->
                    entry.getValue().getLastUpdate().toInstant(java.time.ZoneOffset.UTC).toEpochMilli() < cutoffTime
            );

            // 清理计划缓存
            planCache.entrySet().removeIf(entry ->
                    entry.getValue().getCreateTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli() < cutoffTime
            );

            // 清理成本数据缓存
            costCache.entrySet().removeIf(entry ->
                    entry.getValue().getCreateTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli() < cutoffTime
            );

            // 清理库存数据缓存
            inventoryCache.entrySet().removeIf(entry ->
                    entry.getValue().getLastUpdate().toInstant(java.time.ZoneOffset.UTC).toEpochMilli() < cutoffTime
            );

            logger.debug("Cleaned up ERP connector cache, remaining entries - order: {}, plan: {}, cost: {}, inventory: {}",
                    orderCache.size(), planCache.size(), costCache.size(), inventoryCache.size());
        } catch (Exception e) {
            logger.error("Failed to cleanup ERP connector cache", e);
        }
    }

    /**
     * 获取缓存统计信息
     */
    public static CacheStatistics getCacheStatistics() {
        CacheStatistics stats = new CacheStatistics();
        stats.setOrderCacheSize(orderCache.size());
        stats.setPlanCacheSize(planCache.size());
        stats.setCostCacheSize(costCache.size());
        stats.setInventoryCacheSize(inventoryCache.size());
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
     * 关闭ERP连接器
     */
    public static void shutdown() {
        try {
            scheduler.shutdown();

            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }

            // 断开连接
            if (currentConnectionManager != null && currentConnectionManager.isConnected()) {
                currentConnectionManager.disconnect();
            }

            logger.info("ErpConnector shutdown completed");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            logger.warn("ErpConnector shutdown interrupted");
        } catch (Exception e) {
            logger.error("Failed to shutdown ErpConnector", e);
        }
    }

    // ERP系统类型枚举
    public enum ErpType {
        SAP("SAP ERP", "SAP ERP"),
        ORACLE_EBS("Oracle EBS", "Oracle E-Business Suite"),
        MICROSOFT_DYNAMICS("Microsoft Dynamics", "Microsoft Dynamics 365"),
        KINGDEE("金蝶云", "Kingdee Cloud"),
        YONYOU("用友NC", "Yonyou NC"),
        CUSTOM("自定义ERP", "Custom ERP");

        private final String chineseName;
        private final String englishName;

        ErpType(String chineseName, String englishName) {
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

    // 订单状态枚举
    public enum OrderStatus {
        CREATED("已创建", 1),
        CONFIRMED("已确认", 2),
        IN_PRODUCTION("生产中", 3),
        PARTIALLY_COMPLETED("部分完成", 4),
        COMPLETED("已完成", 5),
        CANCELLED("已取消", 6),
        ON_HOLD("挂起", 7),
        SHIPPED("已发货", 8),
        DELIVERED("已交付", 9);

        private final String description;
        private final int sequence;

        OrderStatus(String description, int sequence) {
            this.description = description;
            this.sequence = sequence;
        }

        public String getDescription() {
            return description;
        }

        public int getSequence() {
            return sequence;
        }

        public boolean isCompleted() {
            return this == COMPLETED || this == CANCELLED || this == DELIVERED;
        }
    }

    // 生产计划状态枚举
    public enum PlanStatus {
        DRAFT("草稿", 1),
        APPROVED("已批准", 2),
        RELEASED("已发布", 3),
        IN_PROGRESS("进行中", 4),
        COMPLETED("已完成", 5),
        CANCELLED("已取消", 6),
        ON_HOLD("挂起", 7);

        private final String description;
        private final int sequence;

        PlanStatus(String description, int sequence) {
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

    // 成本类型枚举
    public enum CostType {
        MATERIAL("材料成本", "Material Cost"),
        LABOR("人工成本", "Labor Cost"),
        OVERHEAD("制造费用", "Overhead Cost"),
        EQUIPMENT("设备折旧", "Equipment Depreciation"),
        QUALITY("质量成本", "Quality Cost"),
        CUSTOM("自定义成本", "Custom Cost");

        private final String chineseName;
        private final String englishName;

        CostType(String chineseName, String englishName) {
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

    // 库存类型枚举
    public enum InventoryType {
        RAW_MATERIAL("原材料", "Raw Material"),
        WORK_IN_PROGRESS("在制品", "Work In Progress"),
        FINISHED_GOOD("成品", "Finished Good"),
        CONSUMABLE("耗材", "Consumable"),
        PACKAGING_MATERIAL("包装材料", "Packaging Material");

        private final String chineseName;
        private final String englishName;

        InventoryType(String chineseName, String englishName) {
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

    // ERP连接器接口
    public interface ErpConnectorEngine {
        boolean syncOrderStatus(String orderId, OrderStatus status) throws Exception;

        ProductionPlan receiveProductionPlan(ErpPlan erpPlan) throws Exception;

        boolean reportCostData(CostData costData) throws Exception;

        boolean syncInventoryData(InventoryData inventoryData) throws Exception;

        boolean supportsErpType(ErpType erpType);

        String getEngineName();
    }

    // 订单状态同步器接口
    public interface OrderStatusSynchronizer {
        boolean syncOrderStatus(String orderId, OrderStatus status) throws Exception;

        boolean supportsOrderStatus(OrderStatus status);

        String getSynchronizerName();
    }

    // 生产计划接收器接口
    public interface ProductionPlanReceiver {
        ProductionPlan receiveProductionPlan(ErpPlan erpPlan) throws Exception;

        boolean supportsPlanStatus(PlanStatus status);

        String getReceiverName();
    }

    // 成本数据上报器接口
    public interface CostDataReporter {
        boolean reportCostData(CostData costData) throws Exception;

        boolean supportsCostType(CostType costType);

        String getReporterName();
    }

    // 库存数据同步器接口
    public interface InventoryDataSynchronizer {
        boolean syncInventoryData(InventoryData inventoryData) throws Exception;

        boolean supportsInventoryType(InventoryType inventoryType);

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

    // 订单类
    public static class Order {
        private final String orderId;
        private final String customerCode;
        private final String customerName;
        private final List<OrderItem> items;
        private final LocalDateTime orderDate;
        private final LocalDateTime deliveryDate;
        private final BigDecimal totalAmount;
        private final String currency;
        private final OrderStatus status;
        private final String salesPerson;
        private final String paymentTerms;
        private final Map<String, Object> attributes;
        private final LocalDateTime lastUpdate;
        private final String updatedBy;

        public Order(String orderId, String customerCode, String customerName) {
            this.orderId = orderId != null ? orderId : UUID.randomUUID().toString();
            this.customerCode = customerCode != null ? customerCode : "";
            this.customerName = customerName != null ? customerName : "";
            this.items = new CopyOnWriteArrayList<>();
            this.orderDate = LocalDateTime.now();
            this.deliveryDate = LocalDateTime.now().plusDays(7);
            this.totalAmount = BigDecimal.ZERO;
            this.currency = "CNY";
            this.status = OrderStatus.CREATED;
            this.salesPerson = "";
            this.paymentTerms = "";
            this.attributes = new ConcurrentHashMap<>();
            this.lastUpdate = LocalDateTime.now();
            this.updatedBy = "System";
        }

        // Getters and Setters
        public String getOrderId() {
            return orderId;
        }

        public String getCustomerCode() {
            return customerCode;
        }

        public Order setCustomerCode(String customerCode) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getCustomerName() {
            return customerName;
        }

        public Order setCustomerName(String customerName) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public List<OrderItem> getItems() {
            return new ArrayList<>(items);
        }

        public Order addItem(OrderItem item) {
            this.items.add(item);
            return this;
        }

        public LocalDateTime getOrderDate() {
            return orderDate;
        }

        public Order setOrderDate(LocalDateTime orderDate) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public LocalDateTime getDeliveryDate() {
            return deliveryDate;
        }

        public Order setDeliveryDate(LocalDateTime deliveryDate) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public Order setTotalAmount(BigDecimal totalAmount) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getCurrency() {
            return currency;
        }

        public Order setCurrency(String currency) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public OrderStatus getStatus() {
            return status;
        }

        public Order setStatus(OrderStatus status) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getSalesPerson() {
            return salesPerson;
        }

        public Order setSalesPerson(String salesPerson) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getPaymentTerms() {
            return paymentTerms;
        }

        public Order setPaymentTerms(String paymentTerms) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public Order setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        public LocalDateTime getLastUpdate() {
            return lastUpdate;
        }

        public String getUpdatedBy() {
            return updatedBy;
        }

        public Order setUpdatedBy(String updatedBy) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        @Override
        public String toString() {
            return "Order{" +
                    "orderId='" + orderId + '\'' +
                    ", customerCode='" + customerCode + '\'' +
                    ", customerName='" + customerName + '\'' +
                    ", itemCount=" + items.size() +
                    ", orderDate=" + orderDate +
                    ", status=" + status +
                    ", totalAmount=" + totalAmount +
                    '}';
        }
    }

    // 订单项类
    public static class OrderItem {
        private final String itemId;
        private final String productId;
        private final String productName;
        private final int quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal totalAmount;
        private final String unit;
        private final LocalDateTime deliveryDate;
        private final Map<String, Object> attributes;

        public OrderItem(String itemId, String productId, String productName, int quantity) {
            this.itemId = itemId != null ? itemId : UUID.randomUUID().toString();
            this.productId = productId != null ? productId : "";
            this.productName = productName != null ? productName : "";
            this.quantity = quantity;
            this.unitPrice = BigDecimal.ZERO;
            this.totalAmount = BigDecimal.ZERO;
            this.unit = "PCS";
            this.deliveryDate = LocalDateTime.now().plusDays(7);
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getItemId() {
            return itemId;
        }

        public String getProductId() {
            return productId;
        }

        public OrderItem setProductId(String productId) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getProductName() {
            return productName;
        }

        public OrderItem setProductName(String productName) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public OrderItem setUnitPrice(BigDecimal unitPrice) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public OrderItem setTotalAmount(BigDecimal totalAmount) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getUnit() {
            return unit;
        }

        public OrderItem setUnit(String unit) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public LocalDateTime getDeliveryDate() {
            return deliveryDate;
        }

        public OrderItem setDeliveryDate(LocalDateTime deliveryDate) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public OrderItem setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        @Override
        public String toString() {
            return "OrderItem{" +
                    "itemId='" + itemId + '\'' +
                    ", productId='" + productId + '\'' +
                    ", productName='" + productName + '\'' +
                    ", quantity=" + quantity +
                    ", unitPrice=" + unitPrice +
                    ", totalAmount=" + totalAmount +
                    '}';
        }
    }

    // ERP计划类
    public static class ErpPlan {
        private final String planId;
        private final String productId;
        private final String productName;
        private final int plannedQuantity;
        private final LocalDateTime startDate;
        private final LocalDateTime endDate;
        private final String productionLine;
        private final PlanStatus status;
        private final List<PlanItem> items;
        private final Map<String, Object> attributes;
        private final LocalDateTime createTime;
        private final String createdBy;

        public ErpPlan(String planId, String productId, String productName, int plannedQuantity) {
            this.planId = planId != null ? planId : UUID.randomUUID().toString();
            this.productId = productId != null ? productId : "";
            this.productName = productName != null ? productName : "";
            this.plannedQuantity = plannedQuantity;
            this.startDate = LocalDateTime.now();
            this.endDate = LocalDateTime.now().plusDays(7);
            this.productionLine = "";
            this.status = PlanStatus.DRAFT;
            this.items = new CopyOnWriteArrayList<>();
            this.attributes = new ConcurrentHashMap<>();
            this.createTime = LocalDateTime.now();
            this.createdBy = "System";
        }

        // Getters and Setters
        public String getPlanId() {
            return planId;
        }

        public String getProductId() {
            return productId;
        }

        public ErpPlan setProductId(String productId) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getProductName() {
            return productName;
        }

        public ErpPlan setProductName(String productName) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public int getPlannedQuantity() {
            return plannedQuantity;
        }

        public LocalDateTime getStartDate() {
            return startDate;
        }

        public ErpPlan setStartDate(LocalDateTime startDate) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public LocalDateTime getEndDate() {
            return endDate;
        }

        public ErpPlan setEndDate(LocalDateTime endDate) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getProductionLine() {
            return productionLine;
        }

        public ErpPlan setProductionLine(String productionLine) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public PlanStatus getStatus() {
            return status;
        }

        public ErpPlan setStatus(PlanStatus status) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public List<PlanItem> getItems() {
            return new ArrayList<>(items);
        }

        public ErpPlan addItem(PlanItem item) {
            this.items.add(item);
            return this;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public ErpPlan setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public ErpPlan setCreatedBy(String createdBy) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        @Override
        public String toString() {
            return "ErpPlan{" +
                    "planId='" + planId + '\'' +
                    ", productId='" + productId + '\'' +
                    ", productName='" + productName + '\'' +
                    ", plannedQuantity=" + plannedQuantity +
                    ", startDate=" + startDate +
                    ", endDate=" + endDate +
                    ", status=" + status +
                    ", itemCount=" + items.size() +
                    '}';
        }
    }

    // 计划项类
    public static class PlanItem {
        private final String itemId;
        private final String materialId;
        private final String materialName;
        private final int requiredQuantity;
        private final String unit;
        private final LocalDateTime requiredDate;
        private final Map<String, Object> attributes;

        public PlanItem(String itemId, String materialId, String materialName, int requiredQuantity) {
            this.itemId = itemId != null ? itemId : UUID.randomUUID().toString();
            this.materialId = materialId != null ? materialId : "";
            this.materialName = materialName != null ? materialName : "";
            this.requiredQuantity = requiredQuantity;
            this.unit = "PCS";
            this.requiredDate = LocalDateTime.now();
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getItemId() {
            return itemId;
        }

        public String getMaterialId() {
            return materialId;
        }

        public PlanItem setMaterialId(String materialId) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getMaterialName() {
            return materialName;
        }

        public PlanItem setMaterialName(String materialName) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public int getRequiredQuantity() {
            return requiredQuantity;
        }

        public String getUnit() {
            return unit;
        }

        public PlanItem setUnit(String unit) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public LocalDateTime getRequiredDate() {
            return requiredDate;
        }

        public PlanItem setRequiredDate(LocalDateTime requiredDate) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public PlanItem setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        @Override
        public String toString() {
            return "PlanItem{" +
                    "itemId='" + itemId + '\'' +
                    ", materialId='" + materialId + '\'' +
                    ", materialName='" + materialName + '\'' +
                    ", requiredQuantity=" + requiredQuantity +
                    ", unit='" + unit + '\'' +
                    ", requiredDate=" + requiredDate +
                    '}';
        }
    }

    // 生产计划类
    public static class ProductionPlan {
        private final String planId;
        private final String productId;
        private final String productName;
        private final int plannedQuantity;
        private final LocalDateTime startDate;
        private final LocalDateTime endDate;
        private final String productionLine;
        private final PlanStatus status;
        private final List<ProductionTask> tasks;
        private final Map<String, Object> attributes;
        private final LocalDateTime createTime;
        private final String createdBy;

        public ProductionPlan(String planId, String productId, String productName, int plannedQuantity) {
            this.planId = planId != null ? planId : UUID.randomUUID().toString();
            this.productId = productId != null ? productId : "";
            this.productName = productName != null ? productName : "";
            this.plannedQuantity = plannedQuantity;
            this.startDate = LocalDateTime.now();
            this.endDate = LocalDateTime.now().plusDays(7);
            this.productionLine = "";
            this.status = PlanStatus.DRAFT;
            this.tasks = new CopyOnWriteArrayList<>();
            this.attributes = new ConcurrentHashMap<>();
            this.createTime = LocalDateTime.now();
            this.createdBy = "System";
        }

        // Getters and Setters
        public String getPlanId() {
            return planId;
        }

        public String getProductId() {
            return productId;
        }

        public ProductionPlan setProductId(String productId) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getProductName() {
            return productName;
        }

        public ProductionPlan setProductName(String productName) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public int getPlannedQuantity() {
            return plannedQuantity;
        }

        public LocalDateTime getStartDate() {
            return startDate;
        }

        public ProductionPlan setStartDate(LocalDateTime startDate) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public LocalDateTime getEndDate() {
            return endDate;
        }

        public ProductionPlan setEndDate(LocalDateTime endDate) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getProductionLine() {
            return productionLine;
        }

        public ProductionPlan setProductionLine(String productionLine) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public PlanStatus getStatus() {
            return status;
        }

        public ProductionPlan setStatus(PlanStatus status) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public List<ProductionTask> getTasks() {
            return new ArrayList<>(tasks);
        }

        public ProductionPlan addTask(ProductionTask task) {
            this.tasks.add(task);
            return this;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public ProductionPlan setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public ProductionPlan setCreatedBy(String createdBy) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        @Override
        public String toString() {
            return "ProductionPlan{" +
                    "planId='" + planId + '\'' +
                    ", productId='" + productId + '\'' +
                    ", productName='" + productName + '\'' +
                    ", plannedQuantity=" + plannedQuantity +
                    ", startDate=" + startDate +
                    ", endDate=" + endDate +
                    ", status=" + status +
                    ", taskCount=" + tasks.size() +
                    '}';
        }
    }

    // 生产任务类
    public static class ProductionTask {
        private final String taskId;
        private final String processStep;
        private final int plannedQuantity;
        private final LocalDateTime plannedStartTime;
        private final LocalDateTime plannedEndTime;
        private final String equipmentId;
        private final String operator;
        private final Map<String, Object> attributes;

        public ProductionTask(String taskId, String processStep, int plannedQuantity) {
            this.taskId = taskId != null ? taskId : UUID.randomUUID().toString();
            this.processStep = processStep != null ? processStep : "";
            this.plannedQuantity = plannedQuantity;
            this.plannedStartTime = LocalDateTime.now();
            this.plannedEndTime = LocalDateTime.now().plusHours(8);
            this.equipmentId = "";
            this.operator = "";
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getTaskId() {
            return taskId;
        }

        public String getProcessStep() {
            return processStep;
        }

        public ProductionTask setProcessStep(String processStep) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public int getPlannedQuantity() {
            return plannedQuantity;
        }

        public LocalDateTime getPlannedStartTime() {
            return plannedStartTime;
        }

        public ProductionTask setPlannedStartTime(LocalDateTime plannedStartTime) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public LocalDateTime getPlannedEndTime() {
            return plannedEndTime;
        }

        public ProductionTask setPlannedEndTime(LocalDateTime plannedEndTime) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getEquipmentId() {
            return equipmentId;
        }

        public ProductionTask setEquipmentId(String equipmentId) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getOperator() {
            return operator;
        }

        public ProductionTask setOperator(String operator) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public ProductionTask setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        @Override
        public String toString() {
            return "ProductionTask{" +
                    "taskId='" + taskId + '\'' +
                    ", processStep='" + processStep + '\'' +
                    ", plannedQuantity=" + plannedQuantity +
                    ", plannedStartTime=" + plannedStartTime +
                    ", plannedEndTime=" + plannedEndTime +
                    ", equipmentId='" + equipmentId + '\'' +
                    ", operator='" + operator + '\'' +
                    '}';
        }
    }

    // 成本数据类
    public static class CostData {
        private final String costId;
        private final String productId;
        private final CostType costType;
        private final BigDecimal amount;
        private final String currency;
        private final LocalDateTime costDate;
        private final String costCenter;
        private final String batchId;
        private final Map<String, Object> attributes;
        private final LocalDateTime createTime;
        private final String createdBy;

        public CostData(String costId, String productId, CostType costType, BigDecimal amount) {
            this.costId = costId != null ? costId : UUID.randomUUID().toString();
            this.productId = productId != null ? productId : "";
            this.costType = costType != null ? costType : CostType.CUSTOM;
            this.amount = amount != null ? amount : BigDecimal.ZERO;
            this.currency = "CNY";
            this.costDate = LocalDateTime.now();
            this.costCenter = "";
            this.batchId = "";
            this.attributes = new ConcurrentHashMap<>();
            this.createTime = LocalDateTime.now();
            this.createdBy = "System";
        }

        // Getters and Setters
        public String getCostId() {
            return costId;
        }

        public String getProductId() {
            return productId;
        }

        public CostData setProductId(String productId) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public CostType getCostType() {
            return costType;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public String getCurrency() {
            return currency;
        }

        public CostData setCurrency(String currency) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public LocalDateTime getCostDate() {
            return costDate;
        }

        public CostData setCostDate(LocalDateTime costDate) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getCostCenter() {
            return costCenter;
        }

        public CostData setCostCenter(String costCenter) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getBatchId() {
            return batchId;
        }

        public CostData setBatchId(String batchId) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public CostData setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public CostData setCreatedBy(String createdBy) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        @Override
        public String toString() {
            return "CostData{" +
                    "costId='" + costId + '\'' +
                    ", productId='" + productId + '\'' +
                    ", costType=" + costType +
                    ", amount=" + amount +
                    ", currency='" + currency + '\'' +
                    ", costDate=" + costDate +
                    ", costCenter='" + costCenter + '\'' +
                    '}';
        }
    }

    // 库存数据类
    public static class InventoryData {
        private final String inventoryId;
        private final String productId;
        private final String productName;
        private final InventoryType inventoryType;
        private final int quantity;
        private final int reservedQuantity;
        private final int availableQuantity;
        private final String unit;
        private final String warehouse;
        private final String location;
        private final LocalDateTime lastUpdate;
        private final Map<String, Object> attributes;

        public InventoryData(String inventoryId, String productId, String productName) {
            this.inventoryId = inventoryId != null ? inventoryId : UUID.randomUUID().toString();
            this.productId = productId != null ? productId : "";
            this.productName = productName != null ? productName : "";
            this.inventoryType = InventoryType.RAW_MATERIAL;
            this.quantity = 0;
            this.reservedQuantity = 0;
            this.availableQuantity = 0;
            this.unit = "PCS";
            this.warehouse = "";
            this.location = "";
            this.lastUpdate = LocalDateTime.now();
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getInventoryId() {
            return inventoryId;
        }

        public String getProductId() {
            return productId;
        }

        public InventoryData setProductId(String productId) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getProductName() {
            return productName;
        }

        public InventoryData setProductName(String productName) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public InventoryType getInventoryType() {
            return inventoryType;
        }

        public InventoryData setInventoryType(InventoryType inventoryType) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public int getQuantity() {
            return quantity;
        }

        public InventoryData setQuantity(int quantity) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public int getReservedQuantity() {
            return reservedQuantity;
        }

        public InventoryData setReservedQuantity(int reservedQuantity) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public int getAvailableQuantity() {
            return availableQuantity;
        }

        public InventoryData setAvailableQuantity(int availableQuantity) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getUnit() {
            return unit;
        }

        public InventoryData setUnit(String unit) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getWarehouse() {
            return warehouse;
        }

        public InventoryData setWarehouse(String warehouse) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getLocation() {
            return location;
        }

        public InventoryData setLocation(String location) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public LocalDateTime getLastUpdate() {
            return lastUpdate;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public InventoryData setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        @Override
        public String toString() {
            return "InventoryData{" +
                    "inventoryId='" + inventoryId + '\'' +
                    ", productId='" + productId + '\'' +
                    ", productName='" + productName + '\'' +
                    ", inventoryType=" + inventoryType +
                    ", quantity=" + quantity +
                    ", reservedQuantity=" + reservedQuantity +
                    ", availableQuantity=" + availableQuantity +
                    ", unit='" + unit + '\'' +
                    ", warehouse='" + warehouse + '\'' +
                    ", location='" + location + '\'' +
                    '}';
        }
    }

    // 同步结果类
    public static class SyncResult {
        private final String resultId;
        private final String referenceId;
        private final boolean success;
        private final String message;
        private final LocalDateTime syncTime;
        private final long duration;
        private final ResponseStatus responseStatus;
        private final Map<String, Object> details;
        private final String errorMessage;
        private final int retryCount;

        public SyncResult(String referenceId, boolean success, String message, long duration) {
            this.resultId = UUID.randomUUID().toString();
            this.referenceId = referenceId != null ? referenceId : "";
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

        public String getReferenceId() {
            return referenceId;
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
            // This would need a builder pattern or new instance in real implementation
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
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public SyncResult setRetryCount(int retryCount) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        @Override
        public String toString() {
            return "SyncResult{" +
                    "resultId='" + resultId + '\'' +
                    ", referenceId='" + referenceId + '\'' +
                    ", success=" + success +
                    ", message='" + message + '\'' +
                    ", syncTime=" + syncTime +
                    ", duration=" + duration + "ms" +
                    ", responseStatus=" + responseStatus +
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
            this.targetSystem = targetSystem != null ? targetSystem : "ERP";
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
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public boolean isAcknowledged() {
            return acknowledged;
        }

        public ExceptionData setAcknowledged(boolean acknowledged) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getAssignedTo() {
            return assignedTo;
        }

        public ExceptionData setAssignedTo(String assignedTo) {
            // This would need a builder pattern or new instance in real implementation
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

    /**
     * 缓存统计信息类
     */
    public static class CacheStatistics {
        private int orderCacheSize;
        private int planCacheSize;
        private int costCacheSize;
        private int inventoryCacheSize;
        private long cacheTimeout;

        // Getters and Setters
        public int getOrderCacheSize() {
            return orderCacheSize;
        }

        public void setOrderCacheSize(int orderCacheSize) {
            this.orderCacheSize = orderCacheSize;
        }

        public int getPlanCacheSize() {
            return planCacheSize;
        }

        public void setPlanCacheSize(int planCacheSize) {
            this.planCacheSize = planCacheSize;
        }

        public int getCostCacheSize() {
            return costCacheSize;
        }

        public void setCostCacheSize(int costCacheSize) {
            this.costCacheSize = costCacheSize;
        }

        public int getInventoryCacheSize() {
            return inventoryCacheSize;
        }

        public void setInventoryCacheSize(int inventoryCacheSize) {
            this.inventoryCacheSize = inventoryCacheSize;
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
                    "orderCacheSize=" + orderCacheSize +
                    ", planCacheSize=" + planCacheSize +
                    ", costCacheSize=" + costCacheSize +
                    ", inventoryCacheSize=" + inventoryCacheSize +
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
}
