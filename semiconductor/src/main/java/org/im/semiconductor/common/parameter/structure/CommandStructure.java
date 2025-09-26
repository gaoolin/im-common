package org.im.semiconductor.common.parameter.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 命令结构接口 - 定义命令结构的核心契约
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
public interface CommandStructure extends Serializable {

    /**
     * 从命令路径解析命令结构
     */
    CommandStructure parse(String commandPath);

    /**
     * 获取命令路径
     */
    String getCommandPath();

    /**
     * 获取规范化路径
     */
    String getNormalizedPath();

    /**
     * 验证命令结构
     */
    ValidationResult validate();

    /**
     * 执行命令路由
     */
    RoutingResult route();

    /**
     * 获取当前命令
     */
    String getCurrentCommand();

    /**
     * 设置当前命令
     */
    void setCurrentCommand(String currentCommand);

    /**
     * 获取配置
     */
    CommandConfig getConfig();

    /**
     * 命令配置类
     */
    class CommandConfig implements Serializable {
        private static final long serialVersionUID = 1L;

        private String separator = "_";                    // 分隔符
        private int maxCommandLength = 50;                 // 最大命令长度
        private int maxPathLength = 200;                   // 最大路径长度
        private boolean caseSensitive = true;             // 是否区分大小写
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
     * 包路径: com.entity.im.semiconductor.equipment.parameter.entity.CommandStructure.RoutingResult
     */
    class RoutingResult {
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
     * 包路径: com.entity.im.semiconductor.equipment.parameter.entity.CommandStructure.ValidationResult
     */
    class ValidationResult {
        private final List<String> successes = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();

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
