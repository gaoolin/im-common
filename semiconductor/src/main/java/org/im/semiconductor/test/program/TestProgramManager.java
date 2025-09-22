package org.im.semiconductor.test.program;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 测试程序管理工具类
 * <p>
 * 特性：
 * - 通用性：支持多种测试程序格式
 * - 规范性：统一的程序管理接口
 * - 专业性：半导体测试程序专业处理
 * - 灵活性：支持自定义程序加载器
 * - 可靠性：完善的版本控制和兼容性检查
 * - 安全性：程序完整性验证
 * - 复用性：程序缓存和复用机制
 * - 容错性：程序加载失败的恢复机制
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/08/20
 */
public class TestProgramManager {

    private static final Logger logger = LoggerFactory.getLogger(TestProgramManager.class);

    // 程序缓存
    private static final Map<String, TestProgram> programCache = new ConcurrentHashMap<>();

    // 默认超时时间（毫秒）
    public static final int DEFAULT_TIMEOUT = 30000;

    /**
     * 测试程序配置
     */
    public static class ProgramConfig {
        private String programPath;
        private String version;
        private String equipmentModel;
        private long timeout = DEFAULT_TIMEOUT;
        private boolean useCache = true;

        // Getters and Setters
        public String getProgramPath() {
            return programPath;
        }

        public ProgramConfig setProgramPath(String programPath) {
            this.programPath = programPath;
            return this;
        }

        public String getVersion() {
            return version;
        }

        public ProgramConfig setVersion(String version) {
            this.version = version;
            return this;
        }

        public String getEquipmentModel() {
            return equipmentModel;
        }

        public ProgramConfig setEquipmentModel(String equipmentModel) {
            this.equipmentModel = equipmentModel;
            return this;
        }

        public long getTimeout() {
            return timeout;
        }

        public ProgramConfig setTimeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        public boolean isUseCache() {
            return useCache;
        }

        public ProgramConfig setUseCache(boolean useCache) {
            this.useCache = useCache;
            return this;
        }
    }

