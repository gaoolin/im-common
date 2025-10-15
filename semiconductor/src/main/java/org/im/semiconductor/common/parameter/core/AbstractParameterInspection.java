package org.im.semiconductor.common.parameter.core;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/29
 */

public abstract class AbstractParameterInspection implements ParameterInspection {
    protected final Map<String, Map.Entry<Object, Object>> differences = new HashMap<>();
    protected final Map<String, Object> emptyInActual = new HashMap<>();
    protected final Map<String, Object> emptyInStandard = new HashMap<>();

    @Override
    public void addDifference(String property, Object modelVal, Object actualVal) {
        if (modelVal == null) {
            emptyInStandard.put(property, null);
        } else if (actualVal == null) {
            emptyInActual.put(property, null);
        } else {
            differences.put(property, new AbstractMap.SimpleImmutableEntry<>(modelVal, actualVal));
        }
    }

    @Override
    public Map<String, Map.Entry<Object, Object>> getDifferences() {
        return new HashMap<>(differences);
    }

    @Override
    public Map<String, Object> getEmptyInActual() {
        return new HashMap<>(emptyInActual);
    }

    @Override
    public Map<String, Object> getEmptyInStandard() {
        return new HashMap<>(emptyInStandard);
    }

    @Override
    public boolean hasDifferences() {
        return !differences.isEmpty() || !emptyInActual.isEmpty() || !emptyInStandard.isEmpty();
    }
}
