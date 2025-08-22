package com.qtech.im.semiconductor.equipment.qtech;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 命令结构基类 - 提供命令路由和解析的核心能力
 * <p>
 * 特性：
 * - 通用性：支持各种命令结构场景
 * - 规范性：遵循命令结构标准
 * - 专业性：提供专业的命令路由和解析能力
 * - 灵活性：支持自定义命令路径
 * - 可靠性：确保命令处理的稳定性
 * - 安全性：防止命令注入攻击
 * - 复用性：可被各种命令场景复用
 * - 容错性：具备良好的错误处理能力
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @since 2025/08/22
 */
public abstract class CommandStructure implements Serializable {
    private static final long serialVersionUID = 1L;

    // 路由相关字段
    protected String parentCommand;      // 上一级命令（父命令）
    protected String currentCommand;     // 当前命令
    protected String subCommand;         // 子命令
    protected Integer sequenceNumber;    // 序号

    // 解析相关字段
    protected String commandPath;        // 命令路径（完整路径）
    protected String normalizedPath;     // 规范化路径（标准化后的路径）

    // 配置相关字段
    protected CommandConfig config;

    /**
     * 默认构造函数
     */
    public CommandStructure() {
        this.config = new CommandConfig();
    }

    /**
     * 带参数的构造函数
     */
    public CommandStructure(String parentCommand, String currentCommand, String subCommand, Integer sequenceNumber) {
        this.parentCommand = parentCommand;
        this.currentCommand = currentCommand;
        this.subCommand = subCommand;
        this.sequenceNumber = sequenceNumber;
        this.config = new CommandConfig();

        // 自动构建命令路径
        buildCommandPath();
    }

    /**
     * 从命令路径解析命令结构
     */
    public CommandStructure parse(String commandPath) {
        if (StringUtils.isBlank(commandPath)) {
            throw new IllegalArgumentException("Command path cannot be null or empty");
        }

        // 使用配置的分隔符进行分割
        String[] parts = StringUtils.split(commandPath, config.getSeparator());

        // 解析各个部分
        this.parentCommand = parts.length > 0 ? parts[0] : null;
        this.currentCommand = parts.length > 1 ? parts[1] : null;
        this.subCommand = parts.length > 2 ? parts[2] : null;

        // 解析序号（如果存在）
        try {
            if (parts.length > 3) {
                this.sequenceNumber = Integer.parseInt(parts[3]);
            }
        } catch (NumberFormatException e) {
            // 忽略无效的序号
            this.sequenceNumber = null;
        }

        // 构建命令路径
        buildCommandPath();

        return this;
    }

    /**
     * 构建命令路径
     */
    protected void buildCommandPath() {
        StringBuilder sb = new StringBuilder();
        boolean hasContent = false;

        if (StringUtils.isNotBlank(parentCommand)) {
            sb.append(parentCommand);
            hasContent = true;
        }
        if (StringUtils.isNotBlank(currentCommand)) {
            if (hasContent) sb.append(config.getSeparator());
            sb.append(currentCommand);
            hasContent = true;
        }
        if (StringUtils.isNotBlank(subCommand)) {
            if (hasContent) sb.append(config.getSeparator());
            sb.append(subCommand);
        }
        if (sequenceNumber != null) {
            if (hasContent) sb.append(config.getSeparator());
            sb.append(sequenceNumber);
        }

        this.commandPath = sb.toString();
        this.normalizedPath = normalizePath(this.commandPath);
    }

    /**
     * 获取命令路径
     */
    public String getCommandPath() {
        if (this.commandPath == null) {
            buildCommandPath();
        }
        return this.commandPath;
    }

    /**
     * 获取规范化路径
     */
    public String getNormalizedPath() {
        if (this.normalizedPath == null) {
            this.normalizedPath = normalizePath(getCommandPath());
        }
        return this.normalizedPath;
    }

    /**
     * 规范化路径
     */
    protected String normalizePath(String path) {
        if (path == null || path.isEmpty()) return path;

        String processed = path.replaceAll("\\s+", "");
        if (!config.isCaseSensitive()) {
            processed = processed.toUpperCase();
        }
        return processed;
    }

