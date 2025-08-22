package com.qtech.im.semiconductor.equipment.qtech;

import com.qtech.im.semiconductor.equipment.parameter.command.HierarchicalCommandStructure;
/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/22 11:33:45
 * desc   :
 */

/**
 * 设备命令参数实现类 - 继承自抽象基类
 */
public class ImAaListCommand extends HierarchicalCommandStructure {
    private BoundsLoader boundsLoader;

    public ImAaListCommand() {
        super();
    }

    public ImAaListCommand(BoundsLoader boundsLoader) {
        this.boundsLoader = boundsLoader;
    }

    public static ImAaListCommand of(BoundsLoader boundsLoader) {
        return new ImAaListCommand(boundsLoader);
    }
}
