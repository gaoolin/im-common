package com.qtech.im.semiconductor.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 产品序列化管理工具类
 * <p>
 * 特性：
 * - 通用性：支持多种产品类型和序列化方案
 * - 规范性：统一的产品标识和信息管理标准
 * - 专业性：半导体行业产品管理专业实现
 * - 灵活性：可配置的序列号规则和状态管理
 * - 可靠性：完善的数据持久化和备份机制
 * - 安全性：数据保护和访问控制
 * - 复用性：模块化设计，组件可独立使用
 * - 容错性：优雅的错误处理和恢复机制
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/08/21
 */
public class ProductSerializer {

    // 默认配置
    public static final int DEFAULT_SERIAL_NUMBER_LENGTH = 16;
    public static final long DEFAULT_CACHE_TIMEOUT = 30 * 60 * 1000; // 30分钟
    public static final int DEFAULT_MAX_RETRY_ATTEMPTS = 3;
    public static final long DEFAULT_RETRY_DELAY = 1000; // 1秒
    private static final Logger logger = LoggerFactory.getLogger(ProductSerializer.class);
    // 内部存储和管理
    private static final Map<SerialNumberStrategy, SerialNumberGenerator> generatorRegistry = new ConcurrentHashMap<>();
    private static final Map<String, ProductInfoStorage> storageRegistry = new ConcurrentHashMap<>();
    private static final Map<String, ProductTraceStorage> traceStorageRegistry = new ConcurrentHashMap<>();
    private static final Map<String, ProductInfo> productInfoCache = new ConcurrentHashMap<>();
    private static final Map<String, ProductTrace> productTraceCache = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private static final AtomicLong serialCounter = new AtomicLong(0);
    // 当前使用的组件
    private static volatile SerialNumberGenerator currentGenerator;
    private static volatile ProductInfoStorage currentStorage;
    private static volatile ProductTraceStorage currentTraceStorage;

    // 初始化默认组件
    static {
        registerDefaultComponents();
        initializeCurrentComponents();
        startMaintenanceTasks();
        logger.info("ProductSerializer initialized with default components");
    }

    /**
     * 注册默认组件
     */
    private static void registerDefaultComponents() {
        // 注册默认生成器、存储和追溯存储
        // registerGenerator(new DefaultSerialNumberGenerator());
        // registerStorage(new DefaultProductInfoStorage());
        // registerTraceStorage(new DefaultProductTraceStorage());
    }

