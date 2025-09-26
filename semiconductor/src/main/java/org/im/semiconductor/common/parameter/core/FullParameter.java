package org.im.semiconductor.common.parameter.core;

/**
 * 完整参数接口 - 组合所有参数功能
 * <p>
 * 职责清晰：每个接口只关注特定功能领域
 * 灵活组合：可根据实际需求选择实现特定接口
 * 易于扩展：新增功能只需定义新接口，不影响现有代码
 * 符合行业标准：满足半导体封装测试行业的参数管理需求
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/26
 */

public interface FullParameter extends
        Parameter,
        DescriptiveParameter,
        IdentifiableParameter,
        TimestampedParameter,
        AccessControlParameter,
        MetadataParameter,
        HierarchicalParameter,
        LifecycleParameter,
        ValidatableParameter {
    // 继承所有接口方法，无需额外定义
}