package com.im.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2024/05/15
 */

public class EqLstCamelCaseConverter {
    public static final Set<String> ABBREVIATIONS = new HashSet<>(Arrays.asList("AA1", "AA2", "AA3", "SID", "MTF", "UVON", "UVOFF", "VCM", "OIS", "UT", "XYZ", "SFR")); // 添加缩写列表
    public static final Set<String> IGNORE_SPECIAL_CHARS = new HashSet<>(Arrays.asList("#", " ")); // 忽略字符列表
    public static final Set<String> IGNORE_LIST_ITEMS = new HashSet<>(Collections.singletonList("")); // 忽略列表项，忽略转换成驼峰
}