package com.qtech.im.semiconductor.material;

/**
 * 物料追踪工具类
 *
 * 解决问题:
 * - 物料流转跟踪困难
 * - 物料批次管理混乱
 * - 物料质量追溯不完整
 * - 物料库存管理不精确
 */
public class MaterialTracker {
    // 物料批次追踪
    public static MaterialTrace traceMaterialBatch(String batchId);

    // 物料质量状态查询
    public static QualityStatus getMaterialQualityStatus(String materialId);

    // 物料有效期管理
    public static ExpirationAlert checkMaterialExpiration(String materialId);

    // 物料库存预警
    public static InventoryAlert checkInventoryLevel(String materialId);
}