    /**
     * 获取上一级命令
     */
    public String getParentCommand() {
        return parentCommand;
    }

    /**
     * 设置上一级命令
     */
    public void setParentCommand(String parentCommand) {
        this.parentCommand = parentCommand;
        invalidatePathCache();
    }

    /**
     * 获取当前命令
     */
    public String getCurrentCommand() {
        return currentCommand;
    }

    /**
     * 设置当前命令
     */
    public void setCurrentCommand(String currentCommand) {
        this.currentCommand = currentCommand;
        invalidatePathCache();
    }

    /**
     * 获取子命令
     */
    public String getSubCommand() {
        return subCommand;
    }

    /**
     * 设置子命令
     */
    public void setSubCommand(String subCommand) {
        this.subCommand = subCommand;
        invalidatePathCache();
    }

    /**
     * 获取序号
     */
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * 设置序号
     */
    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        invalidatePathCache();
    }

    /**
     * 清除路径缓存
     */
    private void invalidatePathCache() {
        this.commandPath = null;
        this.normalizedPath = null;
    }

    /**
     * 验证命令结构
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();

        // 验证当前命令是否为空
        if (StringUtils.isBlank(getCurrentCommand())) {
            result.addError("Current command cannot be null or empty");
        }

        // 验证命令长度
        if (getCurrentCommand() != null && getCurrentCommand().length() > config.getMaxCommandLength()) {
            result.addWarning("Current command length exceeds recommended limit: " + getCurrentCommand().length() + "/" + config.getMaxCommandLength());
        }

        // 验证命令格式
        if (!isValidCommandFormat(getCurrentCommand())) {
            result.addError("Invalid current command format: " + getCurrentCommand());
        }

        // 验证是否为保留命令
        if (config.isReservedCommand(getCurrentCommand())) {
            result.addWarning("Current command is a reserved command: " + getCurrentCommand());
        }

        return result;
    }

    /**
     * 检查命令格式是否有效
     */
    protected boolean isValidCommandFormat(String command) {
        if (command == null || command.isEmpty()) return false;
        // 基本格式检查：只允许字母、数字、下划线和连字符
        return command.matches("^[a-zA-Z0-9_-]+$");
    }

    /**
     * 执行命令路由
     */
    public RoutingResult route() {
        RoutingResult result = new RoutingResult();

        // 根据命令路径执行路由逻辑
        String fullPath = getCommandPath();
        String normalizedPath = getNormalizedPath();

        // 基本路由验证
        if (fullPath == null || fullPath.isEmpty()) {
            result.addError("Command path cannot be null or empty");
            return result;
        }

        // 验证路径是否有效
        if (!isValidPath(fullPath)) {
            result.addError("Invalid command path: " + fullPath);
            return result;
        }

        // 分析路径层次
        String[] parts = fullPath.split(config.getSeparator());
        int level = parts.length;

        result.setPathLevel(level);
        result.setPathSegments(parts);
        result.setFullPath(fullPath);
        result.setNormalizedPath(normalizedPath);
        result.setValid(true);

        return result;
    }

    /**
     * 检查路径是否有效
     */
    protected boolean isValidPath(String path) {
        if (path == null || path.isEmpty()) return false;

        // 基本路径检查
        if (path.length() > config.getMaxPathLength()) {
            return false;
        }

        // 检查是否包含非法字符
        String escapedSeparator = java.util.regex.Pattern.quote(config.getSeparator());
        String regex = "^[a-zA-Z0-9_\\-]+(" + escapedSeparator + "[a-zA-Z0-9_\\-]+)*$";
        return path.matches(regex);
    }

    /**
     * 命令配置类
     */
    public static class CommandConfig {
        private String separator = "_";                    // 分隔符
        private int maxCommandLength = 50;                 // 最大命令长度
        private int maxPathLength = 200;                   // 最大路径长度
        private boolean caseSensitive = false;             // 是否区分大小写
        private List<String> reservedCommands = new ArrayList<>(); // 保留命令

        public CommandConfig() {
            // 添加常见保留命令
            reservedCommands.add("SET");
            reservedCommands.add("GET");
            reservedCommands.add("DEL");
            reservedCommands.add("EXEC");
            reservedCommands.add("QUERY");
            reservedCommands.add("STATUS");
        }

        public String getSeparator() {
            return separator;
        }

        public void setSeparator(String separator) {
            if (separator == null || separator.isEmpty()) {
                this.separator = "_";
            } else {
                this.separator = separator;
            }
        }

        public int getMaxCommandLength() {
            return maxCommandLength;
        }

        public void setMaxCommandLength(int maxCommandLength) {
            this.maxCommandLength = maxCommandLength > 0 ? maxCommandLength : 50;
        }

        public int getMaxPathLength() {
            return maxPathLength;
        }

        public void setMaxPathLength(int maxPathLength) {
            this.maxPathLength = maxPathLength > 0 ? maxPathLength : 200;
        }

        public boolean isCaseSensitive() {
            return caseSensitive;
        }

        public void setCaseSensitive(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
        }

        public List<String> getReservedCommands() {
            return Collections.unmodifiableList(reservedCommands);
        }

        public void addReservedCommand(String command) {
            if (command != null && !command.isEmpty()) {
                reservedCommands.add(command.toUpperCase());
            }
        }

        public boolean isReservedCommand(String command) {
            if (command == null) return false;
            String upperCommand = command.toUpperCase();
            return reservedCommands.contains(upperCommand);
        }
    }

    /**
     * 路由结果类
     */
    public static class RoutingResult {
        private boolean valid;
        private int pathLevel;
        private String[] pathSegments;
        private String fullPath;
        private String normalizedPath;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public int getPathLevel() {
            return pathLevel;
        }

        public void setPathLevel(int pathLevel) {
            this.pathLevel = pathLevel;
        }

        public String[] getPathSegments() {
            return pathSegments;
        }

        public void setPathSegments(String[] pathSegments) {
            this.pathSegments = pathSegments;
        }

        public String getFullPath() {
            return fullPath;
        }

        public void setFullPath(String fullPath) {
            this.fullPath = fullPath;
        }

        public String getNormalizedPath() {
            return normalizedPath;
        }

        public void setNormalizedPath(String normalizedPath) {
            this.normalizedPath = normalizedPath;
        }

        public List<String> getErrors() {
            return Collections.unmodifiableList(errors);
        }

        public void addError(String error) {
            this.errors.add(error);
        }

        public List<String> getWarnings() {
            return Collections.unmodifiableList(warnings);
        }

        public void addWarning(String warning) {
            this.warnings.add(warning);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("RoutingResult{");
            sb.append("valid=").append(valid);
            sb.append(", pathLevel=").append(pathLevel);
            sb.append(", fullPath='").append(fullPath).append('\'');
            sb.append(", normalizedPath='").append(normalizedPath).append('\'');
            sb.append(", errors=").append(errors.size());
            sb.append(", warnings=").append(warnings.size());
            sb.append('}');
            return sb.toString();
        }
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final List<String> successes = new java.util.ArrayList<>();
        private final List<String> warnings = new java.util.ArrayList<>();
        private final List<String> errors = new java.util.ArrayList<>();

        public void addSuccess(String message) {
            successes.add(message);
        }

        public void addWarning(String message) {
            warnings.add(message);
        }

        public void addError(String message) {
            errors.add(message);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        public List<String> getSuccesses() {
            return Collections.unmodifiableList(successes);
        }

        public List<String> getWarnings() {
            return Collections.unmodifiableList(warnings);
        }

        public List<String> getErrors() {
            return Collections.unmodifiableList(errors);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ValidationResult{");
            sb.append("successes=").append(successes.size());
            sb.append(", warnings=").append(warnings.size());
            sb.append(", errors=").append(errors.size());
            sb.append('}');
            return sb.toString();
        }
    }
}
