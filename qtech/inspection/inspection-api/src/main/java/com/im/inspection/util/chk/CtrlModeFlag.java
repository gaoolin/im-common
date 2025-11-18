package com.im.inspection.util.chk;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 控制模式
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/06/26 11:05:55
 */
public class CtrlModeFlag {
    // 为不同模块维护独立的控制模式
    public static final Map<String, ControlMode> moduleControlModes = new ConcurrentHashMap<>();
    // 保持全局默认控制模式
    public static volatile ControlMode defaultControlMode = ControlMode.DEFAULT;

    // 静态初始化块，为已知模块设置初始值
    static {
        // 设置 aa-list 模块初始为常开模式
        moduleControlModes.put(ControlModule.AA_LIST.getValue(), ControlMode.ALWAYS_RETURN);
        // 设置 wb-olp 模块初始为常开模式
        moduleControlModes.put(ControlModule.WB_OLP.getValue(), ControlMode.ALWAYS_RETURN);
    }

    // 获取指定模块的控制模式
    public static ControlMode getControlMode(String module) {
        return moduleControlModes.getOrDefault(module, defaultControlMode);
    }

    // 设置指定模块的控制模式
    public static void setControlMode(String module, ControlMode mode) {
        moduleControlModes.put(module, mode);
    }
}

