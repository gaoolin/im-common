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
 * @date 2025/10/11
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

    /**
     * 表名保持原样（不进行驼峰转下划线）
     */
    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        return name; // ⬅ 关键：表名不转换
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment context) {
        return name;
    }

    /**
     * 仅转换列名
     */
    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        if (name == null) return null;
        return Identifier.toIdentifier(convertColumn(name.getText()));
    }

    /**
     * 列名转换规则：
     * - 包含下划线：不转换
     * - 全大写：不转换，只转小写
     * - 驼峰：转下划线
     */
    private String convertColumn(String input) {
        if (input == null) return null;

        // 1. 已经有下划线，不转换
        if (input.contains("_")) {
            return input.toLowerCase();
        }

        // 2. 全大写
        if (input.matches("^[A-Z0-9]+$")) {
            return input.toLowerCase();
        }

        // 3. 驼峰 → 下划线
        StringBuilder result = new StringBuilder();
        char[] arr = input.toCharArray();

        for (int i = 0; i < arr.length; i++) {
            char c = arr[i];
            if (Character.isUpperCase(c)) {
                result.append('_').append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}