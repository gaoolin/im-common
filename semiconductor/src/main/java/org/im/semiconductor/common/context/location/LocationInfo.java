package org.im.semiconductor.common.context.location;

import java.util.List;
import java.util.Map;

/**
 * 地理位置信息接口
 * <p>
 * 描述半导体生产环境中的地理位置层级信息，包括公司、厂区、车间等完整位置链路。
 * 该接口提供了标准化的位置信息描述，便于在生产、物流、人员管理等环节使用。
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */
public interface LocationInfo {

    /**
     * 获取公司唯一标识符
     * <p>
     * 公司的全局唯一标识
     * </p>
     *
     * @return 公司ID，如果未设置则返回null
     */
    String getCompanyId();

    /**
     * 获取公司名称
     * <p>
     * 公司的可读名称
     * </p>
     *
     * @return 公司名称，如果未设置则返回null
     */
    String getCompany();

    /**
     * 获取厂区唯一标识符
     * <p>
     * 厂区的全局唯一标识
     * </p>
     *
     * @return 厂区ID，如果未设置则返回null
     */
    String getSiteId();

    /**
     * 获取厂区名称
     * <p>
     * 厂区的可读名称
     * </p>
     *
     * @return 厂区名称，如果未设置则返回null
     */
    String getSite();

    /**
     * 获取车间唯一标识符
     * <p>
     * 车间的全局唯一标识
     * </p>
     *
     * @return 车间ID，如果未设置则返回null
     */
    String getWorkshopId();

    /**
     * 获取车间名称
     * <p>
     * 车间的可读名称
     * </p>
     *
     * @return 车间名称，如果未设置则返回null
     */
    String getWorkshop();

    /**
     * 获取建筑物唯一标识符
     * <p>
     * 建筑物的全局唯一标识
     * </p>
     *
     * @return 建筑物ID，如果未设置则返回null
     */
    String getBuildingId();

    /**
     * 获取建筑物名称
     * <p>
     * 建筑物的可读名称
     * </p>
     *
     * @return 建筑物名称，如果未设置则返回null
     */
    String getBuilding();

    /**
     * 获取楼层信息
     * <p>
     * 所在楼层标识
     * </p>
     *
     * @return 楼层，如果未设置则返回null
     */
    String getFloor();

    /**
     * 获取工段唯一标识符
     * <p>
     * 工段的全局唯一标识
     * </p>
     *
     * @return 工段ID，如果未设置则返回null
     */
    String getSectionId();

    /**
     * 获取工段名称
     * <p>
     * 工段的可读名称
     * </p>
     *
     * @return 工段名称，如果未设置则返回null
     */
    String getSection();

    /**
     * 获取完整位置信息
     * <p>
     * 包含从公司到工段的完整位置路径描述
     * </p>
     *
     * @return 完整位置信息字符串
     */
    String getFullLocation();

    /**
     * 获取位置编码
     * <p>
     * 标准化的位置编码标识
     * </p>
     *
     * @return 位置编码，如果未设置则返回null
     */
    String getLocationCode();

    /**
     * 验证位置信息的完整性
     * <p>
     * 检查必需字段是否都已正确设置
     * </p>
     *
     * @return 如果位置信息有效返回true，否则返回false
     */
    boolean isValid();

    /**
     * 检查是否包含必需字段
     *
     * @return 如果包含必需字段返回true，否则返回false
     */
    boolean hasRequiredFields();

    /**
     * 获取验证错误信息列表
     *
     * @return 验证错误信息列表，如果没有错误则返回空列表
     */
    List<String> getValidationErrors();

    /**
     * 转换为JSON字符串表示
     *
     * @return 位置信息的JSON格式字符串
     */
    String toJson();

    /**
     * 转换为键值对映射
     *
     * @return 包含位置信息的Map对象
     */
    Map<String, Object> toMap();

    /**
     * 获取对象的唯一标识符
     *
     * @return 唯一标识符字符串
     */
    String getUniqueIdentifier();
}
