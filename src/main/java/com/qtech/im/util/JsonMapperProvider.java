package com.qtech.im.util

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * JSON 映射器工厂类
 * <p>
 * 职责：
 * 1. 提供共享的 ObjectMapper 实例
 * 2. 创建自定义配置的 ObjectMapper 实例
 * 3. 管理 ObjectMapper 的生命周期
 * <p>
 * 特性：
 * - 线程安全的单例模式实现
 * - 支持懒加载和延迟初始化
 * - 配置了常用的序列化/反序列化特性
 * - 集成 Java 8 时间处理模块
 * - 具备容错和日志记录能力
 *
 * @author gaozhilin
 * @version 1.0
 * <p>
 * // 1. 获取共享实例
 * ObjectMapper mapper = JsonMapperProvider.getSharedInstance();
 * <p>
 * // 2. 创建自定义配置实例
 * ObjectMapper customMapper = JsonMapperProvider.createCustomizedInstance(m -> {
 * m.configure(SerializationFeature.INDENT_OUTPUT, true);
 * });
 * <p>
 * // 3. 创建新实例
 * ObjectMapper newMapper = JsonMapperProvider.createNewInstance();
 * <p>
 * // 4. 创建自定义配置的新实例
 * ObjectMapper customNewMapper = JsonMapperProvider.createCustomizedNewInstance(m -> {
 * m.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
 * });
 */
public class JsonMapperProvider {

    private static final Logger logger = LoggerFactory.getLogger(JsonMapperProvider.class);
    // 用于双重检查锁定的锁
    private static final Lock lock = new ReentrantLock();
    // 默认日期格式
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    // 默认时区
    private static final String DEFAULT_TIMEZONE = "Asia/Shanghai";
    // volatile 确保多线程环境下的可见性
    private static volatile ObjectMapper sharedInstance;

    // 私有构造函数防止外部实例化
    private JsonMapperProvider() {
        // 防止通过反射创建实例
        if (sharedInstance != null) {
            throw new IllegalStateException("JsonMapperProvider instance already exists!");
        }
    }

    /**
     * 获取共享的 ObjectMapper 实例（双重检查锁定实现）
     *
     * @return 全局唯一的 ObjectMapper 实例
     */
    public static ObjectMapper getSharedInstance() {
        // 第一次检查（无锁）
        if (sharedInstance == null) {
            // 加锁
            lock.lock();
            try {
                // 第二次检查（有锁）
                if (sharedInstance == null) {
                    sharedInstance = createDefaultObjectMapper();
                    if (logger.isInfoEnabled()) {
                        logger.info(">>>>> Shared ObjectMapper instance created successfully");
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        return sharedInstance;
    }

    /**
     * 创建具有自定义配置的 ObjectMapper 实例
     *
     * @param configCallback 配置回调函数
     * @return 配置后的 ObjectMapper 实例
     */
    public static ObjectMapper createCustomizedInstance(java.util.function.Consumer<ObjectMapper> configCallback) {
        ObjectMapper mapper = getSharedInstance().copy();
        if (configCallback != null) {
            try {
                configCallback.accept(mapper);
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(">>>>> Failed to apply custom configuration to ObjectMapper", e);
                }
            }
        }
        return mapper;
    }

    /**
     * 创建新的 ObjectMapper 实例（基于共享实例配置）
     *
     * @return 新的 ObjectMapper 实例
     */
    public static ObjectMapper createNewInstance() {
        return getSharedInstance().copy();
    }

    /**
     * 创建新的具有自定义配置的 ObjectMapper 实例
     *
     * @param configCallback 配置回调函数
     * @return 配置后的新 ObjectMapper 实例
     */
    public static ObjectMapper createCustomizedNewInstance(java.util.function.Consumer<ObjectMapper> configCallback) {
        ObjectMapper mapper = getSharedInstance().copy();
        if (configCallback != null) {
            try {
                configCallback.accept(mapper);
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(">>>>> Failed to apply custom configuration to new ObjectMapper", e);
                }
            }
        }
        return mapper;
    }

    /**
     * 创建默认配置的 ObjectMapper 实例
     *
     * @return 配置好的 ObjectMapper 实例
     */
    private static ObjectMapper createDefaultObjectMapper() {
        try {
            JsonMapper.Builder builder = JsonMapper.builder();

            // 注册 JavaTimeModule 以支持 Java 8 时间类型
            builder.addModule(new JavaTimeModule());

            ObjectMapper mapper = builder.build();

            // 配置日期格式
            configureDateFormat(mapper, DEFAULT_DATE_FORMAT);

            // 配置时区
            mapper.setTimeZone(TimeZone.getTimeZone(DEFAULT_TIMEZONE));

            // 配置序列化特性
            configureSerializationFeatures(mapper);

            // 配置反序列化特性
            configureDeserializationFeatures(mapper);

            // 配置 JsonParser 特性
            configureJsonParserFeatures(mapper);

            // 配置 JsonGenerator 特性
            configureJsonGeneratorFeatures(mapper);

            return mapper;
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(">>>>> Failed to create ObjectMapper instance", e);
            }
            throw new RuntimeException(">>>>> Failed to create ObjectMapper instance", e);
        }
    }

    /**
     * 配置日期格式
     *
     * @param mapper  ObjectMapper 实例
     * @param pattern 日期格式模式
     */
    public static void configureDateFormat(ObjectMapper mapper, String pattern) {
        if (mapper == null || pattern == null || pattern.isEmpty()) {
            return;
        }

        try {
            DateFormat dateFormat = new SimpleDateFormat(pattern);
            mapper.setDateFormat(dateFormat);
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(">>>>> Failed to configure date format: {}", pattern, e);
            }
        }
    }

    /**
     * 配置序列化特性
     *
     * @param mapper ObjectMapper 实例
     */
    private static void configureSerializationFeatures(ObjectMapper mapper) {
        if (mapper == null) {
            return;
        }

        // 禁用将日期写为时间戳
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 禁用将空对象序列化为 {}
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        // 启用属性排序
        mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

        // 序列化时包含非空字段
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * 配置反序列化特性
     *
     * @param mapper ObjectMapper 实例
     */
    private static void configureDeserializationFeatures(ObjectMapper mapper) {
        if (mapper == null) {
            return;
        }

        // 禁用遇到未知属性时抛出异常
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // 启用接受空字符串作为 null 对象
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        // 启用接受单值作为数组
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    }

    /**
     * 配置 JsonParser 特性
     *
     * @param mapper ObjectMapper 实例
     */
    private static void configureJsonParserFeatures(ObjectMapper mapper) {
        if (mapper == null) {
            return;
        }

        // 启用允许注释
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);

        // 启用允许单引号
        mapper.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);

        // 启用允许未加引号的字段名
        mapper.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);

        // 启用允许尾随逗号
        mapper.enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);
    }

    /**
     * 配置 JsonGenerator 特性
     *
     * @param mapper ObjectMapper 实例
     */
    private static void configureJsonGeneratorFeatures(ObjectMapper mapper) {
        if (mapper == null) {
            return;
        }

        // 启用自动关闭目标
        mapper.enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

        // 启用引用检测（防止循环引用）
        mapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
    }

    /**
     * 重置共享实例（仅用于测试环境）
     *
     * @deprecated 不应在生产环境中使用
     */
    @Deprecated
    public static void resetSharedInstance() {
        if (logger.isDebugEnabled()) {
            logger.debug(">>>>> Resetting shared ObjectMapper instance");
        }

        lock.lock();
        try {
            sharedInstance = null;
        } finally {
            lock.unlock();
        }
    }
}
