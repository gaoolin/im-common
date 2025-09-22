package org.im.semiconductor.equipment.parameter.list.structure.Hierarchical;

import org.apache.commons.lang3.StringUtils;
import org.im.semiconductor.equipment.parameter.list.structure.AbstractCommandStructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 多层级命令结构类 - 支持任意层级的命令路由和解析
 * <p>
 * 特性：
 * - 通用性：支持任意层级的命令结构
 * - 规范性：遵循命令结构标准
 * - 专业性：提供专业的命令路由和解析能力
 * - 灵活性：支持动态层级扩展
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
public class HierarchicalCommandStructure extends AbstractCommandStructure implements Serializable {
    private static final long serialVersionUID = 1L;

    // 命令层级列表 - 支持任意层级
    private final List<CommandLevel> commandLevels;

    /**
     * 默认构造函数
     */
    public HierarchicalCommandStructure() {
        super();
        this.commandLevels = new ArrayList<>();
    }

    /**
     * 从命令路径构造
     *
     * @param commandPath 命令路径字符串
     */
    public HierarchicalCommandStructure(String commandPath) {
        this();
        parse(commandPath);
    }

    /**
     * 从命令层级列表构造
     *
     * @param commandLevels 命令层级列表
     */
    public HierarchicalCommandStructure(List<CommandLevel> commandLevels) {
        super();
        this.commandLevels = new ArrayList<>(commandLevels);
        buildCommandPath();
    }

    /**
     * 添加命令层级
     *
     * @param name  层级名称
     * @param value 层级值
     * @return 当前命令结构对象，支持链式调用
     */
    public HierarchicalCommandStructure addLevel(String name, Object value) {
        this.commandLevels.add(new CommandLevel(name, value, this.commandLevels.size()));
        invalidatePathCache();
        return this;
    }

    /**
     * 添加命令层级（带描述）
     *
     * @param name        层级名称
     * @param value       层级值
     * @param description 层级描述
     * @return 当前命令结构对象，支持链式调用
     */
    public HierarchicalCommandStructure addLevel(String name, Object value, String description) {
        this.commandLevels.add(new CommandLevel(name, value, description, this.commandLevels.size()));
        invalidatePathCache();
        return this;
    }

