package com.im.aa.inspection.service;

import com.im.aa.inspection.comparator.EqLstComparatorV3;
import com.im.aa.inspection.context.CheckResult;
import com.im.aa.inspection.entity.param.EqLstParsed;
import com.im.aa.inspection.entity.reverse.EqReverseCtrlInfo;
import com.im.aa.inspection.entity.tpl.QtechEqLstTpl;
import com.im.aa.inspection.entity.tpl.QtechEqLstTplInfo;
import com.im.aa.inspection.utils.CacheUtil;
import org.im.semiconductor.common.parameter.comparator.ParameterInspection;
import org.im.semiconductor.common.parameter.mgr.ParameterRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.im.aa.inspection.constants.EqLstChk.PROPERTIES_TO_COMPARE;
import static com.im.aa.inspection.constants.EqLstChk.PROPERTIES_TO_COMPUTE;

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
    private final DatabaseService databaseService;
    private final CacheUtil cacheUtil;

    public ParamCheckService(DatabaseService databaseService, CacheUtil cacheUtil) {
        this.databaseService = databaseService;
        this.cacheUtil = cacheUtil;
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
     * @param param 设备参数
     * @return 检查结果
     */
    public CheckResult checkEquipmentParam(EqLstParsed param) {
        try {
            logger.debug("开始检查设备参数: {}", param);

            // 1. 从Redis缓存中获取检查结果
            CheckResult cachedResult = getCheckResultFromRedis(param);
            if (cachedResult != null) {
                logger.debug("从Redis缓存中获取检查结果");
                return cachedResult;
            }

            // 2. 执行参数检查
            CheckResult result = performParameterCheck(param);

            // 3. 保存结果到Redis缓存
            cacheCheckResultToRedis(result);

            // 4. 保存结果到数据库
            databaseService.saveCheckResult(result);

            logger.info("设备参数检查完成: {} -> {}", param.getSimId(), result.isPassed());
            return result;

        } catch (Exception e) {
            logger.error("检查设备参数时出错: {}", param, e);
            throw new RuntimeException("参数检查失败", e);
        }
    }

    /**
     * 从Redis缓存获取检查结果
     */
    private CheckResult getCheckResultFromRedis(EqLstParsed param) {
        // 这里可以根据需要实现Redis缓存的检查结果获取逻辑
        // 暂时返回null表示不使用缓存或缓存中无数据
        return null;
    }

    /**
     * 将检查结果保存到Redis缓存
     */
    private void cacheCheckResultToRedis(CheckResult result) {
        // 这里可以根据需要实现Redis缓存的检查结果保存逻辑
    }

    /**
     * 执行参数检查
     *
     * @param actualObj 实际参数对象
     * @return 检查结果
     */
    private CheckResult performParameterCheck(EqLstParsed actualObj) {
        // 初始化检查结果
        EqReverseCtrlInfo checkResult = new EqReverseCtrlInfo();
        checkResult.setSource("aa-list");
        checkResult.setSimId(actualObj.getSimId());
        checkResult.setProdType(actualObj.getProdType());
        checkResult.setChkDt(java.time.LocalDateTime.now());

        // 获取模板信息（从Redis缓存）
        QtechEqLstTplInfo modelInfoObj = cacheUtil.getParamsTplInfo(actualObj.getProdType());
        QtechEqLstTpl modelObj = cacheUtil.getParamsTpl(actualObj.getProdType());

        // 模板信息检查
        if (modelInfoObj == null) {
            return createCheckResult(checkResult, 1, "Missing Template Information.");
        }
        if (modelInfoObj.getStatus() == 0) {
            return createCheckResult(checkResult, 6, "Template Offline.");
        }
        if (modelObj == null) {
            return createCheckResult(checkResult, 7, "Missing Template Detail.");
        }

        // 使用专业比较器进行参数对比
        ParameterInspection inspectionResult = COMPARATOR.compare(
                modelObj, actualObj, PROPERTIES_TO_COMPARE, PROPERTIES_TO_COMPUTE);

        // 设置检查结果状态
        int statusCode = calculateStatusCode(inspectionResult);
        checkResult.setCode(statusCode);
        checkResult.setDescription(buildDescription(inspectionResult));

        // 转换为CheckResult格式
        return convertToCheckResult(checkResult);
    }

    /**
     * 构建描述信息
     */
    private String buildDescription(ParameterInspection result) {
        StringBuilder description = new StringBuilder();

        result.getEmptyInStandard().keySet().stream().sorted().forEach(prop ->
                description.append(prop).append("-").append(";"));

        result.getEmptyInActual().keySet().stream().sorted().forEach(prop ->
                description.append(prop).append("+").append(";"));

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
    private CheckResult createCheckResult(EqReverseCtrlInfo checkResult, int code, String description) {
        checkResult.setCode(code);
        checkResult.setDescription(description);
        return convertToCheckResult(checkResult);
    }

    /**
     * 转换为CheckResult格式
     */
    private CheckResult convertToCheckResult(EqReverseCtrlInfo eqReverseCtrlInfo) {
        CheckResult checkResult = new CheckResult();
        checkResult.setEquipmentId(eqReverseCtrlInfo.getSimId());
        checkResult.setParamType(eqReverseCtrlInfo.getSource());
        checkResult.setReason(eqReverseCtrlInfo.getDescription());
        checkResult.setPassed(eqReverseCtrlInfo.getCode() == 0);
        checkResult.setCheckTime(System.currentTimeMillis());
        return checkResult;
    }

    /**
     * 执行实际检查逻辑（保留原有简单检查逻辑作为备选）
     *
     * @param param    设备参数
     * @param standard 标准范围
     * @return 检查结果
     */
    private CheckResult performCheck(EqLstParsed param, ParameterRange standard) {
        CheckResult result = new CheckResult();
        result.setEquipmentId(param.getSimId());
        result.setParamType(param.getProdType());
        result.setParamValue(param.getAa1() != null ? param.getAa2() : null);
        result.setCheckTime(param.getReceivedTime().getTime());

        try {
            // 获取参数值
            Object paramValue = param.getAa1();
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
