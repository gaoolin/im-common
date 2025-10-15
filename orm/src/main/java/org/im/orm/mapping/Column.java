package org.im.orm.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 列注解
 * 标记实体类中的字段与数据库列的映射关系
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/22
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    /**
     * 列名
     *
     * @return 列名
     */
    String name() default "";

    /**
     * 是否允许为空
     *
     * @return true表示允许为空，false表示不允许为空
     */
    boolean nullable() default true;

    /**
     * 字段长度
     *
     * @return 字段长度
     */
    int length() default 255;
}