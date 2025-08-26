package com.qtech.im.semiconductor.equipment.parameter.list.structure;

import java.io.Serializable;

/**
 * 抽象命令结构基类 - 提供命令结构的通用实现
 * <p>
 * 特性：
 * - 通用性：提供命令结构的通用实现
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
public abstract class AbstractCommandStructure implements CommandStructure, Serializable {
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
    public AbstractCommandStructure() {
        this.config = new CommandConfig();
    }

    /**
     * 带参数的构造函数
     *
     * @param parentCommand  父命令
     * @param currentCommand 当前命令
     * @param subCommand     子命令
     * @param sequenceNumber 序号
     */
    public AbstractCommandStructure(String parentCommand, String currentCommand, String subCommand, Integer sequenceNumber) {
        this.parentCommand = parentCommand;
        this.currentCommand = currentCommand;
        this.subCommand = subCommand;
        this.sequenceNumber = sequenceNumber;
        this.config = new CommandConfig();

        // 自动构建命令路径
        buildCommandPath();
    }

    /**
     * 获取命令路径
     *
     * @return 命令路径字符串
     */
    @Override
    public String getCommandPath() {
        if (this.commandPath == null) {
            buildCommandPath();
        }
        return this.commandPath;
    }

    /**
     * 获取规范化路径
     *
     * @return 规范化路径字符串
     */
    @Override
    public String getNormalizedPath() {
        if (this.normalizedPath == null) {
            this.normalizedPath = normalizePath(getCommandPath());
        }
        return this.normalizedPath;
    }

    /**
     * 获取父命令
     *
     * @return 父命令字符串
     */
    public String getParentCommand() {
        return parentCommand;
    }

    /**
     * 设置父命令
     *
     * @param parentCommand 父命令字符串
     */
    public void setParentCommand(String parentCommand) {
        this.parentCommand = parentCommand;
    }

    /**
     * 获取当前命令
     *
     * @return 当前命令字符串
     */
    @Override
    public String getCurrentCommand() {
        return currentCommand;
    }

    /**
     * 设置当前命令
     *
     * @param currentCommand 当前命令字符串
     */
    @Override
    public void setCurrentCommand(String currentCommand) {
        this.currentCommand = currentCommand;
        invalidatePathCache();
    }

    /**
     * 获取子命令
     *
     * @return 子命令字符串
     */
    public String getSubCommand() {
        return subCommand;
    }

    /**
     * 设置子命令
     *
     * @param subCommand 子命令字符串
     */
    public void setSubCommand(String subCommand) {
        this.subCommand = subCommand;
    }

    /**
     * 获取序号
     *
     * @return 序号
     */
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * 设置序号
     *
     * @param sequenceNumber 序号
     */
    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * 获取配置
     *
     * @return 命令配置对象
     */
    @Override
    public CommandConfig getConfig() {
        return config;
    }

    /**
     * 构建命令路径 - 抽象方法，由子类实现
     */
    protected abstract void buildCommandPath();

    /**
     * 规范化路径
     *
     * @param path 待规范化的路径字符串
     * @return 规范化后的路径字符串
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
     * 清除路径缓存
     */
    protected void invalidatePathCache() {
        this.commandPath = null;
        this.normalizedPath = null;
    }

    /**
     * 检查命令格式是否有效
     *
     * @param command 待检查的命令字符串
     * @return true表示格式有效，false表示格式无效
     */
    protected boolean isValidCommandFormat(String command) {
        if (command == null || command.isEmpty()) return false;
        // 基本格式检查：只允许字母、数字、下划线和连字符
        return command.matches("^[a-zA-Z0-9_-]+$");
    }
}
