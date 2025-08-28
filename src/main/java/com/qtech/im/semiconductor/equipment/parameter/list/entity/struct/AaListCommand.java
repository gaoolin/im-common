package com.qtech.im.semiconductor.equipment.parameter.list.entity.struct;

import com.qtech.im.semiconductor.equipment.parameter.list.structure.Hierarchical.HierarchicalCommandStructure;

/**
 * 设备命令参数实现类 - 继承自抽象基类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/22
 */
public class AaListCommand extends HierarchicalCommandStructure {
    private BoundsLoader boundsLoader;

    public AaListCommand() {
        super();
        // 明确初始化为 null，避免歧义
        this.boundsLoader = null;
    }

    public AaListCommand(BoundsLoader boundsLoader) {
        this.boundsLoader = boundsLoader;
    }

    public static AaListCommand of(BoundsLoader boundsLoader) {
        return new AaListCommand(boundsLoader);
    }

    public BoundsLoader getBoundsLoader() {
        return boundsLoader;
    }

    public void setBoundsLoader(BoundsLoader boundsLoader) {
        this.boundsLoader = boundsLoader;
    }

    /**
     * 构建命令路径
     */
    @Override
    protected void buildCommandPath() {
        // 清空现有层级
        this.getCommandLevels().clear();

        // 按顺序添加层级
        if (isNotEmpty(this.getParentCommand())) {
            this.addLevel(this.getParentCommand(), null);
        }

        if (isNotEmpty(this.getCurrentCommand())) {
            this.addLevel(this.getCurrentCommand(), null);
        }

        if (isNotEmpty(this.getSubCommand())) {
            this.addLevel(this.getSubCommand(), null);
        }

        // 调用父类方法完成路径构建
        super.buildCommandPath();
    }

    /**
     * 判断字符串非空且非空字符串
     */
    private boolean isNotEmpty(String str) {
        return str != null && !str.isEmpty();
    }
}
