package com.qtech.im.cache.annotation;

import com.qtech.im.cache.CacheKeyGenerator;
import com.qtech.im.cache.impl.SimpleKeyGenerator;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 缓存注解
 * <p>
 * 用于标记方法的返回值需要被缓存
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/19
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Cacheable {

    /**
     * 缓存名称
     *
     * @return 缓存名称
     */
    String cacheName();

    /**
     * 缓存键表达式（支持SpEL表达式）
     *
     * @return 缓存键表达式
     */
    String key() default "";

    /**
     * 缓存键生成器
     *
     * @return 键生成器类
     */
    Class<? extends CacheKeyGenerator> keyGenerator() default SimpleKeyGenerator.class;

    /**
     * 过期时间
     *
     * @return 过期时间
     */
    long ttl() default 0;

    /**
     * 过期时间单位
     *
     * @return 时间单位
     */
    TimeUnit ttlUnit() default TimeUnit.MILLISECONDS;

    /**
     * 条件表达式（支持SpEL表达式）
     *
     * @return 条件表达式
     */
    String condition() default "";

    /**
     * 除非表达式（支持SpEL表达式）
     *
     * @return 除非表达式
     */
    String unless() default "";
}