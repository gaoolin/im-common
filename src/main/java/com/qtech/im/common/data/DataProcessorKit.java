package com.qtech.im.common.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 通用数据处理工具类
 * <p>
 * 特性：
 * - 通用性：支持多种数据处理操作
 * - 规范性：统一的数据处理接口
 * - 专业性：基于函数式编程实现
 * - 灵活性：支持自定义处理逻辑
 * - 可靠性：完善的异常处理机制
 * - 安全性：输入验证和边界检查
 * - 复用性：高度模块化的处理组件
 * - 容错性：部分失败不影响整体处理
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/08/20
 */
public class DataProcessorKit {

    private static final Logger logger = LoggerFactory.getLogger(DataProcessorKit.class);

    /**
     * 过滤数据列表
     *
     * @param data   数据列表
     * @param filter 过滤条件
     * @param config 过滤配置
     * @param <T>    数据类型
     * @return 过滤后的数据列表
     */
    public static <T> List<T> filter(List<T> data, Predicate<T> filter, FilterConfig config) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }

        if (filter == null) {
            return new ArrayList<>(data);
        }

        if (config == null) {
            config = new FilterConfig();
        }

        List<T> result = new ArrayList<>();
        int errorCount = 0;

        for (T item : data) {
            try {
                // 基本过滤
                if (config.isSkipNulls() && item == null) {
                    continue;
                }

                if (config.isSkipEmptyStrings() && item instanceof String && ((String) item).isEmpty()) {
                    continue;
                }

                // 自定义过滤
                if (filter.test(item)) {
                    result.add(item);
                }
            } catch (Exception e) {
                errorCount++;
                logger.warn("Failed to filter item: {}", item, e);

                if (errorCount >= config.getMaxErrors()) {
                    logger.error("Too many filter errors, stopping processing");
                    break;
                }
            }
        }

        return result;
    }

    /**
     * 转换数据列表
     *
     * @param data        数据列表
     * @param transformer 转换函数
     * @param config      转换配置
     * @param <T>         输入数据类型
     * @param <R>         输出数据类型
     * @return 转换后的数据列表
     */
    public static <T, R> List<R> transform(List<T> data, Function<T, R> transformer, TransformConfig config) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }

        if (transformer == null) {
            throw new IllegalArgumentException("Transformer function cannot be null");
        }

        if (config == null) {
            config = new TransformConfig();
        }

        List<R> result = new ArrayList<>();

        for (T item : data) {
            try {
                R transformed = transformer.apply(item);
                result.add(transformed);
            } catch (Exception e) {
                logger.warn("Failed to transform item: {}", item, e);

                if (config.isContinueOnError()) {
                    result.add((R) config.getDefaultValue());
                } else {
                    throw new RuntimeException("Failed to transform item", e);
                }
            }
        }

        return result;
    }

    /**
     * 数据聚合处理
     *
     * @param data      数据列表
     * @param collector 聚合收集器
     * @param <T>       数据类型
     * @param <A>       中间累积类型
     * @param <R>       聚合结果类型
     * @return 聚合结果
     */
    public static <T, A, R> R aggregate(List<T> data, Collector<T, A, R> collector) {
        if (data == null) {
            // 对于null列表，创建一个空流
            return Stream.<T>empty().collect(collector);
        }

        return data.stream().collect(collector);
    }

    /**
     * 数据分组处理
     *
     * @param data      数据列表
     * @param keyMapper 键映射函数
     * @param <T>       数据类型
     * @param <K>       分组键类型
     * @return 分组结果
     */
    public static <T, K> Map<K, List<T>> groupBy(List<T> data, Function<T, K> keyMapper) {
        if (data == null || data.isEmpty() || keyMapper == null) {
            return new HashMap<>();
        }

        return data.stream().collect(Collectors.groupingBy(keyMapper));
    }

    /**
     * 数据去重处理
     *
     * @param data 数据列表
     * @param <T>  数据类型
     * @return 去重后的数据列表
     */
    public static <T> List<T> distinct(List<T> data) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }

        return data.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 数据过滤配置
     */
    public static class FilterConfig {
        private boolean skipNulls = true;
        private boolean skipEmptyStrings = true;
        private int maxErrors = 10; // 最大错误数，超过则停止处理

        // Getters and Setters
        public boolean isSkipNulls() {
            return skipNulls;
        }

        public FilterConfig setSkipNulls(boolean skipNulls) {
            this.skipNulls = skipNulls;
            return this;
        }

        public boolean isSkipEmptyStrings() {
            return skipEmptyStrings;
        }

        public FilterConfig setSkipEmptyStrings(boolean skipEmptyStrings) {
            this.skipEmptyStrings = skipEmptyStrings;
            return this;
        }

        public int getMaxErrors() {
            return maxErrors;
        }

        public FilterConfig setMaxErrors(int maxErrors) {
            this.maxErrors = maxErrors;
            return this;
        }
    }

    /**
     * 数据转换配置
     */
    public static class TransformConfig {
        private boolean continueOnError = false;
        private Object defaultValue = null;

        // Getters and Setters
        public boolean isContinueOnError() {
            return continueOnError;
        }

        public TransformConfig setContinueOnError(boolean continueOnError) {
            this.continueOnError = continueOnError;
            return this;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public TransformConfig setDefaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
    }
}
