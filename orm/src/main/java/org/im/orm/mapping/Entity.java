package org.im.orm.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实体注解
 * 标记一个类为ORM实体类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/09/22
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {
    /**
     * 表名
     *
     * @return 表名
     */
    String table() default "";

    /**
     * 数据库模式
     *
     * @return 数据库模式
     */
    String schema() default "";

    /**
     * 是否启用关联映射
     *
     * @return 是否启用关联映射
     */
    boolean enableAssociations() default false;
}
