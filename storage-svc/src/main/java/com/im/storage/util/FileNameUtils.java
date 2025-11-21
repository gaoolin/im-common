package com.im.storage.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件名工具类
 * 提供文件名处理相关功能
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/07
 */
public class FileNameUtils {

    /**
     * 根据文件是否存在决定是否重命名文件
     * 如果文件存在，则生成一个新的文件名（在文件名后添加序号）
     *
     * @param originalFileName 原始文件名
     * @param fileExists       文件是否存在标识
     * @return 如果文件存在返回包含新文件名的Map，否则返回null
     */
    public static Map<String, String> getFileNameWithPossibleRename(String originalFileName, boolean fileExists) {
        if (!fileExists) {
            return null;
        }

        String newFileName = generateUniqueFileName(originalFileName);

        Map<String, String> result = new HashMap<>();
        result.put("originalFileName", originalFileName);
        result.put("newFileName", newFileName);
        result.put("renamed", "true");

        return result;
    }

    /**
     * 生成唯一的文件名
     * 在文件名后面添加序号以确保唯一性
     *
     * @param fileName 原始文件名
     * @return 唯一文件名
     */
    private static String generateUniqueFileName(String fileName) {
        // 匹配文件名和扩展名
        Pattern pattern = Pattern.compile("^(.*?)(\\.[^.]+)?$");
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.matches()) {
            String namePart = matcher.group(1);
            String extensionPart = matcher.group(2) != null ? matcher.group(2) : "";

            // 添加时间戳和随机数确保唯一性
            long timestamp = System.currentTimeMillis();
            int randomNum = (int) (Math.random() * 1000);

            return namePart + "_" + timestamp + "_" + randomNum + extensionPart;
        } else {
            // 如果无法解析文件名结构，使用简单的时间戳命名
            long timestamp = System.currentTimeMillis();
            int randomNum = (int) (Math.random() * 1000);
            return fileName + "_" + timestamp + "_" + randomNum;
        }
    }

    /**
     * 从完整路径中提取文件名
     *
     * @param fullPath 完整路径
     * @return 文件名
     */
    public static String extractFileName(String fullPath) {
        if (fullPath == null || fullPath.isEmpty()) {
            return "";
        }

        // 处理Windows和Unix风格的路径分隔符
        int lastSeparatorIndex = Math.max(
                fullPath.lastIndexOf('/'),
                fullPath.lastIndexOf('\\')
        );

        if (lastSeparatorIndex >= 0 && lastSeparatorIndex < fullPath.length() - 1) {
            return fullPath.substring(lastSeparatorIndex + 1);
        }

        return fullPath;
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 文件扩展名（包含点号），如果没有扩展名则返回空字符串
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex >= 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex);
        }

        return "";
    }

    /**
     * 移除文件扩展名
     *
     * @param fileName 文件名
     * @return 不含扩展名的文件名
     */
    public static String removeFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }

        return fileName;
    }
}