    /**
     * 在指定位置插入命令层级
     *
     * @param index 插入位置索引
     * @param name  层级名称
     * @param value 层级值
     * @return 当前命令结构对象，支持链式调用
     */
    public HierarchicalCommandStructure insertLevel(int index, String name, Object value) {
        if (index < 0 || index > this.commandLevels.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.commandLevels.size());
        }
        this.commandLevels.add(index, new CommandLevel(name, value, index));
        // 更新后续层级的索引
        for (int i = index + 1; i < this.commandLevels.size(); i++) {
            this.commandLevels.get(i).setLevelIndex(i);
        }
        invalidatePathCache();
        return this;
    }

    /**
     * 移除指定层级
     *
     * @param index 移除位置索引
     * @return 当前命令结构对象，支持链式调用
     */
    public HierarchicalCommandStructure removeLevel(int index) {
        if (index < 0 || index >= this.commandLevels.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.commandLevels.size());
        }
        this.commandLevels.remove(index);
        // 更新后续层级的索引
        for (int i = index; i < this.commandLevels.size(); i++) {
            this.commandLevels.get(i).setLevelIndex(i);
        }
        invalidatePathCache();
        return this;
    }

    /**
     * 获取指定层级
     *
     * @param index 层级索引
     * @return 命令层级对象，如果索引无效则返回null
     */
    public CommandLevel getLevel(int index) {
        if (index < 0 || index >= this.commandLevels.size()) {
            return null;
        }
        return this.commandLevels.get(index);
    }

    /**
     * 获取根层级命令
     *
     * @return 根层级命令对象，如果列表为空则返回null
     */
    public CommandLevel getRootLevel() {
        return this.commandLevels.isEmpty() ? null : this.commandLevels.get(0);
    }

    /**
     * 获取叶层级命令
     *
     * @return 叶层级命令对象，如果列表为空则返回null
     */
    public CommandLevel getLeafLevel() {
        return this.commandLevels.isEmpty() ? null : this.commandLevels.get(this.commandLevels.size() - 1);
    }

    /**
     * 获取命令层级数量
     *
     * @return 命令层级数量
     */
    public int getLevelCount() {
        return this.commandLevels.size();
    }

    /**
     * 获取所有命令层级
     *
     * @return 命令层级列表的副本
     */
    public List<CommandLevel> getCommandLevels() {
        return new ArrayList<>(this.commandLevels);
    }

    /**
     * 从命令路径解析
     *
     * @param commandPath 命令路径字符串
     * @return 当前命令结构对象，支持链式调用
     */
    @Override
    public HierarchicalCommandStructure parse(String commandPath) {
        if (StringUtils.isBlank(commandPath)) {
            throw new IllegalArgumentException("Command path cannot be null or empty");
        }

        // 清空现有层级
        this.commandLevels.clear();

        // 使用配置的分隔符进行分割
        String[] parts = commandPath.split(java.util.regex.Pattern.quote(config.getSeparator()));

        // 为每个部分创建命令层级
        for (int i = 0; i < parts.length; i++) {
            this.commandLevels.add(new CommandLevel(parts[i], parts[i], i));
        }

        // 更新路径缓存
        this.commandPath = commandPath;
        this.normalizedPath = normalizePath(commandPath);

        return this;
    }

    /**
     * 构建命令路径
     */
    @Override
    protected void buildCommandPath() {
        if (this.commandLevels.isEmpty()) {
            this.commandPath = "";
            this.normalizedPath = "";
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.commandLevels.size(); i++) {
            if (i > 0) {
                sb.append(config.getSeparator());
            }
            sb.append(this.commandLevels.get(i).getName());
        }

        this.commandPath = sb.toString();
        this.normalizedPath = normalizePath(this.commandPath);
    }

    /**
     * 获取父路径（去掉最后一个层级）
     *
     * @return 父路径字符串
     */
    public String getParentPath() {
        if (this.commandLevels.size() <= 1) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.commandLevels.size() - 1; i++) {
            if (i > 0) {
                sb.append(config.getSeparator());
            }
            sb.append(this.commandLevels.get(i).getName());
        }
        return sb.toString();
    }

    /**
     * 获取子路径（从指定层级开始）
     *
     * @param startIndex 起始层级索引
     * @return 子路径字符串
     */
    public String getSubPath(int startIndex) {
        if (startIndex < 0 || startIndex >= this.commandLevels.size()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < this.commandLevels.size(); i++) {
            if (i > startIndex) {
                sb.append(config.getSeparator());
            }
            sb.append(this.commandLevels.get(i).getName());
        }
        return sb.toString();
    }

    /**
     * 查找匹配的命令结构
     *
     * @param commandList 命令结构列表
     * @param strategy    匹配策略
     * @return 匹配的命令结构列表
     */
    public List<HierarchicalCommandStructure> findMatchingCommands(
            List<HierarchicalCommandStructure> commandList, MatchStrategy strategy) {

        List<HierarchicalCommandStructure> result = new ArrayList<>();

        switch (strategy) {
            case EXACT:
                for (HierarchicalCommandStructure cmd : commandList) {
                    if (getCommandPath().equals(cmd.getCommandPath())) {
                        result.add(cmd);
                    }
                }
                break;

            case PREFIX:
                for (HierarchicalCommandStructure cmd : commandList) {
                    if (cmd.getCommandPath().startsWith(getCommandPath())) {
                        result.add(cmd);
                    }
                }
                break;

            case SUFFIX:
                for (HierarchicalCommandStructure cmd : commandList) {
                    if (cmd.getCommandPath().endsWith(getCommandPath())) {
                        result.add(cmd);
                    }
                }
                break;

            case CONTAINS:
                for (HierarchicalCommandStructure cmd : commandList) {
                    if (cmd.getCommandPath().contains(getCommandPath())) {
                        result.add(cmd);
                    }
                }
                break;
        }

        return result;
    }

    /**
     * 验证命令结构
     *
     * @return 验证结果对象
     */
    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();

        // 验证每个层级
        for (int i = 0; i < this.commandLevels.size(); i++) {
            CommandLevel level = this.commandLevels.get(i);

            // 验证层级名称
            if (StringUtils.isBlank(level.getName())) {
                result.addError("Level " + i + " name cannot be null or empty");
            } else if (level.getName().length() > config.getMaxCommandLength()) {
                result.addWarning("Level " + i + " name exceeds recommended length: " + level.getName().length());
            } else if (!isValidCommandFormat(level.getName())) {
                result.addError("Invalid format for level " + i + " name: " + level.getName());
            }
        }

        // 验证路径长度
        String path = getCommandPath();
        if (path.length() > config.getMaxPathLength()) {
            result.addWarning("Command path exceeds recommended length: " + path.length());
        }

        return result;
    }

    /**
     * 执行命令路由
     */
    @Override
    public RoutingResult route() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HierarchicalCommandStructure that = (HierarchicalCommandStructure) o;
        return Objects.equals(commandLevels, that.commandLevels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), commandLevels);
    }

    @Override
    public String toString() {
        return "HierarchicalCommandStructure{" +
                "commandPath='" + getCommandPath() + '\'' +
                ", levelCount=" + getLevelCount() +
                '}';
    }

    /**
     * 匹配策略枚举
     */
    public enum MatchStrategy {
        EXACT,    // 精确匹配
        PREFIX,   // 前缀匹配
        SUFFIX,   // 后缀匹配
        CONTAINS  // 包含匹配
    }

    /**
     * 命令层级类 - 表示命令结构中的一个层级
     */
    public static class CommandLevel implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;           // 层级名称
        private Object value;          // 层级值
        private String description;    // 层级描述
        private int levelIndex;        // 层级索引

        /**
         * 构造函数
         *
         * @param name       层级名称
         * @param value      层级值
         * @param levelIndex 层级索引
         */
        public CommandLevel(String name, Object value, int levelIndex) {
            this.name = name;
            this.value = value;
            this.levelIndex = levelIndex;
        }

        /**
         * 构造函数（带描述）
         *
         * @param name        层级名称
         * @param value       层级值
         * @param description 层级描述
         * @param levelIndex  层级索引
         */
        public CommandLevel(String name, Object value, String description, int levelIndex) {
            this.name = name;
            this.value = value;
            this.description = description;
            this.levelIndex = levelIndex;
        }

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getLevelIndex() {
            return levelIndex;
        }

        public void setLevelIndex(int levelIndex) {
            this.levelIndex = levelIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CommandLevel that = (CommandLevel) o;
            return levelIndex == that.levelIndex &&
                    Objects.equals(name, that.name) &&
                    Objects.equals(value, that.value) &&
                    Objects.equals(description, that.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value, description, levelIndex);
        }

        @Override
        public String toString() {
            return "CommandLevel{" +
                    "name='" + name + '\'' +
                    ", value=" + value +
                    ", description='" + description + '\'' +
                    ", levelIndex=" + levelIndex +
                    '}';
        }
    }
}
