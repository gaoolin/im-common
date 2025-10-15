package com.im.aa.inspection.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/05/07 14:49:46
 */
public class ListParserProcessor {

    public static Map<String, String> parseLines(List<String> lines) {
        Map<String, String> resultMap = new HashMap<>();
        Pattern pattern = Pattern.compile("LIST\\s(\\d+)\\s(.+?)\\s+(.+?)\\s+(.+?)\\s+(.+)");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String listNumber = matcher.group(1);
                String action = matcher.group(2);
                // 忽略 Tester, Continue, Continue 这些字段，直接取最后一个Enable状态
                String status = matcher.group(5);
                String key = "LIST-" + listNumber + "-" + action;
                resultMap.put(key, status);
            }
        }
        return resultMap;
    }

    public static void main(String[] args) {
        List<String> inputLines = new ArrayList<>();
        inputLines.add("LIST 1 Destroy_Start Tester Continue Continue Enable");
        // 如果有更多行，继续添加到inputLines

        Map<String, String> parsedData = parseLines(inputLines);
        for (Map.Entry<String, String> entry : parsedData.entrySet()) {
            System.out.println("\"" + entry.getKey() + "\"=\"" + entry.getValue() + "\"");
        }
    }
}
