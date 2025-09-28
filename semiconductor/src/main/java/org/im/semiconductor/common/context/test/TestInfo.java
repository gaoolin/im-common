package org.im.semiconductor.common.context.test;

import java.util.List;
import java.util.Map;

/**
 * 测试相关信息接口
 * <p>
 * 描述半导体生产过程中的测试相关信息，包括测试程序、晶圆片号等核心属性。
 * 该接口提供了测试的完整信息描述，便于在测试执行、结果分析、质量管控等环节使用。
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */
public interface TestInfo {

    /**
     * 获取测试程序名称
     * <p>
     * 使用的测试程序标识名称
     * </p>
     *
     * @return 测试程序名称，如果未设置则返回null
     */
    String getTestProgram();

    /**
     * 获取晶圆片号
     * <p>
     * 测试晶圆的唯一标识号码
     * </p>
     *
     * @return 晶圆片号，如果未设置则返回null
     */
    String getWafer();

    /**
     * 获取芯片位置
     * <p>
     * 芯片在晶圆上的具体位置坐标
     * </p>
     *
     * @return 芯片位置，如果未设置则返回null
     */
    String getDie();

    /**
     * 获取测试站点
     * <p>
     * 测试执行的站点标识
     * </p>
     *
     * @return 测试站点，如果未设置则返回null
     */
    String getTestSite();

    /**
     * 获取测试结果
     * <p>
     * 测试的执行结果状态
     * </p>
     *
     * @return 测试结果，如果未设置则返回null
     */
    String getTestResult();

    /**
     * 验证测试信息的完整性
     * <p>
     * 检查必需字段是否都已正确设置
     * </p>
     *
     * @return 如果测试信息有效返回true，否则返回false
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
     * @return 测试信息的JSON格式字符串
     */
    String toJson();

    /**
     * 转换为键值对映射
     *
     * @return 包含测试信息的Map对象
     */
    Map<String, Object> toMap();

    /**
     * 获取对象的唯一标识符
     *
     * @return 唯一标识符字符串
     */
    String getUniqueIdentifier();
}
