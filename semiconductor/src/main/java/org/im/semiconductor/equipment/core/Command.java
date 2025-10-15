package org.im.semiconductor.equipment.core;

import org.im.semiconductor.common.parameter.core.Parameter;

import java.util.List;
import java.util.Map;

/**
 * 命令接口 - 定义所有命令的基本契约
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */
public interface Command<T> {
    String getName();

    String getId();

    long getCreateTime();

    CommandStatus getStatus();

    // 参数管理
    void addParameter(Parameter parameter);

    void removeParameter(String parameterId);

    Parameter getParameter(String parameterId);

    List<Parameter> getParameters();

    Map<String, Object> getParameterValues();

    // 元数据
    Map<String, Object> getMetadata();

    void setMetadata(String key, Object value);

    // 执行
    T execute() throws Exception;
}