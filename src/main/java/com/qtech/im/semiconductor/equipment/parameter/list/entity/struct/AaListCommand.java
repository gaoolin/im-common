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
}
