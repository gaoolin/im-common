package org.im.semiconductor.production.metrics;

import org.im.semiconductor.common.context.equipment.EquipmentContext;
import org.im.semiconductor.common.context.location.LocationInfo;
import org.im.semiconductor.common.context.product.ProductInfo;
import org.im.semiconductor.common.context.production.LotInfo;
import org.im.semiconductor.common.context.production.PersonnelInfo;

/**
 * 与生产模块组合
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

// 生产执行上下文
public interface ProductionContextInfo extends LocationInfo, ProductInfo,
        LotInfo, PersonnelInfo, EquipmentContext {
    // 组合所有相关信息，用于完整的生产执行跟踪
}
