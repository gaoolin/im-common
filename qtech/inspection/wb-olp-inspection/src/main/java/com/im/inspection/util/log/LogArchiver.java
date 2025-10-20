package com.im.inspection.util.log;

import org.apache.hadoop.fs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPOutputStream;

/**
 * 日志压缩与归档
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/05/14 13:34:37
 */

public class LogArchiver {
    private static final Logger logger = LoggerFactory.getLogger(LogArchiver.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 扫描目录并压缩昨天的日志文件
     */
    public static void archiveLogs(FileSystem fs, Path baseDir) {
        try {
            if (!fs.exists(baseDir)) return;

            FileStatus[] dirs = fs.listStatus(baseDir);
            for (FileStatus dir : dirs) {
                if (dir.isDirectory()) {
                    archiveTaskDirectory(fs, dir.getPath());
                }
            }

        } catch (IOException e) {
            logger.error(">>>>> Error archiving logs", e);
        }
    }

    /**
     * 压缩某个任务目录下的昨日日志
     */
    private static void archiveTaskDirectory(FileSystem fs, Path taskDir) throws IOException {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dateStr = yesterday.format(DATE_FORMATTER);
        Path logFile = new Path(taskDir, "timing-" + dateStr + ".log");

        if (fs.exists(logFile)) {
            Path gzipFile = new Path(taskDir, "timing-" + dateStr + ".log.gz");
            compressAndDelete(fs, logFile, gzipFile);
        }
    }

    /**
     * 压缩并删除原文件
     */
    private static void compressAndDelete(FileSystem fs, Path src, Path dest) throws IOException {
        FSDataInputStream in = fs.open(src);
        FSDataOutputStream out = fs.create(dest);

        try (GZIPOutputStream gzipOut = new GZIPOutputStream(out)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                gzipOut.write(buffer, 0, len);
            }
        }

        in.close();
        out.close();
        fs.delete(src, false); // 删除源文件
    }
}