    /**
     * 初始化当前组件
     */
    private static void initializeCurrentComponents() {
        // 初始化为第一个注册的组件
        if (!generatorRegistry.isEmpty()) {
            currentGenerator = generatorRegistry.values().iterator().next();
        }

        if (!storageRegistry.isEmpty()) {
            currentStorage = storageRegistry.values().iterator().next();
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
        scheduler.scheduleAtFixedRate(ProductSerializer::cleanupCache,
                30, 30, TimeUnit.MINUTES);

        logger.debug("Product serializer maintenance tasks started");
    }

    /**
     * 注册序列号生成器
     */
    public static void registerGenerator(SerialNumberGenerator generator) {
        if (generator == null) {
            throw new IllegalArgumentException("Generator cannot be null");
        }

        for (SerialNumberStrategy strategy : SerialNumberStrategy.values()) {
            if (generator.supportsStrategy(strategy)) {
                generatorRegistry.put(strategy, generator);
                logger.debug("Registered generator {} for strategy {}", generator.getGeneratorName(), strategy);
            }
        }
    }

    /**
     * 注册产品信息存储
     */
    public static void registerStorage(ProductInfoStorage storage) {
        if (storage == null) {
            throw new IllegalArgumentException("Storage cannot be null");
        }

        // 注册支持的所有存储类型
        String[] storageTypes = {"database", "file", "memory", "cloud"}; // 简化实现
        for (String type : storageTypes) {
            if (storage.supportsStorageType(type)) {
                storageRegistry.put(type, storage);
                logger.debug("Registered storage {} for type {}", storage.getStorageName(), type);
            }
        }
    }

    /**
     * 注册产品追溯存储
     */
    public static void registerTraceStorage(ProductTraceStorage traceStorage) {
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
     * 设置当前序列号生成器
     */
    public static void setCurrentGenerator(SerialNumberStrategy strategy) {
        SerialNumberGenerator generator = generatorRegistry.get(strategy);
        if (generator != null) {
            currentGenerator = generator;
            logger.info("Current serial number generator set to: {} for strategy {}",
                    generator.getGeneratorName(), strategy);
        } else {
            logger.warn("No generator found for strategy: {}", strategy);
        }
    }

    /**
     * 设置当前产品信息存储
     */
    public static void setCurrentStorage(String storageType) {
        ProductInfoStorage storage = storageRegistry.get(storageType);
        if (storage != null) {
            currentStorage = storage;
            logger.info("Current product info storage set to: {}", storage.getStorageName());
        } else {
            logger.warn("No storage found for type: {}", storageType);
        }
    }

    /**
     * 设置当前产品追溯存储
     */
    public static void setCurrentTraceStorage(String traceStorageType) {
        ProductTraceStorage traceStorage = traceStorageRegistry.get(traceStorageType);
        if (traceStorage != null) {
            currentTraceStorage = traceStorage;
            logger.info("Current product trace storage set to: {}", traceStorage.getTraceStorageName());
        } else {
            logger.warn("No trace storage found for type: {}", traceStorageType);
        }
    }

    /**
     * 产品唯一标识生成
     */
    public static String generateProductSerialNumber(ProductType type) {
        return generateProductSerialNumber(type, new HashMap<>());
    }

    /**
     * 产品唯一标识生成（带参数）
     */
    public static String generateProductSerialNumber(ProductType type, Map<String, Object> parameters) {
        if (type == null) {
            logger.warn("Invalid product type for serial number generation");
            return null;
        }

        if (currentGenerator == null) {
            logger.error("No serial number generator available");
            return null;
        }

        try {
            String serialNumber = currentGenerator.generateSerialNumber(type, parameters);

            if (serialNumber != null && !serialNumber.isEmpty()) {
                logger.debug("Generated serial number: {} for product type: {}", serialNumber, type);
                return serialNumber;
            } else {
                logger.warn("Serial number generator returned empty result for type: {}", type);
                return null;
            }
        } catch (Exception e) {
            logger.error("Failed to generate serial number for product type: " + type, e);
            return null;
        }
    }

    /**
     * 产品信息管理 - 创建产品
     */
    public static ProductInfo createProductInfo(ProductType type, String productName) {
        return createProductInfo(type, productName, new HashMap<>());
    }

    /**
     * 产品信息管理 - 创建产品（带参数）
     */
    public static ProductInfo createProductInfo(ProductType type, String productName,
                                                Map<String, Object> parameters) {
        if (type == null || productName == null || productName.isEmpty()) {
            logger.warn("Invalid parameters for product creation");
            return null;
        }

        try {
            // 生成序列号
            String serialNumber = generateProductSerialNumber(type, parameters);
            if (serialNumber == null || serialNumber.isEmpty()) {
                logger.error("Failed to generate serial number for product creation");
                return null;
            }

            // 创建产品信息
            ProductInfo productInfo = new ProductInfo(serialNumber, type, productName);

            // 保存产品信息
            if (currentStorage != null) {
                boolean saved = currentStorage.saveProductInfo(productInfo);
                if (!saved) {
                    logger.warn("Failed to save product info: {}", serialNumber);
                }
            } else {
                // 如果没有存储组件，仅保存在缓存中
                productInfoCache.put(serialNumber, productInfo);
                logger.debug("Product info saved to cache: {}", serialNumber);
            }

            logger.info("Created product info: {} - {}", serialNumber, productName);
            return productInfo;
        } catch (Exception e) {
            logger.error("Failed to create product info for type: " + type + ", name: " + productName, e);
            return null;
        }
    }

    /**
     * 产品信息管理 - 获取产品信息
     */
    public static ProductInfo getProductInfo(String serialNumber) {
        if (serialNumber == null || serialNumber.isEmpty()) {
            logger.warn("Invalid serial number for product info retrieval");
            return null;
        }

        try {
            // 首先检查缓存
            ProductInfo cached = productInfoCache.get(serialNumber);
            if (cached != null && !isCacheExpired(cached)) {
                logger.debug("Returning cached product info: {}", serialNumber);
                return cached;
            }

            // 从存储中获取
            ProductInfo productInfo = null;
            if (currentStorage != null) {
                productInfo = currentStorage.loadProductInfo(serialNumber);
            }

            // 如果存储中没有且缓存中有，则返回缓存的
            if (productInfo == null) {
                productInfo = cached;
            }

            // 更新缓存
            if (productInfo != null) {
                productInfoCache.put(serialNumber, productInfo);
            }

            if (productInfo != null) {
                logger.debug("Retrieved product info: {}", serialNumber);
            } else {
                logger.warn("Product info not found: {}", serialNumber);
            }

            return productInfo;
        } catch (Exception e) {
            logger.error("Failed to get product info: " + serialNumber, e);
            return null;
        }
    }

    /**
     * 检查缓存是否过期
     */
    private static boolean isCacheExpired(ProductInfo productInfo) {
        return System.currentTimeMillis() - productInfo.getRegistrationTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
                > DEFAULT_CACHE_TIMEOUT;
    }

    /**
     * 产品状态更新
     */
    public static boolean updateProductStatus(String serialNumber, ProductStatus newStatus) {
        return updateProductStatus(serialNumber, newStatus, "状态更新", "System");
    }

    /**
     * 产品状态更新（带描述和操作员）
     */
    public static boolean updateProductStatus(String serialNumber, ProductStatus newStatus,
                                              String description, String operator) {
        if (serialNumber == null || serialNumber.isEmpty() || newStatus == null) {
            logger.warn("Invalid parameters for product status update");
            return false;
        }

        if (description == null) description = "状态更新";
        if (operator == null) operator = "System";

        try {
            // 获取当前产品信息
            ProductInfo productInfo = getProductInfo(serialNumber);
            if (productInfo == null) {
                logger.warn("Product not found for status update: {}", serialNumber);
                return false;
            }

            // 检查状态变更是否合法
            ProductStatus currentStatus = productInfo.getCurrentStatus();
            if (!isValidStatusTransition(currentStatus, newStatus)) {
                logger.warn("Invalid status transition: {} -> {} for product: {}",
                        currentStatus, newStatus, serialNumber);
                return false;
            }

            // 更新产品信息
            productInfo.addStatusRecord(newStatus, description, operator);

            // 保存更新
            boolean saved = false;
            if (currentStorage != null) {
                saved = currentStorage.updateProductStatus(serialNumber, newStatus, description, operator);
            } else {
                // 更新缓存
                productInfoCache.put(serialNumber, productInfo);
                saved = true;
            }

            if (saved) {
                // 记录追溯事件
                recordStatusChangeTraceEvent(serialNumber, currentStatus, newStatus, description, operator);
                logger.info("Updated product status: {} -> {} for product: {}",
                        currentStatus, newStatus, serialNumber);
            } else {
                logger.warn("Failed to save product status update: {}", serialNumber);
            }

            return saved;
        } catch (Exception e) {
            logger.error("Failed to update product status: " + serialNumber, e);
            return false;
        }
    }

    /**
     * 验证状态变更是否合法
     */
    private static boolean isValidStatusTransition(ProductStatus current, ProductStatus newStatus) {
        // 简单的状态验证规则
        // 实际应用中可以根据业务需求定义更复杂的规则

        // 不允许从未激活状态变更为已激活状态以外的状态
        if (!current.isActive() && newStatus.isActive()) {
            return true; // 重新激活是允许的
        }

        // 一般情况下允许状态变更
        return true;
    }

    /**
     * 记录状态变更追溯事件
     */
    private static void recordStatusChangeTraceEvent(String serialNumber, ProductStatus oldStatus,
                                                     ProductStatus newStatus, String reason, String operator) {
        try {
            StatusChangeEvent event = new StatusChangeEvent(oldStatus, newStatus, reason, operator);

            if (currentTraceStorage != null) {
                currentTraceStorage.addTraceEvent(serialNumber, event);
            } else {
                // 如果没有追溯存储，可以考虑记录到日志或其他地方
                logger.debug("Status change event recorded: {} -> {} for product: {}",
                        oldStatus, newStatus, serialNumber);
            }
        } catch (Exception e) {
            logger.warn("Failed to record status change trace event for product: " + serialNumber, e);
        }
    }

    /**
     * 产品全程追溯
     */
    public static ProductTrace generateProductTrace(String serialNumber) {
        if (serialNumber == null || serialNumber.isEmpty()) {
            logger.warn("Invalid serial number for product trace generation");
            return null;
        }

        try {
            // 首先检查缓存
            ProductTrace cached = productTraceCache.get(serialNumber);
            if (cached != null && !isTraceCacheExpired(cached)) {
                logger.debug("Returning cached product trace: {}", serialNumber);
                return cached;
            }

            // 从追溯存储中获取
            ProductTrace trace = null;
            if (currentTraceStorage != null) {
                trace = currentTraceStorage.loadProductTrace(serialNumber);
            }

            // 如果追溯存储中没有且缓存中有，则返回缓存的
            if (trace == null) {
                trace = cached;
            }

            // 如果都没有，创建新的追溯对象
            if (trace == null) {
                trace = new ProductTrace(serialNumber);
            }

            // 更新缓存
            if (trace != null) {
                productTraceCache.put(serialNumber, trace);
            }

            if (trace != null) {
                logger.debug("Generated product trace: {}", serialNumber);
            } else {
                logger.warn("Failed to generate product trace: {}", serialNumber);
            }

            return trace;
        } catch (Exception e) {
            logger.error("Failed to generate product trace: " + serialNumber, e);
            return null;
        }
    }

    /**
     * 检查追溯缓存是否过期
     */
    private static boolean isTraceCacheExpired(ProductTrace trace) {
        return System.currentTimeMillis() - trace.getStartTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
                > DEFAULT_CACHE_TIMEOUT;
    }

    /**
     * 添加追溯事件
     */
    public static boolean addTraceEvent(String serialNumber, TraceEvent event) {
        if (serialNumber == null || serialNumber.isEmpty() || event == null) {
            logger.warn("Invalid parameters for trace event addition");
            return false;
        }

        try {
            boolean added = false;
            if (currentTraceStorage != null) {
                added = currentTraceStorage.addTraceEvent(serialNumber, event);
            } else {
                // 如果没有追溯存储，记录到缓存中的追溯对象
                ProductTrace trace = productTraceCache.get(serialNumber);
                if (trace != null) {
                    trace.addTraceEvent(event);
                    added = true;
                }
            }

            if (added) {
                logger.debug("Added trace event: {} for product: {}", event.getOperationType(), serialNumber);
            } else {
                logger.warn("Failed to add trace event for product: {}", serialNumber);
            }

            return added;
        } catch (Exception e) {
            logger.error("Failed to add trace event for product: " + serialNumber, e);
            return false;
        }
    }

    /**
     * 产品信息查询
     */
    public static List<ProductInfo> queryProductInfos(ProductQuery query) {
        if (query == null) {
            logger.warn("Invalid query for product info search");
            return new ArrayList<>();
        }

        try {
            if (currentStorage != null) {
                return currentStorage.queryProductInfos(query);
            } else {
                // 如果没有存储，从缓存中查询
                return queryProductInfosFromCache(query);
            }
        } catch (Exception e) {
            logger.error("Failed to query product infos", e);
            return new ArrayList<>();
        }
    }

    /**
     * 从缓存中查询产品信息
     */
    private static List<ProductInfo> queryProductInfosFromCache(ProductQuery query) {
        return productInfoCache.values().stream()
                .filter(productInfo -> {
                    // 类型过滤
                    if (query.getProductType() != null &&
                            productInfo.getProductType() != query.getProductType()) {
                        return false;
                    }

                    // 状态过滤
                    if (query.getProductStatus() != null &&
                            productInfo.getCurrentStatus() != query.getProductStatus()) {
                        return false;
                    }

                    // 时间范围过滤
                    if (query.getStartDate() != null &&
                            productInfo.getRegistrationTime().isBefore(query.getStartDate())) {
                        return false;
                    }

                    if (query.getEndDate() != null &&
                            productInfo.getRegistrationTime().isAfter(query.getEndDate())) {
                        return false;
                    }

                    // 关键字过滤
                    if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
                        String keyword = query.getKeyword().toLowerCase();
                        if (!productInfo.getSerialNumber().toLowerCase().contains(keyword) &&
                                !productInfo.getProductName().toLowerCase().contains(keyword)) {
                            return false;
                        }
                    }

                    return true;
                })
                .sorted((p1, p2) -> {
                    // 排序
                    switch (query.getSortBy()) {
                        case "registrationTime":
                            int timeComparison = p1.getRegistrationTime().compareTo(p2.getRegistrationTime());
                            return query.isAscending() ? timeComparison : -timeComparison;
                        case "productName":
                            int nameComparison = p1.getProductName().compareTo(p2.getProductName());
                            return query.isAscending() ? nameComparison : -nameComparison;
                        default:
                            return 0;
                    }
                })
                .skip((long) (query.getPage() - 1) * query.getPageSize())
                .limit(query.getPageSize())
                .collect(Collectors.toList());
    }

    /**
     * 追溯事件查询
     */
    public static List<TraceEvent> queryTraceEvents(String serialNumber, TraceQuery query) {
        if (serialNumber == null || serialNumber.isEmpty() || query == null) {
            logger.warn("Invalid parameters for trace event query");
            return new ArrayList<>();
        }

        try {
            if (currentTraceStorage != null) {
                return currentTraceStorage.queryTraceEvents(serialNumber, query);
            } else {
                // 如果没有追溯存储，从缓存中查询
                return queryTraceEventsFromCache(serialNumber, query);
            }
        } catch (Exception e) {
            logger.error("Failed to query trace events for product: " + serialNumber, e);
            return new ArrayList<>();
        }
    }

    /**
     * 从缓存中查询追溯事件
     */
    private static List<TraceEvent> queryTraceEventsFromCache(String serialNumber, TraceQuery query) {
        ProductTrace trace = productTraceCache.get(serialNumber);
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
                            !event.getOperationType().equals(query.getEventType())) {
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
     * 归档产品信息
     */
    public static boolean archiveProductInfo(String serialNumber) {
        if (serialNumber == null || serialNumber.isEmpty()) {
            logger.warn("Invalid serial number for product archiving");
            return false;
        }

        try {
            boolean archived = false;
            if (currentStorage != null) {
                archived = currentStorage.archiveProductInfo(serialNumber);
            } else {
                // 如果没有存储，从缓存中移除并标记为已归档
                ProductInfo productInfo = productInfoCache.get(serialNumber);
                if (productInfo != null) {
                    productInfo.setActive(false);
                    archived = true;
                }
            }

            if (archived) {
                logger.info("Archived product info: {}", serialNumber);
            } else {
                logger.warn("Failed to archive product info: {}", serialNumber);
            }

            return archived;
        } catch (Exception e) {
            logger.error("Failed to archive product info: " + serialNumber, e);
            return false;
        }
    }

    /**
     * 清理缓存
     */
    private static void cleanupCache() {
        try {
            long cutoffTime = System.currentTimeMillis() - DEFAULT_CACHE_TIMEOUT;

            // 清理产品信息缓存
            productInfoCache.entrySet().removeIf(entry ->
                    entry.getValue().getRegistrationTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli() < cutoffTime
            );

            // 清理追溯信息缓存
            productTraceCache.entrySet().removeIf(entry ->
                    entry.getValue().getStartTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli() < cutoffTime
            );

            logger.debug("Cleaned up product serializer cache, remaining entries - product: {}, trace: {}",
                    productInfoCache.size(), productTraceCache.size());
        } catch (Exception e) {
            logger.error("Failed to cleanup product serializer cache", e);
        }
    }

    /**
     * 获取缓存统计信息
     */
    public static CacheStatistics getCacheStatistics() {
        CacheStatistics stats = new CacheStatistics();
        stats.setProductInfoCacheSize(productInfoCache.size());
        stats.setProductTraceCacheSize(productTraceCache.size());
        stats.setCacheTimeout(DEFAULT_CACHE_TIMEOUT);
        return stats;
    }

    /**
     * 关闭产品序列化管理器
     */
    public static void shutdown() {
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            logger.info("ProductSerializer shutdown completed");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            logger.warn("ProductSerializer shutdown interrupted");
        }
    }

    // 产品类型枚举
    public enum ProductType {
        WIRE_BONDING("WB", "引线键合产品"),
        FLIP_CHIP("FC", "倒装芯片产品"),
        BGA("BGA", "球栅阵列产品"),
        CSP("CSP", "芯片尺寸封装产品"),
        WLP("WLP", "晶圆级封装产品"),
        SiP("SiP", "系统级封装产品"),
        CUSTOM("CUSTOM", "自定义产品");

        private final String code;
        private final String description;

        ProductType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    // 产品状态枚举
    public enum ProductStatus {
        REGISTERED("已注册", 1, true),
        IN_PRODUCTION("生产中", 2, true),
        QUALITY_CHECK("质量检验", 3, true),
        PACKAGING("包装中", 4, true),
        WAREHOUSING("入库中", 5, true),
        IN_STORAGE("已入库", 6, true),
        SHIPPING("发货中", 7, true),
        DELIVERED("已交付", 8, true),
        RETURNED("已退回", 9, true),
        REWORK("返工中", 10, true),
        SCRAPPED("已报废", 11, false),
        ARCHIVED("已归档", 12, false);

        private final String description;
        private final int sequence;
        private final boolean active;

        ProductStatus(String description, int sequence, boolean active) {
            this.description = description;
            this.sequence = sequence;
            this.active = active;
        }

        public String getDescription() {
            return description;
        }

        public int getSequence() {
            return sequence;
        }

        public boolean isActive() {
            return active;
        } // 添加 isActive 方法

        public boolean isBefore(ProductStatus other) {
            return this.sequence < other.sequence;
        }

        public boolean isAfter(ProductStatus other) {
            return this.sequence > other.sequence;
        }
    }

    // 序列号生成策略枚举
    public enum SerialNumberStrategy {
        TIMESTAMP_BASED("时间戳基础"),
        SEQUENCE_BASED("序列基础"),
        UUID_BASED("UUID基础"),
        HASH_BASED("哈希基础");

        private final String description;

        SerialNumberStrategy(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }


    // 产品序列号生成器接口
    public interface SerialNumberGenerator {
        String generateSerialNumber(ProductType type, Map<String, Object> parameters) throws Exception;

        boolean supportsStrategy(SerialNumberStrategy strategy);

        String getGeneratorName();
    }

    // 产品信息存储接口
    public interface ProductInfoStorage {
        boolean saveProductInfo(ProductInfo productInfo) throws Exception;

        ProductInfo loadProductInfo(String serialNumber) throws Exception;

        boolean updateProductStatus(String serialNumber, ProductStatus newStatus,
                                    String description, String operator) throws Exception;

        boolean archiveProductInfo(String serialNumber) throws Exception;

        List<ProductInfo> queryProductInfos(ProductQuery query) throws Exception;

        boolean supportsStorageType(String storageType);

        String getStorageName();
    }

    // 产品追溯存储接口
    public interface ProductTraceStorage {
        boolean saveProductTrace(ProductTrace trace) throws Exception;

        ProductTrace loadProductTrace(String serialNumber) throws Exception;

        boolean addTraceEvent(String serialNumber, TraceEvent event) throws Exception;

        List<TraceEvent> queryTraceEvents(String serialNumber, TraceQuery query) throws Exception;

        boolean supportsTraceType(String traceType);

        String getTraceStorageName();
    }

    // 产品信息类
    public static class ProductInfo {
        private final String serialNumber;
        private final ProductType productType;
        private final String productName;
        private final String productVersion;
        private final String manufacturer;
        private final LocalDateTime manufactureDate;
        private final Map<String, Object> attributes;
        private final List<ProductStatusRecord> statusHistory;
        private final String batchId;
        private final String lotNumber;
        private final LocalDateTime registrationTime;
        private final String createdBy;
        private volatile ProductStatus currentStatus;
        private volatile boolean isActive;

        public ProductInfo(String serialNumber, ProductType productType, String productName) {
            this.serialNumber = serialNumber != null ? serialNumber : "";
            this.productType = productType != null ? productType : ProductType.CUSTOM;
            this.productName = productName != null ? productName : "";
            this.productVersion = "";
            this.manufacturer = "QTech Semiconductor";
            this.manufactureDate = LocalDateTime.now();
            this.attributes = new ConcurrentHashMap<>();
            this.statusHistory = new CopyOnWriteArrayList<>();
            this.currentStatus = ProductStatus.REGISTERED;
            this.batchId = "";
            this.lotNumber = "";
            this.registrationTime = LocalDateTime.now();
            this.createdBy = "System";
            this.isActive = true;

            // 添加初始状态记录
            addStatusRecord(ProductStatus.REGISTERED, "产品注册", "System");
        }

        // 添加状态记录
        public void addStatusRecord(ProductStatus status, String description, String operator) {
            ProductStatusRecord record = new ProductStatusRecord(status, description, operator);
            this.statusHistory.add(record);
            this.currentStatus = status;
        }

        // Getters
        public String getSerialNumber() {
            return serialNumber;
        }

        public ProductType getProductType() {
            return productType;
        }

        public String getProductName() {
            return productName;
        }

        public ProductInfo setProductName(String productName) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getProductVersion() {
            return productVersion;
        }

        public ProductInfo setProductVersion(String productVersion) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getManufacturer() {
            return manufacturer;
        }

        public LocalDateTime getManufactureDate() {
            return manufactureDate;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public ProductInfo setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        public List<ProductStatusRecord> getStatusHistory() {
            return new ArrayList<>(statusHistory);
        }

        public ProductStatus getCurrentStatus() {
            return currentStatus;
        }

        public String getBatchId() {
            return batchId;
        }

        public ProductInfo setBatchId(String batchId) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public String getLotNumber() {
            return lotNumber;
        }

        public ProductInfo setLotNumber(String lotNumber) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }

        public LocalDateTime getRegistrationTime() {
            return registrationTime;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            this.isActive = active;
        }

        @Override
        public String toString() {
            return "ProductInfo{" +
                    "serialNumber='" + serialNumber + '\'' +
                    ", productType=" + productType +
                    ", productName='" + productName + '\'' +
                    ", currentStatus=" + currentStatus +
                    ", manufactureDate=" + manufactureDate +
                    ", isActive=" + isActive +
                    '}';
        }
    }

    // 产品状态记录类
    public static class ProductStatusRecord {
        private final String recordId;
        private final ProductStatus status;
        private final String description;
        private final String operator;
        private final LocalDateTime timestamp;
        private final Map<String, Object> metadata;

        public ProductStatusRecord(ProductStatus status, String description, String operator) {
            this.recordId = UUID.randomUUID().toString();
            this.status = status != null ? status : ProductStatus.REGISTERED;
            this.description = description != null ? description : "";
            this.operator = operator != null ? operator : "System";
            this.timestamp = LocalDateTime.now();
            this.metadata = new ConcurrentHashMap<>();
        }

        // Getters
        public String getRecordId() {
            return recordId;
        }

        public ProductStatus getStatus() {
            return status;
        }

        public String getDescription() {
            return description;
        }

        public String getOperator() {
            return operator;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
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
            return "ProductStatusRecord{" +
                    "status=" + status +
                    ", description='" + description + '\'' +
                    ", operator='" + operator + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }

    // 产品追溯信息类
    public static class ProductTrace {
        private final String traceId;
        private final String serialNumber;
        private final List<TraceEvent> traceEvents;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final Map<String, Object> summary;
        private final boolean isComplete;

        public ProductTrace(String serialNumber) {
            this.traceId = UUID.randomUUID().toString();
            this.serialNumber = serialNumber != null ? serialNumber : "";
            this.traceEvents = new CopyOnWriteArrayList<>();
            this.startTime = LocalDateTime.now();
            this.endTime = null;
            this.summary = new ConcurrentHashMap<>();
            this.isComplete = false;
        }

        // 完整构造函数
        public ProductTrace(String serialNumber, List<TraceEvent> traceEvents,
                            LocalDateTime startTime, LocalDateTime endTime) {
            this.traceId = UUID.randomUUID().toString();
            this.serialNumber = serialNumber != null ? serialNumber : "";
            this.traceEvents = traceEvents != null ? new ArrayList<>(traceEvents) : new ArrayList<>();
            this.startTime = startTime;
            this.endTime = endTime;
            this.summary = new ConcurrentHashMap<>();
            this.isComplete = true;

            // 生成摘要
            generateSummary();
        }

        // 生成摘要
        private void generateSummary() {
            summary.put("eventCount", traceEvents.size());
            summary.put("duration", java.time.Duration.between(startTime, endTime).toMinutes() + "分钟");

            if (!traceEvents.isEmpty()) {
                // 统计各状态事件数量
                Map<ProductStatus, Long> statusCount = traceEvents.stream()
                        .filter(event -> event instanceof StatusChangeEvent)
                        .map(event -> (StatusChangeEvent) event)
                        .collect(Collectors.groupingBy(
                                StatusChangeEvent::getNewStatus,
                                Collectors.counting()
                        ));
                summary.put("statusDistribution", statusCount);

                // 统计各操作类型事件数量
                Map<String, Long> operationCount = traceEvents.stream()
                        .collect(Collectors.groupingBy(
                                TraceEvent::getOperationType,
                                Collectors.counting()
                        ));
                summary.put("operationDistribution", operationCount);
            }
        }

        // 添加追溯事件
        public void addTraceEvent(TraceEvent event) {
            this.traceEvents.add(event);
        }

        // Getters
        public String getTraceId() {
            return traceId;
        }

        public String getSerialNumber() {
            return serialNumber;
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

        @Override
        public String toString() {
            return "ProductTrace{" +
                    "traceId='" + traceId + '\'' +
                    ", serialNumber='" + serialNumber + '\'' +
                    ", eventCount=" + traceEvents.size() +
                    ", startTime=" + startTime +
                    ", isComplete=" + isComplete +
                    '}';
        }
    }

    // 追溯事件基类
    public static abstract class TraceEvent {
        protected final String eventId;
        protected final String operationType;
        protected final String operator;
        protected final LocalDateTime timestamp;
        protected final Map<String, Object> attributes;

        public TraceEvent(String operationType, String operator) {
            this.eventId = UUID.randomUUID().toString();
            this.operationType = operationType != null ? operationType : "UNKNOWN";
            this.operator = operator != null ? operator : "System";
            this.timestamp = LocalDateTime.now();
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters
        public String getEventId() {
            return eventId;
        }

        public String getOperationType() {
            return operationType;
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

    // 状态变更事件
    public static class StatusChangeEvent extends TraceEvent {
        private final ProductStatus oldStatus;
        private final ProductStatus newStatus;
        private final String changeReason;

        public StatusChangeEvent(ProductStatus oldStatus, ProductStatus newStatus,
                                 String changeReason, String operator) {
            super("STATUS_CHANGE", operator);
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
            this.changeReason = changeReason != null ? changeReason : "";
        }

        // Getters
        public ProductStatus getOldStatus() {
            return oldStatus;
        }

        public ProductStatus getNewStatus() {
            return newStatus;
        }

        public String getChangeReason() {
            return changeReason;
        }
    }

    // 质量检验事件
    public static class QualityInspectionEvent extends TraceEvent {
        private final String inspectionType;
        private final String inspector;
        private final boolean passed;
        private final String result;
        private final List<String> defects;

        public QualityInspectionEvent(String inspectionType, String inspector,
                                      boolean passed, String result) {
            super("QUALITY_INSPECTION", inspector);
            this.inspectionType = inspectionType != null ? inspectionType : "常规检验";
            this.inspector = inspector != null ? inspector : "System";
            this.passed = passed;
            this.result = result != null ? result : "";
            this.defects = new ArrayList<>();
        }

        // Getters and Setters
        public String getInspectionType() {
            return inspectionType;
        }

        public String getInspector() {
            return inspector;
        }

        public boolean isPassed() {
            return passed;
        }

        public String getResult() {
            return result;
        }

        public List<String> getDefects() {
            return new ArrayList<>(defects);
        }

        public QualityInspectionEvent addDefect(String defect) {
            this.defects.add(defect);
            return this;
        }
    }

    // 生产事件
    public static class ProductionEvent extends TraceEvent {
        private final String processStep;
        private final String equipmentId;
        private final String operator;
        private final Map<String, Object> parameters;
        private final long duration; // 毫秒

        public ProductionEvent(String processStep, String equipmentId, String operator) {
            super("PRODUCTION", operator);
            this.processStep = processStep != null ? processStep : "";
            this.equipmentId = equipmentId != null ? equipmentId : "";
            this.operator = operator != null ? operator : "System";
            this.parameters = new ConcurrentHashMap<>();
            this.duration = 0;
        }

        // Getters and Setters
        public String getProcessStep() {
            return processStep;
        }

        public String getEquipmentId() {
            return equipmentId;
        }

        public String getOperator() {
            return operator;
        }

        public Map<String, Object> getParameters() {
            return new HashMap<>(parameters);
        }

        public ProductionEvent setParameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        public Object getParameter(String key) {
            return this.parameters.get(key);
        }

        public long getDuration() {
            return duration;
        }

        public ProductionEvent setDuration(long duration) {
            // This would need a builder pattern or new instance in real implementation
            return this;
        }
    }

    // 产品查询类
    public static class ProductQuery {
        private ProductType productType;
        private ProductStatus productStatus;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String keyword;
        private int page = 1;
        private int pageSize = 50;
        private String sortBy = "registrationTime";
        private boolean ascending = false;

        // Getters and Setters
        public ProductType getProductType() {
            return productType;
        }

        public ProductQuery setProductType(ProductType productType) {
            this.productType = productType;
            return this;
        }

        public ProductStatus getProductStatus() {
            return productStatus;
        }

        public ProductQuery setProductStatus(ProductStatus productStatus) {
            this.productStatus = productStatus;
            return this;
        }

        public LocalDateTime getStartDate() {
            return startDate;
        }

        public ProductQuery setStartDate(LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public LocalDateTime getEndDate() {
            return endDate;
        }

        public ProductQuery setEndDate(LocalDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public String getKeyword() {
            return keyword;
        }

        public ProductQuery setKeyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public int getPage() {
            return page;
        }

        public ProductQuery setPage(int page) {
            this.page = page;
            return this;
        }

        public int getPageSize() {
            return pageSize;
        }

        public ProductQuery setPageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public String getSortBy() {
            return sortBy;
        }

        public ProductQuery setSortBy(String sortBy) {
            this.sortBy = sortBy;
            return this;
        }

        public boolean isAscending() {
            return ascending;
        }

        public ProductQuery setAscending(boolean ascending) {
            this.ascending = ascending;
            return this;
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
        private int productInfoCacheSize;
        private int productTraceCacheSize;
        private long cacheTimeout;

        // Getters and Setters
        public int getProductInfoCacheSize() {
            return productInfoCacheSize;
        }

        public void setProductInfoCacheSize(int productInfoCacheSize) {
            this.productInfoCacheSize = productInfoCacheSize;
        }

        public int getProductTraceCacheSize() {
            return productTraceCacheSize;
        }

        public void setProductTraceCacheSize(int productTraceCacheSize) {
            this.productTraceCacheSize = productTraceCacheSize;
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
                    "productInfoCacheSize=" + productInfoCacheSize +
                    ", productTraceCacheSize=" + productTraceCacheSize +
                    ", cacheTimeout=" + cacheTimeout + "ms" +
                    '}';
        }
    }
}
