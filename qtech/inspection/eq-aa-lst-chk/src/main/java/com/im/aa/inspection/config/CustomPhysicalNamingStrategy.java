package com.im.aa.inspection.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * 自定义命名策略：在字母与数字之间也插入下划线
 * <p>
 * - 保留数字不拆分，比如 aa1 → aa1，而不是 aa_1
 * - 正常驼峰字母拆成下划线，比如 aa1CcToCornerLimit → aa1_cc_to_corner_limit
 * - 对数据库列名大小写保持 PostgreSQL 小写规则
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/10/11
 */
public class CustomPhysicalNamingStrategy implements PhysicalNamingStrategy {

    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment context) {
        return name;
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment context) {
        return name;
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        return apply(name);
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment context) {
        return apply(name);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        return apply(name);
    }

    private Identifier apply(Identifier name) {
        if (name == null) return null;
        String newName = convert(name.getText());
        return Identifier.toIdentifier(newName);
    }

    /**
     * 自定义驼峰转下划线逻辑，数字不拆分
     * 示例：
     * aa1 -> aa1
     * aa1CcToCornerLimit -> aa1_cc_to_corner_limit
     */
    private String convert(String input) {
        if (input == null) return null;

        StringBuilder result = new StringBuilder();
        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isUpperCase(c)) {
                result.append('_').append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}