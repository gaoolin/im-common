package com.qtech.im.util.dt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 专业的时间处理工具类
 * <p>
 * 特性：
 * - 通用化：支持多种时间格式和类型转换
 * - 规范化：提供统一的时间处理接口和标准格式
 * - 灵活性：支持自定义格式、时区和时间计算
 * - 容错性：具备异常处理和默认值机制
 * - 专业性：遵循Java 8+时间API最佳实践
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/08/20
 */
public class Chronos {

    // 默认时区
    public static final String DEFAULT_TIMEZONE = "Asia/Shanghai";
    // 常用日期时间格式
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";
    public static final String ISO_TIME_FORMAT = "HH:mm:ss";
    public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String ISO_DATETIME_MS_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String COMPACT_DATE_FORMAT = "yyyyMMdd";
    public static final String COMPACT_DATETIME_FORMAT = "yyyyMMddHHmmss";
    public static final String COMPACT_DATETIME_MS_FORMAT = "yyyyMMddHHmmssSSS";
    private static final Logger logger = LoggerFactory.getLogger(Chronos.class);
    // 格式化器缓存，提高性能
    private static final Map<String, DateTimeFormatter> FORMATTER_CACHE = new ConcurrentHashMap<>();

    // 预定义常用格式化器
    static {
        FORMATTER_CACHE.put(ISO_DATE_FORMAT, DateTimeFormatter.ofPattern(ISO_DATE_FORMAT));
        FORMATTER_CACHE.put(ISO_TIME_FORMAT, DateTimeFormatter.ofPattern(ISO_TIME_FORMAT));
        FORMATTER_CACHE.put(ISO_DATETIME_FORMAT, DateTimeFormatter.ofPattern(ISO_DATETIME_FORMAT));
        FORMATTER_CACHE.put(ISO_DATETIME_MS_FORMAT, DateTimeFormatter.ofPattern(ISO_DATETIME_MS_FORMAT));
        FORMATTER_CACHE.put(COMPACT_DATE_FORMAT, DateTimeFormatter.ofPattern(COMPACT_DATE_FORMAT));
        FORMATTER_CACHE.put(COMPACT_DATETIME_FORMAT, DateTimeFormatter.ofPattern(COMPACT_DATETIME_FORMAT));
        FORMATTER_CACHE.put(COMPACT_DATETIME_MS_FORMAT, DateTimeFormatter.ofPattern(COMPACT_DATETIME_MS_FORMAT));
    }

    /**
     * 获取指定格式的DateTimeFormatter实例（带缓存）
     *
     * @param pattern 日期时间格式模式
     * @return DateTimeFormatter实例
     */
    public static DateTimeFormatter getFormatter(String pattern) {
        return FORMATTER_CACHE.computeIfAbsent(pattern, DateTimeFormatter::ofPattern);
    }

    /**
     * 获取默认时区
     *
     * @return ZoneId实例
     */
    public static ZoneId getDefaultZoneId() {
        return ZoneId.of(DEFAULT_TIMEZONE);
    }

    /**
     * 获取指定时区
     *
     * @param zoneId 时区ID
     * @return ZoneId实例
     */
    public static ZoneId getZoneId(String zoneId) {
        try {
            return ZoneId.of(zoneId);
        } catch (Exception e) {
            logger.warn("Invalid zone ID: {}, using default zone", zoneId);
            return getDefaultZoneId();
        }
    }

    /**
     * 获取当前时间戳（毫秒）
     *
     * @return 当前时间戳
     */
    public static long currentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前日期时间
     *
     * @return LocalDateTime实例
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 获取指定时区的当前日期时间
     *
     * @param zoneId 时区ID
     * @return ZonedDateTime实例
     */
    public static ZonedDateTime now(String zoneId) {
        return ZonedDateTime.now(getZoneId(zoneId));
    }

