package com.qtech.im.semiconductor.equipment.parameter.comparator;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;
/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/21 14:10:20
 * desc   :
 */

/**
 * 比较结果类
 * 统一的比较结果数据结构
 */
public class ComparisonResult {
    private final Map<String, Map.Entry<Object, Object>> differences = new HashMap<>();
    private final Map<String, Object> emptyInActual = new HashMap<>();
    private final Map<String, Object> emptyInStandard = new HashMap<>();

    public void addDifference(String property, Object modelVal, Object actualVal) {
        if (modelVal == null) {
            emptyInStandard.put(property, null);
        } else if (actualVal == null) {
            emptyInActual.put(property, null);
        } else {
            differences.put(property, Maps.immutableEntry(modelVal, actualVal));
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
        return "ComparisonResult{" +
                "differences=" + differences.size() +
                ", emptyInActual=" + emptyInActual.size() +
                ", emptyInStandard=" + emptyInStandard.size() +
                '}';
    }
}
