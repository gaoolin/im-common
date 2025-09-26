package org.im.semiconductor.common.context.product;

/**
 * 产品信息
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public interface ProductInfo {
    String getProduct();     // 产品类型/机型

    String getSpec();        // 产品规格

    String getCustomer();    // 客户名称

    String getQuality();     // 质量等级
}