    /**
     * 解析字符串为LocalDate
     *
     * @param dateStr 日期字符串
     * @param pattern 日期格式
     * @return LocalDate实例，解析失败返回null
     */
    public static LocalDate parseDate(String dateStr, String pattern) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dateStr, getFormatter(pattern));
        } catch (DateTimeParseException e) {
            logger.warn("Failed to parse date string: {} with pattern: {}", dateStr, pattern);
            return null;
        }
    }

    /**
     * 解析字符串为LocalDate（使用默认格式）
     *
     * @param dateStr 日期字符串
     * @return LocalDate实例，解析失败返回null
     */
    public static LocalDate parseDate(String dateStr) {
        return parseDate(dateStr, ISO_DATE_FORMAT);
    }

    /**
     * 解析字符串为LocalTime
     *
     * @param timeStr 时间字符串
     * @param pattern 时间格式
     * @return LocalTime实例，解析失败返回null
     */
    public static LocalTime parseTime(String timeStr, String pattern) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }

        try {
            return LocalTime.parse(timeStr, getFormatter(pattern));
        } catch (DateTimeParseException e) {
            logger.warn("Failed to parse time string: {} with pattern: {}", timeStr, pattern);
            return null;
        }
    }

    /**
     * 解析字符串为LocalTime（使用默认格式）
     *
     * @param timeStr 时间字符串
     * @return LocalTime实例，解析失败返回null
     */
    public static LocalTime parseTime(String timeStr) {
        return parseTime(timeStr, ISO_TIME_FORMAT);
    }

    /**
     * 解析字符串为LocalDateTime
     *
     * @param dateTimeStr 日期时间字符串
     * @param pattern     日期时间格式
     * @return LocalDateTime实例，解析失败返回null
     */
    public static LocalDateTime parseDateTime(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dateTimeStr, getFormatter(pattern));
        } catch (DateTimeParseException e) {
            logger.warn("Failed to parse datetime string: {} with pattern: {}", dateTimeStr, pattern);
            return null;
        }
    }

    /**
     * 解析字符串为LocalDateTime（使用默认格式）
     *
     * @param dateTimeStr 日期时间字符串
     * @return LocalDateTime实例，解析失败返回null
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return parseDateTime(dateTimeStr, ISO_DATETIME_FORMAT);
    }

    /**
     * 解析字符串为ZonedDateTime
     *
     * @param dateTimeStr 日期时间字符串
     * @param pattern     日期时间格式
     * @param zoneId      时区ID
     * @return ZonedDateTime实例，解析失败返回null
     */
    public static ZonedDateTime parseZonedDateTime(String dateTimeStr, String pattern, String zoneId) {
        LocalDateTime localDateTime = parseDateTime(dateTimeStr, pattern);
        if (localDateTime == null) {
            return null;
        }

        return localDateTime.atZone(getZoneId(zoneId));
    }

    /**
     * 解析字符串为ZonedDateTime（使用默认格式和时区）
     *
     * @param dateTimeStr 日期时间字符串
     * @return ZonedDateTime实例，解析失败返回null
     */
    public static ZonedDateTime parseZonedDateTime(String dateTimeStr) {
        return parseZonedDateTime(dateTimeStr, ISO_DATETIME_FORMAT, DEFAULT_TIMEZONE);
    }

    /**
     * 将时间对象格式化为字符串
     *
     * @param temporal 时间对象
     * @param pattern  格式模式
     * @return 格式化后的字符串
     */
    public static String format(Temporal temporal, String pattern) {
        if (temporal == null) {
            return null;
        }

        try {
            return getFormatter(pattern).format(temporal);
        } catch (Exception e) {
            logger.warn("Failed to format temporal: {} with pattern: {}", temporal, pattern);
            return null;
        }
    }

    /**
     * 将时间对象格式化为字符串（使用默认格式）
     *
     * @param temporal 时间对象
     * @return 格式化后的字符串
     */
    public static String format(Temporal temporal) {
        return format(temporal, ISO_DATETIME_FORMAT);
    }

    /**
     * 将时间戳转换为LocalDateTime
     *
     * @param timestamp 时间戳（毫秒）
     * @return LocalDateTime实例
     */
    public static LocalDateTime fromTimestamp(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), getDefaultZoneId());
    }

    /**
     * 将时间戳转换为ZonedDateTime
     *
     * @param timestamp 时间戳（毫秒）
     * @param zoneId    时区ID
     * @return ZonedDateTime实例
     */
    public static ZonedDateTime fromTimestamp(long timestamp, String zoneId) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), getZoneId(zoneId)).atZone(getZoneId(zoneId));
    }

    /**
     * 将LocalDateTime转换为时间戳
     *
     * @param dateTime LocalDateTime实例
     * @return 时间戳（毫秒）
     */
    public static long toTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return 0L;
        }

        return dateTime.atZone(getDefaultZoneId()).toInstant().toEpochMilli();
    }

    /**
     * 将ZonedDateTime转换为时间戳
     *
     * @param dateTime ZonedDateTime实例
     * @return 时间戳（毫秒）
     */
    public static long toTimestamp(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return 0L;
        }

        return dateTime.toInstant().toEpochMilli();
    }

    /**
     * 计算两个日期之间的天数差
     *
     * @param start 起始日期
     * @param end   结束日期
     * @return 天数差
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return 0L;
        }

        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * 计算两个日期时间之间的小时差
     *
     * @param start 起始日期时间
     * @param end   结束日期时间
     * @return 小时差
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0L;
        }

        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * 计算两个日期时间之间的分钟差
     *
     * @param start 起始日期时间
     * @param end   结束日期时间
     * @return 分钟差
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0L;
        }

        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * 获取指定日期的开始时间（00:00:00）
     *
     * @param date 日期
     * @return 当天的开始时间
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }

        return date.atStartOfDay();
    }

    /**
     * 获取指定日期的结束时间（23:59:59.999999999）
     *
     * @param date 日期
     * @return 当天的结束时间
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }

        return date.atTime(23, 59, 59, 999_999_999);
    }

    /**
     * 获取指定日期所在月份的第一天
     *
     * @param date 日期
     * @return 月份第一天的日期
     */
    public static LocalDate firstDayOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }

        return date.with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * 获取指定日期所在月份的最后一天
     *
     * @param date 日期
     * @return 月份最后一天的日期
     */
    public static LocalDate lastDayOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }

        return date.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * 获取指定日期所在年份的第一天
     *
     * @param date 日期
     * @return 年份第一天的日期
     */
    public static LocalDate firstDayOfYear(LocalDate date) {
        if (date == null) {
            return null;
        }

        return date.with(TemporalAdjusters.firstDayOfYear());
    }

    /**
     * 获取指定日期所在年份的最后一天
     *
     * @param date 日期
     * @return 年份最后一天的日期
     */
    public static LocalDate lastDayOfYear(LocalDate date) {
        if (date == null) {
            return null;
        }

        return date.with(TemporalAdjusters.lastDayOfYear());
    }

    /**
     * 添加指定天数到日期
     *
     * @param date 日期
     * @param days 天数（可为负数）
     * @return 计算后的日期
     */
    public static LocalDate addDays(LocalDate date, long days) {
        if (date == null) {
            return null;
        }

        return date.plusDays(days);
    }

    /**
     * 添加指定月数到日期
     *
     * @param date   日期
     * @param months 月数（可为负数）
     * @return 计算后的日期
     */
    public static LocalDate addMonths(LocalDate date, long months) {
        if (date == null) {
            return null;
        }

        return date.plusMonths(months);
    }

    /**
     * 添加指定年数到日期
     *
     * @param date  日期
     * @param years 年数（可为负数）
     * @return 计算后的日期
     */
    public static LocalDate addYears(LocalDate date, long years) {
        if (date == null) {
            return null;
        }

        return date.plusYears(years);
    }

    /**
     * 添加指定小时数到日期时间
     *
     * @param dateTime 日期时间
     * @param hours    小时数（可为负数）
     * @return 计算后的日期时间
     */
    public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
        if (dateTime == null) {
            return null;
        }

        return dateTime.plusHours(hours);
    }

    /**
     * 添加指定分钟数到日期时间
     *
     * @param dateTime 日期时间
     * @param minutes  分钟数（可为负数）
     * @return 计算后的日期时间
     */
    public static LocalDateTime addMinutes(LocalDateTime dateTime, long minutes) {
        if (dateTime == null) {
            return null;
        }

        return dateTime.plusMinutes(minutes);
    }

    /**
     * 添加指定秒数到日期时间
     *
     * @param dateTime 日期时间
     * @param seconds  秒数（可为负数）
     * @return 计算后的日期时间
     */
    public static LocalDateTime addSeconds(LocalDateTime dateTime, long seconds) {
        if (dateTime == null) {
            return null;
        }

        return dateTime.plusSeconds(seconds);
    }

    /**
     * 判断是否为闰年
     *
     * @param year 年份
     * @return 是否为闰年
     */
    public static boolean isLeapYear(int year) {
        return Year.of(year).isLeap();
    }

    /**
     * 获取指定年份的总天数
     *
     * @param year 年份
     * @return 总天数
     */
    public static int lengthOfYear(int year) {
        return Year.of(year).length();
    }

    /**
     * 获取指定年月的总天数
     *
     * @param year  年份
     * @param month 月份
     * @return 总天数
     */
    public static int lengthOfMonth(int year, int month) {
        return YearMonth.of(year, month).lengthOfMonth();
    }

    /**
     * 检查两个时间段是否有重叠
     *
     * @param start1 第一个时间段的开始时间
     * @param end1   第一个时间段的结束时间
     * @param start2 第二个时间段的开始时间
     * @param end2   第二个时间段的结束时间
     * @return 是否有重叠
     */
    public static boolean isOverlap(LocalDateTime start1, LocalDateTime end1,
                                    LocalDateTime start2, LocalDateTime end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }

        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * 将 LocalDateTime 转换为 Date
     *
     * @param localDateTime 待转换的 LocalDateTime
     * @return 转换后的 Date 对象
     */
    public static Date toDate(LocalDateTime localDateTime) {
        return toDate(localDateTime, ZoneId.systemDefault());
    }

    /**
     * 将 LocalDateTime 转换为 Date（指定时区）
     *
     * @param localDateTime 待转换的 LocalDateTime
     * @param zoneId        时区
     * @return 转换后的 Date 对象
     */
    public static Date toDate(LocalDateTime localDateTime, ZoneId zoneId) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(zoneId).toInstant());
    }

    /**
     * 将 Date 转换为 LocalDateTime
     *
     * @param date 待转换的 Date
     * @return 转换后的 LocalDateTime 对象
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return toLocalDateTime(date, ZoneId.systemDefault());
    }

    /**
     * 将 Date 转换为 LocalDateTime（指定时区）
     *
     * @param date   待转换的 Date
     * @param zoneId 时区
     * @return 转换后的 LocalDateTime 对象
     */
    public static LocalDateTime toLocalDateTime(Date date, ZoneId zoneId) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(zoneId).toLocalDateTime();
    }
}
