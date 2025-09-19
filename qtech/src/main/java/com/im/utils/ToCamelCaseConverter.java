package com.im.utils;

import static com.im.constants.EqLstCamelCaseConverter.*;

/**
 * 将AA List中的参数名称字符串转换为驼峰式命名
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/05/15 09:43:23
 */
public class ToCamelCaseConverter {
    public static String doConvert(String input) {
        // 步骤1：匹配关键词并转换

        for (String keyword : ABBREVIATIONS) {
            if (input.contains(keyword)) {
                input = input.replaceAll(keyword, capitalizeFirstOthersLowerCase(keyword));
            }
        }

        // 步骤2：只有当输入字符串包含下划线时才执行处理
        if (input.contains("_")) {
            String[] parts = input.split("_");
            StringBuilder resultBuilder = new StringBuilder();
            for (String part : parts) {
                if (part.equals(part.toUpperCase())) {
                    part = part.toLowerCase();
                    part = capitalizeFirstOthersLowerCase(part);
                } else {
                    part = capitalizeFirstOthersNoChange(part);
                }
                resultBuilder.append(part);
            }
            input = resultBuilder.toString();
        }

        // 步骤3：检查并处理首个字符和特殊字符
        if (!IGNORE_LIST_ITEMS.contains(input)) {
            if (!IGNORE_SPECIAL_CHARS.contains(input)) {
                if (!Character.isLowerCase(input.charAt(0))) {
                    input = Character.toLowerCase(input.charAt(0)) + input.substring(1);
                }
                input = input.replaceAll("[^a-zA-Z0-9]", "");
            }
        }
        return input;
    }

    private static String capitalizeFirstOthersLowerCase(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private static String capitalizeFirstOthersNoChange(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static void main(String[] args) {
        String input = "LP_Intensity";
        String convert = doConvert(input);
        System.out.println(input);
        System.out.println(convert);
    }
}