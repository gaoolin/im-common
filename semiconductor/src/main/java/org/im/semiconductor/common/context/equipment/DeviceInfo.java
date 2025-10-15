package org.im.semiconductor.common.context.equipment;

import java.util.List;
import java.util.Map;

/**
 * 设备相关信息接口
 * <p>
 * 描述半导体生产过程中使用的设备相关信息，包括设备标识、工艺参数等核心属性。
 * 该接口提供了设备的完整信息描述，便于在生产、维护、监控等环节使用。
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/26
 */

// 具体的单台设备模型
public interface DeviceInfo {

    /**
     * 获取设备唯一标识符
     * <p>
     * 设备的全局唯一标识，用于系统内设备识别和追踪
     * </p>
     *
     * @return 设备ID，如果未设置则返回null
     */
    String getDeviceId();

    /**
     * 获取设备名称
     * <p>
     * 设备的可读名称，用于显示和业务交流
     * </p>
     *
     * @return 设备名称，如果未设置则返回null
     */
    String getDeviceName();

    /**
     * 获取设备类型
     * <p>
     * 设备的分类标识，表示设备的基本类型或功能
     * </p>
     *
     * @return 设备类型，如果未设置则返回null
     */
    String getDeviceType();

    /**
     * 获取生产线标识
     * <p>
     * 设备所属的生产线标识
     * </p>
     *
     * @return 生产线标识，如果未设置则返回null
     */
    String getLine();

    /**
     * 获取工艺步骤
     * <p>
     * 设备当前执行的工艺步骤标识
     * </p>
     *
     * @return 工艺步骤，如果未设置则返回null
     */
    String getStep();

    /**
     * 获取配方名称
     * <p>
     * 设备当前使用的工艺配方名称
     * </p>
     *
     * @return 配方名称，如果未设置则返回null
     */
    String getRecipe();

    /**
     * 获取参数群组
     * <p>
     * 设备使用的参数群组标识
     * </p>
     *
     * @return 参数群组，如果未设置则返回null
     */
    String getParamGroup();

    /**
     * 获取设备状态
     * <p>
     * 设备的当前运行状态，如运行、停止、维护等
     * </p>
     *
     * @return 设备状态，如果未设置则返回null
     */
    String getDeviceStatus();

    /**
     * 获取设备型号
     * <p>
     * 设备的具体型号信息
     * </p>
     *
     * @return 设备型号，如果未设置则返回null
     */
    String getModelNumber();

    /**
     * 验证设备信息的完整性
     * <p>
     * 检查必需字段是否都已正确设置
     * </p>
     *
     * @return 如果设备信息有效返回true，否则返回false
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
     * @return 设备信息的JSON格式字符串
     */
    String toJson();

    /**
     * 转换为键值对映射
     *
     * @return 包含设备信息的Map对象
     */
    Map<String, Object> toMap();

    /**
     * 获取对象的唯一标识符
     *
     * @return 唯一标识符字符串
     */
    String getUniqueIdentifier();
}
