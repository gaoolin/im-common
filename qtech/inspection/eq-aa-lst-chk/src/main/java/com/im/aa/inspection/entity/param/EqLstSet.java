package com.im.aa.inspection.entity.param;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.im.aa.inspection.entity.struct.BoundsLoader;
import com.im.aa.inspection.entity.struct.EqLstCommand;
import com.im.aa.inspection.util.ToCamelCaseConverter;
import com.im.qtech.common.dto.param.EqLstPOJO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
public class EqLstSet extends EqLstPOJO implements Cloneable {
    private static final long serialVersionUID = 2L;
    private static final Logger logger = LoggerFactory.getLogger(EqLstSet.class);

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
                        logger.warn(">>>>> Failed to reset field: {}", field.getName(), e);
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
            logger.warn(">>>>> AA entity commands is empty, no data to fill");
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
                    logger.warn(">>>>> Failed to get field value: {}", field.getName(), e);
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
                throw new RuntimeException("Failed to clone EqLstPOJO", ex);
            }
        }
    }
}
