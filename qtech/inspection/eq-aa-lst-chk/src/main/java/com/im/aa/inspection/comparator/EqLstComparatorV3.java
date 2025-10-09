package com.im.aa.inspection.comparator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.im.aa.inspection.entity.struct.BoundsLoader;
import org.apache.commons.lang3.StringUtils;
import org.im.semiconductor.common.parameter.comparator.AbstractParameterComparator;
import org.im.semiconductor.common.parameter.core.DefaultParameterInspection;
import org.im.semiconductor.common.parameter.core.ParameterInspection;

import java.util.*;

import static com.im.qtech.common.constant.QtechImBizConstant.EQP_LST_EPOXY_INSPECTION_AUTO_MAX;
import static com.im.qtech.common.constant.QtechImBizConstant.EQP_LST_EPOXY_INSPECTION_AUTO_MIN;
import static org.im.common.math.MathEvaluator.isNumeric;

/**
 * <p>
 * Aa参数比较器具体实现
 * 基于AbstractParameterComparator的特定实现
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/21 14:18:58
 */

public class EqLstComparatorV3 extends AbstractParameterComparator<Object> {

    // 单例实例
    private static volatile EqLstComparatorV3 instance;

    // 私有构造函数
    private EqLstComparatorV3() {
    }

    // 获取单例实例
    public static EqLstComparatorV3 getInstance() {
        if (instance == null) {
            synchronized (EqLstComparatorV3.class) {
                if (instance == null) {
                    instance = new EqLstComparatorV3();
                }
            }
        }
        return instance;
    }

    // 统一比较入口
    @Override
    public DefaultParameterInspection compare(Object standardObj, Object actualObj, List<String> compareProps, List<String> computeProps) {

        if ((compareProps == null || compareProps.isEmpty()) && (computeProps == null || computeProps.isEmpty())) {
            return new DefaultParameterInspection();
        }

        DefaultParameterInspection result = new DefaultParameterInspection();
        Set<String> allProps = mergeProps(compareProps, computeProps);

        for (String property : allProps) {
            FieldPair fields = getFieldPair(standardObj, actualObj, property);
            if (fields == null) {
                logger.warn(">>>>> Field not found: {}", property);
                continue;
            }

            Object modelVal = fields.getModelValue();
            Object actualVal = fields.getActualValue();

            if (isComputeProperty(property, computeProps)) {
                handleComputed(property, modelVal, actualVal, result);
            } else {
                compareValues(property, modelVal, actualVal, result);
            }
        }
        return result;
    }

    /* ================= 核心比较分发逻辑 ================= */

    private void handleComputed(String property, Object modelVal, Object actualVal, ParameterInspection result) {
        try {
            if (isAaItemProperty(property)) {
                compareAaItem(property, modelVal, actualVal, result);
            } else if (isMtfCheckProperty(property)) {
                compareMtfCheck(property, modelVal, actualVal, result);
            } else if (isEpoxyInspectionProperty(property)) {
                compareEpoxyInspection(property, modelVal, actualVal, result);
            } else {
                compareNumericOrString(property, modelVal, actualVal, result);
            }
        } catch (Exception e) {
            logger.error("Error comparing property {}: {}", property, e.getMessage());
            result.addDifference(property, modelVal, actualVal);
        }
    }

    private void compareValues(String property, Object modelVal, Object actualVal, ParameterInspection result) {
        if (!Objects.equals(modelVal, actualVal)) {
            result.addDifference(property, modelVal, actualVal);
        }
    }

    /* ================= 各种类型的特殊比较 ================= */

    private void compareAaItem(String property, Object modelVal, Object actualVal, ParameterInspection result) {
        if (areBothNull(modelVal, actualVal)) return;
        if (isNumeric(modelVal) && isNumeric(actualVal)) {
            BoundsLoader m = BoundsLoader.single(modelVal.toString());
            BoundsLoader a = BoundsLoader.single(actualVal.toString());
            if (m.compareSingleValue(a.getSingleValue(), BoundsLoader.Operator.GREATER_THAN)) {
                result.addDifference(property, modelVal, actualVal);
            }
        } else {
            compareValues(property, modelVal, actualVal, result);
        }
    }

