# IOKit 文件和路径操作工具类使用文档

## 1. 概述

[IOKit](file://E:\dossier\others\im-common\src\main\java\com\qtech\im\util\IOKit.java#L30-L582) 是一个专业的文件和路径操作工具类，提供了丰富且易用的文件系统操作功能。该工具类支持文件读写、复制、移动、删除、目录遍历、安全检查等操作，具备通用化、规范化、灵活性、容错性、安全性、可靠性和专业性等特点。

## 2. 核心特性

- **通用化**：支持文件读写、复制、移动、删除等常见操作
- **规范化**：统一的API接口和错误处理
- **灵活性**：支持多种编码格式、过滤条件、批量操作等
- **容错性**：完善的异常处理和恢复机制
- **安全性**：路径遍历防护、权限检查、文件锁定等安全机制
- **可靠性**：原子操作、资源自动释放、并发安全等
- **专业性**：遵循NIO.2规范和最佳实践

## 3. 基本配置

### 3.1 默认配置常量
```java
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
```


## 4. 文件状态检查

### 4.1 基本状态检查
```java
// 检查文件或目录是否存在
boolean exists = IOKit.exists("/path/to/file.txt");

// 检查是否为文件
boolean isFile = IOKit.isFile("/path/to/file.txt");

// 检查是否为目录
boolean isDirectory = IOKit.isDirectory("/path/to/directory");

// 检查文件权限
boolean readable = IOKit.isReadable("/path/to/file.txt");
boolean writable = IOKit.isWritable("/path/to/file.txt");
boolean executable = IOKit.isExecutable("/path/to/script.sh");
```


### 4.2 文件属性获取
```java
// 获取文件大小
long fileSize = IOKit.getFileSize("/path/to/file.txt");

// 获取最后修改时间
FileTime lastModified = IOKit.getLastModifiedTime("/path/to/file.txt");

// 获取可用磁盘空间
long usableSpace = IOKit.getUsableSpace("/path/to/directory");

// 获取总磁盘空间
long totalSpace = IOKit.getTotalSpace("/path/to/directory");

// 获取已用磁盘空间
long usedSpace = IOKit.getUsedSpace("/path/to/directory");
```


## 5. 文件读取操作

### 5.1 读取整个文件
```java
// 读取文件内容为字符串（默认UTF-8编码）
String content = IOKit.readFileToString("/path/to/file.txt");

// 读取文件内容为字符串（指定字符集）
String content = IOKit.readFileToString("/path/to/file.txt", StandardCharsets.UTF_8);

// 读取文件内容为字节数组
byte[] bytes = IOKit.readFileToByteArray("/path/to/file.txt");
```


### 5.2 逐行读取文件
```java
// 逐行读取文件（默认UTF-8编码）
List<String> lines = IOKit.readLines("/path/to/file.txt");

// 逐行读取文件（指定字符集）
List<String> lines = IOKit.readLines("/path/to/file.txt", StandardCharsets.UTF_8);
```


## 6. 文件写入操作

### 6.1 写入字符串到文件
```java
String content = "Hello, World!";

// 写入字符串到文件（覆盖模式）
boolean success = IOKit.writeStringToFile("/path/to/file.txt", content);

// 写入字符串到文件（指定字符集和追加模式）
boolean success = IOKit.writeStringToFile("/path/to/file.txt", content, 
                                          StandardCharsets.UTF_8, true); // 追加模式
```


### 6.2 写入字节数组到文件
```java
byte[] data = "Hello, World!".getBytes(StandardCharsets.UTF_8);

// 写入字节数组到文件（覆盖模式）
boolean success = IOKit.writeByteArrayToFile("/path/to/file.txt", data);

// 写入字节数组到文件（追加模式）
boolean success = IOKit.writeByteArrayToFile("/path/to/file.txt", data, true);
```


### 6.3 逐行写入文件
```java
List<String> lines = Arrays.asList("Line 1", "Line 2", "Line 3");

// 逐行写入文件（覆盖模式）
boolean success = IOKit.writeLines("/path/to/file.txt", lines);

// 逐行写入文件（指定字符集和追加模式）
boolean success = IOKit.writeLines("/path/to/file.txt", lines, 
                                  StandardCharsets.UTF_8, true); // 追加模式
```


## 7. 目录操作

### 7.1 创建目录
```java
// 创建目录（包括父目录）
boolean success = IOKit.createDirectory("/path/to/new/directory");

// 创建父目录
boolean success = IOKit.createParentDirectories("/path/to/new/file.txt");
```


### 7.2 列出目录内容
```java
// 获取目录下的所有文件和子目录
List<String> files = IOKit.listFiles("/path/to/directory");

// 获取目录下的文件（非递归，带过滤）
List<String> textFiles = IOKit.listFiles("/path/to/directory", 
    path -> path.toString().endsWith(".txt"), false);

// 获取目录下的所有文件（递归）
List<String> allFiles = IOKit.listFiles("/path/to/directory", null, true);

// 获取目录下的所有Java文件（递归）
List<String> javaFiles = IOKit.listFiles("/path/to/project", 
    path -> path.toString().endsWith(".java"), true);
```


## 8. 文件操作

### 8.1 删除文件或目录
```java
// 删除文件或目录（递归删除目录）
boolean success = IOKit.delete("/path/to/file_or_directory");
```


### 8.2 复制文件或目录
```java
// 复制文件或目录（默认覆盖）
boolean success = IOKit.copy("/path/to/source", "/path/to/target");

// 复制文件或目录（指定选项）
boolean success = IOKit.copy("/path/to/source", "/path/to/target", 
                            StandardCopyOption.REPLACE_EXISTING, 
                            StandardCopyOption.COPY_ATTRIBUTES);
```


### 8.3 移动文件或目录
```java
// 移动文件或目录（默认覆盖）
boolean success = IOKit.move("/path/to/source", "/path/to/target");

// 移动文件或目录（指定选项）
boolean success = IOKit.move("/path/to/source", "/path/to/target", 
                            StandardCopyOption.REPLACE_EXISTING);
```


## 9. 路径操作

### 9.1 路径信息提取
```java
String path = "/home/user/documents/file.txt";

// 获取文件扩展名
String extension = IOKit.getFileExtension(path); // "txt"

// 获取不带扩展名的文件名
String baseName = IOKit.getBaseName(path); // "file"

// 获取文件名
String fileName = IOKit.getFileName(path); // "file.txt"

// 获取父目录路径
String parentPath = IOKit.getParentPath(path); // "/home/user/documents"

// 规范化路径
String normalizedPath = IOKit.normalizePath(".././path//to/file.txt");
```


### 9.2 路径安全检查
```java
// 检查路径是否安全（防止路径遍历攻击）
boolean isSafe = IOKit.isPathSafe("../etc/passwd"); // false
boolean isSafe = IOKit.isPathSafe("safe/path/file.txt"); // true
```


## 10. 临时文件操作

### 10.1 创建临时文件和目录
```java
// 创建临时文件
String tempFile = IOKit.createTempFile("prefix", ".tmp");

// 创建临时目录
String tempDir = IOKit.createTempDirectory("prefix");

// 获取系统临时目录
String tempDirPath = IOKit.getTempDirectory();

// 获取用户主目录
String userHome = IOKit.getUserHomeDirectory();

// 获取当前工作目录
String currentDir = IOKit.getCurrentWorkingDirectory();
```


## 11. 文件安全操作

### 11.1 文件锁定
```java
// 锁定文件
boolean locked = IOKit.lockFile("/path/to/file.txt");

// 解锁文件
boolean unlocked = IOKit.unlockFile("/path/to/file.txt");
```


### 11.2 文件摘要计算
```java
// 计算文件MD5摘要
String md5 = IOKit.calculateMD5("/path/to/file.txt");

// 计算文件SHA-256摘要
String sha256 = IOKit.calculateSHA256("/path/to/file.txt");
```


## 12. 实际使用示例

### 12.1 配置文件读取示例
```java
public class ConfigManager {
    private Properties properties = new Properties();
    
    public void loadConfig(String configPath) {
        try {
            String content = IOKit.readFileToString(configPath);
            if (content != null) {
                properties.load(new StringReader(content));
            }
        } catch (Exception e) {
            System.err.println("Failed to load config: " + e.getMessage());
        }
    }
    
    public void saveConfig(String configPath) {
        try {
            StringWriter writer = new StringWriter();
            properties.store(writer, "Application Configuration");
            IOKit.writeStringToFile(configPath, writer.toString());
        } catch (Exception e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }
}
```


### 12.2 日志文件处理示例
```java
public class LogFileProcessor {
    public void rotateLogFile(String logPath, int maxFileSize) {
        long fileSize = IOKit.getFileSize(logPath);
        if (fileSize > maxFileSize) {
            String backupPath = logPath + ".backup";
            // 备份当前日志文件
            IOKit.copy(logPath, backupPath);
            // 清空原日志文件
            IOKit.writeStringToFile(logPath, "");
        }
    }
    
    public List<String> getErrorLogs(String logPath) {
        List<String> lines = IOKit.readLines(logPath);
        return lines.stream()
                   .filter(line -> line.contains("ERROR"))
                   .collect(Collectors.toList());
    }
}
```


### 12.3 文件批量操作示例
```java
public class FileBatchProcessor {
    public void copyFiles(List<String> sourceFiles, String targetDirectory) {
        IOKit.createDirectory(targetDirectory);
        
        for (String sourceFile : sourceFiles) {
            String fileName = IOKit.getFileName(sourceFile);
            String targetPath = targetDirectory + IOKit.FILE_SEPARATOR + fileName;
            
            if (!IOKit.copy(sourceFile, targetPath)) {
                System.err.println("Failed to copy: " + sourceFile);
            }
        }
    }
    
    public void deleteOldFiles(String directory, int daysOld) {
        List<String> files = IOKit.listFiles(directory, path -> {
            try {
                FileTime lastModified = Files.getLastModifiedTime(path);
                long diff = System.currentTimeMillis() - lastModified.toMillis();
                return diff > daysOld * 24 * 60 * 60 * 1000L;
            } catch (Exception e) {
                return false;
            }
        }, false);
        
        files.forEach(file -> IOKit.delete(file));
    }
}
```


### 12.4 安全文件操作示例
```java
public class SecureFileHandler {
    public boolean secureWrite(String path, String content) {
        // 检查路径安全性
        if (!IOKit.isPathSafe(path)) {
            System.err.println("Unsafe path: " + path);
            return false;
        }
        
        // 锁定文件
        if (!IOKit.lockFile(path)) {
            System.err.println("Failed to lock file: " + path);
            return false;
        }
        
        try {
            // 写入文件
            return IOKit.writeStringToFile(path, content);
        } finally {
            // 解锁文件
            IOKit.unlockFile(path);
        }
    }
    
    public String secureRead(String path) {
        // 检查路径安全性
        if (!IOKit.isPathSafe(path)) {
            System.err.println("Unsafe path: " + path);
            return null;
        }
        
        // 检查文件权限
        if (!IOKit.isReadable(path)) {
            System.err.println("File not readable: " + path);
            return null;
        }
        
        return IOKit.readFileToString(path);
    }
}
```


## 13. 最佳实践

### 13.1 错误处理
```java
// 总是检查返回值
String content = IOKit.readFileToString("/path/to/file.txt");
if (content == null) {
    // 处理读取失败的情况
    System.err.println("Failed to read file");
    return;
}

// 使用try-catch处理预期的异常情况
try {
    IOKit.writeStringToFile("/path/to/file.txt", "content");
} catch (Exception e) {
    // 处理写入失败的情况
    System.err.println("Failed to write file: " + e.getMessage());
}
```


### 13.2 资源管理
```java
// IOKit会自动管理资源，但要注意检查返回值
// 对于大文件操作，考虑使用流式处理
```


### 13.3 性能优化
```java
// 对于大量小文件操作，考虑批量处理
// 对于大文件，使用适当的缓冲区大小
// 合理使用文件锁定避免并发冲突
```


### 13.4 安全建议
```java
// 始终验证用户输入的路径
if (!IOKit.isPathSafe(userPath)) {
    throw new SecurityException("Unsafe path");
}

// 检查文件权限
if (!IOKit.isWritable(filePath)) {
    throw new SecurityException("File not writable");
}
```


这个文档涵盖了 [IOKit](file://E:\dossier\others\im-common\src\main\java\com\qtech\im\util\IOKit.java#L30-L582) 文件和路径操作工具类的主要功能和使用方法，可以帮助开发者快速上手并正确使用该工具类。