package org.im.semiconductor.common.context.production;

import java.util.List;
import java.util.Map;

/**
 * 人员和班次信息接口
 * <p>
 * 描述半导体生产过程中的操作人员和班次相关信息。
 * 该接口提供了人员和班次的完整信息描述，便于在人员管理、排班、权限控制等环节使用。
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */
public interface PersonnelInfo {

    /**
     * 获取操作员唯一标识符
     * <p>
     * 操作员的全局唯一标识
     * </p>
     *
     * @return 操作员ID，如果未设置则返回null
     */
    String getOperatorId();

    /**
     * 获取操作员姓名
     * <p>
     * 操作员的可读姓名
     * </p>
     *
     * @return 操作员姓名，如果未设置则返回null
     */
    String getOperator();

    /**
     * 获取班次标识
     * <p>
     * 当前班次的标识
     * </p>
     *
     * @return 班次，如果未设置则返回null
     */
    String getShift();

    /**
     * 获取班次时间
     * <p>
     * 班次的具体时间范围
     * </p>
     *
     * @return 班次时间，如果未设置则返回null
     */
    String getShiftTime();

    /**
     * 获取部门信息
     * <p>
     * 操作员所属部门
     * </p>
     *
     * @return 部门信息，如果未设置则返回null
     */
    String getDepartment();

    /**
     * 验证人员信息的完整性
     * <p>
     * 检查必需字段是否都已正确设置
     * </p>
     *
     * @return 如果人员信息有效返回true，否则返回false
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
     * @return 人员信息的JSON格式字符串
     */
    String toJson();

    /**
     * 转换为键值对映射
     *
     * @return 包含人员信息的Map对象
     */
    Map<String, Object> toMap();

    /**
     * 获取对象的唯一标识符
     *
     * @return 唯一标识符字符串
     */
    String getUniqueIdentifier();
}
