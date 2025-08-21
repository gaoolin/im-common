package com.qtech.im.semiconductor.equipment.parameter;

import java.util.List;
/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/21 14:09:35
 * desc   :
 */

/**
 * 参数比较器接口
 * 定义了参数比较的基本契约
 */
public interface ParameterComparator<T> {

    /**
     * 比较两个对象的指定属性
     *
     * @param standardObj  标准对象
     * @param actualObj    实际对象
     * @param compareProps 需要比较的属性列表
     * @param computeProps 需要计算的属性列表
     * @return 比较结果
     */
    ComparisonResult compare(T standardObj, T actualObj,
                             List<String> compareProps, List<String> computeProps);

    /**
     * 获取支持的属性类型
     *
     * @return 支持的属性类型列表
     */
    List<String> getSupportedPropertyTypes();
}
