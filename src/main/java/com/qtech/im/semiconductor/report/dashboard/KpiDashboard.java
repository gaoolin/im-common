// 路径: com.qtech.im.semiconductor.dashboard.KpiDashboard
/**
 * KPI仪表板工具类
 *
 * 解决问题:
 * - KPI指标分散难查看
 * - KPI数据实时性差
 * - KPI异常预警不及时
 * - KPI趋势分析困难
 */
public class KpiDashboard {
    // KPI指标计算
    public static KpiValue calculateKpi(KpiDefinition kpiDef, TimeRange range);

    // 实时KPI监控
    public static KpiAlert monitorKpiRealTime(String kpiId, ThresholdConfig thresholds);

    // KPI趋势分析
    public static KpiTrend analyzeKpiTrend(String kpiId, TimeRange range);

    // KPI仪表板生成
    public static Dashboard generateDashboard(DashboardConfig config);
}