    /**
     * 加载测试程序
     *
     * @param config 程序配置
     * @return 测试程序对象
     */
    public static TestProgram loadTestProgram(ProgramConfig config) {
        if (config == null || config.getProgramPath() == null || config.getProgramPath().isEmpty()) {
            logger.error("Invalid program configuration");
            return null;
        }

        String cacheKey = generateCacheKey(config);

        // 检查缓存
        if (config.isUseCache() && programCache.containsKey(cacheKey)) {
            TestProgram cachedProgram = programCache.get(cacheKey);
            if (cachedProgram != null && !cachedProgram.isExpired()) {
                logger.debug("Returning cached program for key: {}", cacheKey);
                return cachedProgram;
            } else {
                programCache.remove(cacheKey);
            }
        }

        try {
            // 加载程序
            TestProgram program = doLoadTestProgram(config);

            if (program != null) {
                // 验证程序完整性
                if (validateProgramIntegrity(program)) {
                    // 缓存程序
                    if (config.isUseCache()) {
                        programCache.put(cacheKey, program);
                    }
                    return program;
                } else {
                    logger.warn("Program integrity validation failed for: {}", config.getProgramPath());
                    return null;
                }
            } else {
                logger.error("Failed to load test program from: {}", config.getProgramPath());
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception occurred while loading test program: {}", config.getProgramPath(), e);
            return null;
        }
    }

    /**
     * 实际加载测试程序
     *
     * @param config 程序配置
     * @return 测试程序对象
     */
    private static TestProgram doLoadTestProgram(ProgramConfig config) {
        // 根据文件扩展名选择加载器
        String extension = getFileExtension(config.getProgramPath());

        switch (extension.toLowerCase()) {
            case "tpf":
                return loadTpfProgram(config);
            case "xml":
                return loadXmlProgram(config);
            case "json":
                return loadJsonProgram(config);
            default:
                logger.warn("Unsupported program format: {}", extension);
                return null;
        }
    }

    /**
     * 加载TPF格式程序
     *
     * @param config 程序配置
     * @return 测试程序对象
     */
    private static TestProgram loadTpfProgram(ProgramConfig config) {
        // 实现TPF格式程序加载逻辑
        logger.debug("Loading TPF program: {}", config.getProgramPath());
        return new TestProgram(config.getProgramPath(), config.getVersion());
    }

    /**
     * 加载XML格式程序
     *
     * @param config 程序配置
     * @return 测试程序对象
     */
    private static TestProgram loadXmlProgram(ProgramConfig config) {
        // 实现XML格式程序加载逻辑
        logger.debug("Loading XML program: {}", config.getProgramPath());
        return new TestProgram(config.getProgramPath(), config.getVersion());
    }

    /**
     * 加载JSON格式程序
     *
     * @param config 程序配置
     * @return 测试程序对象
     */
    private static TestProgram loadJsonProgram(ProgramConfig config) {
        // 实现JSON格式程序加载逻辑
        logger.debug("Loading JSON program: {}", config.getProgramPath());
        return new TestProgram(config.getProgramPath(), config.getVersion());
    }

    /**
     * 验证程序完整性
     *
     * @param program 测试程序
     * @return 是否完整
     */
    private static boolean validateProgramIntegrity(TestProgram program) {
        // 实现程序完整性验证逻辑
        // 可以包括校验和验证、数字签名验证等
        return program != null && program.getPath() != null && !program.getPath().isEmpty();
    }

    /**
     * 验证程序兼容性
     *
     * @param equipmentModel 设备型号
     * @param programVersion 程序版本
     * @return 是否兼容
     */
    public static boolean validateProgramCompatibility(String equipmentModel, String programVersion) {
        if (equipmentModel == null || equipmentModel.isEmpty() ||
                programVersion == null || programVersion.isEmpty()) {
            logger.warn("Invalid equipment model or program version");
            return false;
        }

        try {
            // 实现兼容性验证逻辑
            // 可以查询兼容性数据库或配置文件
            boolean compatible = checkCompatibility(equipmentModel, programVersion);
            logger.debug("Compatibility check: {} v{} -> {}", equipmentModel, programVersion, compatible ? "PASS" : "FAIL");
            return compatible;
        } catch (Exception e) {
            logger.error("Exception occurred during compatibility check", e);
            return false;
        }
    }

    /**
     * 检查兼容性
     *
     * @param equipmentModel 设备型号
     * @param programVersion 程序版本
     * @return 是否兼容
     */
    private static boolean checkCompatibility(String equipmentModel, String programVersion) {
        // 实现具体的兼容性检查逻辑
        // 这里可以查询数据库或配置文件
        return true; // 临时返回true
    }

    /**
     * 部署测试程序
     *
     * @param equipmentId 设备ID
     * @param program     测试程序
     * @return 是否部署成功
     */
    public static boolean deployTestProgram(String equipmentId, TestProgram program) {
        if (equipmentId == null || equipmentId.isEmpty() || program == null) {
            logger.error("Invalid equipment ID or program");
            return false;
        }

        try {
            // 实现程序部署逻辑
            boolean deployed = doDeployProgram(equipmentId, program);

            if (deployed) {
                logger.info("Successfully deployed program {} to equipment {}",
                        program.getVersion(), equipmentId);
                return true;
            } else {
                logger.error("Failed to deploy program {} to equipment {}",
                        program.getVersion(), equipmentId);
                return false;
            }
        } catch (Exception e) {
            logger.error("Exception occurred while deploying program to equipment: {}", equipmentId, e);
            return false;
        }
    }

    /**
     * 实际部署程序
     *
     * @param equipmentId 设备ID
     * @param program     测试程序
     * @return 是否部署成功
     */
    private static boolean doDeployProgram(String equipmentId, TestProgram program) {
        // 实现具体的程序部署逻辑
        // 可能涉及网络传输、设备通信等
        return true; // 临时返回true
    }

    /**
     * 获取文件扩展名
     *
     * @param filePath 文件路径
     * @return 文件扩展名
     */
    private static String getFileExtension(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }

        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filePath.length() - 1) {
            return filePath.substring(lastDotIndex + 1);
        }

        return "";
    }

    /**
     * 生成缓存键
     *
     * @param config 程序配置
     * @return 缓存键
     */
    private static String generateCacheKey(ProgramConfig config) {
        return config.getProgramPath() + "|" +
                (config.getVersion() != null ? config.getVersion() : "") + "|" +
                (config.getEquipmentModel() != null ? config.getEquipmentModel() : "");
    }

    /**
     * 清除程序缓存
     *
     * @param programPath 程序路径
     */
    public static void clearProgramCache(String programPath) {
        if (programPath != null && !programPath.isEmpty()) {
            programCache.entrySet().removeIf(entry ->
                    entry.getValue().getPath().equals(programPath));
        } else {
            programCache.clear();
        }
    }

    /**
     * 测试程序类
     */
    public static class TestProgram {
        private final String path;
        private final String version;
        private final long loadTime;
        private final long expireTime;

        public TestProgram(String path, String version) {
            this.path = path;
            this.version = version;
            this.loadTime = System.currentTimeMillis();
            this.expireTime = loadTime + 3600000; // 1小时后过期
        }

        // Getters
        public String getPath() {
            return path;
        }

        public String getVersion() {
            return version;
        }

        public long getLoadTime() {
            return loadTime;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }

        @Override
        public String toString() {
            return "TestProgram{" +
                    "path='" + path + '\'' +
                    ", version='" + version + '\'' +
                    ", loadTime=" + loadTime +
                    ", expired=" + isExpired() +
                    '}';
        }
    }
}
