package com.qtech.im.cache.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;
/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/19 15:57:49
 */

/**
 * 缓存更新注解
 * <p>
 * 用于标记方法执行后需要更新缓存
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CachePut {

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
}