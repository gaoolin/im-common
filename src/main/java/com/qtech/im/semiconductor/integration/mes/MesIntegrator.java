package com.qtech.im.semiconductor.integration.mes;

/**
 * MES系统集成工具类
 *
 * 解决问题:
 * - 与MES系统数据交互复杂
 * - 数据格式转换困难
 * - 实时数据同步不及时
 * - 系统间异常处理不统一
 */
public class MesIntegrator {
    // MES数据同步
    public static SyncResult syncDataToMes(DataPackage data);

    // MES指令接收和处理
    public static CommandResponse processMesCommand(MesCommand command);

    // 实时数据推送
    public static boolean pushRealTimeData(String topic, Object data);

    // 异常数据上报
    public static boolean reportExceptionData(ExceptionData data);
}