package org.im.orm.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 一对一关联注解
 * 用于标记实体间的一对一关联关系
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/09/22
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToOne {
    /**
     * 关联的实体类
     *
     * @return 实体类
     */
    Class<?> targetEntity() default void.class;

    /**
     * 外键字段名
     *
     * @return 外键字段名
     */
    String foreignKey() default "";

    /**
     * 是否延迟加载
     *
     * @return 是否延迟加载
     */
    boolean lazy() default true;

    /**
     * 关联字段名
     *
     * @return 关联字段名
     */
    String mappedBy() default "";
}