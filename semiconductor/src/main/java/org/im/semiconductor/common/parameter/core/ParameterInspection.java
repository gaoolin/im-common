package org.im.semiconductor.common.parameter.core;

import java.util.Map;

/**
 * 工业制造和质量管控领域参数检查、检验、审查
 * <p>
 * - 参数点检：对设备参数、工艺参数进行检查验证
 * - 质量检验：对产品规格、性能参数进行核查
 * - 合规性检查：确保生产过程符合标准要求
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/08/21 14:10:20
 */
public interface ParameterInspection {
    void addDifference(String property, Object modelVal, Object actualVal);

    Map<String, Map.Entry<Object, Object>> getDifferences();

    Map<String, Object> getEmptyInActual();

    Map<String, Object> getEmptyInStandard();

    boolean hasDifferences();
}

