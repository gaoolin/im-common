package org.im.common.string;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 专业的文件和路径操作工具类
 * <p>
 * 特性：
 * - 通用化：支持文件读写、复制、移动、删除等常见操作
 * - 规范化：统一的API接口和错误处理
 * - 灵活性：支持多种编码格式、过滤条件、批量操作等
 * - 容错性：完善的异常处理和恢复机制
 * - 安全性：路径遍历防护、权限检查、文件锁定等安全机制
 * - 可靠性：原子操作、资源自动释放、并发安全等
 * - 专业性：遵循NIO.2规范和最佳实践
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/08/20
 */
public class IOKit {

    // 默认缓冲区大小
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    // 默认字符集
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    // 系统临时目录
    public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    // 系统文件分隔符
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    // 系统行分隔符
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final Logger logger = LoggerFactory.getLogger(IOKit.class);
    // 非法字符集（Windows文件名中不允许的字符）
    private static final Set<Character> ILLEGAL_CHARACTERS = new HashSet<>(Arrays.asList(
            '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'
    ));

    // 文件锁定缓存
    private static final Map<String, FileLock> FILE_LOCKS = new ConcurrentHashMap<>();

    /**
     * 检查文件或目录是否存在
     *
     * @param path 文件或目录路径
     * @return 是否存在
     */
    public static boolean exists(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        try {
            return Files.exists(Paths.get(path));
        } catch (Exception e) {
            logger.warn("Failed to check if path exists: {}", path, e);
            return false;
        }
    }

    /**
     * 检查是否为文件
     *
     * @param path 文件路径
     * @return 是否为文件
     */
    public static boolean isFile(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        try {
            return Files.isRegularFile(Paths.get(path));
        } catch (Exception e) {
            logger.warn("Failed to check if path is file: {}", path, e);
            return false;
        }
    }

    /**
     * 检查是否为目录
     *
     * @param path 目录路径
     * @return 是否为目录
     */
    public static boolean isDirectory(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        try {
            return Files.isDirectory(Paths.get(path));
        } catch (Exception e) {
            logger.warn("Failed to check if path is directory: {}", path, e);
            return false;
        }
    }

    /**
     * 检查文件是否可读
     *
     * @param path 文件路径
     * @return 是否可读
     */
    public static boolean isReadable(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        try {
            return Files.isReadable(Paths.get(path));
        } catch (Exception e) {
            logger.warn("Failed to check if file is readable: {}", path, e);
            return false;
        }
    }

    /**
     * 检查文件是否可写
     *
     * @param path 文件路径
     * @return 是否可写
     */
    public static boolean isWritable(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        try {
            return Files.isWritable(Paths.get(path));
        } catch (Exception e) {
            logger.warn("Failed to check if file is writable: {}", path, e);
            return false;
        }
    }

    /**
     * 检查文件是否可执行
     *
     * @param path 文件路径
     * @return 是否可执行
     */
    public static boolean isExecutable(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        try {
            return Files.isExecutable(Paths.get(path));
        } catch (Exception e) {
            logger.warn("Failed to check if file is executable: {}", path, e);
            return false;
        }
    }

    /**
     * 获取文件大小（字节）
     *
     * @param path 文件路径
     * @return 文件大小，失败返回-1
     */
    public static long getFileSize(String path) {
        if (path == null || path.isEmpty()) {
            return -1;
        }

        try {
            return Files.size(Paths.get(path));
        } catch (Exception e) {
            logger.warn("Failed to get file size: {}", path, e);
            return -1;
        }
    }

    /**
     * 获取文件最后修改时间
     *
     * @param path 文件路径
     * @return 最后修改时间，失败返回null
     */
    public static FileTime getLastModifiedTime(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        try {
            return Files.getLastModifiedTime(Paths.get(path));
        } catch (Exception e) {
            logger.warn("Failed to get last modified time: {}", path, e);
            return null;
        }
    }

    /**
     * 读取文件内容为字符串
     *
     * @param path 文件路径
     * @return 文件内容，失败返回null
     */
    public static String readFileToString(String path) {
        return readFileToString(path, DEFAULT_CHARSET);
    }

