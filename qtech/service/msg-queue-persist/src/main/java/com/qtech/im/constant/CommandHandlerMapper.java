package com.qtech.im.constant;

import java.util.*;

/**
 * 列表项多键映射常量类
 * <p>
 * 功能说明：
 * 1. 提供多键到单值的映射关系
 * 2. 将机台List命令映射为处理器前缀，以便使用Handler处理器解析
 * 3. 支持通过命令关键字快速查找对应的处理器类型
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since : 2024/05/15
 */
public class CommandHandlerMapper {

    /**
     * 需要解析的List要不断地加入其中
     * 需要调整 EqLstChk 类 CONTROL_LIST_SET
     */
    public static final Map<String, Set<String>> KEY_MAP;
    // 单例实例
    private static volatile CommandHandlerMapper instance;

    // 静态映射关系定义 - 把要【解析】的机台List命令，映射为处理器的前缀

    // 静态初始化块 - 构建不可变的键映射表
    static {
        Map<String, Set<String>> keyMap = new HashMap<>();

        // AA相关命令映射
        keyMap.put("Aa", new HashSet<>(Arrays.asList("AA1", "AA2", "AA3")));

        // MTF检查相关命令映射
        keyMap.put("MtfCheck", new HashSet<>(Arrays.asList("MTF_Check", "MTF_Check1", "MTF_Check2", "MTF_Check3")));

        // MTF离轴检查相关命令映射
        keyMap.put("MtfOffAxisCheck", new HashSet<>(Arrays.asList("MTFOffAxisCheck1", "MTFOffAxisCheck2", "MTFOffAxisCheck3", "MTFOffAxisCheck4")));  // 2025-03-11

        // 图表对齐相关命令映射
        keyMap.put("ChartAlignment", new HashSet<>(Arrays.asList("ChartAlignment", "ChartAlignment1", "ChartAlignment2")));

        // 环氧树脂检测自动相关命令映射
        keyMap.put("EpoxyInspectionAuto", new HashSet<>(Collections.singletonList("EpoxyInspection_Auto")));

        // VCM霍尔传感器相关命令映射
        keyMap.put("VcmHall", new HashSet<>(Arrays.asList("VCM_Hall", "VCM_Hall1", "VCM_Hall2")));

        // 位置记录相关命令映射
        keyMap.put("RecordPosition", new HashSet<>(Collections.singletonList("RecordPosition")));

        // MTF保存相关命令映射
        keyMap.put("SaveMtf", new HashSet<>(Collections.singletonList("Save_MTF")));

        // 创建不可变映射表
        KEY_MAP = Collections.unmodifiableMap(keyMap);
    }

    // 存储键值映射关系
    private final Map<String, String> valueMap = new HashMap<>();

    /**
     * 私有构造函数 - 防止外部直接实例化
     * 在构造时初始化键值映射关系
     */
    private CommandHandlerMapper() {
        // 根据预定义的KEY_MAP构建valueMap
        for (Map.Entry<String, Set<String>> entry : KEY_MAP.entrySet()) {
            addMapping(entry.getValue(), entry.getKey());
        }
    }

    /**
     * 获取单例实例
     *
     * @return ListItemMultiKeyMapConstants单例实例
     */
    public static CommandHandlerMapper getInstance() {
        // 双重检查锁定实现线程安全的单例
        if (instance == null) {
            synchronized (CommandHandlerMapper.class) {
                if (instance == null) {
                    instance = new CommandHandlerMapper();
                }
            }
        }
        return instance;
    }

    /**
     * 添加键值映射关系
     * 将多个键映射到同一个值
     *
     * @param keys  键集合
     * @param value 值
     */
    private void addMapping(Set<String> keys, String value) {
        for (String key : keys) {
            valueMap.put(key, value);
        }
    }

    /**
     * 根据键获取对应的值
     *
     * @param key 查找的键
     * @return 对应的值，如果未找到则返回null
     */
    public String get(String key) {
        return valueMap.get(key);
    }

    /**
     * 检查是否包含指定的键
     *
     * @param key 要检查的键
     * @return 如果包含该键返回true，否则返回false
     */
    public boolean containsKey(String key) {
        return valueMap.containsKey(key);
    }

    /**
     * 检查是否包含指定的值
     *
     * @param value 要检查的值
     * @return 如果包含该值返回true，否则返回false
     */
    public boolean containsValue(String value) {
        return valueMap.containsValue(value);
    }

    /**
     * 获取所有键的集合
     *
     * @return 所有键的集合
     */
    public Set<String> keySet() {
        return new HashSet<>(valueMap.keySet());
    }

    /**
     * 获取所有值的集合
     *
     * @return 所有值的集合
     */
    public Collection<String> values() {
        return new HashSet<>(valueMap.values());
    }

    /**
     * 获取键值对的数量
     *
     * @return 映射表的大小
     */
    public int size() {
        return valueMap.size();
    }

    /**
     * 检查映射表是否为空
     *
     * @return 如果映射表为空返回true，否则返回false
     */
    public boolean isEmpty() {
        return valueMap.isEmpty();
    }

    /**
     * 根据值获取所有对应的键
     *
     * @param value 查找的值
     * @return 对应的所有键的集合
     */
    public Set<String> getKeysByValue(String value) {
        Set<String> keys = new HashSet<>();
        for (Map.Entry<String, String> entry : valueMap.entrySet()) {
            if (Objects.equals(entry.getValue(), value)) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    /**
     * 获取原始的键映射表(KEY_MAP)
     *
     * @return 不可变的键映射表
     */
    public Map<String, Set<String>> getOriginalKeyMap() {
        return KEY_MAP;
    }

    /**
     * 重新加载映射关系（用于测试或动态更新）
     */
    public synchronized void reload() {
        valueMap.clear();
        for (Map.Entry<String, Set<String>> entry : KEY_MAP.entrySet()) {
            addMapping(entry.getValue(), entry.getKey());
        }
    }

    @Override
    public String toString() {
        return "CommandHandlerMapper{" + "valueMap=" + valueMap + ", size=" + valueMap.size() + '}';
    }
}
