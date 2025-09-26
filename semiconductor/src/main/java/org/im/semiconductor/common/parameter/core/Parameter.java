package org.im.semiconductor.common.parameter.core;

import org.im.semiconductor.common.parameter.mgr.ParameterRange;
import org.im.semiconductor.common.parameter.mgr.ParameterType;

import java.io.Serializable;

/**
 * 基础参数接口 - 定义参数的核心属性和行为
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public interface Parameter extends Serializable, Cloneable {
    /**
     * 获取参数名称
     */
    String getParameterName();

    void setParameterName(String parameterName);

    /**
     * 获取参数值
     */
    Object getParameterValue();

    void setParameterValue(Object parameterValue);

    /**
     * 获取参数类型
     */
    ParameterType getParameterType();

    void setParameterType(ParameterType parameterType);

    /**
     * 获取参数范围
     */
    ParameterRange getParameterRange();

    void setParameterRange(ParameterRange parameterRange);

    /**
     * 验证参数值是否有效
     */
    boolean isValid();

    /**
     * 克隆参数
     */
    Parameter clone() throws CloneNotSupportedException;
}