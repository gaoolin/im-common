package org.im.semiconductor.common.context.location;

/**
 * 地理位置信息
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public interface LocationInfo {
    /**
     * 公司
     */
    String getCompany();     // 公司名称

    /**
     * 厂区
     */
    String getSite();        // 厂区名称

    /**
     * 车间
     */
    String getWorkshop();    // 车间名称

    /**
     * 楼栋
     */
    String getBuilding();    // 建筑物名称

    /**
     * 楼层
     */
    String getFloor();       // 楼层

    /**
     * 工段/站位
     */
    String getSection();     // 工段名称

    // 获取完整位置信息
    String getFullLocation();
}