    private void compareMtfCheck(String property, Object modelVal, Object actualVal, ParameterInspection result) {
        if (areBothNull(modelVal, actualVal)) return;

        try {
            Map<String, String> m = objectMapper.readValue(modelVal.toString(), new TypeReference<Map<String, String>>() {
            });
            Map<String, String> a = objectMapper.readValue(actualVal.toString(), new TypeReference<Map<String, String>>() {
            });

            if (!m.equals(a)) {
                compareMtfCheckMaps(property, m, a, result);
            }
        } catch (Exception e) {
            logger.warn("JSON parse error in {}, fallback to string compare", property);
            result.addDifference(property, modelVal, actualVal);
        }
    }

    private void compareMtfCheckMaps(String property, Map<String, String> m, Map<String, String> a, ParameterInspection result) {
        Set<String> keys = new HashSet<>(m.keySet());
        keys.addAll(a.keySet());
        for (String key : keys) {
            String mv = m.get(key);
            String av = a.get(key);
            if (areBothNull(mv, av)) continue;

            if (mv == null || av == null) {
                result.addDifference(property + "#" + key, mv, av);
                continue;
            }

            BoundsLoader mb = BoundsLoader.single(mv);
            BoundsLoader ab = BoundsLoader.single(av);
            if (mb.compareSingleValue(ab.getSingleValue(), BoundsLoader.Operator.GREATER_THAN)) {
                result.addDifference(property + "#" + key, mv, av);
            }
        }
    }

    private void compareEpoxyInspection(String property, Object modelVal, Object actualVal, ParameterInspection result) {
        if (areBothNull(modelVal, actualVal)) return;

        if (modelVal != null) {
            if (actualVal == null || StringUtils.isBlank(actualVal.toString())) {
                result.addDifference(property, modelVal, null);
                return;
            }
            BoundsLoader bounds = BoundsLoader.of(EQP_LST_EPOXY_INSPECTION_AUTO_MIN, EQP_LST_EPOXY_INSPECTION_AUTO_MAX);
            if (!bounds.contains(actualVal.toString())) {
                result.addDifference(property, "[ " + EQP_LST_EPOXY_INSPECTION_AUTO_MIN + " <= epoxyInspectionAuto <= " + EQP_LST_EPOXY_INSPECTION_AUTO_MAX + " ]", actualVal);
            }
        } else {
            result.addDifference(property, null, actualVal);
        }
    }

    private void compareNumericOrString(String property, Object modelVal, Object actualVal, ParameterInspection result) {
        if (isNumeric(modelVal) && isNumeric(actualVal)) {
            if (!numericEquals(modelVal, actualVal, 0.0001)) {
                result.addDifference(property, modelVal, actualVal);
            }
        } else {
            compareValues(property, modelVal, actualVal, result);
        }
    }

    /* ================= 工具方法 ================= */

    private boolean isComputeProperty(String property, List<String> computeProps) {
        return computeProps != null && computeProps.contains(property);
    }

    private boolean isAaItemProperty(String property) {
        return StringUtils.startsWith(property, "dto") && !(StringUtils.endsWith(property, "Min") || StringUtils.endsWith(property, "Max") || StringUtils.endsWith(property, "CcToCornerLimit"));
    }

    private boolean isMtfCheckProperty(String property) {
        return StringUtils.startsWith(property, "mtfCheck") && StringUtils.endsWith(property, "F");
    }

    private boolean isEpoxyInspectionProperty(String property) {
        return "epoxyInspectionInterval".equals(property);
    }

    private Set<String> mergeProps(List<String> p1, List<String> p2) {
        Set<String> merged = new HashSet<>();
        if (p1 != null) merged.addAll(p1);
        if (p2 != null) merged.addAll(p2);
        return merged;
    }
}
