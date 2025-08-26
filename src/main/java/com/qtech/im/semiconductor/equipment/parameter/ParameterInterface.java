package com.qtech.im.semiconductor.equipment.parameter;

import com.qtech.im.common.security.AccessLevel;
import com.qtech.im.semiconductor.equipment.parameter.mgr.ParameterRange;
import com.qtech.im.semiconductor.equipment.parameter.mgr.ParameterStatus;
import com.qtech.im.semiconductor.equipment.parameter.mgr.ParameterType;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 设备参数接口
 * <p>
 * 特性：
 * - 通用性：适用于所有设备参数体系
 * - 规范性：遵循设备参数管理标准
 * - 专业性：提供专业的参数管理能力
 * - 灵活性：支持多种参数类型和结构
 * - 可靠性：确保参数数据的完整性
 * - 安全性：防止敏感信息泄露
 * - 复用性：可被各种设备项目复用
 * - 容错性：具备良好的错误处理能力
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @since 2025/08/22
 */
public interface ParameterInterface extends Serializable {

    /**
     * 获取设备标识符
     *
     * @return 设备标识符
     */
    String getDeviceId();

    /**
     * 设置设备标识符
     *
     * @param deviceId 设备标识符
     */
    void setDeviceId(String deviceId);

    /**
     * 获取参数名称
     *
     * @return 参数名称
     */
    String getParameterName();

    /**
     * 设置参数名称
     *
     * @param parameterName 参数名称
     */
    void setParameterName(String parameterName);

    /**
     * 获取参数值
     *
     * @return 参数值
     */
    Object getParameterValue();

    /**
     * 设置参数值
     *
     * @param parameterValue 参数值
     */
    void setParameterValue(Object parameterValue);

    /**
     * 获取参数类型
     *
     * @return 参数类型
     */
    ParameterType getParameterType();

    /**
     * 设置参数类型
     *
     * @param parameterType 参数类型
     */
    void setParameterType(ParameterType parameterType);

    /**
     * 获取参数描述
     *
     * @return 参数描述
     */
    String getParameterDescription();

    /**
     * 设置参数描述
     *
     * @param parameterDescription 参数描述
     */
    void setParameterDescription(String parameterDescription);

    /**
     * 获取参数单位
     *
     * @return 参数单位
     */
    String getParameterUnit();

    /**
     * 设置参数单位
     *
     * @param parameterUnit 参数单位
     */
    void setParameterUnit(String parameterUnit);

    /**
     * 获取参数范围
     *
     * @return 参数范围
     */
    ParameterRange getParameterRange();

    /**
     * 设置参数范围
     *
     * @param parameterRange 参数范围
     */
    void setParameterRange(ParameterRange parameterRange);

    /**
     * 获取参数状态
     *
     * @return 参数状态
     */
    ParameterStatus getParameterStatus();

    /**
     * 设置参数状态
     *
     * @param parameterStatus 参数状态
     */
    void setParameterStatus(ParameterStatus parameterStatus);

    /**
     * 获取参数配置
     *
     * @return 参数配置
     */
    Map<String, Object> getParameterConfig();

    /**
     * 设置参数配置
     *
     * @param parameterConfig 参数配置
     */
    void setParameterConfig(Map<String, Object> parameterConfig);

    /**
     * 验证参数值是否有效
     *
     * @return true表示有效
     */
    boolean isValid();

    /**
     * 获取参数的唯一标识符
     *
     * @return 唯一标识符
     */
    String getUniqueId();

    /**
     * 设置参数的唯一标识符
     *
     * @param uniqueId 唯一标识符
     */
    void setUniqueId(String uniqueId);

    /**
     * 获取参数的版本号
     *
     * @return 版本号
     */
    String getVersion();

    /**
     * 设置参数的版本号
     *
     * @param version 版本号
     */
    void setVersion(String version);

    /**
     * 获取参数的创建时间
     *
     * @return 创建时间
     */
    long getCreateTime();

    /**
     * 设置参数的创建时间
     *
     * @param createTime 创建时间
     */
    void setCreateTime(long createTime);

    /**
     * 获取参数的更新时间
     *
     * @return 更新时间
     */
    long getUpdateTime();

    /**
     * 设置参数的更新时间
     *
     * @param updateTime 更新时间
     */
    void setUpdateTime(long updateTime);

    /**
     * 检查参数是否为只读
     *
     * @return true表示只读
     */
    boolean isReadOnly();

    /**
     * 设置参数是否为只读
     *
     * @param readOnly 是否为只读
     */
    void setReadOnly(boolean readOnly);

    /**
     * 获取参数的访问权限
     *
     * @return 访问权限
     */
    AccessLevel getAccessLevel();

    /**
     * 设置参数的访问权限
     *
     * @param accessLevel 访问权限
     */
    void setAccessLevel(AccessLevel accessLevel);

    /**
     * 获取参数的标签列表
     *
     * @return 标签列表
     */
    List<String> getTags();

    /**
     * 添加标签
     *
     * @param tag 标签
     */
    void addTag(String tag);

    /**
     * 移除标签
     *
     * @param tag 标签
     */
    void removeTag(String tag);

    /**
     * 检查是否包含指定标签
     *
     * @param tag 标签
     * @return true表示包含
     */
    boolean hasTag(String tag);

    /**
     * 获取参数的元数据
     *
     * @return 元数据
     */
    Map<String, Object> getMetadata();

    /**
     * 设置参数的元数据
     *
     * @param metadata 元数据
     */
    void setMetadata(Map<String, Object> metadata);

    /**
     * 获取参数的父级参数
     *
     * @return 父级参数
     */
    ParameterInterface getParentParameter();

    /**
     * 设置父级参数
     *
     * @param parentParameter 父级参数
     */
    void setParentParameter(ParameterInterface parentParameter);

    /**
     * 获取子参数列表
     *
     * @return 子参数列表
     */
    List<ParameterInterface> getChildParameters();

    /**
     * 添加子参数
     *
     * @param childParameter 子参数
     */
    void addChildParameter(ParameterInterface childParameter);

    /**
     * 移除子参数
     *
     * @param childParameter 子参数
     */
    void removeChildParameter(ParameterInterface childParameter);

    /**
     * 检查是否包含指定子参数
     *
     * @param childParameter 子参数
     * @return true表示包含
     */
    boolean hasChildParameter(ParameterInterface childParameter);

    /**
     * 获取参数的路径
     *
     * @return 参数路径
     */
    String getPath();

    /**
     * 获取参数的层级深度
     *
     * @return 层级深度
     */
    int getDepth();

    /**
     * 获取参数的完整路径（包括父级）
     *
     * @return 完整路径
     */
    String getFullPath();

    /**
     * 获取参数的属性值
     *
     * @param key 属性键
     * @return 属性值
     */
    Object getAttribute(String key);

    /**
     * 设置参数的属性值
     *
     * @param key   属性键
     * @param value 属性值
     */
    void setAttribute(String key, Object value);

    /**
     * 检查参数是否已初始化
     *
     * @return true表示已初始化
     */
    boolean isInitialized();

    /**
     * 初始化参数
     */
    void initialize();

    /**
     * 克隆参数
     *
     * @return 克隆后的参数
     */
    ParameterInterface clone();
}
