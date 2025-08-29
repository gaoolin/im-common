package com.qtech.im.cache.annotation;

import com.qtech.im.cache.CacheKeyGenerator;
import com.qtech.im.cache.impl.SimpleKeyGenerator;

import java.lang.annotation.*;

/**
 * 缓存清除注解
 * <p>
 * 用于标记方法执行后需要清除缓存
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/19
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CacheEvict {

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
     * 是否清除所有缓存项
     *
     * @return 是否清除所有
     */
    boolean allEntries() default false;

    /**
     * 是否在方法执行前清除缓存
     *
     * @return 是否在方法执行前清除
     */
    boolean beforeInvocation() default false;
}