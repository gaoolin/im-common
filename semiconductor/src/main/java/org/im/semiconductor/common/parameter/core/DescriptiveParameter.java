package org.im.semiconductor.common.parameter.core;

/**
 * 描述性参数接口 - 提供参数的描述信息
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */

public interface DescriptiveParameter extends Parameter {
    /**
     * 获取参数描述
     */
    String getParameterDescription();

    void setParameterDescription(String parameterDescription);

    /**
     * 获取参数单位
     */
    String getParameterUnit();

    void setParameterUnit(String parameterUnit);
}