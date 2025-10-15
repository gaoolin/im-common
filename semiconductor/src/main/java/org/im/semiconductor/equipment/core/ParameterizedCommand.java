package org.im.semiconductor.equipment.core;

import org.im.semiconductor.common.parameter.core.Parameter;

import java.util.List;

/**
 * 参数化命令接口
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */
public interface ParameterizedCommand<T> extends Command<T> {
    void addParameter(Parameter parameter);

    void removeParameter(String parameterId);

    Parameter getParameter(String parameterId);

    List<Parameter> getParameters();
}