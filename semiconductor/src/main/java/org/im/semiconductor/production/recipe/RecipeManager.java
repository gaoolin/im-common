package org.im.semiconductor.production.recipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 工艺配方管理工具类
 * <p>
 * 特性：
 * - 通用性：支持多种工艺配方类型和管理方法
 * - 规范性：统一的配方定义和管理标准
 * - 专业性：半导体行业工艺配方专业管理实现
 * - 灵活性：可配置的配方参数和验证规则
 * - 可靠性：完善的版本控制和备份机制
 * - 安全性：配方数据保护和访问控制
 * - 复用性：模块化设计，组件可独立使用
 * - 容错性：优雅的错误处理和恢复机制
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/08/21
 */
public class RecipeManager {

    // 默认配置
    public static final long DEFAULT_CACHE_TIMEOUT = 30 * 60 * 1000; // 30分钟
    public static final int DEFAULT_MAX_VERSIONS = 50;
    public static final int DEFAULT_MAX_RETRY_ATTEMPTS = 3;
    public static final long DEFAULT_RETRY_DELAY = 1000; // 1秒
    public static final int DEFAULT_OPTIMIZATION_ITERATIONS = 100;
    public static final double DEFAULT_CONVERGENCE_THRESHOLD = 0.001;
    private static final Logger logger = LoggerFactory.getLogger(RecipeManager.class);
    // 内部存储和管理
    private static final Map<String, RecipeLoader> loaderRegistry = new ConcurrentHashMap<>();
    private static final Map<RecipeType, RecipeValidator> validatorRegistry = new ConcurrentHashMap<>();
    private static final Map<OptimizationAlgorithm, ParameterOptimizer> optimizerRegistry = new ConcurrentHashMap<>();
    private static final Map<String, VersionController> versionControllerRegistry = new ConcurrentHashMap<>();
    private static final Map<String, ApprovalManager> approvalManagerRegistry = new ConcurrentHashMap<>();
    private static final Map<String, ProcessRecipe> recipeCache = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private static final AtomicLong optimizationCounter = new AtomicLong(0);
    // 当前使用的组件
    private static volatile RecipeLoader currentLoader;
    private static volatile RecipeValidator currentValidator;
    private static volatile ParameterOptimizer currentOptimizer;
    private static volatile VersionController currentVersionController;
    private static volatile ApprovalManager currentApprovalManager;

    // 初始化默认组件
    static {
        registerDefaultComponents();
        initializeCurrentComponents();
        startMaintenanceTasks();
        logger.info("RecipeManager initialized with default components");
    }

    /**
     * 注册默认组件
     */
    private static void registerDefaultComponents() {
        // 注册默认加载器、验证器、优化器、版本控制器和审批管理器
        // registerLoader(new DefaultRecipeLoader());
        // registerValidator(new DefaultRecipeValidator());
        // registerOptimizer(new DefaultParameterOptimizer());
        // registerVersionController(new DefaultVersionController());
        // registerApprovalManager(new DefaultApprovalManager());
    }

    /**
     * 初始化当前组件
     */
    private static void initializeCurrentComponents() {
        // 初始化为第一个注册的组件
        if (!loaderRegistry.isEmpty()) {
            currentLoader = loaderRegistry.values().iterator().next();
        }

        if (!validatorRegistry.isEmpty()) {
            currentValidator = validatorRegistry.values().iterator().next();
        }

        if (!optimizerRegistry.isEmpty()) {
            currentOptimizer = optimizerRegistry.values().iterator().next();
        }

        if (!versionControllerRegistry.isEmpty()) {
            currentVersionController = versionControllerRegistry.values().iterator().next();
        }

        if (!approvalManagerRegistry.isEmpty()) {
            currentApprovalManager = approvalManagerRegistry.values().iterator().next();
        }
    }

    /**
     * 启动维护任务
     */
    private static void startMaintenanceTasks() {
        // 启动缓存清理任务
        scheduler.scheduleAtFixedRate(RecipeManager::cleanupCache,
                30, 30, TimeUnit.MINUTES);

        logger.debug("Recipe manager maintenance tasks started");
    }

    /**
     * 注册配方加载器
     */
    public static void registerLoader(RecipeLoader loader) {
        if (loader == null) {
            throw new IllegalArgumentException("Loader cannot be null");
        }

        // 注册支持的所有存储类型
        String[] storageTypes = {"database", "file", "memory", "cloud"}; // 简化实现
        for (String type : storageTypes) {
            if (loader.supportsStorageType(type)) {
                loaderRegistry.put(type, loader);
                logger.debug("Registered loader {} for storage type {}", loader.getLoaderName(), type);
            }
        }
    }

    /**
     * 注册配方验证器
     */
    public static void registerValidator(RecipeValidator validator) {
        if (validator == null) {
            throw new IllegalArgumentException("Validator cannot be null");
        }

        for (RecipeType type : RecipeType.values()) {
            if (validator.supportsRecipeType(type)) {
                validatorRegistry.put(type, validator);
                logger.debug("Registered validator {} for recipe type {}", validator.getValidatorName(), type);
            }
        }
    }

    /**
     * 注册参数优化器
     */
    public static void registerOptimizer(ParameterOptimizer optimizer) {
        if (optimizer == null) {
            throw new IllegalArgumentException("Optimizer cannot be null");
        }

        for (OptimizationAlgorithm algorithm : OptimizationAlgorithm.values()) {
            if (optimizer.supportsAlgorithm(algorithm)) {
                optimizerRegistry.put(algorithm, optimizer);
                logger.debug("Registered optimizer {} for algorithm {}", optimizer.getOptimizerName(), algorithm);
            }
        }
    }

    /**
     * 注册版本控制器
     */
    public static void registerVersionController(VersionController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("Version controller cannot be null");
        }

