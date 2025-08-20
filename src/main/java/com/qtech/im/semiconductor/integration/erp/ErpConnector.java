package com.qtech.im.semiconductor.integration.erp;

/**
 * ERP系统连接工具类
 *
 * 解决问题:
 * - 与ERP系统集成复杂
 * - 业务数据流转不顺畅
 * - 订单状态更新不及时
 * - 财务数据准确性难保证
 */
public class ErpConnector {
    // 订单状态同步
    public static boolean syncOrderStatus(String orderId, OrderStatus status);

    // 生产计划接收
    public static ProductionPlan receiveProductionPlan(ErpPlan erpPlan);

    // 成本数据上报
    public static boolean reportCostData(CostData costData);

    // 库存信息同步
    public static boolean syncInventoryData(InventoryData inventoryData);
}
