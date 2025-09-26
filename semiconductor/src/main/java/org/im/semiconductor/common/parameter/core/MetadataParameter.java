package org.im.semiconductor.common.parameter.core;

import java.util.List;
import java.util.Map;

/**
 * 元数据参数接口 - 提供参数的元数据和标签管理
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public interface MetadataParameter extends Parameter {
    /**
     * 获取参数的标签列表
     */
    List<String> getTags();

    void addTag(String tag);

    void removeTag(String tag);

    boolean hasTag(String tag);

    /**
     * 获取参数的元数据
     */
    Map<String, Object> getMetadata();

    void setMetadata(Map<String, Object> metadata);

    /**
     * 获取参数的属性值
     */
    Object getAttribute(String key);

    void setAttribute(String key, Object value);
}