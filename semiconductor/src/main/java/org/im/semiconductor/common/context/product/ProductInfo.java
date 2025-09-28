package org.im.semiconductor.common.context.product;

import java.util.List;
import java.util.Map;

/**
 * 产品信息接口
 * <p>
 * 用于描述半导体生产过程中的产品相关信息，包括产品标识、规格、客户等核心属性。
 * 该接口提供了产品的完整信息描述，便于在生产、测试、质量管控等环节使用。
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */
public interface ProductInfo {

    /**
     * 获取产品唯一标识符
     * <p>
     * 产品的全局唯一标识，用于系统内产品识别和追踪
     * </p>
     *
     * @return 产品ID，如果未设置则返回null
     */
    String getProductId();

    /**
     * 获取产品名称
     * <p>
     * 产品的可读名称，用于显示和业务交流
     * </p>
     *
     * @return 产品名称，如果未设置则返回null
     */
    String getProductName();

    /**
     * 获取产品类型/机型
     * <p>
     * 产品的分类标识，表示产品的基本类型或系列
     * </p>
     *
     * @return 产品类型，如果未设置则返回null
     */
    String getProductType();

    /**
     * 获取产品规格
     * <p>
     * 产品的技术规格参数，定义产品的具体技术要求
     * </p>
     *
     * @return 产品规格，如果未设置则返回null
     */
    String getSpec();

    /**
     * 获取客户名称
     * <p>
     * 产品的目标客户或订单客户名称
     * </p>
     *
     * @return 客户名称，如果未设置则返回null
     */
    String getCustomer();

    /**
     * 获取质量等级
     * <p>
     * 产品的质量标准等级，如A级、B级等
     * </p>
     *
     * @return 质量等级，如果未设置则返回null
     */
    String getQuality();

    /**
     * 验证产品信息的完整性
     * <p>
     * 检查必需字段是否都已正确设置
     * </p>
     *
     * @return 如果产品信息有效返回true，否则返回false
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
     * @return 产品的JSON格式字符串
     */
    String toJson();

    /**
     * 转换为键值对映射
     *
     * @return 包含产品信息的Map对象
     */
    Map<String, Object> toMap();

    /**
     * 获取对象的唯一标识符
     *
     * @return 唯一标识符字符串
     */
    String getUniqueIdentifier();
}