        // 注册支持的所有版本控制类型
        String[] versionControlTypes = {"git", "database", "file"}; // 简化实现
        for (String type : versionControlTypes) {
            if (controller.supportsVersionControl(type)) {
                versionControllerRegistry.put(type, controller);
                logger.debug("Registered version controller {} for type {}", controller.getVersionControllerName(), type);
            }
        }
    }

    /**
     * 注册审批管理器
     */
    public static void registerApprovalManager(ApprovalManager manager) {
        if (manager == null) {
            throw new IllegalArgumentException("Approval manager cannot be null");
        }

        // 注册支持的所有审批类型
        String[] approvalTypes = {"sequential", "parallel", "hierarchical"}; // 简化实现
        for (String type : approvalTypes) {
            if (manager.supportsApprovalType(type)) {
                approvalManagerRegistry.put(type, manager);
                logger.debug("Registered approval manager {} for type {}", manager.getApprovalManagerName(), type);
            }
        }
    }

    /**
     * 设置当前配方加载器
     */
    public static void setCurrentLoader(String storageType) {
        RecipeLoader loader = loaderRegistry.get(storageType);
        if (loader != null) {
            currentLoader = loader;
            logger.info("Current recipe loader set to: {}", loader.getLoaderName());
        } else {
            logger.warn("No loader found for storage type: {}", storageType);
        }
    }

    /**
     * 设置当前配方验证器
     */
    public static void setCurrentValidator(RecipeType recipeType) {
        RecipeValidator validator = validatorRegistry.get(recipeType);
        if (validator != null) {
            currentValidator = validator;
            logger.info("Current recipe validator set to: {} for recipe type {}",
                    validator.getValidatorName(), recipeType);
        } else {
            logger.warn("No validator found for recipe type: {}", recipeType);
        }
    }

    /**
     * 设置当前参数优化器
     */
    public static void setCurrentOptimizer(OptimizationAlgorithm algorithm) {
        ParameterOptimizer optimizer = optimizerRegistry.get(algorithm);
        if (optimizer != null) {
            currentOptimizer = optimizer;
            logger.info("Current parameter optimizer set to: {} for algorithm {}",
                    optimizer.getOptimizerName(), algorithm);
        } else {
            logger.warn("No optimizer found for algorithm: {}", algorithm);
        }
    }

    /**
     * 设置当前版本控制器
     */
    public static void setCurrentVersionController(String versionControlType) {
        VersionController controller = versionControllerRegistry.get(versionControlType);
        if (controller != null) {
            currentVersionController = controller;
            logger.info("Current version controller set to: {}", controller.getVersionControllerName());
        } else {
            logger.warn("No version controller found for type: {}", versionControlType);
        }
    }

    /**
     * 设置当前审批管理器
     */
    public static void setCurrentApprovalManager(String approvalType) {
        ApprovalManager manager = approvalManagerRegistry.get(approvalType);
        if (manager != null) {
            currentApprovalManager = manager;
            logger.info("Current approval manager set to: {}", manager.getApprovalManagerName());
        } else {
            logger.warn("No approval manager found for type: {}", approvalType);
        }
    }

    /**
     * 工艺配方加载和验证
     */
    public static ProcessRecipe loadRecipe(String recipeId) {
        return loadRecipe(recipeId, true);
    }

    /**
     * 工艺配方加载（可选验证）
     */
    public static ProcessRecipe loadRecipe(String recipeId, boolean validate) {
        if (recipeId == null || recipeId.isEmpty()) {
            logger.warn("Invalid recipe ID for loading");
            return null;
        }

        try {
            // 首先检查缓存
            ProcessRecipe cached = recipeCache.get(recipeId);
            if (cached != null && !isCacheExpired(cached)) {
                logger.debug("Returning cached recipe: {}", recipeId);
                if (validate) {
                    validateRecipe(cached);
                }
                return cached;
            }

            // 从存储中加载
            ProcessRecipe recipe = null;
            if (currentLoader != null) {
                recipe = currentLoader.loadRecipe(recipeId);
            }

            // 如果存储中没有且缓存中有，则返回缓存的
            if (recipe == null) {
                recipe = cached;
            }

            // 验证配方
            if (recipe != null && validate) {
                validateRecipe(recipe);
            }

            // 更新缓存
            if (recipe != null) {
                recipeCache.put(recipeId, recipe);
            }

            if (recipe != null) {
                logger.debug("Loaded recipe: {} - {}", recipeId, recipe.getRecipeName());
            } else {
                logger.warn("Recipe not found: {}", recipeId);
            }

            return recipe;
        } catch (Exception e) {
            logger.error("Failed to load recipe: " + recipeId, e);
            return null;
        }
    }

    /**
     * 检查缓存是否过期
     */
    private static boolean isCacheExpired(ProcessRecipe recipe) {
        return System.currentTimeMillis() - recipe.getLastModifiedTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
                > DEFAULT_CACHE_TIMEOUT;
    }

    /**
     * 配方验证
     */
    public static List<ValidationError> validateRecipe(ProcessRecipe recipe) {
        if (recipe == null) {
            logger.warn("Invalid recipe for validation");
            return Arrays.asList(new ValidationError("INVALID_RECIPE", "配方为空", true));
        }

        try {
            List<ValidationError> errors = new ArrayList<>();

            // 使用注册的验证器进行验证
            RecipeValidator validator = validatorRegistry.get(recipe.getRecipeType());
            if (validator != null) {
                errors.addAll(validator.validateRecipe(recipe));
            } else if (currentValidator != null) {
                // 使用当前验证器
                errors.addAll(currentValidator.validateRecipe(recipe));
            } else {
                // 执行基本验证
                errors.addAll(performBasicValidation(recipe));
            }

            // 记录验证结果
            if (errors.isEmpty()) {
                logger.debug("Recipe validation passed: {} - {}", recipe.getRecipeId(), recipe.getRecipeName());
            } else {
                long criticalErrors = errors.stream().filter(ValidationError::isCritical).count();
                logger.warn("Recipe validation completed with {} errors ({} critical) for recipe: {} - {}",
                        errors.size(), criticalErrors, recipe.getRecipeId(), recipe.getRecipeName());
            }

            return errors;
        } catch (Exception e) {
            logger.error("Failed to validate recipe: " + recipe.getRecipeId(), e);
            return Arrays.asList(new ValidationError("VALIDATION_ERROR", "验证过程出错: " + e.getMessage(), true));
        }
    }

    /**
     * 执行基本验证
     */
    private static List<ValidationError> performBasicValidation(ProcessRecipe recipe) {
        List<ValidationError> errors = new ArrayList<>();

        // 检查必需字段
        if (recipe.getRecipeName() == null || recipe.getRecipeName().isEmpty()) {
            errors.add(new ValidationError("MISSING_NAME", "配方名称不能为空", true)
                    .setField("recipeName"));
        }

        if (recipe.getRecipeType() == null) {
            errors.add(new ValidationError("MISSING_TYPE", "配方类型不能为空", true)
                    .setField("recipeType"));
        }

        // 验证参数
        for (RecipeParameter parameter : recipe.getParameters().values()) {
            if (parameter.getParameterName() == null || parameter.getParameterName().isEmpty()) {
                errors.add(new ValidationError("MISSING_PARAMETER_NAME",
                        "参数名称不能为空: " + parameter.getParameterId(), true)
                        .setField("parameterName"));
            }
        }

        // 验证步骤
        for (RecipeStep step : recipe.getSteps()) {
            if (step.getStepName() == null || step.getStepName().isEmpty()) {
                errors.add(new ValidationError("MISSING_STEP_NAME",
                        "步骤名称不能为空: " + step.getStepId(), true)
                        .setField("stepName"));
            }
        }

        return errors;
    }

    /**
     * 工艺参数优化
     */
    public static OptimizedParameters optimizeParameters(ProcessRecipe recipe, HistoricalData data) {
        return optimizeParameters(recipe, data, OptimizationAlgorithm.GENETIC_ALGORITHM);
    }

    /**
     * 工艺参数优化（指定算法）
     */
    public static OptimizedParameters optimizeParameters(ProcessRecipe recipe, HistoricalData data,
                                                         OptimizationAlgorithm algorithm) {
        if (recipe == null) {
            logger.warn("Invalid recipe for optimization");
            return null;
        }

        if (data == null || data.getDataPoints().isEmpty()) {
            logger.warn("Invalid historical data for optimization");
            return null;
        }

        if (algorithm == null) {
            algorithm = OptimizationAlgorithm.GENETIC_ALGORITHM;
        }

        try {
            long startTime = System.currentTimeMillis();

            // 使用注册的优化器进行优化
            ParameterOptimizer optimizer = optimizerRegistry.get(algorithm);
            OptimizedParameters optimized = null;

            if (optimizer != null) {
                optimized = optimizer.optimize(recipe, data);
            } else if (currentOptimizer != null) {
                // 使用当前优化器
                optimized = currentOptimizer.optimize(recipe, data);
            } else {
                // 使用默认优化算法
                optimized = performDefaultOptimization(recipe, data);
            }

            long duration = System.currentTimeMillis() - startTime;

            if (optimized != null) {
                optimized.setIterations((int) optimizationCounter.incrementAndGet())
                        .setStatistic("optimizationTime", duration)
                        .setStatistic("dataPointCount", data.getDataPoints().size());

                logger.info("Parameter optimization completed inspection {}ms using {} for recipe: {}",
                        duration, algorithm.getChineseName(), recipe.getRecipeId());
            } else {
                logger.warn("Parameter optimization failed for recipe: {}", recipe.getRecipeId());
            }

            return optimized;
        } catch (Exception e) {
            logger.error("Failed to optimize parameters for recipe: " + recipe.getRecipeId(), e);
            return null;
        }
    }

    /**
     * 执行默认优化算法
     */
    private static OptimizedParameters performDefaultOptimization(ProcessRecipe recipe, HistoricalData data) {
        try {
            // 简化的遗传算法实现示例
            OptimizedParameters result = new OptimizedParameters(OptimizationAlgorithm.GENETIC_ALGORITHM, 0.0);

            // 获取配方参数
            Map<String, RecipeParameter> parameters = recipe.getParameters();
            if (parameters.isEmpty()) {
                logger.warn("No parameters found inspection recipe for optimization");
                return result;
            }

            // 基于历史数据计算最优参数（简化实现）
            List<DataPoint> dataPoints = data.getDataPoints();
            if (dataPoints.isEmpty()) {
                logger.warn("No data points found for optimization");
                return result;
            }

            // 计算每个参数的最优值（基于高质量数据点的平均值）
            double bestQuality = dataPoints.stream()
                    .mapToDouble(DataPoint::getQualityScore)
                    .max()
                    .orElse(0.0);

            // 找到高质量数据点
            List<DataPoint> bestDataPoints = dataPoints.stream()
                    .filter(dp -> dp.getQualityScore() >= bestQuality * 0.9) // 90%以上的高质量点
                    .collect(Collectors.toList());

            if (bestDataPoints.isEmpty()) {
                logger.warn("No high quality data points found for optimization");
                return result;
            }

            // 计算平均参数值
            for (String paramId : parameters.keySet()) {
                double avgValue = bestDataPoints.stream()
                        .mapToDouble(dp -> {
                            Object value = dp.getParameters().get(paramId);
                            if (value instanceof Number) {
                                return ((Number) value).doubleValue();
                            }
                            return 0.0;
                        })
                        .average()
                        .orElse(0.0);

                result.setOptimizedValue(paramId, avgValue);
            }

            result.setConverged(true)
                    .setStatistic("bestQuality", bestQuality)
                    .setStatistic("bestDataPointCount", bestDataPoints.size());

            return result;
        } catch (Exception e) {
            logger.warn("Failed to perform default optimization", e);
            return null;
        }
    }

    /**
     * 工艺配方版本控制
     */
    public static RecipeVersionHistory getRecipeHistory(String recipeId) {
        if (recipeId == null || recipeId.isEmpty()) {
            logger.warn("Invalid recipe ID for history retrieval");
            return null;
        }

        try {
            if (currentVersionController != null) {
                RecipeVersionHistory history = currentVersionController.getRecipeHistory(recipeId);
                if (history != null) {
                    logger.debug("Retrieved recipe history for: {}", recipeId);
                    return history;
                }
            }

            logger.warn("Failed to retrieve recipe history: {}", recipeId);
            return null;
        } catch (Exception e) {
            logger.error("Failed to get recipe history: " + recipeId, e);
            return null;
        }
    }

    /**
     * 获取特定版本的配方
     */
    public static ProcessRecipe getRecipeVersion(String recipeId, String version) {
        if (recipeId == null || recipeId.isEmpty() || version == null || version.isEmpty()) {
            logger.warn("Invalid parameters for recipe version retrieval");
            return null;
        }

        try {
            if (currentVersionController != null) {
                ProcessRecipe recipe = currentVersionController.getVersion(recipeId, version);
                if (recipe != null) {
                    logger.debug("Retrieved recipe version: {} - {}", recipeId, version);
                    return recipe;
                }
            }

            logger.warn("Failed to retrieve recipe version: {} - {}", recipeId, version);
            return null;
        } catch (Exception e) {
            logger.error("Failed to get recipe version: " + recipeId + " - " + version, e);
            return null;
        }
    }

    /**
     * 保存配方版本
     */
    public static boolean saveRecipeVersion(ProcessRecipe recipe) {
        if (recipe == null) {
            logger.warn("Invalid recipe for version saving");
            return false;
        }

        try {
            boolean saved = false;
            if (currentVersionController != null) {
                saved = currentVersionController.saveVersion(recipe);
            }

            if (saved) {
                logger.info("Saved recipe version: {} - {}", recipe.getRecipeId(), recipe.getVersion());
            } else {
                logger.warn("Failed to save recipe version: {} - {}", recipe.getRecipeId(), recipe.getVersion());
            }

            return saved;
        } catch (Exception e) {
            logger.error("Failed to save recipe version: " + recipe.getRecipeId(), e);
            return false;
        }
    }

    /**
     * 工艺配方审批流程
     */
    public static boolean submitRecipeForApproval(ProcessRecipe recipe) {
        if (recipe == null) {
            logger.warn("Invalid recipe for approval submission");
            return false;
        }

        try {
            // 首先验证配方
            List<ValidationError> validationErrors = validateRecipe(recipe);
            if (validationErrors.stream().anyMatch(ValidationError::isCritical)) {
                logger.warn("Recipe has critical validation errors, cannot submit for approval: {}",
                        recipe.getRecipeId());
                return false;
            }

            boolean submitted = false;
            if (currentApprovalManager != null) {
                submitted = currentApprovalManager.submitForApproval(recipe);
            }

            if (submitted) {
                recipe.setStatus(RecipeStatus.SUBMITTED);
                logger.info("Submitted recipe for approval: {} - {}", recipe.getRecipeId(), recipe.getRecipeName());
            } else {
                logger.warn("Failed to submit recipe for approval: {} - {}", recipe.getRecipeId(), recipe.getRecipeName());
            }

            return submitted;
        } catch (Exception e) {
            logger.error("Failed to submit recipe for approval: " + recipe.getRecipeId(), e);
            return false;
        }
    }

    /**
     * 获取审批工作流
     */
    public static ApprovalWorkflow getApprovalWorkflow(String recipeId) {
        if (recipeId == null || recipeId.isEmpty()) {
            logger.warn("Invalid recipe ID for approval workflow retrieval");
            return null;
        }

        try {
            if (currentApprovalManager != null) {
                ApprovalWorkflow workflow = currentApprovalManager.getApprovalWorkflow(recipeId);
                if (workflow != null) {
                    logger.debug("Retrieved approval workflow for recipe: {}", recipeId);
                    return workflow;
                }
            }

            logger.warn("Failed to retrieve approval workflow: {}", recipeId);
            return null;
        } catch (Exception e) {
            logger.error("Failed to get approval workflow: " + recipeId, e);
            return null;
        }
    }

    /**
     * 批准配方
     */
    public static boolean approveRecipe(String recipeId, String approver, String comments) {
        if (recipeId == null || recipeId.isEmpty() || approver == null || approver.isEmpty()) {
            logger.warn("Invalid parameters for recipe approval");
            return false;
        }

        try {
            boolean approved = false;
            if (currentApprovalManager != null) {
                approved = currentApprovalManager.approveRecipe(recipeId, approver, comments);
            }

            if (approved) {
                // 更新缓存中的配方状态
                ProcessRecipe recipe = recipeCache.get(recipeId);
                if (recipe != null) {
                    recipe.setStatus(RecipeStatus.APPROVED);
                }

                logger.info("Approved recipe: {} by {}", recipeId, approver);
            } else {
                logger.warn("Failed to approve recipe: {} by {}", recipeId, approver);
            }

            return approved;
        } catch (Exception e) {
            logger.error("Failed to approve recipe: " + recipeId, e);
            return false;
        }
    }

    /**
     * 拒绝配方
     */
    public static boolean rejectRecipe(String recipeId, String approver, String comments) {
        if (recipeId == null || recipeId.isEmpty() || approver == null || approver.isEmpty()) {
            logger.warn("Invalid parameters for recipe rejection");
            return false;
        }

        try {
            boolean rejected = false;
            if (currentApprovalManager != null) {
                rejected = currentApprovalManager.rejectRecipe(recipeId, approver, comments);
            }

            if (rejected) {
                // 更新缓存中的配方状态
                ProcessRecipe recipe = recipeCache.get(recipeId);
                if (recipe != null) {
                    recipe.setStatus(RecipeStatus.REJECTED);
                }

                logger.info("Rejected recipe: {} by {}", recipeId, approver);
            } else {
                logger.warn("Failed to reject recipe: {} by {}", recipeId, approver);
            }

            return rejected;
        } catch (Exception e) {
            logger.error("Failed to reject recipe: " + recipeId, e);
            return false;
        }
    }

    /**
     * 创建新配方
     */
    public static ProcessRecipe createRecipe(String recipeName, RecipeType recipeType, String version) {
        if (recipeName == null || recipeName.isEmpty() || recipeType == null) {
            logger.warn("Invalid parameters for recipe creation");
            return null;
        }

        try {
            ProcessRecipe recipe = new ProcessRecipe(null, recipeName, recipeType, version);
            recipe.setStatus(RecipeStatus.DRAFT);

            // 添加到缓存
            recipeCache.put(recipe.getRecipeId(), recipe);

            logger.info("Created new recipe: {} - {} (Type: {}, Version: {})",
                    recipe.getRecipeId(), recipeName, recipeType, version);

            return recipe;
        } catch (Exception e) {
            logger.error("Failed to create recipe: " + recipeName, e);
            return null;
        }
    }

    /**
     * 更新配方
     */
    public static boolean updateRecipe(ProcessRecipe recipe) {
        if (recipe == null) {
            logger.warn("Invalid recipe for update");
            return false;
        }

        try {
            recipeCache.put(recipe.getRecipeId(), recipe);
            logger.debug("Updated recipe inspection cache: {}", recipe.getRecipeId());
            return true;
        } catch (Exception e) {
            logger.error("Failed to update recipe: " + recipe.getRecipeId(), e);
            return false;
        }
    }

    /**
     * 删除配方
     */
    public static boolean deleteRecipe(String recipeId) {
        if (recipeId == null || recipeId.isEmpty()) {
            logger.warn("Invalid recipe ID for deletion");
            return false;
        }

        try {
            ProcessRecipe removed = recipeCache.remove(recipeId);
            if (removed != null) {
                logger.info("Deleted recipe: {}", recipeId);
                return true;
            } else {
                logger.warn("Recipe not found for deletion: {}", recipeId);
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to delete recipe: " + recipeId, e);
            return false;
        }
    }

    /**
     * 查询配方
     */
    public static List<ProcessRecipe> queryRecipes(RecipeQuery query) {
        if (query == null) {
            logger.warn("Invalid query for recipe search");
            return new ArrayList<>();
        }

        try {
            return recipeCache.values().stream()
                    .filter(recipe -> {
                        // 类型过滤
                        if (query.getRecipeType() != null && recipe.getRecipeType() != query.getRecipeType()) {
                            return false;
                        }

                        // 状态过滤
                        if (query.getStatus() != null && recipe.getStatus() != query.getStatus()) {
                            return false;
                        }

                        // 名称关键字过滤
                        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
                            String keyword = query.getKeyword().toLowerCase();
                            if (!recipe.getRecipeName().toLowerCase().contains(keyword) &&
                                    !recipe.getRecipeId().toLowerCase().contains(keyword)) {
                                return false;
                            }
                        }

                        // 时间范围过滤
                        if (query.getStartDate() != null && recipe.getCreateTime().isBefore(query.getStartDate())) {
                            return false;
                        }

                        if (query.getEndDate() != null && recipe.getCreateTime().isAfter(query.getEndDate())) {
                            return false;
                        }

                        return true;
                    })
                    .sorted((r1, r2) -> {
                        // 排序
                        switch (query.getSortBy()) {
                            case "createTime":
                                int timeComparison = r1.getCreateTime().compareTo(r2.getCreateTime());
                                return query.isAscending() ? timeComparison : -timeComparison;
                            case "recipeName":
                                int nameComparison = r1.getRecipeName().compareTo(r2.getRecipeName());
                                return query.isAscending() ? nameComparison : -nameComparison;
                            default:
                                return 0;
                        }
                    })
                    .skip((long) (query.getPage() - 1) * query.getPageSize())
                    .limit(query.getPageSize())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to query recipes", e);
            return new ArrayList<>();
        }
    }

    /**
     * 清理缓存
     */
    private static void cleanupCache() {
        try {
            long cutoffTime = System.currentTimeMillis() - DEFAULT_CACHE_TIMEOUT;
            recipeCache.entrySet().removeIf(entry ->
                    entry.getValue().getLastModifiedTime().toInstant(java.time.ZoneOffset.UTC).toEpochMilli() < cutoffTime
            );
            logger.debug("Cleaned up recipe cache, remaining entries: {}", recipeCache.size());
        } catch (Exception e) {
            logger.error("Failed to cleanup recipe cache", e);
        }
    }

    /**
     * 获取缓存统计信息
     */
    public static CacheStatistics getCacheStatistics() {
        CacheStatistics stats = new CacheStatistics();
        stats.setRecipeCacheSize(recipeCache.size());
        stats.setCacheTimeout(DEFAULT_CACHE_TIMEOUT);
        return stats;
    }

    /**
     * 关闭配方管理器
     */
    public static void shutdown() {
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            logger.info("RecipeManager shutdown completed");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            logger.warn("RecipeManager shutdown interrupted");
        }
    }

    // 配方类型枚举
    public enum RecipeType {
        WIRE_BONDING("引线键合工艺", "Wire Bonding Process"),
        DIE_ATTACH("芯片贴装工艺", "Die Attach Process"),
        PLASTIC_PACKAGING("塑封工艺", "Plastic Packaging Process"),
        METAL_PACKAGING("金属封装工艺", "Metal Packaging Process"),
        CERAMIC_PACKAGING("陶瓷封装工艺", "Ceramic Packaging Process"),
        SOLDERING("焊接工艺", "Soldering Process"),
        CLEANING("清洗工艺", "Cleaning Process"),
        TESTING("测试工艺", "Testing Process"),
        CUSTOM("自定义工艺", "Custom Process");

        private final String chineseName;
        private final String englishName;

        RecipeType(String chineseName, String englishName) {
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

    // 配方状态枚举
    public enum RecipeStatus {
        DRAFT("草稿", 1),
        SUBMITTED("已提交", 2),
        UNDER_REVIEW("审核中", 3),
        APPROVED("已批准", 4),
        REJECTED("已拒绝", 5),
        ACTIVE("激活", 6),
        DEPRECATED("已弃用", 7),
        ARCHIVED("已归档", 8);

        private final String description;
        private final int sequence;

        RecipeStatus(String description, int sequence) {
            this.description = description;
            this.sequence = sequence;
        }

        public String getDescription() {
            return description;
        }

        public int getSequence() {
            return sequence;
        }

        public boolean isActive() {
            return this == ACTIVE || this == APPROVED;
        }
    }

    // 参数类型枚举
    public enum ParameterType {
        TEMPERATURE("温度", "Temperature"),
        PRESSURE("压力", "Pressure"),
        TIME("时间", "Time"),
        SPEED("速度", "Speed"),
        VOLTAGE("电压", "Voltage"),
        CURRENT("电流", "Current"),
        FREQUENCY("频率", "Frequency"),
        POWER("功率", "Power"),
        FLOW_RATE("流量", "Flow Rate"),
        POSITION("位置", "Position"),
        CUSTOM("自定义", "Custom");

        private final String chineseName;
        private final String englishName;

        ParameterType(String chineseName, String englishName) {
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

    // 优化算法枚举
    public enum OptimizationAlgorithm {
        GENETIC_ALGORITHM("遗传算法", "Genetic Algorithm"),
        SIMULATED_ANNEALING("模拟退火", "Simulated Annealing"),
        PARTICLE_SWARM("粒子群优化", "Particle Swarm Optimization"),
        GRADIENT_DESCENT("梯度下降", "Gradient Descent"),
        BAYESIAN_OPTIMIZATION("贝叶斯优化", "Bayesian Optimization");

        private final String chineseName;
        private final String englishName;

        OptimizationAlgorithm(String chineseName, String englishName) {
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

    // 审批状态枚举
    public enum ApprovalStatus {
        PENDING("待审批", "Pending"),
        APPROVED("已批准", "Approved"),
        REJECTED("已拒绝", "Rejected"),
        REVISED("已修订", "Revised");

        private final String chineseName;
        private final String englishName;

        ApprovalStatus(String chineseName, String englishName) {
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

    // 配方加载器接口
    public interface RecipeLoader {
        ProcessRecipe loadRecipe(String recipeId) throws Exception;

        boolean supportsStorageType(String storageType);

        String getLoaderName();
    }

    // 配方验证器接口
    public interface RecipeValidator {
        List<ValidationError> validateRecipe(ProcessRecipe recipe) throws Exception;

        boolean supportsRecipeType(RecipeType recipeType);

        String getValidatorName();
    }

    // 参数优化器接口
    public interface ParameterOptimizer {
        OptimizedParameters optimize(ProcessRecipe recipe, HistoricalData data) throws Exception;

        boolean supportsAlgorithm(OptimizationAlgorithm algorithm);

        String getOptimizerName();
    }

    // 版本控制器接口
    public interface VersionController {
        RecipeVersionHistory getRecipeHistory(String recipeId) throws Exception;

        ProcessRecipe getVersion(String recipeId, String version) throws Exception;

        boolean saveVersion(ProcessRecipe recipe) throws Exception;

        boolean supportsVersionControl(String versionControlType);

        String getVersionControllerName();
    }

    // 审批管理器接口
    public interface ApprovalManager {
        boolean submitForApproval(ProcessRecipe recipe) throws Exception;

        ApprovalWorkflow getApprovalWorkflow(String recipeId) throws Exception;

        boolean approveRecipe(String recipeId, String approver, String comments) throws Exception;

        boolean rejectRecipe(String recipeId, String approver, String comments) throws Exception;

        boolean supportsApprovalType(String approvalType);

        String getApprovalManagerName();
    }

    // 工艺配方类
    public static class ProcessRecipe {
        private final String recipeId;
        private final String recipeName;
        private final RecipeType recipeType;
        private final String version;
        private final String description;
        private final Map<String, RecipeParameter> parameters;
        private final List<RecipeStep> steps;
        private final LocalDateTime createTime;
        private final LocalDateTime lastModifiedTime;
        private final String createdBy;
        private final String lastModifiedBy;
        private final String equipmentType;
        private final Map<String, Object> metadata;
        private final List<RecipeConstraint> constraints;
        private final List<RecipeValidationRule> validationRules;
        private volatile RecipeStatus status;

        public ProcessRecipe(String recipeId, String recipeName, RecipeType recipeType, String version) {
            this.recipeId = recipeId != null ? recipeId : UUID.randomUUID().toString();
            this.recipeName = recipeName != null ? recipeName : "";
            this.recipeType = recipeType != null ? recipeType : RecipeType.CUSTOM;
            this.version = version != null ? version : "1.0";
            this.description = "";
            this.parameters = new ConcurrentHashMap<>();
            this.steps = new CopyOnWriteArrayList<>();
            this.createTime = LocalDateTime.now();
            this.lastModifiedTime = LocalDateTime.now();
            this.createdBy = "System";
            this.lastModifiedBy = "System";
            this.status = RecipeStatus.DRAFT;
            this.equipmentType = "";
            this.metadata = new ConcurrentHashMap<>();
            this.constraints = new ArrayList<>();
            this.validationRules = new ArrayList<>();
        }

        // Getters and Setters
        public String getRecipeId() {
            return recipeId;
        }

        public String getRecipeName() {
            return recipeName;
        }

        public ProcessRecipe setRecipeName(String recipeName) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public RecipeType getRecipeType() {
            return recipeType;
        }

        public String getVersion() {
            return version;
        }

        public String getDescription() {
            return description;
        }

        public ProcessRecipe setDescription(String description) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public Map<String, RecipeParameter> getParameters() {
            return new HashMap<>(parameters);
        }

        public ProcessRecipe addParameter(RecipeParameter parameter) {
            this.parameters.put(parameter.getParameterId(), parameter);
            return this;
        }

        public RecipeParameter getParameter(String parameterId) {
            return this.parameters.get(parameterId);
        }

        public List<RecipeStep> getSteps() {
            return new ArrayList<>(steps);
        }

        public ProcessRecipe addStep(RecipeStep step) {
            this.steps.add(step);
            return this;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public LocalDateTime getLastModifiedTime() {
            return lastModifiedTime;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public String getLastModifiedBy() {
            return lastModifiedBy;
        }

        public RecipeStatus getStatus() {
            return status;
        }

        public ProcessRecipe setStatus(RecipeStatus status) {
            this.status = status;
            return this;
        }

        public String getEquipmentType() {
            return equipmentType;
        }

        public ProcessRecipe setEquipmentType(String equipmentType) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public Map<String, Object> getMetadata() {
            return new HashMap<>(metadata);
        }

        public ProcessRecipe setMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Object getMetadata(String key) {
            return this.metadata.get(key);
        }

        public List<RecipeConstraint> getConstraints() {
            return new ArrayList<>(constraints);
        }

        public ProcessRecipe addConstraint(RecipeConstraint constraint) {
            this.constraints.add(constraint);
            return this;
        }

        public List<RecipeValidationRule> getValidationRules() {
            return new ArrayList<>(validationRules);
        }

        public ProcessRecipe addValidationRule(RecipeValidationRule rule) {
            this.validationRules.add(rule);
            return this;
        }

        @Override
        public String toString() {
            return "ProcessRecipe{" +
                    "recipeId='" + recipeId + '\'' +
                    ", recipeName='" + recipeName + '\'' +
                    ", recipeType=" + recipeType +
                    ", version='" + version + '\'' +
                    ", status=" + status +
                    ", parameterCount=" + parameters.size() +
                    ", stepCount=" + steps.size() +
                    '}';
        }
    }

    // 配方参数类
    public static class RecipeParameter {
        private final String parameterId;
        private final String parameterName;
        private final ParameterType parameterType;
        private final Object defaultValue;
        private final Object minValue;
        private final Object maxValue;
        private final String unit;
        private final boolean required;
        private final String description;
        private final Map<String, Object> attributes;

        public RecipeParameter(String parameterId, String parameterName, ParameterType parameterType) {
            this.parameterId = parameterId != null ? parameterId : UUID.randomUUID().toString();
            this.parameterName = parameterName != null ? parameterName : "";
            this.parameterType = parameterType != null ? parameterType : ParameterType.CUSTOM;
            this.defaultValue = null;
            this.minValue = null;
            this.maxValue = null;
            this.unit = "";
            this.required = false;
            this.description = "";
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getParameterId() {
            return parameterId;
        }

        public String getParameterName() {
            return parameterName;
        }

        public RecipeParameter setParameterName(String parameterName) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public ParameterType getParameterType() {
            return parameterType;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public RecipeParameter setDefaultValue(Object defaultValue) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public Object getMinValue() {
            return minValue;
        }

        public RecipeParameter setMinValue(Object minValue) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public Object getMaxValue() {
            return maxValue;
        }

        public RecipeParameter setMaxValue(Object maxValue) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public String getUnit() {
            return unit;
        }

        public RecipeParameter setUnit(String unit) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public boolean isRequired() {
            return required;
        }

        public RecipeParameter setRequired(boolean required) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public String getDescription() {
            return description;
        }

        public RecipeParameter setDescription(String description) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public RecipeParameter setAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Object getAttribute(String key) {
            return this.attributes.get(key);
        }

        @Override
        public String toString() {
            return "RecipeParameter{" +
                    "parameterId='" + parameterId + '\'' +
                    ", parameterName='" + parameterName + '\'' +
                    ", parameterType=" + parameterType +
                    ", unit='" + unit + '\'' +
                    ", required=" + required +
                    '}';
        }
    }

    // 配方步骤类
    public static class RecipeStep {
        private final String stepId;
        private final String stepName;
        private final int stepNumber;
        private final String description;
        private final Map<String, Object> parameters;
        private final long duration; // 毫秒
        private final Map<String, Object> conditions;
        private final List<String> dependencies;

        public RecipeStep(String stepId, String stepName, int stepNumber) {
            this.stepId = stepId != null ? stepId : UUID.randomUUID().toString();
            this.stepName = stepName != null ? stepName : "";
            this.stepNumber = stepNumber;
            this.description = "";
            this.parameters = new ConcurrentHashMap<>();
            this.duration = 0;
            this.conditions = new ConcurrentHashMap<>();
            this.dependencies = new ArrayList<>();
        }

        // Getters and Setters
        public String getStepId() {
            return stepId;
        }

        public String getStepName() {
            return stepName;
        }

        public RecipeStep setStepName(String stepName) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public int getStepNumber() {
            return stepNumber;
        }

        public String getDescription() {
            return description;
        }

        public RecipeStep setDescription(String description) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public Map<String, Object> getParameters() {
            return new HashMap<>(parameters);
        }

        public RecipeStep setParameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        public Object getParameter(String key) {
            return this.parameters.get(key);
        }

        public long getDuration() {
            return duration;
        }

        public RecipeStep setDuration(long duration) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public Map<String, Object> getConditions() {
            return new HashMap<>(conditions);
        }

        public RecipeStep setCondition(String key, Object value) {
            this.conditions.put(key, value);
            return this;
        }

        public Object getCondition(String key) {
            return this.conditions.get(key);
        }

        public List<String> getDependencies() {
            return new ArrayList<>(dependencies);
        }

        public RecipeStep addDependency(String dependency) {
            this.dependencies.add(dependency);
            return this;
        }

        @Override
        public String toString() {
            return "RecipeStep{" +
                    "stepId='" + stepId + '\'' +
                    ", stepName='" + stepName + '\'' +
                    ", stepNumber=" + stepNumber +
                    ", duration=" + duration + "ms" +
                    ", dependencyCount=" + dependencies.size() +
                    '}';
        }
    }

    // 配方约束类
    public static class RecipeConstraint {
        private final String constraintId;
        private final String constraintName;
        private final String expression;
        private final String description;
        private final boolean enabled;

        public RecipeConstraint(String constraintId, String constraintName, String expression) {
            this.constraintId = constraintId != null ? constraintId : UUID.randomUUID().toString();
            this.constraintName = constraintName != null ? constraintName : "";
            this.expression = expression != null ? expression : "";
            this.description = "";
            this.enabled = true;
        }

        // Getters and Setters
        public String getConstraintId() {
            return constraintId;
        }

        public String getConstraintName() {
            return constraintName;
        }

        public RecipeConstraint setConstraintName(String constraintName) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public String getExpression() {
            return expression;
        }

        public RecipeConstraint setExpression(String expression) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public String getDescription() {
            return description;
        }

        public RecipeConstraint setDescription(String description) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public RecipeConstraint setEnabled(boolean enabled) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        @Override
        public String toString() {
            return "RecipeConstraint{" +
                    "constraintId='" + constraintId + '\'' +
                    ", constraintName='" + constraintName + '\'' +
                    ", expression='" + expression + '\'' +
                    ", enabled=" + enabled +
                    '}';
        }
    }

    // 配方验证规则类
    public static class RecipeValidationRule {
        private final String ruleId;
        private final String ruleName;
        private final String expression;
        private final String errorMessage;
        private final boolean critical;

        public RecipeValidationRule(String ruleId, String ruleName, String expression) {
            this.ruleId = ruleId != null ? ruleId : UUID.randomUUID().toString();
            this.ruleName = ruleName != null ? ruleName : "";
            this.expression = expression != null ? expression : "";
            this.errorMessage = "";
            this.critical = false;
        }

        // Getters and Setters
        public String getRuleId() {
            return ruleId;
        }

        public String getRuleName() {
            return ruleName;
        }

        public RecipeValidationRule setRuleName(String ruleName) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public String getExpression() {
            return expression;
        }

        public RecipeValidationRule setExpression(String expression) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public RecipeValidationRule setErrorMessage(String errorMessage) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public boolean isCritical() {
            return critical;
        }

        public RecipeValidationRule setCritical(boolean critical) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        @Override
        public String toString() {
            return "RecipeValidationRule{" +
                    "ruleId='" + ruleId + '\'' +
                    ", ruleName='" + ruleName + '\'' +
                    ", expression='" + expression + '\'' +
                    ", critical=" + critical +
                    '}';
        }
    }

    // 优化参数类
    public static class OptimizedParameters {
        private final String optimizationId;
        private final Map<String, Object> optimizedValues;
        private final double objectiveValue;
        private final OptimizationAlgorithm algorithm;
        private final int iterations;
        private final LocalDateTime optimizationTime;
        private final Map<String, Object> statistics;
        private final boolean converged;

        public OptimizedParameters(OptimizationAlgorithm algorithm, double objectiveValue) {
            this.optimizationId = UUID.randomUUID().toString();
            this.optimizedValues = new ConcurrentHashMap<>();
            this.objectiveValue = objectiveValue;
            this.algorithm = algorithm != null ? algorithm : OptimizationAlgorithm.GENETIC_ALGORITHM;
            this.iterations = 0;
            this.optimizationTime = LocalDateTime.now();
            this.statistics = new ConcurrentHashMap<>();
            this.converged = false;
        }

        // Getters and Setters
        public String getOptimizationId() {
            return optimizationId;
        }

        public Map<String, Object> getOptimizedValues() {
            return new HashMap<>(optimizedValues);
        }

        public OptimizedParameters setOptimizedValue(String key, Object value) {
            this.optimizedValues.put(key, value);
            return this;
        }

        public Object getOptimizedValue(String key) {
            return this.optimizedValues.get(key);
        }

        public double getObjectiveValue() {
            return objectiveValue;
        }

        public OptimizationAlgorithm getAlgorithm() {
            return algorithm;
        }

        public int getIterations() {
            return iterations;
        }

        public OptimizedParameters setIterations(int iterations) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public LocalDateTime getOptimizationTime() {
            return optimizationTime;
        }

        public Map<String, Object> getStatistics() {
            return new HashMap<>(statistics);
        }

        public OptimizedParameters setStatistic(String key, Object value) {
            this.statistics.put(key, value);
            return this;
        }

        public Object getStatistic(String key) {
            return this.statistics.get(key);
        }

        public boolean isConverged() {
            return converged;
        }

        public OptimizedParameters setConverged(boolean converged) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        @Override
        public String toString() {
            return "OptimizedParameters{" +
                    "optimizationId='" + optimizationId + '\'' +
                    ", parameterCount=" + optimizedValues.size() +
                    ", objectiveValue=" + String.format("%.4f", objectiveValue) +
                    ", algorithm=" + algorithm +
                    ", iterations=" + iterations +
                    ", converged=" + converged +
                    '}';
        }
    }

    // 配方版本历史类
    public static class RecipeVersionHistory {
        private final String recipeId;
        private final List<RecipeVersion> versions;
        private final LocalDateTime lastUpdated;

        public RecipeVersionHistory(String recipeId) {
            this.recipeId = recipeId != null ? recipeId : "";
            this.versions = new CopyOnWriteArrayList<>();
            this.lastUpdated = LocalDateTime.now();
        }

        // Getters
        public String getRecipeId() {
            return recipeId;
        }

        public List<RecipeVersion> getVersions() {
            return new ArrayList<>(versions);
        }

        public void addVersion(RecipeVersion version) {
            this.versions.add(version);
        }

        public LocalDateTime getLastUpdated() {
            return lastUpdated;
        }

        @Override
        public String toString() {
            return "RecipeVersionHistory{" +
                    "recipeId='" + recipeId + '\'' +
                    ", versionCount=" + versions.size() +
                    ", lastUpdated=" + lastUpdated +
                    '}';
        }
    }

    // 配方版本类
    public static class RecipeVersion {
        private final String versionId;
        private final String versionNumber;
        private final String recipeId;
        private final LocalDateTime createTime;
        private final String createdBy;
        private final String description;
        private final RecipeStatus status;
        private final String changeLog;

        public RecipeVersion(String versionNumber, String recipeId) {
            this.versionId = UUID.randomUUID().toString();
            this.versionNumber = versionNumber != null ? versionNumber : "1.0";
            this.recipeId = recipeId != null ? recipeId : "";
            this.createTime = LocalDateTime.now();
            this.createdBy = "System";
            this.description = "";
            this.status = RecipeStatus.DRAFT;
            this.changeLog = "";
        }

        // Getters and Setters
        public String getVersionId() {
            return versionId;
        }

        public String getVersionNumber() {
            return versionNumber;
        }

        public String getRecipeId() {
            return recipeId;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public String getDescription() {
            return description;
        }

        public RecipeVersion setDescription(String description) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public RecipeStatus getStatus() {
            return status;
        }

        public String getChangeLog() {
            return changeLog;
        }

        public RecipeVersion setChangeLog(String changeLog) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        @Override
        public String toString() {
            return "RecipeVersion{" +
                    "versionId='" + versionId + '\'' +
                    ", versionNumber='" + versionNumber + '\'' +
                    ", recipeId='" + recipeId + '\'' +
                    ", createTime=" + createTime +
                    ", status=" + status +
                    '}';
        }
    }

    // 历史数据类
    public static class HistoricalData {
        private final List<DataPoint> dataPoints;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final String productId;
        private final Map<String, Object> metadata;

        public HistoricalData(List<DataPoint> dataPoints, LocalDateTime startTime,
                              LocalDateTime endTime, String productId) {
            this.dataPoints = dataPoints != null ? new ArrayList<>(dataPoints) : new ArrayList<>();
            this.startTime = startTime;
            this.endTime = endTime;
            this.productId = productId != null ? productId : "";
            this.metadata = new ConcurrentHashMap<>();
        }

        // Getters
        public List<DataPoint> getDataPoints() {
            return new ArrayList<>(dataPoints);
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public String getProductId() {
            return productId;
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
            return "HistoricalData{" +
                    "dataPointCount=" + dataPoints.size() +
                    ", startTime=" + startTime +
                    ", endTime=" + endTime +
                    ", productId='" + productId + '\'' +
                    '}';
        }
    }

    // 数据点类
    public static class DataPoint {
        private final String dataId;
        private final Map<String, Object> parameters;
        private final double qualityScore;
        private final LocalDateTime timestamp;
        private final String batchId;
        private final Map<String, Object> attributes;

        public DataPoint(Map<String, Object> parameters, double qualityScore, LocalDateTime timestamp) {
            this.dataId = UUID.randomUUID().toString();
            this.parameters = parameters != null ? new HashMap<>(parameters) : new HashMap<>();
            this.qualityScore = qualityScore;
            this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
            this.batchId = "";
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters
        public String getDataId() {
            return dataId;
        }

        public Map<String, Object> getParameters() {
            return new HashMap<>(parameters);
        }

        public double getQualityScore() {
            return qualityScore;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public String getBatchId() {
            return batchId;
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

        @Override
        public String toString() {
            return "DataPoint{" +
                    "dataId='" + dataId + '\'' +
                    ", parameterCount=" + parameters.size() +
                    ", qualityScore=" + String.format("%.2f", qualityScore) +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }

    // 审批流程类
    public static class ApprovalWorkflow {
        private final String workflowId;
        private final String recipeId;
        private final List<ApprovalStep> steps;
        private final LocalDateTime createTime;
        private final String initiator;
        private final LocalDateTime lastUpdateTime;
        private volatile ApprovalStatus status;

        public ApprovalWorkflow(String recipeId) {
            this.workflowId = UUID.randomUUID().toString();
            this.recipeId = recipeId != null ? recipeId : "";
            this.steps = new CopyOnWriteArrayList<>();
            this.createTime = LocalDateTime.now();
            this.initiator = "System";
            this.status = ApprovalStatus.PENDING;
            this.lastUpdateTime = LocalDateTime.now();
        }

        // Getters and Setters
        public String getWorkflowId() {
            return workflowId;
        }

        public String getRecipeId() {
            return recipeId;
        }

        public List<ApprovalStep> getSteps() {
            return new ArrayList<>(steps);
        }

        public void addStep(ApprovalStep step) {
            this.steps.add(step);
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public String getInitiator() {
            return initiator;
        }

        public ApprovalStatus getStatus() {
            return status;
        }

        public void setStatus(ApprovalStatus status) {
            this.status = status;
        }

        public LocalDateTime getLastUpdateTime() {
            return lastUpdateTime;
        }

        @Override
        public String toString() {
            return "ApprovalWorkflow{" +
                    "workflowId='" + workflowId + '\'' +
                    ", recipeId='" + recipeId + '\'' +
                    ", stepCount=" + steps.size() +
                    ", status=" + status +
                    ", createTime=" + createTime +
                    '}';
        }
    }

    // 审批步骤类
    public static class ApprovalStep {
        private final String stepId;
        private final String approver;
        private final String role;
        private final LocalDateTime assignTime;
        private final LocalDateTime completeTime;
        private final String comments;
        private volatile ApprovalStatus status;

        public ApprovalStep(String approver, String role) {
            this.stepId = UUID.randomUUID().toString();
            this.approver = approver != null ? approver : "";
            this.role = role != null ? role : "";
            this.assignTime = LocalDateTime.now();
            this.status = ApprovalStatus.PENDING;
            this.completeTime = null;
            this.comments = "";
        }

        // Getters and Setters
        public String getStepId() {
            return stepId;
        }

        public String getApprover() {
            return approver;
        }

        public String getRole() {
            return role;
        }

        public LocalDateTime getAssignTime() {
            return assignTime;
        }

        public ApprovalStatus getStatus() {
            return status;
        }

        public void setStatus(ApprovalStatus status) {
            this.status = status;
        }

        public LocalDateTime getCompleteTime() {
            return completeTime;
        }

        public String getComments() {
            return comments;
        }

        public ApprovalStep setComments(String comments) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        @Override
        public String toString() {
            return "ApprovalStep{" +
                    "stepId='" + stepId + '\'' +
                    ", approver='" + approver + '\'' +
                    ", role='" + role + '\'' +
                    ", status=" + status +
                    ", assignTime=" + assignTime +
                    '}';
        }
    }

    // 验证错误类
    public static class ValidationError {
        private final String errorId;
        private final String errorCode;
        private final String errorMessage;
        private final boolean critical;
        private final String field;
        private final Object value;

        public ValidationError(String errorCode, String errorMessage, boolean critical) {
            this.errorId = UUID.randomUUID().toString();
            this.errorCode = errorCode != null ? errorCode : "UNKNOWN_ERROR";
            this.errorMessage = errorMessage != null ? errorMessage : "";
            this.critical = critical;
            this.field = "";
            this.value = null;
        }

        // Getters and Setters
        public String getErrorId() {
            return errorId;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public boolean isCritical() {
            return critical;
        }

        public String getField() {
            return field;
        }

        public ValidationError setField(String field) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        public Object getValue() {
            return value;
        }

        public ValidationError setValue(Object value) {
            // This would need a builder pattern or new instance inspection real implementation
            return this;
        }

        @Override
        public String toString() {
            return "ValidationError{" +
                    "errorId='" + errorId + '\'' +
                    ", errorCode='" + errorCode + '\'' +
                    ", errorMessage='" + errorMessage + '\'' +
                    ", critical=" + critical +
                    ", field='" + field + '\'' +
                    '}';
        }
    }

    /**
     * 配方查询类
     */
    public static class RecipeQuery {
        private RecipeType recipeType;
        private RecipeStatus status;
        private String keyword;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private int page = 1;
        private int pageSize = 50;
        private String sortBy = "createTime";
        private boolean ascending = false;

        // Getters and Setters
        public RecipeType getRecipeType() {
            return recipeType;
        }

        public RecipeQuery setRecipeType(RecipeType recipeType) {
            this.recipeType = recipeType;
            return this;
        }

        public RecipeStatus getStatus() {
            return status;
        }

        public RecipeQuery setStatus(RecipeStatus status) {
            this.status = status;
            return this;
        }

        public String getKeyword() {
            return keyword;
        }

        public RecipeQuery setKeyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public LocalDateTime getStartDate() {
            return startDate;
        }

        public RecipeQuery setStartDate(LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public LocalDateTime getEndDate() {
            return endDate;
        }

        public RecipeQuery setEndDate(LocalDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public int getPage() {
            return page;
        }

        public RecipeQuery setPage(int page) {
            this.page = page;
            return this;
        }

        public int getPageSize() {
            return pageSize;
        }

        public RecipeQuery setPageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public String getSortBy() {
            return sortBy;
        }

        public RecipeQuery setSortBy(String sortBy) {
            this.sortBy = sortBy;
            return this;
        }

        public boolean isAscending() {
            return ascending;
        }

        public RecipeQuery setAscending(boolean ascending) {
            this.ascending = ascending;
            return this;
        }
    }

    /**
     * 缓存统计信息类
     */
    public static class CacheStatistics {
        private int recipeCacheSize;
        private long cacheTimeout;

        // Getters and Setters
        public int getRecipeCacheSize() {
            return recipeCacheSize;
        }

        public void setRecipeCacheSize(int recipeCacheSize) {
            this.recipeCacheSize = recipeCacheSize;
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
                    "recipeCacheSize=" + recipeCacheSize +
                    ", cacheTimeout=" + cacheTimeout + "ms" +
                    '}';
        }
    }
}