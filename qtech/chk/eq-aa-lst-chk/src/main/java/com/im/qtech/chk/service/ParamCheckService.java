package com.im.qtech.chk.service;

import com.im.qtech.chk.model.CheckResult;
import com.im.qtech.chk.model.EqpContextInfoImpl;
import org.im.semiconductor.common.parameter.mgr.ParameterRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 参数检查服务
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/25
 */
public class ParamCheckService {
    private static final Logger logger = LoggerFactory.getLogger(ParamCheckService.class);

    private DatabaseService databaseService;
    private CacheService cacheService;

    public ParamCheckService(DatabaseService databaseService, CacheService cacheService) {
        this.databaseService = databaseService;
        this.cacheService = cacheService;
    }

    /**
     * 检查设备参数
     *
     * @param param 设备参数
     * @return 检查结果
     */
    public CheckResult checkEquipmentParam(EqpContextInfoImpl param) {
        try {
            logger.debug("开始检查设备参数: {}", param);

            // 1. 检查缓存中是否有结果
            CheckResult cachedResult = cacheService.getCheckResult(param);
            if (cachedResult != null) {
                logger.debug("从缓存中获取检查结果");
                return cachedResult;
            }

            // 2. 从数据库获取标准参数
            // 这里应该根据设备类型、参数名称等信息获取标准范围
            // ParameterRange standardRange = databaseService.getParamRange(
            //     param.getDeviceId(),
            //     param.getParameterName(),
            //     param.getProductionContext().getProduct(),
            //     param.getProductionContext().getSpec()
            // );

            // 3. 执行检查逻辑
            CheckResult result = performCheck(param, null); // 实际应传入standardRange

            // 4. 缓存结果
            cacheService.cacheCheckResult(result);

            // 5. 保存结果到数据库
            databaseService.saveCheckResult(result);

            logger.info("设备参数检查完成: {} -> {}", param.getDeviceId(), result.isPassed());
            return result;

        } catch (Exception e) {
            logger.error("检查设备参数时出错: {}", param, e);
            throw new RuntimeException("参数检查失败", e);
        }
    }

    /**
     * 执行实际检查逻辑
     *
     * @param param    设备参数
     * @param standard 标准范围
     * @return 检查结果
     */
    private CheckResult performCheck(EqpContextInfoImpl param, ParameterRange standard) {
        CheckResult result = new CheckResult();
        result.setEquipmentId(param.getDeviceId());
        result.setParamType(param.getParamGroup());
        result.setParamValue(param.getDeviceName() != null ? param.getParamGroup().toString() : null);
        result.setCheckTime(System.currentTimeMillis());

        try {
            // 获取参数值
            Object paramValue = param.getParamGroup();
            if (paramValue == null) {
                result.setPassed(false);
                result.setReason("参数值为空");
                return result;
            }

            // 如果参数值是数字类型，则进行数值检查
            if (paramValue instanceof Number) {
                double value = ((Number) paramValue).doubleValue();

                // 实际应该使用从数据库获取的标准范围
                if (standard != null) {
                    // 使用ParameterRange进行检查
                    if (standard.contains(value)) {
                        result.setPassed(true);
                        result.setReason("参数在允许范围内");
                    } else {
                        result.setPassed(false);
                        result.setReason("参数超出允许范围");
                        // 这里应该从ParameterRange获取标准值
                        // result.setStandardMin(String.valueOf(standard.getMin()));
                        // result.setStandardMax(String.valueOf(standard.getMax()));
                    }
                } else {
                    // 简单示例检查
                    if (value >= 0 && value <= 1000) {
                        result.setPassed(true);
                        result.setReason("参数在允许范围内");
                    } else {
                        result.setPassed(false);
                        result.setReason("参数超出允许范围");
                        result.setStandardMin("0");
                        result.setStandardMax("1000");
                    }
                }
            }
            // 如果参数值是字符串类型
            else if (paramValue instanceof String) {
                String value = (String) paramValue;

                // 检查是否为数值字符串
                try {
                    double numericValue = Double.parseDouble(value);

                    // 实际应该使用从数据库获取的标准范围
                    if (standard != null) {
                        // 使用ParameterRange进行检查
                        if (standard.contains(numericValue)) {
                            result.setPassed(true);
                            result.setReason("参数在允许范围内");
                        } else {
                            result.setPassed(false);
                            result.setReason("参数超出允许范围");
                        }
                    } else {
                        // 简单示例检查
                        if (numericValue >= 0 && numericValue <= 1000) {
                            result.setPassed(true);
                            result.setReason("参数在允许范围内");
                        } else {
                            result.setPassed(false);
                            result.setReason("参数超出允许范围");
                            result.setStandardMin("0");
                            result.setStandardMax("1000");
                        }
                    }
                } catch (NumberFormatException e) {
                    // 非数值字符串，可以进行其他类型的检查
                    result.setPassed(true);
                    result.setReason("非数值参数，检查通过");
                }
            }
            // 其他类型参数
            else {
                // 对于其他类型，可以基于业务需求进行检查
                result.setPassed(true);
                result.setReason("参数类型无需数值检查");
            }
        } catch (Exception e) {
            result.setPassed(false);
            result.setReason("参数检查过程中发生错误: " + e.getMessage());
            logger.error("参数检查过程中发生错误", e);
        }

        return result;
    }
}
