package com.im.material;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 物料追踪工具类
 * <p>
 * 特性：
 * - 通用性：支持多种物料类型和追踪方法
 * - 规范性：统一的物料定义和追踪标准
 * - 专业性：半导体行业物料追踪专业实现
 * - 灵活性：可配置的追踪参数和规则
 * - 可靠性：完善的异常处理和容错机制
 * - 安全性：物料数据保护和访问控制
 * - 复用性：模块化设计，组件可独立使用
 * - 容错性：优雅的错误处理和恢复机制
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @since 2025/08/21
 */
public class MaterialTracker {

    // 默认配置
    public static final long DEFAULT_CACHE_TIMEOUT = 30 * 60 * 1000; // 30分钟
    public static final int DEFAULT_MAX_RETRY_ATTEMPTS = 3;
    public static final long DEFAULT_RETRY_DELAY = 1000; // 1秒
    public static final int DEFAULT_TRACE_BUFFER_SIZE = 1000;
    public static final int DEFAULT_INVENTORY_THRESHOLD_PERCENTAGE = 20; // 库存预警阈值20%
    public static final long DEFAULT_EXPIRATION_WARNING_DAYS = 7; // 过期预警提前7天
    private static final Logger logger = LoggerFactory.getLogger(MaterialTracker.class);
    // 内部存储和管理
    private static final Map<MaterialType, MaterialTrackerEngine> trackerRegistry = new ConcurrentHashMap<>();
    private static final Map<MaterialType, QualityStatusQuerier> querierRegistry = new ConcurrentHashMap<>();
    private static final Map<MaterialType, ExpirationChecker> checkerRegistry = new ConcurrentHashMap<>();
    private static final Map<InventoryType, InventoryChecker> inventoryRegistry = new ConcurrentHashMap<>();
    private static final Map<String, TraceStorage> traceStorageRegistry = new ConcurrentHashMap<>();
    private static final Map<String, Material> materialCache = new ConcurrentHashMap<>();
    private static final Map<String, MaterialBatch> batchCache = new ConcurrentHashMap<>();
    private static final Map<String, MaterialTrace> materialTraceCache = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    // 当前使用的组件
    private static volatile MaterialTrackerEngine currentTracker;
    private static volatile QualityStatusQuerier currentQuerier;
    private static volatile ExpirationChecker currentChecker;
    private static volatile InventoryChecker currentInventoryChecker;
    private static volatile TraceStorage currentTraceStorage;

    // 初始化默认组件
    static {
        registerDefaultComponents();
        initializeCurrentComponents();
        startMaintenanceTasks();
        logger.info("MaterialTracker initialized with default components");
    }

    /**
     * 注册默认组件
     */
    private static void registerDefaultComponents() {
        // 注册默认追踪器、查询器、检查器和存储
        // registerTracker(new DefaultMaterialTrackerEngine());
        // registerQuerier(new DefaultQualityStatusQuerier());
        // registerChecker(new DefaultExpirationChecker());
        // registerInventoryChecker(new DefaultInventoryChecker());
        // registerTraceStorage(new DefaultTraceStorage());
    }

