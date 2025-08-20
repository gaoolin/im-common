package com.qtech.im.semiconductor.process.controller;

/**
 * 工艺过程控制工具类
 *
 * 解决问题:
 * - 工艺过程控制不精确
 * - 工艺参数调整不及时
 * - 工艺异常处理不规范
 * - 工艺过程追溯不完整
 */
public class ProcessController {
    // 工艺步骤执行
    public static ProcessStepResult executeStep(ProcessStep step, ExecutionContext context);

    // 实时工艺参数调整
    public static boolean adjustParameters(String processId, ParameterAdjustment adjustment);

    // 工艺异常处理
    public static ExceptionHandlingResult handleProcessException(ProcessException exception);

    // 工艺过程追溯
    public static ProcessTrace generateProcessTrace(String processId);
}