package org.im.semiconductor.common.context.production;

/**
 * 人员和班次信息
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public interface PersonnelInfo {
    String getOperatorId();  // 操作员ID

    String getOperator();    // 操作员姓名

    String getShift();       // 班次
}