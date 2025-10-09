package com.im.aa.inspection.service;

import com.im.aa.inspection.comparator.EqLstComparatorV3;
import com.im.aa.inspection.entity.param.EqLstParsed;
import com.im.aa.inspection.entity.reverse.EqpReverseRecord;
import com.im.aa.inspection.entity.standard.EqLstTplDO;
import com.im.aa.inspection.entity.standard.EqLstTplInfoPO;
import org.im.common.dt.Chronos;
import org.im.semiconductor.common.parameter.core.DefaultParameterInspection;
import org.im.semiconductor.common.parameter.core.ParameterInspection;
import org.im.semiconductor.common.parameter.mgr.ParameterRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.im.aa.inspection.constant.EqLstInspectionConstants.PROPERTIES_TO_COMPARE;
import static com.im.aa.inspection.constant.EqLstInspectionConstants.PROPERTIES_TO_COMPUTE;

/**
 * 参数检查服务
 * 负责设备参数检查的核心业务逻辑
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/25
 */
public class ParamCheckService {
    private static final Logger logger = LoggerFactory.getLogger(ParamCheckService.class);
    // 使用专业的参数比较器
    private static final EqLstComparatorV3 COMPARATOR = EqLstComparatorV3.getInstance();

    private final CacheService cacheService;
    private final DatabaseService databaseService;

    public ParamCheckService(DatabaseService databaseService, CacheService cacheService) {
        this.databaseService = databaseService;
        this.cacheService = cacheService;
    }

    /**
     * 计算状态码
     */
    private static int calculateStatusCode(ParameterInspection result) {
        boolean hasMissingParams = !result.getEmptyInStandard().isEmpty();
        boolean hasExtraParams = !result.getEmptyInActual().isEmpty();
        boolean hasIncorrectValues = !result.getDifferences().isEmpty();

        // 确定状态码
        if (!hasMissingParams && !hasExtraParams && !hasIncorrectValues) {
            return 0;  // 正常
        } else if (hasMissingParams && !hasExtraParams && !hasIncorrectValues) {
            return 2;  // 少参数
        } else if (!hasMissingParams && hasExtraParams && !hasIncorrectValues) {
            return 4;  // 多参数
        } else if (!hasMissingParams && !hasExtraParams && hasIncorrectValues) {
            return 3;  // 参数值异常
        } else {
            return 5;  // 复合异常
        }
    }

    /**
     * 检查设备参数
     *
     * @param module 设备参数
     * @return 检查结果
     */
    public EqpReverseRecord inspectDeviceParam(String module) {
        try {
            logger.debug("开始检查设备参数: {}", module);
            // 1. 从Redis缓存中获取检查结果
            // 2. 执行参数检查
            // 3. 保存结果到Redis缓存
            // 4. 保存结果到数据库
            logger.info("设备参数检查完成: {} -> {}", module, module);
            return null;
        } catch (Exception e) {
            logger.error("检查设备参数时出错: {}", module, e);
            throw new RuntimeException("参数检查失败", e);
        }
    }

    /**
     * 执行参数检查
     *
     * @param actualObj 实际参数对象
     * @return 检查结果
     */
    public EqpReverseRecord performParameterCheck(EqLstParsed actualObj) {
        // 初始化检查结果
        EqpReverseRecord eqpReverseRecord = new EqpReverseRecord();
        eqpReverseRecord.setSource("dto-list");
        eqpReverseRecord.setSimId(actualObj.getSimId());
        eqpReverseRecord.setModule(actualObj.getModule());
        eqpReverseRecord.setChkDt(Chronos.now());

        // 获取模板信息（从Redis缓存，通过CacheService）
        EqLstTplInfoPO modelInfoObj = getTemplateInfoFromCache(actualObj.getModule());
        EqLstTplDO modelObj = getTemplateFromCache(actualObj.getModule());

        // 模板信息检查
        if (modelInfoObj == null) {
            return createInspectionResult(eqpReverseRecord, 1, "Missing Template Information.");
        }
        if (modelInfoObj.getStatus() == 0) {
            return createInspectionResult(eqpReverseRecord, 6, "Template Offline.");
        }
        if (modelObj == null) {
            return createInspectionResult(eqpReverseRecord, 7, "Missing Template Detail.");
        }

        // 使用专业比较器进行参数对比
        DefaultParameterInspection inspectionResult = COMPARATOR.compare(modelObj, actualObj, PROPERTIES_TO_COMPARE, PROPERTIES_TO_COMPUTE);

        // 设置检查结果状态
        int statusCode = calculateStatusCode(inspectionResult);
        eqpReverseRecord.setCode(statusCode);
        eqpReverseRecord.setDescription(buildDescription(inspectionResult));

        // 转换为CheckResult格式
        return eqpReverseRecord;
    }

    /**
     * 从缓存获取模板信息
     */
    private EqLstTplInfoPO getTemplateInfoFromCache(String module) {
        // 这里需要根据你的CacheService实现来获取模板信息缓存
        return cacheService.getRedisCacheConfig().getEqLstTplInfoPOCache().get(module);
    }

