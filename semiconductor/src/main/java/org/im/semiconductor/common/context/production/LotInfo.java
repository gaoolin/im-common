package org.im.semiconductor.common.context.production;

import java.util.List;
import java.util.Map;

/**
 * 生产批次信息接口
 * <p>
 * 描述半导体生产过程中的批次相关信息，包括工单、批次号等核心属性。
 * 该接口提供了批次的完整信息描述，便于在生产计划、执行、追踪等环节使用。
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */
public interface LotInfo {

    /**
     * 获取工单号码
     * <p>
     * 生产工单的唯一标识号码
     * </p>
     *
     * @return 工单号码，如果未设置则返回null
     */
    String getWorkOrder();

    /**
     * 获取批次号码
     * <p>
     * 生产批次的唯一标识号码
     * </p>
     *
     * @return 批次号码，如果未设置则返回null
     */
    String getLot();

    /**
     * 获取子批次号码
     * <p>
     * 批次下的子批次标识号码
     * </p>
     *
     * @return 子批次号码，如果未设置则返回null
     */
    String getSubLot();

    /**
     * 获取批次状态
     * <p>
     * 批次的当前生产状态
     * </p>
     *
     * @return 批次状态，如果未设置则返回null
     */
    String getLotStatus();

    /**
     * 获取批次数量
     * <p>
     * 批次包含的产品数量
     * </p>
     *
     * @return 批次数量，如果未设置则返回null
     */
    Integer getQuantity();

    /**
     * 验证批次信息的完整性
     * <p>
     * 检查必需字段是否都已正确设置
     * </p>
     *
     * @return 如果批次信息有效返回true，否则返回false
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
     * @return 批次信息的JSON格式字符串
     */
    String toJson();

    /**
     * 转换为键值对映射
     *
     * @return 包含批次信息的Map对象
     */
    Map<String, Object> toMap();

    /**
     * 获取对象的唯一标识符
     *
     * @return 唯一标识符字符串
     */
    String getUniqueIdentifier();
}
