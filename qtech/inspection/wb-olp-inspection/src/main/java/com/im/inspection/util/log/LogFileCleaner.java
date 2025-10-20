package com.im.inspection.util.log;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 日志清理
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/05/14 13:36:45
 */

public class LogFileCleaner {
    private static final Logger logger = LoggerFactory.getLogger(LogFileCleaner.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 清理指定目录下超过 N 天的历史日志文件
     */
    public static void cleanupOldFiles(FileSystem fs, Path baseDir, int daysToKeep) {
        try {
            if (!fs.exists(baseDir)) return;

            LocalDate cutoffDate = LocalDate.now().minusDays(daysToKeep);

            FileStatus[] dirs = fs.listStatus(baseDir);
            for (FileStatus dir : dirs) {
                if (dir.isDirectory()) {
                    cleanupTaskDirectory(fs, dir.getPath(), cutoffDate);
                }
            }

        } catch (IOException e) {
            logger.error(">>>>> Error cleaning up old files", e);
        }
    }

    /**
     * 清理某个任务目录下的旧文件
     */
    private static void cleanupTaskDirectory(FileSystem fs, Path taskDir, LocalDate cutoffDate) throws IOException {
        FileStatus[] files = fs.listStatus(taskDir);
        for (FileStatus file : files) {
            String fileName = file.getPath().getName();
            if (fileName.startsWith("timing-") && (fileName.endsWith(".log") || fileName.endsWith(".log.gz"))) {
                String dateStr = fileName.substring(7, 17); // timing-yyyy-MM-dd.log
                LocalDate fileDate = LocalDate.parse(dateStr, DATE_FORMATTER);
                if (fileDate.isBefore(cutoffDate)) {
                    fs.delete(file.getPath(), false);
                }
            }
        }
    }
}