    /**
     * 读取文件内容为字符串（指定字符集）
     *
     * @param path    文件路径
     * @param charset 字符集
     * @return 文件内容，失败返回null
     */
    public static String readFileToString(String path, Charset charset) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        try {
            return new String(Files.readAllBytes(Paths.get(path)), charset);
        } catch (Exception e) {
            logger.warn("Failed to read file to string: {}", path, e);
            return null;
        }
    }

    /**
     * 读取文件内容为字节数组
     *
     * @param path 文件路径
     * @return 文件内容字节数组，失败返回null
     */
    public static byte[] readFileToByteArray(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (Exception e) {
            logger.warn("Failed to read file to byte array: {}", path, e);
            return null;
        }
    }

    /**
     * 逐行读取文件内容
     *
     * @param path 文件路径
     * @return 文件行列表，失败返回空列表
     */
    public static List<String> readLines(String path) {
        return readLines(path, DEFAULT_CHARSET);
    }

    /**
     * 逐行读取文件内容（指定字符集）
     *
     * @param path    文件路径
     * @param charset 字符集
     * @return 文件行列表，失败返回空列表
     */
    public static List<String> readLines(String path, Charset charset) {
        if (path == null || path.isEmpty()) {
            return new ArrayList<>();
        }

        try (Stream<String> lines = Files.lines(Paths.get(path), charset)) {
            return lines.collect(Collectors.toList());
        } catch (Exception e) {
            logger.warn("Failed to read lines from file: {}", path, e);
            return new ArrayList<>();
        }
    }

    /**
     * 将字符串写入文件
     *
     * @param path    文件路径
     * @param content 文件内容
     * @return 是否写入成功
     */
    public static boolean writeStringToFile(String path, String content) {
        return writeStringToFile(path, content, DEFAULT_CHARSET, false);
    }

    /**
     * 将字符串写入文件（指定字符集和追加模式）
     *
     * @param path    文件路径
     * @param content 文件内容
     * @param charset 字符集
     * @param append  是否追加
     * @return 是否写入成功
     */
    public static boolean writeStringToFile(String path, String content, Charset charset, boolean append) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        try {
            Path filePath = Paths.get(path);
            createParentDirectories(path);

            if (append) {
                Files.write(filePath, content.getBytes(charset), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } else {
                Files.write(filePath, content.getBytes(charset), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            return true;
        } catch (Exception e) {
            logger.warn("Failed to write string to file: {}", path, e);
            return false;
        }
    }

    /**
     * 将字节数组写入文件
     *
     * @param path     文件路径
     * @param contents 文件内容
     * @return 是否写入成功
     */
    public static boolean writeByteArrayToFile(String path, byte[] contents) {
        return writeByteArrayToFile(path, contents, false);
    }

    /**
     * 将字节数组写入文件（指定追加模式）
     *
     * @param path     文件路径
     * @param contents 文件内容
     * @param append   是否追加
     * @return 是否写入成功
     */
    public static boolean writeByteArrayToFile(String path, byte[] contents, boolean append) {
        if (path == null || path.isEmpty() || contents == null) {
            return false;
        }

        try {
            Path filePath = Paths.get(path);
            createParentDirectories(path);

            if (append) {
                Files.write(filePath, contents, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } else {
                Files.write(filePath, contents, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            return true;
        } catch (Exception e) {
            logger.warn("Failed to write byte array to file: {}", path, e);
            return false;
        }
    }

    /**
     * 逐行写入文件
     *
     * @param path  文件路径
     * @param lines 文件行列表
     * @return 是否写入成功
     */
    public static boolean writeLines(String path, List<String> lines) {
        return writeLines(path, lines, DEFAULT_CHARSET, false);
    }

    /**
     * 逐行写入文件（指定字符集和追加模式）
     *
     * @param path    文件路径
     * @param lines   文件行列表
     * @param charset 字符集
     * @param append  是否追加
     * @return 是否写入成功
     */
    public static boolean writeLines(String path, List<String> lines, Charset charset, boolean append) {
        if (path == null || path.isEmpty() || lines == null) {
            return false;
        }

        try {
            Path filePath = Paths.get(path);
            createParentDirectories(path);

            if (append) {
                Files.write(filePath, lines, charset, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } else {
                Files.write(filePath, lines, charset, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            return true;
        } catch (Exception e) {
            logger.warn("Failed to write lines to file: {}", path, e);
            return false;
        }
    }

    /**
     * 创建目录（包括父目录）
     *
     * @param path 目录路径
     * @return 是否创建成功
     */
    public static boolean createDirectory(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        try {
            Files.createDirectories(Paths.get(path));
            return true;
        } catch (Exception e) {
            logger.warn("Failed to create directory: {}", path, e);
            return false;
        }
    }

    /**
     * 创建父目录
     *
     * @param path 文件或目录路径
     * @return 是否创建成功
     */
    public static boolean createParentDirectories(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        try {
            Path parent = Paths.get(path).getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            return true;
        } catch (Exception e) {
            logger.warn("Failed to create parent directories for: {}", path, e);
            return false;
        }
    }

    /**
     * 删除文件或目录
     *
     * @param path 文件或目录路径
     * @return 是否删除成功
     */
    public static boolean delete(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        try {
            Path filePath = Paths.get(path);
            if (Files.isDirectory(filePath)) {
                // 递归删除目录
                Files.walkFileTree(filePath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                Files.deleteIfExists(filePath);
            }
            return true;
        } catch (Exception e) {
            logger.warn("Failed to delete path: {}", path, e);
            return false;
        }
    }

    /**
     * 复制文件或目录
     *
     * @param source 源路径
     * @param target 目标路径
     * @return 是否复制成功
     */
    public static boolean copy(String source, String target) {
        return copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 复制文件或目录（指定复制选项）
     *
     * @param source      源路径
     * @param target      目标路径
     * @param copyOptions 复制选项
     * @return 是否复制成功
     */
    public static boolean copy(String source, String target, CopyOption... copyOptions) {
        if (source == null || source.isEmpty() || target == null || target.isEmpty()) {
            return false;
        }

        try {
            Path sourcePath = Paths.get(source);
            Path targetPath = Paths.get(target);

            createParentDirectories(target);

            if (Files.isDirectory(sourcePath)) {
                // 复制目录
                Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        Path targetDir = targetPath.resolve(sourcePath.relativize(dir));
                        Files.createDirectories(targetDir);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path targetFile = targetPath.resolve(sourcePath.relativize(file));
                        Files.copy(file, targetFile, copyOptions);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                // 复制文件
                Files.copy(sourcePath, targetPath, copyOptions);
            }

            return true;
        } catch (Exception e) {
            logger.warn("Failed to copy from {} to {}", source, target, e);
            return false;
        }
    }

    /**
     * 移动文件或目录
     *
     * @param source 源路径
     * @param target 目标路径
     * @return 是否移动成功
     */
    public static boolean move(String source, String target) {
        return move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 移动文件或目录（指定移动选项）
     *
     * @param source      源路径
     * @param target      目标路径
     * @param moveOptions 移动选项
     * @return 是否移动成功
     */
    public static boolean move(String source, String target, CopyOption... moveOptions) {
        if (source == null || source.isEmpty() || target == null || target.isEmpty()) {
            return false;
        }

        try {
            Path sourcePath = Paths.get(source);
            Path targetPath = Paths.get(target);

            createParentDirectories(target);
            Files.move(sourcePath, targetPath, moveOptions);
            return true;
        } catch (Exception e) {
            logger.warn("Failed to move from {} to {}", source, target, e);
            return false;
        }
    }

    /**
     * 获取目录下的所有文件和子目录
     *
     * @param path 目录路径
     * @return 文件和子目录列表，失败返回空列表
     */
    public static List<String> listFiles(String path) {
        return listFiles(path, null, false);
    }

    /**
     * 获取目录下的文件和子目录（支持过滤和递归）
     *
     * @param path      目录路径
     * @param filter    过滤条件
     * @param recursive 是否递归
     * @return 文件和子目录列表，失败返回空列表
     */
    public static List<String> listFiles(String path, Predicate<Path> filter, boolean recursive) {
        if (path == null || path.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            Path dirPath = Paths.get(path);
            if (!Files.isDirectory(dirPath)) {
                return new ArrayList<>();
            }

            List<String> result = new ArrayList<>();
            if (recursive) {
                Files.walk(dirPath)
                        .filter(p -> !p.equals(dirPath)) // 排除目录本身
                        .filter(filter == null ? p -> true : filter)
                        .forEach(p -> result.add(p.toString()));
            } else {
                try (Stream<Path> stream = Files.list(dirPath)) {
                    stream.filter(filter == null ? p -> true : filter)
                            .forEach(p -> result.add(p.toString()));
                }
            }

            return result;
        } catch (Exception e) {
            logger.warn("Failed to entity files inspection directory: {}", path, e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param path 文件路径
     * @return 文件扩展名（不包含点号），无扩展名返回空字符串
     */
    public static String getFileExtension(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        try {
            String fileName = Paths.get(path).getFileName().toString();
            int dotIndex = fileName.lastIndexOf('.');
            return dotIndex > 0 ? fileName.substring(dotIndex + 1) : "";
        } catch (Exception e) {
            logger.warn("Failed to get file extension: {}", path, e);
            return "";
        }
    }

    /**
     * 获取不带扩展名的文件名
     *
     * @param path 文件路径
     * @return 不带扩展名的文件名
     */
    public static String getBaseName(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        try {
            String fileName = Paths.get(path).getFileName().toString();
            int dotIndex = fileName.lastIndexOf('.');
            return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
        } catch (Exception e) {
            logger.warn("Failed to get base name: {}", path, e);
            return "";
        }
    }

    /**
     * 获取文件名
     *
     * @param path 文件路径
     * @return 文件名
     */
    public static String getFileName(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        try {
            return Paths.get(path).getFileName().toString();
        } catch (Exception e) {
            logger.warn("Failed to get file name: {}", path, e);
            return "";
        }
    }

    /**
     * 获取父目录路径
     *
     * @param path 文件或目录路径
     * @return 父目录路径
     */
    public static String getParentPath(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        try {
            Path parent = Paths.get(path).getParent();
            return parent != null ? parent.toString() : null;
        } catch (Exception e) {
            logger.warn("Failed to get parent path: {}", path, e);
            return null;
        }
    }

    /**
     * 规范化路径
     *
     * @param path 路径
     * @return 规范化后的路径
     */
    public static String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }

        try {
            return Paths.get(path).normalize().toString();
        } catch (Exception e) {
            logger.warn("Failed to normalize path: {}", path, e);
            return path;
        }
    }

    /**
     * 检查路径是否安全（防止路径遍历攻击）
     *
     * @param path 路径
     * @return 是否安全
     */
    public static boolean isPathSafe(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        // 检查是否包含非法字符
        for (char c : path.toCharArray()) {
            if (ILLEGAL_CHARACTERS.contains(c)) {
                return false;
            }
        }

        // 检查是否包含路径遍历字符
        if (path.contains("..") || path.contains("./") || path.contains(".\\")) {
            return false;
        }

        return true;
    }

    /**
     * 创建临时文件
     *
     * @param prefix 文件名前缀
     * @param suffix 文件扩展名
     * @return 临时文件路径，失败返回null
     */
    public static String createTempFile(String prefix, String suffix) {
        try {
            Path tempFile = Files.createTempFile(prefix, suffix);
            return tempFile.toString();
        } catch (Exception e) {
            logger.warn("Failed to create temporary file with prefix: {} and suffix: {}", prefix, suffix, e);
            return null;
        }
    }

    /**
     * 创建临时目录
     *
     * @param prefix 目录名前缀
     * @return 临时目录路径，失败返回null
     */
    public static String createTempDirectory(String prefix) {
        try {
            Path tempDir = Files.createTempDirectory(prefix);
            return tempDir.toString();
        } catch (Exception e) {
            logger.warn("Failed to create temporary directory with prefix: {}", prefix, e);
            return null;
        }
    }

    /**
     * 计算文件MD5摘要
     *
     * @param path 文件路径
     * @return MD5摘要（十六进制字符串），失败返回null
     */
    public static String calculateMD5(String path) {
        return calculateDigest(path, "MD5");
    }

    /**
     * 计算文件SHA-256摘要
     *
     * @param path 文件路径
     * @return SHA-256摘要（十六进制字符串），失败返回null
     */
    public static String calculateSHA256(String path) {
        return calculateDigest(path, "SHA-256");
    }

    /**
     * 计算文件摘要
     *
     * @param path      文件路径
     * @param algorithm 摘要算法
     * @return 摘要（十六进制字符串），失败返回null
     */
    private static String calculateDigest(String path, String algorithm) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        try (InputStream is = Files.newInputStream(Paths.get(path))) {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int read;

            while ((read = is.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }

            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            logger.warn("Failed to calculate {} digest for file: {}", algorithm, path, e);
            return null;
        }
    }

    /**
     * 锁定文件
     *
     * @param path 文件路径
     * @return 是否锁定成功
     */
    public static boolean lockFile(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        try {
            Path filePath = Paths.get(path);
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }

            FileChannel channel = FileChannel.open(filePath, StandardOpenOption.WRITE);
            FileLock lock = channel.tryLock();

            if (lock != null) {
                FILE_LOCKS.put(path, lock);
                return true;
            }
        } catch (Exception e) {
            logger.warn("Failed to lock file: {}", path, e);
        }

        return false;
    }

    /**
     * 解锁文件
     *
     * @param path 文件路径
     * @return 是否解锁成功
     */
    public static boolean unlockFile(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        FileLock lock = FILE_LOCKS.get(path);
        if (lock != null) {
            try {
                lock.release();
                FILE_LOCKS.remove(path);
                return true;
            } catch (Exception e) {
                logger.warn("Failed to unlock file: {}", path, e);
            }
        }

        return false;
    }

    /**
     * 获取系统临时目录
     *
     * @return 系统临时目录路径
     */
    public static String getTempDirectory() {
        return TEMP_DIR;
    }

    /**
     * 获取用户主目录
     *
     * @return 用户主目录路径
     */
    public static String getUserHomeDirectory() {
        return System.getProperty("user.home");
    }

    /**
     * 获取当前工作目录
     *
     * @return 当前工作目录路径
     */
    public static String getCurrentWorkingDirectory() {
        return System.getProperty("user.dir");
    }

    /**
     * 检查磁盘空间
     *
     * @param path 路径
     * @return 可用空间（字节），失败返回-1
     */
    public static long getUsableSpace(String path) {
        if (path == null || path.isEmpty()) {
            return -1;
        }

        try {
            return Paths.get(path).toFile().getUsableSpace();
        } catch (Exception e) {
            logger.warn("Failed to get usable space for path: {}", path, e);
            return -1;
        }
    }

    /**
     * 检查磁盘总空间
     *
     * @param path 路径
     * @return 总空间（字节），失败返回-1
     */
    public static long getTotalSpace(String path) {
        if (path == null || path.isEmpty()) {
            return -1;
        }

        try {
            return Paths.get(path).toFile().getTotalSpace();
        } catch (Exception e) {
            logger.warn("Failed to get total space for path: {}", path, e);
            return -1;
        }
    }

    /**
     * 检查磁盘已用空间
     *
     * @param path 路径
     * @return 已用空间（字节），失败返回-1
     */
    public static long getUsedSpace(String path) {
        if (path == null || path.isEmpty()) {
            return -1;
        }

        long total = getTotalSpace(path);
        long usable = getUsableSpace(path);

        if (total >= 0 && usable >= 0) {
            return total - usable;
        }

        return -1;
    }
}
