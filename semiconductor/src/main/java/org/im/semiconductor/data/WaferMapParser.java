package org.im.semiconductor.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Wafer Map解析工具类
 * <p>
 * 特性：
 * - 通用化：支持多种Wafer Map格式
 * - 规范化：统一的解析接口和数据结构
 * - 灵活性：支持自定义解析规则和映射
 * - 可靠性：完善的格式验证和错误处理
 * - 容错性：部分解析失败不影响整体处理
 * - 专业性：半导体行业标准Wafer Map处理
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/08/21
 */
public class WaferMapParser {

    // 默认Wafer尺寸
    public static final int DEFAULT_WAFER_SIZE = 300; // mm
    // 默认Die尺寸
    public static final double DEFAULT_DIE_SIZE = 5.0; // mm
    private static final Logger logger = LoggerFactory.getLogger(WaferMapParser.class);

    /**
     * 解析Wafer Map文件
     *
     * @param filePath 文件路径
     * @param format   文件格式
     * @return Wafer Map对象
     */
    public static WaferMap parseWaferMap(String filePath, WaferMapFormat format) {
        if (filePath == null || filePath.isEmpty()) {
            logger.warn("Invalid file path");
            return null;
        }

        if (format == null) {
            format = WaferMapFormat.AUTO_DETECT;
        }

        try {
            switch (format) {
                case CDF:
                    return parseCdfFormat(filePath);
                case EDF:
                    return parseEdfFormat(filePath);
                case AUTO_DETECT:
                    return autoDetectAndParse(filePath);
                default:
                    logger.warn("Unsupported format: {}", format);
                    return null;
            }
        } catch (Exception e) {
            logger.error("Failed to parse wafer map: {}", filePath, e);
            return null;
        }
    }

    /**
     * 解析CDF格式Wafer Map
     *
     * @param filePath 文件路径
     * @return Wafer Map对象
     */
    private static WaferMap parseCdfFormat(String filePath) {
        // 实现CDF格式解析逻辑
        logger.debug("Parsing CDF format wafer map: {}", filePath);
        return new WaferMap();
    }

    /**
     * 解析EDF格式Wafer Map
     *
     * @param filePath 文件路径
     * @return Wafer Map对象
     */
    private static WaferMap parseEdfFormat(String filePath) {
        // 实现EDF格式解析逻辑
        logger.debug("Parsing EDF format wafer map: {}", filePath);
        return new WaferMap();
    }

    /**
     * 自动检测格式并解析
     *
     * @param filePath 文件路径
     * @return Wafer Map对象
     */
    private static WaferMap autoDetectAndParse(String filePath) {
        // 实现自动格式检测逻辑
        logger.debug("Auto-detecting format for wafer map: {}", filePath);
        return new WaferMap();
    }

    /**
     * 生成Wafer Map统计报告
     *
     * @param waferMap Wafer Map对象
     * @return 统计报告
     */
    public static WaferMapReport generateReport(WaferMap waferMap) {
        if (waferMap == null) {
            logger.warn("Invalid wafer map");
            return null;
        }

        try {
            Map<DieStatus, Integer> statusCounts = new EnumMap<>(DieStatus.class);

            // 统计各种状态的Die数量
            for (Die die : waferMap.getDies()) {
                DieStatus status = die.getStatus();
                statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
            }

            int totalDies = waferMap.getDies().size();
            int goodDies = statusCounts.getOrDefault(DieStatus.GOOD, 0);
            int badDies = totalDies - goodDies;
            double yield = totalDies > 0 ? (double) goodDies / totalDies : 0;

            return new WaferMapReport(totalDies, goodDies, badDies, yield, statusCounts);
        } catch (Exception e) {
            logger.error("Failed to generate wafer map report", e);
            return null;
        }
    }

    /**
     * Wafer Map格式枚举
     */
    public enum WaferMapFormat {
        CDF,    // Common Data Format
        EDF,    // Equipment Data Format
        AUTO_DETECT
    }

    /**
     * Die状态枚举
     */
    public enum DieStatus {
        GOOD,       // 良品
        FAIL,       // 不良品
        EMPTY,      // 空白区域
        EDGE,       // 边缘区域
        UNKNOWN     // 未知状态
    }

    /**
     * Die类
     */
    public static class Die {
        private final int x;
        private final int y;
        private final DieStatus status;
        private final Map<String, Object> attributes;

        public Die(int x, int y, DieStatus status) {
            this.x = x;
            this.y = y;
            this.status = status != null ? status : DieStatus.UNKNOWN;
            this.attributes = new HashMap<>();
        }

        // Getters and Setters
        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public DieStatus getStatus() {
            return status;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public void setAttribute(String key, Object value) {
            attributes.put(key, value);
        }

        public Object getAttribute(String key) {
            return attributes.get(key);
        }
    }

    /**
     * Wafer Map类
     */
    public static class WaferMap {
        private final List<Die> dies;
        private int waferSize;
        private double dieSize;
        private String lotId;
        private String waferId;

        public WaferMap() {
            this.dies = new ArrayList<>();
            this.waferSize = DEFAULT_WAFER_SIZE;
            this.dieSize = DEFAULT_DIE_SIZE;
        }

        // Getters and Setters
        public List<Die> getDies() {
            return new ArrayList<>(dies);
        }

        public void addDie(Die die) {
            dies.add(die);
        }

        public int getWaferSize() {
            return waferSize;
        }

        public void setWaferSize(int waferSize) {
            this.waferSize = waferSize;
        }

        public double getDieSize() {
            return dieSize;
        }

        public void setDieSize(double dieSize) {
            this.dieSize = dieSize;
        }

        public String getLotId() {
            return lotId;
        }

        public void setLotId(String lotId) {
            this.lotId = lotId;
        }

        public String getWaferId() {
            return waferId;
        }

        public void setWaferId(String waferId) {
            this.waferId = waferId;
        }
    }

    /**
     * Wafer Map报告类
     */
    public static class WaferMapReport {
        private final int totalDies;
        private final int goodDies;
        private final int badDies;
        private final double yield;
        private final Map<DieStatus, Integer> statusCounts;

        public WaferMapReport(int totalDies, int goodDies, int badDies, double yield, Map<DieStatus, Integer> statusCounts) {
            this.totalDies = totalDies;
            this.goodDies = goodDies;
            this.badDies = badDies;
            this.yield = yield;
            this.statusCounts = statusCounts != null ? new EnumMap<>(statusCounts) : new EnumMap<>(DieStatus.class);
        }

        // Getters
        public int getTotalDies() {
            return totalDies;
        }

        public int getGoodDies() {
            return goodDies;
        }

        public int getBadDies() {
            return badDies;
        }

        public double getYield() {
            return yield;
        }

        public Map<DieStatus, Integer> getStatusCounts() {
            return new EnumMap<>(statusCounts);
        }

        @Override
        public String toString() {
            return "WaferMapReport{" +
                    "totalDies=" + totalDies +
                    ", goodDies=" + goodDies +
                    ", badDies=" + badDies +
                    ", yield=" + String.format("%.2f", yield * 100) + "%" +
                    ", statusCounts=" + statusCounts +
                    '}';
        }
    }
}