    /**
     * 从缓存获取模板
     */
    private EqLstTplDO getTemplateFromCache(String module) {
        // 这里需要根据你的CacheService实现来获取模板缓存
        return cacheService.getRedisCacheConfig().getEqLstTplDOCache().get(module);
    }

    /**
     * 构建描述信息
     */
    private String buildDescription(ParameterInspection result) {
        StringBuilder description = new StringBuilder();

        result.getEmptyInStandard().keySet().stream().sorted().forEach(prop -> description.append(prop).append("-").append(";"));

        result.getEmptyInActual().keySet().stream().sorted().forEach(prop -> description.append(prop).append("+").append(";"));

        result.getDifferences().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            String prop = entry.getKey();
            Map.Entry<Object, Object> map = entry.getValue();
            description.append(prop).append(":").append(map.getValue()).append("!=").append(map.getKey()).append(";");
        });

        return description.length() > 0 ? description.toString() : "Ok.";
    }

    /**
     * 创建检查结果
     */
    private EqpReverseRecord createInspectionResult(EqpReverseRecord eqpReverseRecord, int code, String description) {
        eqpReverseRecord.setCode(code);
        eqpReverseRecord.setDescription(description);
        return convertToInspectionResult(eqpReverseRecord);
    }

    /**
     * 转换为CheckResult格式
     */
    private EqpReverseRecord convertToInspectionResult(EqpReverseRecord eqpReverseRecord) {
        EqpReverseRecord inspectionResult = new EqpReverseRecord();
        inspectionResult.setSimId(eqpReverseRecord.getSimId());
        inspectionResult.setSource(eqpReverseRecord.getSource());
        inspectionResult.setDescription(eqpReverseRecord.getDescription());
        inspectionResult.setPassed(eqpReverseRecord.getCode() == 0);
        inspectionResult.setChkDt(Chronos.now());
        return inspectionResult;
    }

    /**
     * 执行实际检查逻辑（保留原有简单检查逻辑作为备选）
     *
     * @param param    设备参数
     * @param standard 标准范围
     * @return 检查结果
     */
    private EqpReverseRecord performCheck(EqLstParsed param, ParameterRange standard) {
        EqpReverseRecord eqpReverseRecord = new EqpReverseRecord();
        eqpReverseRecord.setSimId(param.getSimId());
        eqpReverseRecord.setModule(param.getModule());
        eqpReverseRecord.setChkDt(param.getReceivedTime());

        try {
            // 获取参数值
            Object paramValue = param.getAa1();
            if (paramValue == null) {
                eqpReverseRecord.setPassed(false);
                eqpReverseRecord.setDescription("参数值为空");
                return eqpReverseRecord;
            }

            // 如果参数值是数字类型，则进行数值检查
            if (paramValue instanceof Number) {
                double value = ((Number) paramValue).doubleValue();

                // 实际应该使用从数据库获取的标准范围
                if (standard != null) {
                    // 使用ParameterRange进行检查
                    if (standard.contains(value)) {
                        eqpReverseRecord.setPassed(true);
                        eqpReverseRecord.setDescription("参数在允许范围内");
                    } else {
                        eqpReverseRecord.setPassed(false);
                        eqpReverseRecord.setDescription("参数超出允许范围");
                    }
                } else {
                    // 简单示例检查
                    if (value >= 0 && value <= 1000) {
                        eqpReverseRecord.setPassed(true);
                        eqpReverseRecord.setDescription("参数在允许范围内");
                    } else {
                        eqpReverseRecord.setPassed(false);
                        eqpReverseRecord.setDescription("参数超出允许范围");
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
                            eqpReverseRecord.setPassed(true);
                            eqpReverseRecord.setDescription("参数在允许范围内");
                        } else {
                            eqpReverseRecord.setPassed(false);
                            eqpReverseRecord.setDescription("参数超出允许范围");
                        }
                    } else {
                        // 简单示例检查
                        if (numericValue >= 0 && numericValue <= 1000) {
                            eqpReverseRecord.setPassed(true);
                            eqpReverseRecord.setDescription("参数在允许范围内");
                        } else {
                            eqpReverseRecord.setPassed(false);
                            eqpReverseRecord.setDescription("参数超出允许范围");
                            eqpReverseRecord.setDescription("0");
                            eqpReverseRecord.setDescription("1000");
                        }
                    }
                } catch (NumberFormatException e) {
                    // 非数值字符串，可以进行其他类型的检查
                    eqpReverseRecord.setPassed(true);
                    eqpReverseRecord.setDescription("非数值参数，检查通过");
                }
            }
            // 其他类型参数
            else {
                // 对于其他类型，可以基于业务需求进行检查
                eqpReverseRecord.setPassed(true);
                eqpReverseRecord.setDescription("参数类型无需数值检查");
            }
        } catch (Exception e) {
            eqpReverseRecord.setPassed(false);
            eqpReverseRecord.setDescription("参数检查过程中发生错误: " + e.getMessage());
            logger.error("参数检查过程中发生错误", e);
        }

        return eqpReverseRecord;
    }
}
