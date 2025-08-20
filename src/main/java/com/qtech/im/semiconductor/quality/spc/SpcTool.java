package com.qtech.im.semiconductor.quality.spc;

/**
 * 统计过程控制工具类
 *
 * 解决问题:
 * - 质量数据统计分析复杂
 * - 质量异常检测不及时
 * - 质量趋势预测困难
 * - 质量改进措施不明确
 */
public class SpcTool {
    // SPC控制图生成
    public static ControlChart generateControlChart(List<Measurement> data, ChartType type);

    // CPK计算
    public static ProcessCapability calculateCPK(List<Measurement> data, SpecificationLimits limits);

    // 质量异常实时检测
    public static List<QualityAlert> detectRealTimeAnomalies(Stream<Measurement> dataStream);

    // 质量趋势预测
    public static QualityTrend predictQualityTrend(HistoricalData historicalData);
}
