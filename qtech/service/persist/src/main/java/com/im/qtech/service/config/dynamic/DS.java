package com.im.qtech.service.config.dynamic;

import java.lang.annotation.*;

/**
 * 数据源切换注解
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/15
 */

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DS {
    DSName value() default DSName.FIRST;
}