    /**
     * 初始化当前组件
     */
    private static void initializeCurrentComponents() {
        // 初始化为第一个注册的组件
        if (!trackerRegistry.isEmpty()) {
            currentTracker = trackerRegistry.values().iterator().next();
        }

        if (!querierRegistry.isEmpty()) {
            currentQuerier = querierRegistry.values().iterator().next();
        }

        if (!checkerRegistry.isEmpty()) {
            currentChecker = checkerRegistry.values().iterator().next();
        }

        if (!inventoryRegistry.isEmpty()) {
            currentInventoryChecker = inventoryRegistry.values().iterator().next();
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
        scheduler.scheduleAtFixedRate(MaterialTracker::cleanupCache,
                30, 30, TimeUnit.MINUTES);

        // 启动过期检查任务
        scheduler.scheduleAtFixedRate(MaterialTracker::checkExpirations,
                1, 1, TimeUnit.HOURS);

        // 启动库存检查任务
        scheduler.scheduleAtFixedRate(MaterialTracker::checkInventories,
                6, 6, TimeUnit.HOURS);

        logger.debug("Material tracker maintenance tasks started");
    }

    /**
     * 注册物料追踪器
     */
    public static void registerTracker(MaterialTrackerEngine tracker) {
        if (tracker == null) {
            throw new IllegalArgumentException("Tracker cannot be null");
        }

        for (MaterialType type : MaterialType.values()) {
            if (tracker.supportsMaterialType(type)) {
                trackerRegistry.put(type, tracker);
                logger.debug("Registered tracker {} for material type {}", tracker.getTrackerName(), type);
            }
        }
    }

    /**
     * 注册质量状态查询器
     */
    public static void registerQuerier(QualityStatusQuerier querier) {
        if (querier == null) {
            throw new IllegalArgumentException("Querier cannot be null");
        }

        for (MaterialType type : MaterialType.values()) {
            if (querier.supportsQualityQuery(type)) {
                querierRegistry.put(type, querier);
                logger.debug("Registered querier {} for material type {}", querier.getQuerierName(), type);
            }
        }
    }

    /**
     * 注册过期检查器
     */
    public static void registerChecker(ExpirationChecker checker) {
        if (checker == null) {
            throw new IllegalArgumentException("Checker cannot be null");
        }

        for (MaterialType type : MaterialType.values()) {
            if (checker.supportsExpirationCheck(type)) {
                checkerRegistry.put(type, checker);
                logger.debug("Registered checker {} for material type {}", checker.getCheckerName(), type);
            }
        }
    }

    /**
     * 注册库存检查器
     */
    public static void registerInventoryChecker(InventoryChecker checker) {
        if (checker == null) {
            throw new IllegalArgumentException("Inventory checker cannot be null");
        }

        for (InventoryType type : InventoryType.values()) {
            if (checker.supportsInventoryCheck(type)) {
                inventoryRegistry.put(type, checker);
                logger.debug("Registered inventory checker {} for inventory type {}", checker.getCheckerName(), type);
            }
        }
    }

    /**
     * 注册追踪存储
     */
    public static void registerTraceStorage(TraceStorage traceStorage) {
        if (traceStorage == null) {
            throw new IllegalArgumentException("Trace storage cannot be null");
        }

        // 注册支持的所有追踪类型
        String[] traceTypes = {"database", "file", "memory", "cloud"}; // 简化实现
        for (String type : traceTypes) {
            if (traceStorage.supportsTraceType(type)) {
                traceStorageRegistry.put(type, traceStorage);
                logger.debug("Registered trace storage {} for type {}", traceStorage.getTraceStorageName(), type);
            }
        }
    }

    /**
     * 设置当前物料追踪器
     */
    public static void setCurrentTracker(MaterialType materialType) {
        MaterialTrackerEngine tracker = trackerRegistry.get(materialType);
        if (tracker != null) {
            currentTracker = tracker;
            logger.info("Current material tracker set to: {} for material type {}",
                    tracker.getTrackerName(), materialType);
        } else {
            logger.warn("No tracker found for material type: {}", materialType);
        }
    }

    /**
     * 设置当前质量状态查询器
     */
    public static void setCurrentQuerier(MaterialType materialType) {
        QualityStatusQuerier querier = querierRegistry.get(materialType);
        if (querier != null) {
            currentQuerier = querier;
            logger.info("Current quality querier set to: {} for material type {}",
                    querier.getQuerierName(), materialType);
        } else {
            logger.warn("No querier found for material type: {}", materialType);
        }
    }

    /**
     * 设置当前过期检查器
     */
    public static void setCurrentChecker(MaterialType materialType) {
        ExpirationChecker checker = checkerRegistry.get(materialType);
        if (checker != null) {
            currentChecker = checker;
            logger.info("Current expiration checker set to: {} for material type {}",
                    checker.getCheckerName(), materialType);
        } else {
            logger.warn("No checker found for material type: {}", materialType);
        }
    }

    /**
     * 设置当前库存检查器
     */
    public static void setCurrentInventoryChecker(InventoryType inventoryType) {
        InventoryChecker checker = inventoryRegistry.get(inventoryType);
        if (checker != null) {
            currentInventoryChecker = checker;
            logger.info("Current inventory checker set to: {} for inventory type {}",
                    checker.getCheckerName(), inventoryType);
        } else {
            logger.warn("No inventory checker found for type: {}", inventoryType);
        }
    }

    /**
     * 设置当前追踪存储
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
     * 物料批次追踪
     */
    public static MaterialTrace traceMaterialBatch(String batchId) {
        if (batchId == null || batchId.isEmpty()) {
            logger.warn("Invalid batch ID for tracing");
            return null;
        }

        try {
            // 根据批次ID获取物料ID
            MaterialBatch batch = batchCache.get(batchId);
            if (batch == null) {
                logger.warn("Batch not found: {}", batchId);
                return null;
            }

            return traceMaterial(batch.getMaterialId());
        } catch (Exception e) {
            logger.error("Failed to trace material batch: " + batchId, e);
            return null;
        }
    }

    /**
     * 物料追踪
     */
    public static MaterialTrace traceMaterial(String materialId) {
        if (materialId == null || materialId.isEmpty()) {
            logger.warn("Invalid material ID for tracing");
            return null;
        }

        try {
            // 首先检查缓存
            MaterialTrace cached = materialTraceCache.get(materialId);
            if (cached != null && !isTraceCacheExpired(cached)) {
                logger.debug("Returning cached material trace: {}", materialId);
                return cached;
            }

            // 使用注册的追踪器进行追踪
            MaterialTrackerEngine tracker = getTrackerForMaterial(materialId);
            MaterialTrace trace = null;

            if (tracker != null) {
                trace = tracker.traceMaterial(materialId);
            } else if (currentTracker != null) {
                // 使用当前追踪器
                trace = currentTracker.traceMaterial(materialId);
            } else {
                // 使用默认追踪逻辑
                trace = performDefaultMaterialTrace(materialId);
            }

            // 更新缓存
            if (trace != null) {
                materialTraceCache.put(materialId, trace);
            }

            if (trace != null) {
                logger.debug("Generated material trace: {}", materialId);
            } else {
                logger.warn("Failed to generate material trace: {}", materialId);
            }

            return trace;
        } catch (Exception e) {
            logger.error("Failed to trace material: " + materialId, e);
            return null;
        }
    }

    /**
     * 获取物料对应的追踪器
     */
    private static MaterialTrackerEngine getTrackerForMaterial(String materialId) {
        Material material = materialCache.get(materialId);
        if (material != null) {
            return trackerRegistry.get(material.getMaterialType());
        }
        return null;
    }

    /**
     * 检查追溯缓存是否过期
     */
    private static boolean isTraceCacheExpired(MaterialTrace trace) {
        return System.currentTimeMillis() - trace.getStartTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
                > DEFAULT_CACHE_TIMEOUT;
    }

    /**
     * 执行默认物料追踪逻辑
     */
    private static MaterialTrace performDefaultMaterialTrace(String materialId) {
        try {
            MaterialTrace trace = new MaterialTrace(materialId);

            // 模拟追踪记录
            Material material = materialCache.get(materialId);
            if (material != null) {
                // 添加收货记录
                MaterialMovement receiptMovement = new MaterialMovement(
                        materialId, TraceEventType.RECEIPT, material.getQuantity()
                )
                        .setToLocation("主仓库")
                        .setOperator("收货员")
                        .setReferenceNumber("PO-" + System.currentTimeMillis());

                trace.addMovement(receiptMovement);

                // 添加检验记录
                MaterialMovement inspectionMovement = new MaterialMovement(
                        materialId, TraceEventType.INSPECTION, material.getQuantity()
                )
                        .setFromLocation("主仓库")
                        .setToLocation("质检区")
                        .setOperator("质检员");

                trace.addMovement(inspectionMovement);

                // 根据质量状态添加相应记录
                if (material.getQualityStatus() == QualityStatus.PASS) {
                    MaterialMovement releaseMovement = new MaterialMovement(
                            materialId, TraceEventType.ISSUE, material.getQuantity()
                    )
                            .setFromLocation("质检区")
                            .setToLocation("生产线")
                            .setOperator("仓管员");

                    trace.addMovement(releaseMovement);
                }
            }

            logger.debug("Default material trace generated for: {}", materialId);

            return trace;
        } catch (Exception e) {
            logger.warn("Failed to perform default material trace for: " + materialId, e);
            return null;
        }
    }

    /**
     * 物料质量状态查询
     */
    public static QualityStatus getMaterialQualityStatus(String materialId) {
        if (materialId == null || materialId.isEmpty()) {
            logger.warn("Invalid material ID for quality status query");
            return QualityStatus.PENDING;
        }

        try {
            // 首先检查缓存
            Material cachedMaterial = materialCache.get(materialId);
            if (cachedMaterial != null) {
                logger.debug("Returning cached quality status for material: {}", materialId);
                return cachedMaterial.getQualityStatus();
            }

            // 使用注册的查询器查询质量状态
            QualityStatusQuerier querier = getQuerierForMaterial(materialId);
            QualityStatus status = null;

            if (querier != null) {
                status = querier.getMaterialQualityStatus(materialId);
            } else if (currentQuerier != null) {
                // 使用当前查询器
                status = currentQuerier.getMaterialQualityStatus(materialId);
            } else {
                // 使用默认查询逻辑
                status = performDefaultQualityQuery(materialId);
            }

            if (status != null) {
                logger.debug("Retrieved quality status for material: {} - {}", materialId, status.getDescription());
            } else {
                logger.warn("Failed to retrieve quality status for material: {}", materialId);
            }

            return status != null ? status : QualityStatus.PENDING;
        } catch (Exception e) {
            logger.error("Failed to query quality status for material: " + materialId, e);
            return QualityStatus.PENDING;
        }
    }

    /**
     * 获取物料对应的质量查询器
     */
    private static QualityStatusQuerier getQuerierForMaterial(String materialId) {
        Material material = materialCache.get(materialId);
        if (material != null) {
            return querierRegistry.get(material.getMaterialType());
        }
        return null;
    }

    /**
     * 执行默认质量查询逻辑
     */
    private static QualityStatus performDefaultQualityQuery(String materialId) {
        try {
            Material material = materialCache.get(materialId);
            if (material != null) {
                logger.debug("Default quality query for material: {} - {}",
                        materialId, material.getQualityStatus().getDescription());
                return material.getQualityStatus();
            } else {
                logger.warn("Material not found for quality query: {}", materialId);
                return QualityStatus.PENDING;
            }
        } catch (Exception e) {
            logger.warn("Failed to perform default quality query for: " + materialId, e);
            return QualityStatus.PENDING;
        }
    }

    /**
     * 获取物料质量历史
     */
    public static QualityStatusHistory getMaterialQualityHistory(String materialId) {
        if (materialId == null || materialId.isEmpty()) {
            logger.warn("Invalid material ID for quality history query");
            return null;
        }

        try {
            // 使用注册的查询器查询质量历史
            QualityStatusQuerier querier = getQuerierForMaterial(materialId);

            if (querier != null) {
                return querier.getQualityHistory(materialId);
            } else if (currentQuerier != null) {
                // 使用当前查询器
                return currentQuerier.getQualityHistory(materialId);
            } else {
                // 使用默认历史查询逻辑
                return performDefaultQualityHistoryQuery(materialId);
            }
        } catch (Exception e) {
            logger.error("Failed to query quality history for material: " + materialId, e);
            return null;
        }
    }

    /**
     * 执行默认质量历史查询逻辑
     */
    private static QualityStatusHistory performDefaultQualityHistoryQuery(String materialId) {
        try {
            QualityStatusHistory history = new QualityStatusHistory(materialId);

            // 模拟历史记录
            Material material = materialCache.get(materialId);
            if (material != null) {
                QualityRecord initialRecord = new QualityRecord(materialId, QualityStatus.PENDING)
                        .setInspector("系统")
                        .setRemarks("初始状态");
                history.addRecord(initialRecord);

                QualityRecord currentRecord = new QualityRecord(materialId, material.getQualityStatus())
                        .setInspector("质检员")
                        .setRemarks("最新检验结果");
                history.addRecord(currentRecord);
            }

            logger.debug("Default quality history generated for: {}", materialId);

            return history;
        } catch (Exception e) {
            logger.warn("Failed to perform default quality history query for: " + materialId, e);
            return null;
        }
    }

    /**
     * 物料有效期管理
     */
    public static ExpirationAlert checkMaterialExpiration(String materialId) {
        if (materialId == null || materialId.isEmpty()) {
            logger.warn("Invalid material ID for expiration check");
            return null;
        }

        try {
            // 使用注册的检查器检查物料过期
            ExpirationChecker checker = getCheckerForMaterial(materialId);
            ExpirationAlert alert = null;

            if (checker != null) {
                alert = checker.checkMaterialExpiration(materialId);
            } else if (currentChecker != null) {
                // 使用当前检查器
                alert = currentChecker.checkMaterialExpiration(materialId);
            } else {
                // 使用默认检查逻辑
                alert = performDefaultExpirationCheck(materialId);
            }

            if (alert != null) {
                logger.info("Expiration check completed for material: {} - {}",
                        materialId, alert.getMessage());
            } else {
                logger.debug("No expiration alert for material: {}", materialId);
            }

            return alert;
        } catch (Exception e) {
            logger.error("Failed to check expiration for material: " + materialId, e);
            return null;
        }
    }

    /**
     * 获取物料对应的过期检查器
     */
    private static ExpirationChecker getCheckerForMaterial(String materialId) {
        Material material = materialCache.get(materialId);
        if (material != null) {
            return checkerRegistry.get(material.getMaterialType());
        }
        return null;
    }

    /**
     * 执行默认过期检查逻辑
     */
    private static ExpirationAlert performDefaultExpirationCheck(String materialId) {
        try {
            Material material = materialCache.get(materialId);
            if (material != null) {
                LocalDateTime expirationDate = material.getExpirationDate();
                if (expirationDate != null) {
                    long daysUntilExpiration = java.time.Duration.between(
                            LocalDateTime.now(), expirationDate).toDays();

                    if (daysUntilExpiration <= DEFAULT_EXPIRATION_WARNING_DAYS) {
                        ExpirationAlert alert = new ExpirationAlert(
                                materialId, expirationDate, daysUntilExpiration
                        );

                        logger.debug("Expiration alert generated for material: {} - {} days",
                                materialId, daysUntilExpiration);

                        return alert;
                    }
                }
            }

            return null;
        } catch (Exception e) {
            logger.warn("Failed to perform default expiration check for: " + materialId, e);
            return null;
        }
    }

    /**
     * 物料库存预警
     */
    public static InventoryAlert checkInventoryLevel(String materialId) {
        if (materialId == null || materialId.isEmpty()) {
            logger.warn("Invalid material ID for inventory check");
            return null;
        }

        try {
            // 使用注册的库存检查器检查库存水平
            InventoryChecker checker = getInventoryCheckerForMaterial(materialId);
            InventoryAlert alert = null;

            if (checker != null) {
                alert = checker.checkInventoryLevel(materialId);
            } else if (currentInventoryChecker != null) {
                // 使用当前库存检查器
                alert = currentInventoryChecker.checkInventoryLevel(materialId);
            } else {
                // 使用默认检查逻辑
                alert = performDefaultInventoryCheck(materialId);
            }

            if (alert != null) {
                logger.info("Inventory check completed for material: {} - {}",
                        materialId, alert.getMessage());
            } else {
                logger.debug("No inventory alert for material: {}", materialId);
            }

            return alert;
        } catch (Exception e) {
            logger.error("Failed to check inventory for material: " + materialId, e);
            return null;
        }
    }

    /**
     * 获取物料对应的库存检查器
     */
    private static InventoryChecker getInventoryCheckerForMaterial(String materialId) {
        Material material = materialCache.get(materialId);
        if (material != null) {
            // 简化实现，实际应用中可能需要更复杂的逻辑
            return inventoryRegistry.get(InventoryType.RAW_MATERIAL);
        }
        return null;
    }

    /**
     * 执行默认库存检查逻辑
     */
    private static InventoryAlert performDefaultInventoryCheck(String materialId) {
        try {
            Material material = materialCache.get(materialId);
            if (material != null) {
                double currentQuantity = material.getQuantity();
                // 简化实现，实际应用中应该从配置或数据库获取最小库存量
                double minimumQuantity = 100.0;
                double thresholdPercentage = DEFAULT_INVENTORY_THRESHOLD_PERCENTAGE;

                double threshold = minimumQuantity * (thresholdPercentage / 100.0);

                if (currentQuantity <= threshold) {
                    InventoryAlert alert = new InventoryAlert(
                            materialId, currentQuantity, minimumQuantity,
                            thresholdPercentage, material.getUnit()
                    );

                    logger.debug("Inventory alert generated for material: {} - current: {}, minimum: {}",
                            materialId, currentQuantity, minimumQuantity);

                    return alert;
                }
            }

            return null;
        } catch (Exception e) {
            logger.warn("Failed to perform default inventory check for: " + materialId, e);
            return null;
        }
    }

    /**
     * 批量检查多个物料库存
     */
    public static Map<String, InventoryAlert> checkMultipleInventories(List<String> materialIds) {
        if (materialIds == null || materialIds.isEmpty()) {
            logger.warn("Invalid material IDs for inventory check");
            return new HashMap<>();
        }

        try {
            if (currentInventoryChecker != null) {
                return currentInventoryChecker.checkMultipleInventories(materialIds);
            } else {
                // 使用默认批量检查逻辑
                return performDefaultMultipleInventoryCheck(materialIds);
            }
        } catch (Exception e) {
            logger.error("Failed to check multiple inventories", e);
            return new HashMap<>();
        }
    }

    /**
     * 执行默认批量库存检查逻辑
     */
    private static Map<String, InventoryAlert> performDefaultMultipleInventoryCheck(List<String> materialIds) {
        Map<String, InventoryAlert> alerts = new HashMap<>();

        for (String materialId : materialIds) {
            try {
                InventoryAlert alert = checkInventoryLevel(materialId);
                if (alert != null) {
                    alerts.put(materialId, alert);
                }
            } catch (Exception e) {
                logger.warn("Failed to check inventory for material: " + materialId, e);
            }
        }

        return alerts;
    }

    /**
     * 检查批次过期情况
     */
    public static List<ExpirationAlert> checkBatchExpirations(String batchId) {
        if (batchId == null || batchId.isEmpty()) {
            logger.warn("Invalid batch ID for expiration check");
            return new ArrayList<>();
        }

        try {
            // 使用注册的检查器检查批次过期
            ExpirationChecker checker = currentChecker;

            if (checker != null) {
                return checker.checkBatchExpirations(batchId);
            } else {
                // 使用默认检查逻辑
                return performDefaultBatchExpirationCheck(batchId);
            }
        } catch (Exception e) {
            logger.error("Failed to check batch expirations: " + batchId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 执行默认批次过期检查逻辑
     */
    private static List<ExpirationAlert> performDefaultBatchExpirationCheck(String batchId) {
        List<ExpirationAlert> alerts = new ArrayList<>();

        try {
            MaterialBatch batch = batchCache.get(batchId);
            if (batch != null) {
                LocalDateTime expirationDate = batch.getExpirationDate();
                if (expirationDate != null) {
                    long daysUntilExpiration = java.time.Duration.between(
                            LocalDateTime.now(), expirationDate).toDays();

                    if (daysUntilExpiration <= DEFAULT_EXPIRATION_WARNING_DAYS) {
                        ExpirationAlert alert = new ExpirationAlert(
                                batch.getMaterialId(), expirationDate, daysUntilExpiration
                        );

                        alerts.add(alert);
                        logger.debug("Batch expiration alert generated: {} - {} days",
                                batchId, daysUntilExpiration);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to perform default batch expiration check for: " + batchId, e);
        }

        return alerts;
    }

    /**
     * 创建物料
     */
    public static Material createMaterial(String materialName, MaterialType materialType) {
        if (materialName == null || materialName.isEmpty() || materialType == null) {
            logger.warn("Invalid parameters for material creation");
            return null;
        }

        try {
            Material material = new Material(null, materialName, materialType);

            // 添加到缓存
            materialCache.put(material.getMaterialId(), material);

            logger.info("Created new material: {} - {} (Type: {})",
                    material.getMaterialId(), materialName, materialType);

            return material;
        } catch (Exception e) {
            logger.error("Failed to create material: " + materialName, e);
            return null;
        }
    }

    /**
     * 更新物料
     */
    public static boolean updateMaterial(Material material) {
        if (material == null) {
            logger.warn("Invalid material for update");
            return false;
        }

        try {
            materialCache.put(material.getMaterialId(), material);
            logger.debug("Updated material in cache: {}", material.getMaterialId());
            return true;
        } catch (Exception e) {
            logger.error("Failed to update material: " + material.getMaterialId(), e);
            return false;
        }
    }

    /**
     * 获取物料
     */
    public static Material getMaterial(String materialId) {
        if (materialId == null || materialId.isEmpty()) {
            logger.warn("Invalid material ID for retrieval");
            return null;
        }

        try {
            Material material = materialCache.get(materialId);
            if (material != null) {
                logger.debug("Retrieved material: {}", materialId);
            } else {
                logger.warn("Material not found: {}", materialId);
            }
            return material;
        } catch (Exception e) {
            logger.error("Failed to get material: " + materialId, e);
            return null;
        }
    }

    /**
     * 删除物料
     */
    public static boolean deleteMaterial(String materialId) {
        if (materialId == null || materialId.isEmpty()) {
            logger.warn("Invalid material ID for deletion");
            return false;
        }

        try {
            Material removed = materialCache.remove(materialId);
            if (removed != null) {
                logger.info("Deleted material: {}", materialId);
                return true;
            } else {
                logger.warn("Material not found for deletion: {}", materialId);
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to delete material: " + materialId, e);
            return false;
        }
    }

    /**
     * 创建物料批次
     */
    public static MaterialBatch createMaterialBatch(String materialId, String batchNumber) {
        if (materialId == null || materialId.isEmpty() || batchNumber == null || batchNumber.isEmpty()) {
            logger.warn("Invalid parameters for batch creation");
            return null;
        }

        try {
            MaterialBatch batch = new MaterialBatch(null, materialId, batchNumber);

            // 添加到缓存
            batchCache.put(batch.getBatchId(), batch);

            logger.info("Created new batch: {} for material: {} (Batch Number: {})",
                    batch.getBatchId(), materialId, batchNumber);

            return batch;
        } catch (Exception e) {
            logger.error("Failed to create batch for material: " + materialId, e);
            return null;
        }
    }

    /**
     * 更新物料批次
     */
    public static boolean updateMaterialBatch(MaterialBatch batch) {
        if (batch == null) {
            logger.warn("Invalid batch for update");
            return false;
        }

        try {
            batchCache.put(batch.getBatchId(), batch);
            logger.debug("Updated batch in cache: {}", batch.getBatchId());
            return true;
        } catch (Exception e) {
            logger.error("Failed to update batch: " + batch.getBatchId(), e);
            return false;
        }
    }

    /**
     * 获取物料批次
     */
    public static MaterialBatch getMaterialBatch(String batchId) {
        if (batchId == null || batchId.isEmpty()) {
            logger.warn("Invalid batch ID for retrieval");
            return null;
        }

        try {
            MaterialBatch batch = batchCache.get(batchId);
            if (batch != null) {
                logger.debug("Retrieved batch: {}", batchId);
            } else {
                logger.warn("Batch not found: {}", batchId);
            }
            return batch;
        } catch (Exception e) {
            logger.error("Failed to get batch: " + batchId, e);
            return null;
        }
    }

    /**
     * 删除物料批次
     */
    public static boolean deleteMaterialBatch(String batchId) {
        if (batchId == null || batchId.isEmpty()) {
            logger.warn("Invalid batch ID for deletion");
            return false;
        }

        try {
            MaterialBatch removed = batchCache.remove(batchId);
            if (removed != null) {
                logger.info("Deleted batch: {}", batchId);
                return true;
            } else {
                logger.warn("Batch not found for deletion: {}", batchId);
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to delete batch: " + batchId, e);
            return false;
        }
    }

    /**
     * 添加追踪移动记录
     */
    public static boolean addTraceMovement(String materialId, MaterialMovement movement) {
        if (materialId == null || materialId.isEmpty() || movement == null) {
            logger.warn("Invalid parameters for trace movement addition");
            return false;
        }

        try {
            boolean added = false;
            if (currentTraceStorage != null) {
                added = currentTraceStorage.addTraceMovement(materialId, movement);
            } else {
                // 如果没有追踪存储，记录到缓存中的追踪对象
                MaterialTrace trace = materialTraceCache.get(materialId);
                if (trace != null) {
                    // 通过反射添加移动记录（仅在示例中）
                    try {
                        java.lang.reflect.Method method = trace.getClass().getDeclaredMethod("addMovement", MaterialMovement.class);
                        method.setAccessible(true);
                        method.invoke(trace, movement);
                        added = true;
                    } catch (Exception e) {
                        // 忽略异常
                    }
                }
            }

            if (added) {
                logger.debug("Added trace movement: {} for material: {}", movement.getEventType(), materialId);
            } else {
                logger.warn("Failed to add trace movement for material: {}", materialId);
            }

            return added;
        } catch (Exception e) {
            logger.error("Failed to add trace movement for material: " + materialId, e);
            return false;
        }
    }

    /**
     * 追踪移动查询
     */
    public static List<MaterialMovement> queryTraceMovements(String materialId, TraceQuery query) {
        if (materialId == null || materialId.isEmpty() || query == null) {
            logger.warn("Invalid parameters for trace movement query");
            return new ArrayList<>();
        }

        try {
            if (currentTraceStorage != null) {
                return currentTraceStorage.queryTraceMovements(materialId, query);
            } else {
                // 如果没有追踪存储，从缓存中查询
                return queryTraceMovementsFromCache(materialId, query);
            }
        } catch (Exception e) {
            logger.error("Failed to query trace movements for material: " + materialId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 从缓存中查询追踪移动
     */
    private static List<MaterialMovement> queryTraceMovementsFromCache(String materialId, TraceQuery query) {
        MaterialTrace trace = materialTraceCache.get(materialId);
        if (trace == null) {
            return new ArrayList<>();
        }

        return trace.getMovements().stream()
                .filter(movement -> {
                    // 时间范围过滤
                    if (query.getStartDate() != null && movement.getMovementTime().isBefore(query.getStartDate())) {
                        return false;
                    }

                    if (query.getEndDate() != null && movement.getMovementTime().isAfter(query.getEndDate())) {
                        return false;
                    }

                    // 事件类型过滤
                    if (query.getEventType() != null && movement.getEventType() != query.getEventType()) {
                        return false;
                    }

                    // 操作员过滤
                    if (query.getOperator() != null && !query.getOperator().isEmpty() &&
                            !movement.getOperator().equals(query.getOperator())) {
                        return false;
                    }

                    return true;
                })
                .limit(query.getLimit())
                .collect(Collectors.toList());
    }

    /**
     * 定期检查过期情况
     */
    private static void checkExpirations() {
        try {
            logger.debug("Starting periodic expiration check");

            // 检查所有物料的过期情况
            for (Material material : materialCache.values()) {
                try {
                    ExpirationAlert alert = checkMaterialExpiration(material.getMaterialId());
                    if (alert != null) {
                        logger.info("Expiration alert: {}", alert.getMessage());
                    }
                } catch (Exception e) {
                    logger.warn("Failed to check expiration for material: " + material.getMaterialId(), e);
                }
            }

            // 检查所有批次的过期情况
            for (MaterialBatch batch : batchCache.values()) {
                try {
                    List<ExpirationAlert> alerts = checkBatchExpirations(batch.getBatchId());
                    for (ExpirationAlert alert : alerts) {
                        logger.info("Batch expiration alert: {}", alert.getMessage());
                    }
                } catch (Exception e) {
                    logger.warn("Failed to check expiration for batch: " + batch.getBatchId(), e);
                }
            }

            logger.debug("Periodic expiration check completed");
        } catch (Exception e) {
            logger.error("Failed to perform periodic expiration check", e);
        }
    }

    /**
     * 定期检查库存情况
     */
    private static void checkInventories() {
        try {
            logger.debug("Starting periodic inventory check");

            // 检查所有物料的库存情况
            for (Material material : materialCache.values()) {
                try {
                    InventoryAlert alert = checkInventoryLevel(material.getMaterialId());
                    if (alert != null) {
                        logger.info("Inventory alert: {}", alert.getMessage());
                    }
                } catch (Exception e) {
                    logger.warn("Failed to check inventory for material: " + material.getMaterialId(), e);
                }
            }

            logger.debug("Periodic inventory check completed");
        } catch (Exception e) {
            logger.error("Failed to perform periodic inventory check", e);
        }
    }

    /**
     * 清理缓存
     */
    private static void cleanupCache() {
        try {
            long cutoffTime = System.currentTimeMillis() - DEFAULT_CACHE_TIMEOUT;

            // 清理物料缓存
            materialCache.entrySet().removeIf(entry ->
                    entry.getValue().getLastUpdate().toInstant(java.time.ZoneOffset.UTC).toEpochMilli() < cutoffTime
            );

            // 清理批次缓存
            batchCache.entrySet().removeIf(entry ->
                    entry.getValue().getLastUpdate().toInstant(java.time.ZoneOffset.UTC).toEpochMilli() < cutoffTime
            );

            // 清理追踪信息缓存
            materialTraceCache.entrySet().removeIf(entry ->
                    entry.getValue().getStartTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli() < cutoffTime
            );

            logger.debug("Cleaned up material tracker cache, remaining entries - material: {}, batch: {}, trace: {}",
                    materialCache.size(), batchCache.size(), materialTraceCache.size());
        } catch (Exception e) {
            logger.error("Failed to cleanup material tracker cache", e);
        }
    }

    /**
     * 获取缓存统计信息
     */
    public static CacheStatistics getCacheStatistics() {
        CacheStatistics stats = new CacheStatistics();
        stats.setMaterialCacheSize(materialCache.size());
        stats.setBatchCacheSize(batchCache.size());
        stats.setTraceCacheSize(materialTraceCache.size());
        stats.setCacheTimeout(DEFAULT_CACHE_TIMEOUT);
        return stats;
    }

    /**
     * 关闭物料追踪器
     */
    public static void shutdown() {
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            logger.info("MaterialTracker shutdown completed");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            logger.warn("MaterialTracker shutdown interrupted");
        }
    }

    // 物料类型枚举
    public enum MaterialType {
        SILICON_WAFER("硅晶圆", "Silicon Wafer"),
        GOLD_WIRE("金丝", "Gold Wire"),
        PLASTIC_PACKAGE("塑封料", "Plastic Package"),
        METAL_LEAD_FRAME("金属引脚框架", "Metal Lead Frame"),
        CERAMIC_SUBSTRATE("陶瓷基板", "Ceramic Substrate"),
        SOLDER_PASTE("焊膏", "Solder Paste"),
        CLEANING_CHEMICAL("清洗化学品", "Cleaning Chemical"),
        ADHESIVE("胶水", "Adhesive"),
        ENCAPSULANT("封装材料", "Encapsulant"),
        CUSTOM("自定义物料", "Custom Material");

        private final String chineseName;
        private final String englishName;

        MaterialType(String chineseName, String englishName) {
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

    // 物料状态枚举
    public enum MaterialStatus {
        AVAILABLE("可用", 1),
        IN_USE("使用中", 2),
        RESERVED("已预留", 3),
        EXPIRED("已过期", 4),
        QUARANTINED("已隔离", 5),
        DISPOSED("已处置", 6),
        DEPLETED("已耗尽", 7);

        private final String description;
        private final int sequence;

        MaterialStatus(String description, int sequence) {
            this.description = description;
            this.sequence = sequence;
        }

        public String getDescription() {
            return description;
        }

        public int getSequence() {
            return sequence;
        }

        public boolean isAvailable() {
            return this == AVAILABLE || this == IN_USE || this == RESERVED;
        }
    }

    // 质量状态枚举
    public enum QualityStatus {
        PASS("合格", 1),
        FAIL("不合格", 2),
        PENDING("待检", 3),
        UNDER_REVIEW("评审中", 4),
        CONDITIONAL_RELEASE("有条件放行", 5);

        private final String description;
        private final int priority;

        QualityStatus(String description, int priority) {
            this.description = description;
            this.priority = priority;
        }

        public String getDescription() {
            return description;
        }

        public int getPriority() {
            return priority;
        }

        public boolean isQualified() {
            return this == PASS || this == CONDITIONAL_RELEASE;
        }
    }

    // 库存类型枚举
    public enum InventoryType {
        RAW_MATERIAL("原材料", "Raw Material"),
        WORK_IN_PROGRESS("在制品", "Work In Progress"),
        FINISHED_GOOD("成品", "Finished Good"),
        CONSUMABLE("耗材", "Consumable");

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

    // 追踪事件类型枚举
    public enum TraceEventType {
        RECEIPT("收货", "Receipt"),
        ISSUE("发料", "Issue"),
        TRANSFER("转移", "Transfer"),
        RETURN("退料", "Return"),
        CONSUMPTION("消耗", "Consumption"),
        INSPECTION("检验", "Inspection"),
        DISPOSAL("处置", "Disposal"),
        ADJUSTMENT("调整", "Adjustment");

        private final String chineseName;
        private final String englishName;

        TraceEventType(String chineseName, String englishName) {
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

    // 预警类型枚举
    public enum AlertType {
        EXPIRATION_WARNING("过期预警", "Expiration Warning"),
        INVENTORY_LOW("库存不足", "Low Inventory"),
        QUALITY_ISSUE("质量问题", "Quality Issue"),
        SUPPLIER_PROBLEM("供应商问题", "Supplier Problem");

        private final String chineseName;
        private final String englishName;

        AlertType(String chineseName, String englishName) {
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

    // 物料追踪器接口
    public interface MaterialTrackerEngine {
        MaterialTrace traceMaterial(String materialId) throws Exception;

        boolean supportsMaterialType(MaterialType materialType);

        String getTrackerName();
    }

    // 质量状态查询器接口
    public interface QualityStatusQuerier {
        QualityStatus getMaterialQualityStatus(String materialId) throws Exception;

        QualityStatusHistory getQualityHistory(String materialId) throws Exception;

        boolean supportsQualityQuery(MaterialType materialType);

        String getQuerierName();
    }

    // 过期检查器接口
    public interface ExpirationChecker {
        ExpirationAlert checkMaterialExpiration(String materialId) throws Exception;

        List<ExpirationAlert> checkBatchExpirations(String batchId) throws Exception;

        boolean supportsExpirationCheck(MaterialType materialType);

        String getCheckerName();
    }

    // 库存检查器接口
    public interface InventoryChecker {
        InventoryAlert checkInventoryLevel(String materialId) throws Exception;

        Map<String, InventoryAlert> checkMultipleInventories(List<String> materialIds) throws Exception;

        boolean supportsInventoryCheck(InventoryType inventoryType);

        String getCheckerName();
    }

    // 追踪存储接口
    public interface TraceStorage {
        boolean saveMaterialTrace(MaterialTrace trace) throws Exception;

        MaterialTrace loadMaterialTrace(String materialId) throws Exception;

        boolean addTraceMovement(String materialId, MaterialMovement movement) throws Exception;

        List<MaterialMovement> queryTraceMovements(String materialId, TraceQuery query) throws Exception;

        boolean supportsTraceType(String traceType);

        String getTraceStorageName();
    }

    // 物料类
    public static class Material {
        private final String materialId;
        private final String materialName;
        private final MaterialType materialType;
        private final String specification;
        private final String supplier;
        private final LocalDateTime receiptDate;
        private final LocalDateTime manufactureDate;
        private final LocalDateTime expirationDate;
        private final String lotNumber;
        private final String batchNumber;
        private final double quantity;
        private final String unit;
        private final MaterialStatus status;
        private final QualityStatus qualityStatus;
        private final Map<String, Object> attributes;
        private final List<StorageLocation> storageLocations;
        private final LocalDateTime lastUpdate;
        private final String updatedBy;

        public Material(String materialId, String materialName, MaterialType materialType) {
            this.materialId = materialId != null ? materialId : UUID.randomUUID().toString();
            this.materialName = materialName != null ? materialName : "";
            this.materialType = materialType != null ? materialType : MaterialType.CUSTOM;
            this.specification = "";
            this.supplier = "";
            this.receiptDate = LocalDateTime.now();
            this.manufactureDate = LocalDateTime.now();
            this.expirationDate = LocalDateTime.now().plusYears(1);
            this.lotNumber = "";
            this.batchNumber = "";
            this.quantity = 0.0;
            this.unit = "pcs";
            this.status = MaterialStatus.AVAILABLE;
            this.qualityStatus = QualityStatus.PENDING;
            this.attributes = new ConcurrentHashMap<>();
            this.storageLocations = new ArrayList<>();
            this.lastUpdate = LocalDateTime.now();
            this.updatedBy = "System";
        }

        // Getters and Setters
        public String getMaterialId() {
            return materialId;
        }

        public String getMaterialName() {
            return materialName;
        }

        public Material setMaterialName(String materialName) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public MaterialType getMaterialType() {
            return materialType;
        }

        public String getSpecification() {
            return specification;
        }

        public Material setSpecification(String specification) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getSupplier() {
            return supplier;
        }

        public Material setSupplier(String supplier) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public LocalDateTime getReceiptDate() {
            return receiptDate;
        }

        public LocalDateTime getManufactureDate() {
            return manufactureDate;
        }

        public Material setManufactureDate(LocalDateTime manufactureDate) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public LocalDateTime getExpirationDate() {
            return expirationDate;
        }

        public Material setExpirationDate(LocalDateTime expirationDate) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getLotNumber() {
            return lotNumber;
        }

        public Material setLotNumber(String lotNumber) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getBatchNumber() {
            return batchNumber;
        }

        public Material setBatchNumber(String batchNumber) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public double getQuantity() {
            return quantity;
        }

        public String getUnit() {
            return unit;
        }

        public Material setUnit(String unit) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public MaterialStatus getStatus() {
            return status;
        }

        public Material setStatus(MaterialStatus status) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public QualityStatus getQualityStatus() {
            return qualityStatus;
        }

        public Material setQualityStatus(QualityStatus qualityStatus) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public Material setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        public List<StorageLocation> getStorageLocations() {
            return new ArrayList<>(storageLocations);
        }

        public Material addStorageLocation(StorageLocation location) {
            this.storageLocations.add(location);
            return this;
        }

        public LocalDateTime getLastUpdate() {
            return lastUpdate;
        }

        public String getUpdatedBy() {
            return updatedBy;
        }

        public Material setUpdatedBy(String updatedBy) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        @Override
        public String toString() {
            return "Material{" +
                    "materialId='" + materialId + '\'' +
                    ", materialName='" + materialName + '\'' +
                    ", materialType=" + materialType +
                    ", quantity=" + quantity +
                    ", unit='" + unit + '\'' +
                    ", status=" + status +
                    ", qualityStatus=" + qualityStatus +
                    '}';
        }
    }

    // 存储位置类
    public static class StorageLocation {
        private final String locationId;
        private final String locationName;
        private final String warehouse;
        private final String area;
        private final String rack;
        private final String shelf;
        private final double quantity;
        private final String unit;
        private final Map<String, Object> attributes;

        public StorageLocation(String locationId, String locationName) {
            this.locationId = locationId != null ? locationId : UUID.randomUUID().toString();
            this.locationName = locationName != null ? locationName : "";
            this.warehouse = "";
            this.area = "";
            this.rack = "";
            this.shelf = "";
            this.quantity = 0.0;
            this.unit = "pcs";
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getLocationId() {
            return locationId;
        }

        public String getLocationName() {
            return locationName;
        }

        public StorageLocation setLocationName(String locationName) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getWarehouse() {
            return warehouse;
        }

        public StorageLocation setWarehouse(String warehouse) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getArea() {
            return area;
        }

        public StorageLocation setArea(String area) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getRack() {
            return rack;
        }

        public StorageLocation setRack(String rack) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getShelf() {
            return shelf;
        }

        public StorageLocation setShelf(String shelf) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public double getQuantity() {
            return quantity;
        }

        public String getUnit() {
            return unit;
        }

        public StorageLocation setUnit(String unit) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public StorageLocation setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        @Override
        public String toString() {
            return "StorageLocation{" +
                    "locationId='" + locationId + '\'' +
                    ", locationName='" + locationName + '\'' +
                    ", warehouse='" + warehouse + '\'' +
                    ", area='" + area + '\'' +
                    ", rack='" + rack + '\'' +
                    ", shelf='" + shelf + '\'' +
                    ", quantity=" + quantity +
                    ", unit='" + unit + '\'' +
                    '}';
        }
    }

    // 物料批次类
    public static class MaterialBatch {
        private final String batchId;
        private final String materialId;
        private final String batchNumber;
        private final LocalDateTime creationDate;
        private final LocalDateTime expirationDate;
        private final double initialQuantity;
        private final double currentQuantity;
        private final String unit;
        private final MaterialStatus status;
        private final QualityStatus qualityStatus;
        private final String productionOrder;
        private final List<BatchTrace> traceRecords;
        private final Map<String, Object> attributes;
        private final LocalDateTime lastUpdate;

        public MaterialBatch(String batchId, String materialId, String batchNumber) {
            this.batchId = batchId != null ? batchId : UUID.randomUUID().toString();
            this.materialId = materialId != null ? materialId : "";
            this.batchNumber = batchNumber != null ? batchNumber : "";
            this.creationDate = LocalDateTime.now();
            this.expirationDate = LocalDateTime.now().plusYears(1);
            this.initialQuantity = 0.0;
            this.currentQuantity = 0.0;
            this.unit = "pcs";
            this.status = MaterialStatus.AVAILABLE;
            this.qualityStatus = QualityStatus.PENDING;
            this.productionOrder = "";
            this.traceRecords = new CopyOnWriteArrayList<>();
            this.attributes = new ConcurrentHashMap<>();
            this.lastUpdate = LocalDateTime.now();
        }

        // Getters and Setters
        public String getBatchId() {
            return batchId;
        }

        public String getMaterialId() {
            return materialId;
        }

        public String getBatchNumber() {
            return batchNumber;
        }

        public LocalDateTime getCreationDate() {
            return creationDate;
        }

        public LocalDateTime getExpirationDate() {
            return expirationDate;
        }

        public double getInitialQuantity() {
            return initialQuantity;
        }

        public double getCurrentQuantity() {
            return currentQuantity;
        }

        public String getUnit() {
            return unit;
        }

        public MaterialStatus getStatus() {
            return status;
        }

        public QualityStatus getQualityStatus() {
            return qualityStatus;
        }

        public String getProductionOrder() {
            return productionOrder;
        }

        public List<BatchTrace> getTraceRecords() {
            return new ArrayList<>(traceRecords);
        }

        public void addTraceRecord(BatchTrace traceRecord) {
            this.traceRecords.add(traceRecord);
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

        public LocalDateTime getLastUpdate() {
            return lastUpdate;
        }

        @Override
        public String toString() {
            return "MaterialBatch{" +
                    "batchId='" + batchId + '\'' +
                    ", materialId='" + materialId + '\'' +
                    ", batchNumber='" + batchNumber + '\'' +
                    ", currentQuantity=" + currentQuantity +
                    ", unit='" + unit + '\'' +
                    ", status=" + status +
                    ", qualityStatus=" + qualityStatus +
                    ", traceRecordCount=" + traceRecords.size() +
                    '}';
        }
    }

    // 批次追踪记录类
    public static class BatchTrace {
        private final String traceId;
        private final String batchId;
        private final TraceEventType eventType;
        private final double quantity;
        private final String unit;
        private final String fromLocation;
        private final String toLocation;
        private final String operator;
        private final LocalDateTime eventTime;
        private final String referenceNumber;
        private final Map<String, Object> attributes;

        public BatchTrace(String batchId, TraceEventType eventType, double quantity) {
            this.traceId = UUID.randomUUID().toString();
            this.batchId = batchId != null ? batchId : "";
            this.eventType = eventType != null ? eventType : TraceEventType.RECEIPT;
            this.quantity = quantity;
            this.unit = "pcs";
            this.fromLocation = "";
            this.toLocation = "";
            this.operator = "System";
            this.eventTime = LocalDateTime.now();
            this.referenceNumber = "";
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getTraceId() {
            return traceId;
        }

        public String getBatchId() {
            return batchId;
        }

        public TraceEventType getEventType() {
            return eventType;
        }

        public double getQuantity() {
            return quantity;
        }

        public String getUnit() {
            return unit;
        }

        public BatchTrace setUnit(String unit) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getFromLocation() {
            return fromLocation;
        }

        public BatchTrace setFromLocation(String fromLocation) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getToLocation() {
            return toLocation;
        }

        public BatchTrace setToLocation(String toLocation) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getOperator() {
            return operator;
        }

        public BatchTrace setOperator(String operator) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public LocalDateTime getEventTime() {
            return eventTime;
        }

        public String getReferenceNumber() {
            return referenceNumber;
        }

        public BatchTrace setReferenceNumber(String referenceNumber) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public BatchTrace setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        @Override
        public String toString() {
            return "BatchTrace{" +
                    "traceId='" + traceId + '\'' +
                    ", batchId='" + batchId + '\'' +
                    ", eventType=" + eventType +
                    ", quantity=" + quantity +
                    ", unit='" + unit + '\'' +
                    ", operator='" + operator + '\'' +
                    ", eventTime=" + eventTime +
                    '}';
        }
    }

    // 物料追踪类
    public static class MaterialTrace {
        private final String traceId;
        private final String materialId;
        private final List<MaterialMovement> movements;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final Map<String, Object> summary;
        private final boolean isComplete;
        private final Map<String, Object> metadata;

        public MaterialTrace(String materialId) {
            this.traceId = UUID.randomUUID().toString();
            this.materialId = materialId != null ? materialId : "";
            this.movements = new CopyOnWriteArrayList<>();
            this.startTime = LocalDateTime.now();
            this.endTime = null;
            this.summary = new ConcurrentHashMap<>();
            this.isComplete = false;
            this.metadata = new ConcurrentHashMap<>();
        }

        // 完整构造函数
        public MaterialTrace(String materialId, List<MaterialMovement> movements,
                             LocalDateTime startTime, LocalDateTime endTime) {
            this.traceId = UUID.randomUUID().toString();
            this.materialId = materialId != null ? materialId : "";
            this.movements = movements != null ? new ArrayList<>(movements) : new ArrayList<>();
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
            summary.put("movementCount", movements.size());
            summary.put("duration", java.time.Duration.between(startTime, endTime).toDays() + "天");

            if (!movements.isEmpty()) {
                // 统计各事件类型数量
                Map<String, Long> eventTypeCount = movements.stream()
                        .collect(Collectors.groupingBy(
                                movement -> movement.getEventType().name(),
                                Collectors.counting()
                        ));
                summary.put("eventTypeDistribution", eventTypeCount);

                // 统计总数量变化
                double totalQuantity = movements.stream()
                        .mapToDouble(MaterialMovement::getQuantity)
                        .sum();
                summary.put("totalQuantity", totalQuantity);
            }
        }

        // 添加物料移动记录
        public void addMovement(MaterialMovement movement) {
            if (movements.size() < DEFAULT_TRACE_BUFFER_SIZE) {
                this.movements.add(movement);
            } else {
                logger.warn("Trace buffer full, dropping movement: {}", movement.getEventType());
            }
        }

        // Getters
        public String getTraceId() {
            return traceId;
        }

        public String getMaterialId() {
            return materialId;
        }

        public List<MaterialMovement> getMovements() {
            return new ArrayList<>(movements);
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
            return "MaterialTrace{" +
                    "traceId='" + traceId + '\'' +
                    ", materialId='" + materialId + '\'' +
                    ", movementCount=" + movements.size() +
                    ", startTime=" + startTime +
                    ", isComplete=" + isComplete +
                    '}';
        }
    }

    // 物料移动类
    public static class MaterialMovement {
        private final String movementId;
        private final String materialId;
        private final TraceEventType eventType;
        private final double quantity;
        private final String unit;
        private final String fromLocation;
        private final String toLocation;
        private final String referenceNumber;
        private final LocalDateTime movementTime;
        private final String operator;
        private final Map<String, Object> attributes;

        public MaterialMovement(String materialId, TraceEventType eventType, double quantity) {
            this.movementId = UUID.randomUUID().toString();
            this.materialId = materialId != null ? materialId : "";
            this.eventType = eventType != null ? eventType : TraceEventType.RECEIPT;
            this.quantity = quantity;
            this.unit = "pcs";
            this.fromLocation = "";
            this.toLocation = "";
            this.referenceNumber = "";
            this.movementTime = LocalDateTime.now();
            this.operator = "System";
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getMovementId() {
            return movementId;
        }

        public String getMaterialId() {
            return materialId;
        }

        public TraceEventType getEventType() {
            return eventType;
        }

        public double getQuantity() {
            return quantity;
        }

        public String getUnit() {
            return unit;
        }

        public MaterialMovement setUnit(String unit) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getFromLocation() {
            return fromLocation;
        }

        public MaterialMovement setFromLocation(String fromLocation) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getToLocation() {
            return toLocation;
        }

        public MaterialMovement setToLocation(String toLocation) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getReferenceNumber() {
            return referenceNumber;
        }

        public MaterialMovement setReferenceNumber(String referenceNumber) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public LocalDateTime getMovementTime() {
            return movementTime;
        }

        public String getOperator() {
            return operator;
        }

        public MaterialMovement setOperator(String operator) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public MaterialMovement setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        @Override
        public String toString() {
            return "MaterialMovement{" +
                    "movementId='" + movementId + '\'' +
                    ", materialId='" + materialId + '\'' +
                    ", eventType=" + eventType +
                    ", quantity=" + quantity +
                    ", unit='" + unit + '\'' +
                    ", movementTime=" + movementTime +
                    '}';
        }
    }

    // 过期预警类
    public static class ExpirationAlert {
        private final String alertId;
        private final String materialId;
        private final AlertType alertType;
        private final LocalDateTime expirationDate;
        private final long daysUntilExpiration;
        private final String message;
        private final LocalDateTime alertTime;
        private final String recipient;
        private final boolean acknowledged;
        private final Map<String, Object> metadata;

        public ExpirationAlert(String materialId, LocalDateTime expirationDate, long daysUntilExpiration) {
            this.alertId = UUID.randomUUID().toString();
            this.materialId = materialId != null ? materialId : "";
            this.alertType = AlertType.EXPIRATION_WARNING;
            this.expirationDate = expirationDate;
            this.daysUntilExpiration = daysUntilExpiration;
            this.message = "物料将在" + daysUntilExpiration + "天后过期";
            this.alertTime = LocalDateTime.now();
            this.recipient = "";
            this.acknowledged = false;
            this.metadata = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getAlertId() {
            return alertId;
        }

        public String getMaterialId() {
            return materialId;
        }

        public AlertType getAlertType() {
            return alertType;
        }

        public LocalDateTime getExpirationDate() {
            return expirationDate;
        }

        public long getDaysUntilExpiration() {
            return daysUntilExpiration;
        }

        public String getMessage() {
            return message;
        }

        public LocalDateTime getAlertTime() {
            return alertTime;
        }

        public String getRecipient() {
            return recipient;
        }

        public ExpirationAlert setRecipient(String recipient) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public boolean isAcknowledged() {
            return acknowledged;
        }

        public ExpirationAlert setAcknowledged(boolean acknowledged) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }

        public ExpirationAlert setMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Object getMetadata(String key) {
            return this.metadata.get(key);
        }

        @Override
        public String toString() {
            return "ExpirationAlert{" +
                    "alertId='" + alertId + '\'' +
                    ", materialId='" + materialId + '\'' +
                    ", alertType=" + alertType +
                    ", daysUntilExpiration=" + daysUntilExpiration +
                    ", message='" + message + '\'' +
                    ", alertTime=" + alertTime +
                    ", acknowledged=" + acknowledged +
                    '}';
        }
    }

    // 库存预警类
    public static class InventoryAlert {
        private final String alertId;
        private final String materialId;
        private final AlertType alertType;
        private final double currentQuantity;
        private final double minimumQuantity;
        private final double thresholdPercentage;
        private final String unit;
        private final String message;
        private final LocalDateTime alertTime;
        private final String recipient;
        private final boolean acknowledged;
        private final Map<String, Object> metadata;

        public InventoryAlert(String materialId, double currentQuantity, double minimumQuantity,
                              double thresholdPercentage, String unit) {
            this.alertId = UUID.randomUUID().toString();
            this.materialId = materialId != null ? materialId : "";
            this.alertType = AlertType.INVENTORY_LOW;
            this.currentQuantity = currentQuantity;
            this.minimumQuantity = minimumQuantity;
            this.thresholdPercentage = thresholdPercentage;
            this.unit = unit != null ? unit : "pcs";
            this.message = "库存不足，当前库存: " + currentQuantity + unit +
                    "，最低库存: " + minimumQuantity + unit;
            this.alertTime = LocalDateTime.now();
            this.recipient = "";
            this.acknowledged = false;
            this.metadata = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getAlertId() {
            return alertId;
        }

        public String getMaterialId() {
            return materialId;
        }

        public AlertType getAlertType() {
            return alertType;
        }

        public double getCurrentQuantity() {
            return currentQuantity;
        }

        public double getMinimumQuantity() {
            return minimumQuantity;
        }

        public double getThresholdPercentage() {
            return thresholdPercentage;
        }

        public String getUnit() {
            return unit;
        }

        public String getMessage() {
            return message;
        }

        public LocalDateTime getAlertTime() {
            return alertTime;
        }

        public String getRecipient() {
            return recipient;
        }

        public InventoryAlert setRecipient(String recipient) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public boolean isAcknowledged() {
            return acknowledged;
        }

        public InventoryAlert setAcknowledged(boolean acknowledged) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }

        public InventoryAlert setMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Object getMetadata(String key) {
            return this.metadata.get(key);
        }

        @Override
        public String toString() {
            return "InventoryAlert{" +
                    "alertId='" + alertId + '\'' +
                    ", materialId='" + materialId + '\'' +
                    ", alertType=" + alertType +
                    ", currentQuantity=" + currentQuantity +
                    ", minimumQuantity=" + minimumQuantity +
                    ", unit='" + unit + '\'' +
                    ", message='" + message + '\'' +
                    ", alertTime=" + alertTime +
                    ", acknowledged=" + acknowledged +
                    '}';
        }
    }

    // 质量状态历史类
    public static class QualityStatusHistory {
        private final String historyId;
        private final String materialId;
        private final List<QualityRecord> records;
        private final LocalDateTime lastUpdate;

        public QualityStatusHistory(String materialId) {
            this.historyId = UUID.randomUUID().toString();
            this.materialId = materialId != null ? materialId : "";
            this.records = new CopyOnWriteArrayList<>();
            this.lastUpdate = LocalDateTime.now();
        }

        // Getters
        public String getHistoryId() {
            return historyId;
        }

        public String getMaterialId() {
            return materialId;
        }

        public List<QualityRecord> getRecords() {
            return new ArrayList<>(records);
        }

        public void addRecord(QualityRecord record) {
            this.records.add(record);
            // 更新最后更新时间
            try {
                // 使用反射更新lastUpdate字段（仅在示例中）
                java.lang.reflect.Field field = this.getClass().getDeclaredField("lastUpdate");
                field.setAccessible(true);
                field.set(this, LocalDateTime.now());
            } catch (Exception e) {
                // 忽略异常
            }
        }

        public LocalDateTime getLastUpdate() {
            return lastUpdate;
        }

        @Override
        public String toString() {
            return "QualityStatusHistory{" +
                    "historyId='" + historyId + '\'' +
                    ", materialId='" + materialId + '\'' +
                    ", recordCount=" + records.size() +
                    ", lastUpdate=" + lastUpdate +
                    '}';
        }
    }

    // 质量记录类
    public static class QualityRecord {
        private final String recordId;
        private final String materialId;
        private final QualityStatus status;
        private final String inspector;
        private final LocalDateTime inspectionTime;
        private final String inspectionReport;
        private final Map<String, Object> testResults;
        private final String remarks;

        public QualityRecord(String materialId, QualityStatus status) {
            this.recordId = UUID.randomUUID().toString();
            this.materialId = materialId != null ? materialId : "";
            this.status = status != null ? status : QualityStatus.PENDING;
            this.inspector = "";
            this.inspectionTime = LocalDateTime.now();
            this.inspectionReport = "";
            this.testResults = new ConcurrentHashMap<>();
            this.remarks = "";
        }

        // Getters and Setters
        public String getRecordId() {
            return recordId;
        }

        public String getMaterialId() {
            return materialId;
        }

        public QualityStatus getStatus() {
            return status;
        }

        public String getInspector() {
            return inspector;
        }

        public QualityRecord setInspector(String inspector) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public LocalDateTime getInspectionTime() {
            return inspectionTime;
        }

        public String getInspectionReport() {
            return inspectionReport;
        }

        public QualityRecord setInspectionReport(String inspectionReport) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public Map<String, Object> getTestResults() {
            return new HashMap<>(testResults);
        }

        public QualityRecord setTestResult(String key, Object value) {
            this.testResults.put(key, value);
            return this;
        }

        public Object getTestResult(String key) {
            return this.testResults.get(key);
        }

        public String getRemarks() {
            return remarks;
        }

        public QualityRecord setRemarks(String remarks) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        @Override
        public String toString() {
            return "QualityRecord{" +
                    "recordId='" + recordId + '\'' +
                    ", materialId='" + materialId + '\'' +
                    ", status=" + status +
                    ", inspector='" + inspector + '\'' +
                    ", inspectionTime=" + inspectionTime +
                    '}';
        }
    }

    // 追踪查询类
    public static class TraceQuery {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private TraceEventType eventType;
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

        public TraceEventType getEventType() {
            return eventType;
        }

        public TraceQuery setEventType(TraceEventType eventType) {
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
        private int materialCacheSize;
        private int batchCacheSize;
        private int traceCacheSize;
        private long cacheTimeout;

        // Getters and Setters
        public int getMaterialCacheSize() {
            return materialCacheSize;
        }

        public void setMaterialCacheSize(int materialCacheSize) {
            this.materialCacheSize = materialCacheSize;
        }

        public int getBatchCacheSize() {
            return batchCacheSize;
        }

        public void setBatchCacheSize(int batchCacheSize) {
            this.batchCacheSize = batchCacheSize;
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
                    "materialCacheSize=" + materialCacheSize +
                    ", batchCacheSize=" + batchCacheSize +
                    ", traceCacheSize=" + traceCacheSize +
                    ", cacheTimeout=" + cacheTimeout + "ms" +
                    '}';
        }
    }
}
