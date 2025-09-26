package org.im.semiconductor.common.parameter.core;

import java.util.List;

/**
 * 层级结构参数接口 - 提供参数的层级关系管理
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public interface HierarchicalParameter extends Parameter {
    /**
     * 获取参数的父级参数
     */
    Parameter getParentParameter();

    void setParentParameter(Parameter parentParameter);

    /**
     * 获取子参数列表
     */
    List<Parameter> getChildParameters();

    void addChildParameter(Parameter childParameter);

    void removeChildParameter(Parameter childParameter);

    boolean hasChildParameter(Parameter childParameter);

    /**
     * 获取参数的路径
     */
    String getPath();

    /**
     * 获取参数的层级深度
     */
    int getDepth();

    /**
     * 获取参数的完整路径（包括父级）
     */
    String getFullPath();
}