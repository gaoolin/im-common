package com.im.aa.inspection.entity.param;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.im.aa.inspection.entity.struct.BoundsLoader;
import com.im.aa.inspection.entity.struct.EqLstCommand;
import com.im.aa.inspection.utils.ToCamelCaseConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AA List参数基础实体类
 * <p>
 * 该类作为设备参数点检的基础实体，具有以下特点：
 * - 可扩展性：支持通过继承扩展新的参数字段
 * - 通用性：适用于多种设备参数管理场景
 * - 易维护性：提供统一的参数重置和填充机制
 * - 兼容性：保持与现有代码的兼容性
 *
 * @author gaozhilin
 * @since 2024/11/27
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EqLstSet implements Serializable, Cloneable {
    private static final long serialVersionUID = 529L;
    private static final Logger logger = LoggerFactory.getLogger(EqLstSet.class);

    // 基础设备参数
    private String prodType;
    private String aa1;
    private String aa2;
    private String aa3;
    private String backToPosition;
    private String blemish;

    // 2025-01-15 扩展参数
    private String blemish1;
    private String blemish2;
    private String chartAlignment;
    private String chartAlignment1;
    private String chartAlignment2;
    private String clampOnOff;
    private String delay;
    private String destroy;
    private String destroyStart;
    private String dispense;

    // 2025-01-15 扩展参数
    private String dispense1;
    private String dispense2;
    private String epoxyInspection;
    private String epoxyInspectionAuto;
    private String grab;

    // 2025-01-15 扩展参数
    private String grab1;
    private String grab2;

    // 2025-03-11 扩展参数
    private String grab3;
    private String gripperOpen;
    private String init;

    // 2025-01-15 扩展参数
    private String init1;
    private String init2;
    private String init3;
    private String lpBlemish;
    private String lpOc;
    private String lpOcCheck;
    private String lpOn;

    // 2025-03-11 扩展参数
    private String lpOn1;
    private String lpOnBlemish;
    private String lpOff;

    // 2025-03-11 扩展参数
    private String lpOff1;

    // 2025-01-16 lpOff0对于mybatis驼峰转换不友好，更改以下指令
    private String lpIntensity;
    private String moveToBlemishPos;
    private String mtfCheck;
    private String mtfCheck1;
    private String mtfCheck2;
    private String mtfCheck3;

    // 2025-03-11 多焦段MTF检查
    private String mtfOffAxisCheck1;
    private String mtfOffAxisCheck2;
    private String mtfOffAxisCheck3;
    private String mtfOffAxisCheck4;
    private String openCheck;

    // 2025-01-15 扩展参数
    private String openCheck1;
    private String openCheck2;
    private String openCheck3;

    // 2025-01-15 新增参数
    private String prToBond;

    // 台虹厂区的utXyzMove和recordPosition分开管控，因此新增 2025-01-15
    private String utXyzMove;
    private String recordPosition;
    private String reInit;
    private String saveOc;
    private String saveMtf;
    private String sensorReset;
    private String sfrCheck; // 2025-04-17

    private String sid;
    private String uvon;
    private String uvoff;

    // vcmHall 指标 2025-03-27
    private String vcmHall;
    private String vcmHall1;
    private String vcmHall2;

    @JsonProperty("yLevel")
    private String yLevel;

    // AA Item 指标
    private String aa1RoiCc;
    private String aa1RoiUl;
    private String aa1RoiUr;
    private String aa1RoiLl;
    private String aa1RoiLr;

    // 新增 2024-10-28
    private String aa1Target;
    private String aa1CcToCornerLimit;
    private String aa1CcToCornerLimitMin;
    private String aa1CornerScoreDifferenceRejectValue;
    private String aa1ZRef;
    private String aa1SrchStep;
    private String aa1GoldenGlueThicknessMin;
    private String aa1GoldenGlueThicknessMax;

    private String aa2RoiCc;
    private String aa2RoiUl;
    private String aa2RoiUr;
    private String aa2RoiLl;
    private String aa2RoiLr;

    // 新增 2024-10-28
    private String aa2Target;
    private String aa2CcToCornerLimit;
    private String aa2CcToCornerLimitMin;
    private String aa2CornerScoreDifferenceRejectValue;
    private String aa2ZRef;
    private String aa2SrchStep;
    private String aa2GoldenGlueThicknessMin;
    private String aa2GoldenGlueThicknessMax;

    private String aa3RoiCc;
    private String aa3RoiUl;
    private String aa3RoiUr;
    private String aa3RoiLl;
    private String aa3RoiLr;

    // 新增 2024-10-28
    private String aa3Target;
    private String aa3CcToCornerLimit;
    private String aa3CcToCornerLimitMin;
    private String aa3CornerScoreDifferenceRejectValue;
    private String aa3ZRef;
    private String aa3SrchStep;
    private String aa3GoldenGlueThicknessMin;
    private String aa3GoldenGlueThicknessMax;

    // mtfCheck Item 指标
    private String mtfCheckF;
    private String mtfCheck1F;
    private String mtfCheck2F;
    private String mtfCheck3F;

    // 2025-03-11 多焦段 mtfCheck
    private String mtfOffAxisCheck1F;
    private String mtfOffAxisCheck2F;
    private String mtfOffAxisCheck3F;
    private String mtfOffAxisCheck4F;

    // chartAlignment Item 指标
    private String chartAlignmentXResMin;
    private String chartAlignmentXResMax;
    private String chartAlignmentYResMin;
    private String chartAlignmentYResMax;

    private String chartAlignment1XResMin;
    private String chartAlignment1XResMax;
    private String chartAlignment1YResMin;
    private String chartAlignment1YResMax;

    private String chartAlignment2XResMin;
    private String chartAlignment2XResMax;
    private String chartAlignment2YResMin;
    private String chartAlignment2YResMax;

    // EpoxyInspection Auto Item 指标
    private String epoxyInspectionInterval;

    // vcmHall 指标 2025-03-27
    private String vcmHallHallAfMin;
    private String vcmHallHallAfMax;
    private String vcmHallHallXMin;
    private String vcmHallHallXMax;
    private String vcmHallHallYMin;
    private String vcmHallHallYMax;

    private String vcmHall1HallAfMin;
    private String vcmHall1HallAfMax;
    private String vcmHall1HallXMin;
    private String vcmHall1HallXMax;
    private String vcmHall1HallYMin;
    private String vcmHall1HallYMax;

    private String vcmHall2HallAfMin;
    private String vcmHall2HallAfMax;
    private String vcmHall2HallXMin;
    private String vcmHall2HallXMax;
    private String vcmHall2HallYMin;
    private String vcmHall2HallYMax;

    // RecordPosition 指标
    // 由于新增utXyzMove List命令，因此新增utXyzMoveVal作为参数管控值 2025-01-15
    private String utXyzMoveVal;
    // 由于新增utXyzMove List命令，因此新增recordPositionName作为参数管控值，原recordPositionXyzMove 被替换掉 2025-01-15
    private String recordPositionName;

    // OcCheck 指标 Save Oc
    private String saveOcXOffsetMin;
    private String saveOcXOffsetMax;
    private String saveOcYOffsetMin;
    private String saveOcYOffsetMax;

    // SaveMtf 指标
    private String saveMtfCcMin;
    private String saveMtfCcMax;

    /**
     * 重置所有字符串类型参数为null
     * 便于重新初始化参数值
     */
    public void reset() {
        resetFields(this.getClass());
    }

    /**
     * 递归重置字段值
     *
     * @param clazz 当前处理的类
     */
    private void resetFields(Class<?> clazz) {
        if (clazz == null || Object.class.equals(clazz)) {
            return;
        }

        Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> String.class.equals(field.getType()))
                .forEach(field -> {
                    field.setAccessible(true);
                    try {
                        field.set(this, null);
                    } catch (IllegalAccessException e) {
                        logger.warn("Failed to reset field: {}", field.getName(), e);
                    }
                });

        resetFields(clazz.getSuperclass()); // 递归处理父类
    }

    /**
     * 从命令列表填充参数数据
     *
     * @param eqLstCommands 命令列表
     */
    public void fillWithData(List<EqLstCommand> eqLstCommands) {
        if (eqLstCommands == null || eqLstCommands.isEmpty()) {
            logger.warn("AA entity commands is empty, no data to fill");
            return;
        }

        List<Map<String, String>> camelCaseData = eqLstCommands.stream()
                .filter(Objects::nonNull)
                .map(this::convertCommandToMap)
                .filter(map -> !map.isEmpty())
                .collect(Collectors.toList());

        try {
            setFieldsFromData(this, camelCaseData);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to set properties due to reflection error", e);
        }
    }

    /**
     * 将单个命令转换为Map
     *
     * @param cmd 命令对象
     * @return 转换后的Map
     */
    private Map<String, String> convertCommandToMap(EqLstCommand cmd) {
        Map<String, String> map = new HashMap<>();

        // 获取命令路径作为集成键
        String integration = cmd.getCommandPath();
        BoundsLoader boundsLoader = cmd.getBoundsLoader();

        if (integration != null && boundsLoader != null) {
            String key = ToCamelCaseConverter.doConvert(integration);
            if (boundsLoader.hasSingleValue()) {
                map.put(key, boundsLoader.getSingleValue());
            } else if (boundsLoader.getMin() != null && boundsLoader.getMax() != null) {
                map.put(key + "Min", String.valueOf(boundsLoader.getMin()));
                map.put(key + "Max", String.valueOf(boundsLoader.getMax()));
            }
        }

        return map;
    }

    /**
     * 从数据列表设置字段值
     *
     * @param target 目标对象
     * @param data   数据列表
     * @throws IllegalAccessException 访问异常
     */
    public void setFieldsFromData(Object target, List<Map<String, String>> data) throws IllegalAccessException {
        if (target == null || data == null || data.isEmpty()) {
            return;
        }

        Class<?> clazz = target.getClass();
        while (clazz != null && !Object.class.equals(clazz)) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!String.class.equals(field.getType())) {
                    continue;
                }

                String camelCaseKey = field.getName();
                for (Map<String, String> map : data) {
                    if (map.containsKey(camelCaseKey)) {
                        field.setAccessible(true);
                        field.set(target, map.get(camelCaseKey));
                        break;
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    /**
     * 获取所有非空参数的映射
     *
     * @return 参数映射
     */
    public Map<String, String> toParameterMap() {
        Map<String, String> paramMap = new HashMap<>();
        Class<?> clazz = this.getClass();

        while (clazz != null && !Object.class.equals(clazz)) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!String.class.equals(field.getType())) {
                    continue;
                }

                field.setAccessible(true);
                try {
                    String value = (String) field.get(this);
                    if (value != null) {
                        paramMap.put(field.getName(), value);
                    }
                } catch (IllegalAccessException e) {
                    logger.warn("Failed to get field value: {}", field.getName(), e);
                }
            }
            clazz = clazz.getSuperclass();
        }

        return paramMap;
    }

    /**
     * 深度克隆参数对象
     *
     * @return 克隆的对象
     */
    @Override
    public EqLstSet clone() {
        try {
            return (EqLstSet) super.clone();
        } catch (CloneNotSupportedException e) {
            // 使用序列化方式进行深拷贝
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(this);

                ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(bis);
                return (EqLstSet) ois.readObject();
            } catch (Exception ex) {
                throw new RuntimeException("Failed to clone EqLstSet", ex);
            }
        }
    }
}
