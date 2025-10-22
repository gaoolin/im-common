package com.im.aa.inspection.context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.aa.inspection.entity.struct.BoundsLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/05/20 10:59:31
 */
/*
Objects.equals()方法用于比较两个对象是否相等，包括null值。这意味着Objects.equals(value1, value2)会正确处理null值的情况。如果value1和value2都为null，
Objects.equals()会返回true，否则如果它们不相等（包括一个为null，另一个不为null），它会返回false。因此，if (!Objects.equals(value1, value2))会捕获不相等的值，
包括null和非null的组合。接下来的两个if语句分别检查value1和value2的空值情况，确保不会错过任何情况。
*/
public class AaListParamsTestComparator {
    private static final Logger logger = LoggerFactory.getLogger(AaListParamsTestComparator.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 比较两个对象的指定属性并返回不一致的属性及其值。
     *
     * @param standardObj         标准对象
     * @param actualObj           实际对象
     * @param propertiesToCompare 需要比较的属性列表
     * @param propertiesToCompute 需要额外处理的属性列表
     * @return 包含不一致属性、实际对象中为空的属性、标准对象中为空的属性的Triple
     */
    public static ImmutableTriple<Map<String, Map.Entry<Object, Object>>, Map<String, Object>, Map<String, Object>> compareObjectsWithStandardAndActual(Object standardObj, Object actualObj, List<String> propertiesToCompare, List<String> propertiesToCompute) {

        if ((propertiesToCompare == null || propertiesToCompare.isEmpty()) && (propertiesToCompute == null || propertiesToCompute.isEmpty())) {
            // 如果没有属性需要比较，则直接返回表示没有不一致的结果
            return new ImmutableTriple<>(new HashMap<>(), new HashMap<>(), new HashMap<>());
        }

        Map<String, Map.Entry<Object, Object>> inconsistentProperties = new HashMap<>();
        Map<String, Object> emptyInActual = new HashMap<>();
        Map<String, Object> emptyInStandard = new HashMap<>();

        // 合并两个列表进行处理
        Set<String> allProperties = new HashSet<>();
        if (propertiesToCompare != null) {
            allProperties.addAll(propertiesToCompare);
        }
        if (propertiesToCompute != null) {
            allProperties.addAll(propertiesToCompute);
        }

        // TODO 这里需要优化，胶检频率 0-30 都Ok。模版值和实际值，需要重新设计。
        for (String propertyName : allProperties) {
            try {
                Field modelField = getFieldFromClassHierarchy(standardObj.getClass(), propertyName);
                Field actualField = getFieldFromClassHierarchy(actualObj.getClass(), propertyName);

                // 获取字段的类型
                Class<?> fieldType = modelField.getType();
                String modelTypeName = fieldType.getName();

                modelField.setAccessible(true);
                actualField.setAccessible(true);

                // 获取字段的值并进行类型转换
                Object modelVal = fieldType.cast(modelField.get(standardObj));
                Object actualVal = fieldType.cast(actualField.get(actualObj));

                // 用于调试
                // if ("mtfCheckF".equals(propertyName) && modelVal != null) {
                //     logger.info(">>>>> 检测到属性: {}", propertyName);
                // }

                if (propertiesToCompute != null && propertiesToCompute.contains(propertyName)) {
                    // 特殊处理 mtfCheck 属性
                    if (StringUtils.startsWith(propertyName, "mtfCheck") && StringUtils.endsWith(propertyName, "F")) {
                        if (modelTypeName.equals("java.lang.String")) {
                            int isModelNull = modelVal == null ? 1 : 0;
                            int isActualNull = actualVal == null ? 1 : 0;

                            if (isModelNull + isActualNull == 0) {
                                // modelVal 和 actualVal 都不为空
                                String modelValJsonString = null;
                                String actualValJsonString = null;
                                modelValJsonString = modelVal.toString();
                                actualValJsonString = actualVal.toString();
                                compareJsonMaps(modelValJsonString, actualValJsonString, propertyName, inconsistentProperties, emptyInActual, emptyInStandard);
                            } else if (isModelNull + isActualNull == 1) {
                                // modelVal 或 actualVal 不为空
                                addToResult(modelVal, actualVal, propertyName, inconsistentProperties, emptyInActual, emptyInStandard);
                            } else {
                                // modelVal 为空，actualVal 为空
                                logger.info(">>>>> both modelVal and actualVal are empty propertyName: {}", propertyName);
                            }
                        } else {
                            logger.error(">>>>> Unsupported data policy for mtfCheck: {}", modelTypeName);
                        }
                        // FIXME: 这里需要优化，胶检频率 5-30 都Ok。模版值和实际值，需要重新设计。
                    } else if ("epoxyInspectionAuto".equals(propertyName)) {  // 需要特殊处理 胶检频率
                        if (modelVal != null) {
                            if (modelVal instanceof String) {
                                if (StringUtils.isNotBlank(modelVal.toString())) {
                                    int modelValInt = Integer.parseInt(modelVal.toString());
                                    if (StringUtils.isBlank(actualVal.toString())) {
                                        addToResult(modelVal, null, propertyName, inconsistentProperties, emptyInActual, emptyInStandard);
                                    } else {
                                        int actualValInt = Integer.parseInt(actualVal.toString());
                                        if (actualValInt > 30 || actualValInt < 5) {
                                            addToResult(modelValInt, actualValInt, propertyName, inconsistentProperties, emptyInActual, emptyInStandard);
                                        }
                                    }
                                } else {
                                    if (StringUtils.isBlank(actualVal.toString())) {
                                        logger.info(">>>>> both modelVal and actualVal are empty propertyName: {}", propertyName);
                                    } else {
                                        addToResult(null, actualVal, propertyName, inconsistentProperties, emptyInActual, emptyInStandard);
                                    }
                                }
                            } else {
                                logger.error(">>>>> Unsupported data policy for epoxyInspectionAuto: {}", modelTypeName);
                            }
                        } else {
                            if (StringUtils.isBlank(actualVal.toString())) {
                                logger.info(">>>>> both modelVal and actualVal are empty propertyName: {}", propertyName);
                            } else {
                                addToResult(null, actualVal, propertyName, inconsistentProperties, emptyInActual, emptyInStandard);
                            }
                        }
                    } else {
                        // 处理数字类型
                        if (!compareStringNumbers(modelVal, actualVal)) {
                            addToResult(modelVal, actualVal, propertyName, inconsistentProperties, emptyInActual, emptyInStandard);
                        }
                    }
                } else {
                    if (!Objects.equals(modelVal, actualVal)) {
                        addToResult(modelVal, actualVal, propertyName, inconsistentProperties, emptyInActual, emptyInStandard);
                    }
                }
                // 其他类型的处理可以在这里添加
            } catch (NoSuchFieldException e) {
                logger.error(">>>>> Field not found inspection actualVal: {}", propertyName);
            } catch (IllegalAccessException e) {
                logger.error(">>>>> Access denied for field: {}", propertyName);
            }
        }

        return new ImmutableTriple<>(inconsistentProperties, emptyInActual, emptyInStandard);
    }

    /**
     * 获取类及其所有父类中的字段
     *
     * @param clazz     类对象
     * @param fieldName 字段名称
     * @return 字段对象
     * @throws NoSuchFieldException 如果未找到字段
     */
    private static Field getFieldFromClassHierarchy(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();  // 继续查找父类
            }
        }
        throw new NoSuchFieldException("Field " + fieldName + " not found inspection class hierarchy.");
    }

    /**
     * 比较两个字符串数值是否相等
     *
     * @param modelVal  标准值
     * @param actualVal 实际值
     * @return 是否相等
     */
    private static boolean compareStringNumbers(Object modelVal, Object actualVal) {
        if (modelVal == null && actualVal == null) {
            return true;
        }
        if (modelVal == null || actualVal == null) {
            return false;
        }
        try {
            Double modelNum = Double.parseDouble(modelVal.toString());
            Double actualNum = Double.parseDouble(actualVal.toString());
            // 使用一个小的阈值进行比较
            return Math.abs(modelNum - actualNum) < 0.0001;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 将比较结果添加到结果集合中
     *
     * @param modelVal               标准值
     * @param actualVal              实际值
     * @param propertyName           属性名称
     * @param inconsistentProperties 不一致的属性集合
     * @param emptyInActual          实际对象为空的属性集合
     * @param emptyInStandard        标准对象为空的属性集合
     */
    private static void addToResult(Object modelVal, Object actualVal, String propertyName, Map<String, Map.Entry<Object, Object>> inconsistentProperties, Map<String, Object> emptyInActual, Map<String, Object> emptyInStandard) {
        if (modelVal == null) {
            emptyInStandard.put(propertyName, null);
        } else if (actualVal == null) {
            emptyInActual.put(propertyName, null);
        } else {
            // inconsistentProperties.put(propertyName, Maps.immutableEntry(modelVal, actualVal));
        }
    }

    // 原有的 compareObjectsWithRanges 方法保持不变
    private static boolean compareObjectsWithRanges(Object obj1, Object obj2, List<String> propertiesToCompare, Map<String, BoundsLoader> propertiesWithRanges) {
        if (obj1 == obj2) return true;
        if (obj1 == null || obj2 == null) return false;

        // 检查范围
        for (Map.Entry<String, BoundsLoader> entry : propertiesWithRanges.entrySet()) {
            try {
                Field field1 = getFieldFromClassHierarchy(obj1.getClass(), entry.getKey());
                Field field2 = getFieldFromClassHierarchy(obj2.getClass(), entry.getKey());

                field1.setAccessible(true);
                field2.setAccessible(true);

                Integer value1 = (Integer) field1.get(obj1);
                Integer value2 = (Integer) field2.get(obj2);

                if (value1 == null || entry.getValue().compareSingleValue(value1, BoundsLoader.Operator.GREATER_THAN_OR_EQUAL) ||
                        entry.getValue().compareSingleValue(value1, BoundsLoader.Operator.LESS_THAN_OR_EQUAL) ||
                        value2 == null ||
                        entry.getValue().compareSingleValue(value2, BoundsLoader.Operator.GREATER_THAN_OR_EQUAL) ||
                        entry.getValue().compareSingleValue(value2, BoundsLoader.Operator.LESS_THAN_OR_EQUAL)) {
                    return false;
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalArgumentException("Error accessing field: " + entry.getKey(), e);
            }
        }

        return true;
    }

    /**
     * 比较两个 JSON 字符串表示的 Map<Integer, Double>
     *
     * @param modelJson              标准 JSON 字符串
     * @param actualJson             实际 JSON 字符串
     * @param propertyName           属性名称
     * @param inconsistentProperties 不一致的属性集合
     * @param emptyInActual          实际对象为空的属性集合
     * @param emptyInStandard        标准对象为空的属性集合
     */
    private static void compareJsonMaps(String modelJson, String actualJson, String propertyName, Map<String, Map.Entry<Object, Object>> inconsistentProperties, Map<String, Object> emptyInActual, Map<String, Object> emptyInStandard) {
        try {
            // 读取 JSON 字符串并转换为 Map<String, Double>
            Map<String, Double> tempModelMap = objectMapper.readValue(modelJson, new TypeReference<Map<String, Double>>() {
            });
            Map<String, Double> tempActualMap = objectMapper.readValue(actualJson, new TypeReference<Map<String, Double>>() {
            });

            // 创建一个新的 Map<Integer, Double>
            Map<Integer, Double> modelMap = new HashMap<>();
            Map<Integer, Double> actualMap = new HashMap<>();

            // 将 tempMap 的键和值转换为数值类型并放入 intKeyMap
            for (Map.Entry<String, Double> entry : tempModelMap.entrySet()) {
                modelMap.put(Integer.parseInt(entry.getKey()), entry.getValue());
            }
            for (Map.Entry<String, Double> entry : tempActualMap.entrySet()) {
                actualMap.put(Integer.parseInt(entry.getKey()), entry.getValue());
            }

            // 比较两个 Map<Integer, Double> 中的每个键值对
            Set<Integer> allKeys = new HashSet<>(modelMap.keySet());
            allKeys.addAll(actualMap.keySet());

            for (Integer key : allKeys) {
                Double modelValue = modelMap.get(key);
                Double actualValue = actualMap.get(key);

                if (!Objects.equals(modelValue, actualValue)) {
                    addToResult(modelValue, actualValue, StringUtils.joinWith("_", propertyName, key), inconsistentProperties, emptyInActual, emptyInStandard);
                }
            }
        } catch (JsonProcessingException e) {
            logger.error("JSON解析失败, msg: {}, {}", modelJson, e.getMessage());
        } catch (NumberFormatException e) {
            logger.error("键转换为整数失败, msg: {}, {}", modelJson, e.getMessage());
        }
    }
}