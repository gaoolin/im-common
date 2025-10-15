package org.im.semiconductor.common.context.carrier;

import java.util.List;
import java.util.Map;

/**
 * 载具信息接口
 * <p>
 * 描述半导体生产过程中使用的载具相关信息，包括载具标识、类型等核心属性。
 * 载具用于承载晶圆或其他产品在生产流程中进行传输和处理。
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */
public interface CarrierInfo {

    /**
     * 获取载具唯一标识符
     * <p>
     * 载具的全局唯一标识，用于系统内载具识别和追踪
     * </p>
     *
     * @return 载具ID，如果未设置则返回null
     */
    String getCarrierId();

    /**
     * 获取载具名称
     * <p>
     * 载具的可读名称，用于显示和业务交流
     * </p>
     *
     * @return 载具名称，如果未设置则返回null
     */
    String getCarrierName();

    /**
     * 获取载具类型
     * <p>
     * 载具的分类标识，表示载具的基本类型或规格
     * </p>
     *
     * @return 载具类型，如果未设置则返回null
     */
    String getCarrierType();

    /**
     * 获取载具状态
     * <p>
     * 载具的当前使用状态，如可用、使用中、维护中等
     * </p>
     *
     * @return 载具状态，如果未设置则返回null
     */
    String getStatus();

    /**
     * 验证载具信息的完整性
     * <p>
     * 检查必需字段是否都已正确设置
     * </p>
     *
     * @return 如果载具信息有效返回true，否则返回false
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
     * @return 载具信息的JSON格式字符串
     */
    String toJson();

    /**
     * 转换为键值对映射
     *
     * @return 包含载具信息的Map对象
     */
    Map<String, Object> toMap();

    /**
     * 获取对象的唯一标识符
     *
     * @return 唯一标识符字符串
     */
    String getUniqueIdentifier();
}
