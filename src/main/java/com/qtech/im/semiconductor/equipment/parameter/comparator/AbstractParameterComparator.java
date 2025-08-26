package com.qtech.im.semiconductor.equipment.parameter.comparator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qtech.im.util.json.JsonMapperProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/21 14:14:42
 */

/**
 * 参数比较器抽象基类
 * 提供通用的比较逻辑和工具方法
 */
public abstract class AbstractParameterComparator<T> implements ParameterComparator<T> {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractParameterComparator.class);
    protected static final ObjectMapper objectMapper = JsonMapperProvider.getSharedInstance();
    protected static final Map<String, Field> FIELD_CACHE = new ConcurrentHashMap<>();

    @Override
    public List<String> getSupportedPropertyTypes() {
        return Arrays.asList("numeric", "string", "bounds", "map", "custom");
    }

    protected Field getFieldFromCache(Class<?> clazz, String fieldName) {
        String key = clazz.getName() + "." + fieldName;
        return FIELD_CACHE.computeIfAbsent(key, k -> {
            try {
                return getFieldFromHierarchy(clazz, fieldName);
            } catch (NoSuchFieldException e) {
                return null;
            }
        });
    }

    protected Field getFieldFromHierarchy(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    protected FieldPair getFieldPair(T standardObj, T actualObj, String fieldName) {
        try {
            Field modelField = getFieldFromCache(standardObj.getClass(), fieldName);
            Field actualField = getFieldFromCache(actualObj.getClass(), fieldName);
            if (modelField == null || actualField == null) return null;
            modelField.setAccessible(true);
            actualField.setAccessible(true);
            return new FieldPair(modelField.get(standardObj), actualField.get(actualObj));
        } catch (Exception e) {
            logger.error("Error getting field {}: {}", fieldName, e.getMessage());
            return null;
        }
    }

    protected boolean areBothNull(Object a, Object b) {
        return a == null && b == null;
    }


    protected boolean numericEquals(Object a, Object b, double epsilon) {
        try {
            if (a == null || b == null) {
                return false;
            }
            double n1 = Double.parseDouble(a.toString());
            double n2 = Double.parseDouble(b.toString());
            return Math.abs(n1 - n2) < epsilon;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

    // 需要被子类访问
    protected static class FieldPair {
        private final Object modelValue;
        private final Object actualValue;

        FieldPair(Object m, Object a) {
            this.modelValue = m;
            this.actualValue = a;
        }

        public Object getModelValue() {
            return modelValue;
        }

        public Object getActualValue() {
            return actualValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FieldPair fieldPair = (FieldPair) o;
            return Objects.equals(modelValue, fieldPair.modelValue) &&
                    Objects.equals(actualValue, fieldPair.actualValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(modelValue, actualValue);
        }
    }
}
