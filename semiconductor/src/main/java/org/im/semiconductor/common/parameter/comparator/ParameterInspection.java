package org.im.semiconductor.common.parameter.comparator;

import java.util.AbstractMap;
import java.util.HashMap;
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
 * @since 2025/08/21 14:10:20
 */
public class ParameterInspection {
    private final Map<String, Map.Entry<Object, Object>> differences = new HashMap<>();
    private final Map<String, Object> emptyInActual = new HashMap<>();
    private final Map<String, Object> emptyInStandard = new HashMap<>();

    public void addDifference(String property, Object modelVal, Object actualVal) {
        if (modelVal == null) {
            emptyInStandard.put(property, null);
        } else if (actualVal == null) {
            emptyInActual.put(property, null);
        } else {
            differences.put(property, new AbstractMap.SimpleImmutableEntry<>(modelVal, actualVal));
        }
    }

    public Map<String, Map.Entry<Object, Object>> getDifferences() {
        return new HashMap<>(differences);
    }

    public Map<String, Object> getEmptyInActual() {
        return new HashMap<>(emptyInActual);
    }

    public Map<String, Object> getEmptyInStandard() {
        return new HashMap<>(emptyInStandard);
    }

    public boolean hasDifferences() {
        return !differences.isEmpty() || !emptyInActual.isEmpty() || !emptyInStandard.isEmpty();
    }

    @Override
    public String toString() {
        return "ParameterInspection{" + "differences=" + differences.size() + ", emptyInActual=" + emptyInActual.size() + ", emptyInStandard=" + emptyInStandard.size() + '}';
    }
}
