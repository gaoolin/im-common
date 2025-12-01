package com.qtech.im.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

/**
 * 访问控制管理器
 * <p>
 * 特性：
 * - 通用性：支持多种认证方式和权限模型
 * - 规范性：统一的安全接口和标准流程
 * - 专业性：基于行业标准的安全算法实现
 * - 灵活性：可配置的认证策略和权限控制
 * - 可靠性：完善的会话管理和安全审计
 * - 安全性：多层安全防护和加密机制
 * - 复用性：模块化设计，组件可独立使用
 * - 容错性：优雅的错误处理和恢复机制
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/08/21
 */
public class AccessController {

    // 默认加密算法
    public static final String DEFAULT_HASH_ALGORITHM = "SHA-256";
    public static final String DEFAULT_ENCRYPTION_ALGORITHM = "AES";
    public static final int DEFAULT_KEY_SIZE = 256;
    // 默认会话超时时间（毫秒）
    public static final long DEFAULT_SESSION_TIMEOUT = 30 * 60 * 1000; // 30分钟
    // 默认密码策略
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final long LOCKOUT_DURATION = 30 * 60 * 1000; // 30分钟
    private static final Logger logger = LoggerFactory.getLogger(AccessController.class);
    // 会话管理器
    private static final SessionManager sessionManager = new SessionManager();
    // 用户存储（实际应用中应使用数据库）
    private static final Map<String, User> userStore = new ConcurrentHashMap<>();
    // 密码错误记录
    private static final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();
    // 审计日志存储（实际应用中应使用专门的日志系统）
    private static final List<AuditLog> auditLogs = new CopyOnWriteArrayList<>();
    // 定时任务执行器
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    // 安全配置
    private static SecurityConfig securityConfig = new SecurityConfig();

    // 初始化安全管理器
    static {
        // 启动会话清理任务
        scheduler.scheduleAtFixedRate(sessionManager::cleanupExpiredSessions,
                60, 60, TimeUnit.SECONDS);

        // 启动登录尝试清理任务
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            loginAttempts.entrySet().removeIf(entry ->
                    now - entry.getValue().getLastAttemptTime() > LOCKOUT_DURATION);
        }, 30, 30, TimeUnit.MINUTES);

        logger.info("AccessController initialized with default configuration");
    }

    /**
     * 更新安全配置
     *
     * @param config 安全配置
     */
    public static void updateSecurityConfig(SecurityConfig config) {
        if (config != null) {
            securityConfig = config;
            logger.info("Security configuration updated");
        }
    }

    /**
     * 用户身份验证
     *
     * @param username  用户名
     * @param password  密码
     * @param ipAddress IP地址
     * @return 认证结果
     */
    public static AuthenticationResult authenticateUser(String username, String password, String ipAddress) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            String message = "Invalid username or password";
            logger.warn("Authentication failed: {}", message);
            recordAuditLog(null, "AUTHENTICATE", "USER", message, ipAddress, false);
            return new AuthenticationResult(false, null, null, message);
        }

        try {
            // 检查账户是否被锁定
            if (isAccountLocked(username)) {
                String message = "Account is locked";
                logger.warn("Authentication failed for user {}: {}", username, message);
                recordAuditLog(username, "AUTHENTICATE", "USER", message, ipAddress, false);
                return new AuthenticationResult(false, null, username, message);
            }

            // 获取用户信息
            User user = userStore.get(username);
            if (user == null) {
                String message = "User not found";
                logger.warn("Authentication failed for user {}: {}", username, message);
                recordLoginAttempt(username);
                recordAuditLog(username, "AUTHENTICATE", "USER", message, ipAddress, false);
                return new AuthenticationResult(false, null, username, message);
            }

            // 验证密码
            String hashedPassword = hashPassword(password);
            if (!hashedPassword.equals(user.getHashedPassword())) {
                String message = "Invalid password";
                logger.warn("Authentication failed for user {}: {}", username, message);
                recordLoginAttempt(username);
                recordAuditLog(username, "AUTHENTICATE", "USER", message, ipAddress, false);
                return new AuthenticationResult(false, null, username, message);
            }

            // 检查账户是否被管理员锁定
            if (user.isLocked()) {
                String message = "Account is administratively locked";
                logger.warn("Authentication failed for user {}: {}", username, message);
                recordAuditLog(username, "AUTHENTICATE", "USER", message, ipAddress, false);
                return new AuthenticationResult(false, null, username, message);
            }

            // 认证成功，清除登录尝试记录
            clearLoginAttempts(username);

            // 创建会话
            String sessionId = null;
            if (securityConfig.isEnableSessionManagement()) {
                Session session = sessionManager.createSession(user.getUserId(), ipAddress);
                sessionId = session.getSessionId();
            }

            String message = "Authentication successful";
            logger.info("User {} authenticated successfully", username);
            recordAuditLog(username, "AUTHENTICATE", "USER", message, ipAddress, true);

            return new AuthenticationResult(true, sessionId, user.getUserId(), message);
        } catch (Exception e) {
            String message = "Authentication error: " + e.getMessage();
            logger.error("Authentication error for user {}: {}", username, message, e);
            recordAuditLog(username, "AUTHENTICATE", "USER", message, ipAddress, false);
            return new AuthenticationResult(false, null, username, message);
        }
    }

    /**
     * 检查账户是否被锁定
     *
     * @param username 用户名
     * @return 是否被锁定
     */
    private static boolean isAccountLocked(String username) {
        LoginAttempt attempt = loginAttempts.get(username);
        if (attempt != null) {
            // 检查是否还在锁定期内
            if (attempt.isLocked() &&
                    System.currentTimeMillis() - attempt.getLastAttemptTime() < securityConfig.getLockoutDuration()) {
                return true;
            }
            // 超过锁定时间，清除记录
            if (attempt.isLocked()) {
                loginAttempts.remove(username);
            }
        }
        return false;
    }

    /**
     * 记录登录尝试
     *
     * @param username 用户名
     */
    private static void recordLoginAttempt(String username) {
        LoginAttempt attempt = loginAttempts.computeIfAbsent(username, k -> new LoginAttempt());
        attempt.incrementAttemptCount();
        attempt.updateLastAttemptTime();

        // 检查是否达到最大尝试次数
        if (attempt.getAttemptCount() >= securityConfig.getMaxLoginAttempts()) {
            attempt.setLocked(true);
            logger.warn("Account {} locked due to excessive login attempts", username);
        }
    }

    /**
     * 清除登录尝试记录
     *
     * @param username 用户名
     */
    private static void clearLoginAttempts(String username) {
        loginAttempts.remove(username);
    }

    /**
     * 访问权限检查
     *
     * @param sessionId  会话ID
     * @param resource   资源
     * @param permission 权限类型
     * @return 权限检查结果
     */
    public static PermissionCheckResult checkPermission(String sessionId, String resource, PermissionType permission) {
        if (sessionId == null || sessionId.isEmpty() || resource == null || resource.isEmpty() || permission == null) {
            String message = "Invalid parameters";
            logger.warn("Permission check failed: {}", message);
            return new PermissionCheckResult(false, message);
        }

        try {
            // 验证会话
            Session session = sessionManager.getSession(sessionId);
            if (session == null || session.isExpired()) {
                String message = "Invalid or expired session";
                logger.warn("Permission check failed: {}", message);
                return new PermissionCheckResult(false, message);
            }

            // 更新会话最后访问时间
            session.updateLastAccessTime();

            // 获取用户信息
            User user = userStore.get(session.getUserId());
            if (user == null) {
                String message = "User not found";
                logger.warn("Permission check failed: {}", message);
                return new PermissionCheckResult(false, message);
            }

            // 检查权限（简化实现，实际应用中应根据具体业务逻辑检查）
            boolean granted = checkUserPermission(user, resource, permission);

            String message = granted ? "Permission granted" : "Permission denied";
            logger.debug("Permission check for user {} on resource {}: {}",
                    user.getUsername(), resource, message);

            // 记录审计日志
            recordAuditLog(user.getUserId(), "CHECK_PERMISSION", resource,
                    "Permission: " + permission + ", Granted: " + granted,
                    session.getIpAddress(), granted);

            return new PermissionCheckResult(granted, message);
        } catch (Exception e) {
            String message = "Permission check error: " + e.getMessage();
            logger.error("Permission check error: {}", message, e);
            return new PermissionCheckResult(false, message);
        }
    }

    /**
     * 检查用户权限（简化实现）
     *
     * @param user       用户
     * @param resource   资源
     * @param permission 权限类型
     * @return 是否有权限
     */
    private static boolean checkUserPermission(User user, String resource, PermissionType permission) {
        // 简化实现：管理员角色拥有所有权限
        if (user.getRoles().contains("ADMIN")) {
            return true;
        }

        // 根据资源和权限类型检查（这里只是示例）
        switch (permission) {
            case READ:
                return user.getRoles().contains("USER") || user.getRoles().contains("READER");
            case WRITE:
                return user.getRoles().contains("USER") || user.getRoles().contains("WRITER");
            case EXECUTE:
                return user.getRoles().contains("USER") || user.getRoles().contains("EXECUTOR");
            case ADMIN:
                return user.getRoles().contains("ADMIN");
            default:
                return false;
        }
    }

    /**
     * 操作审计日志记录
     *
     * @param userId    用户ID
     * @param operation 操作类型
     * @param resource  资源
     * @param details   详细信息
     * @param ipAddress IP地址
     * @param success   是否成功
     * @return 是否记录成功
     */
    public static boolean recordOperation(String userId, String operation, String resource,
                                          String details, String ipAddress, boolean success) {
        if (!securityConfig.isEnableAuditLogging()) {
            return true; // 审计日志功能未启用，视为成功
        }

        try {
            recordAuditLog(userId, operation, resource, details, ipAddress, success);
            return true;
        } catch (Exception e) {
            logger.error("Failed to record operation audit log", e);
            return false;
        }
    }

    /**
     * 记录审计日志
     *
     * @param userId    用户ID
     * @param operation 操作类型
     * @param resource  资源
     * @param details   详细信息
     * @param ipAddress IP地址
     * @param success   是否成功
     */
    private static void recordAuditLog(String userId, String operation, String resource,
                                       String details, String ipAddress, boolean success) {
        if (!securityConfig.isEnableAuditLogging()) {
            return;
        }

        try {
            AuditLog auditLog = new AuditLog(userId, operation, resource, details, ipAddress, success);
            auditLogs.add(auditLog);

            // 实际应用中应该将日志写入专门的日志系统
            logger.info("Audit Log - User: {}, Operation: {}, Resource: {}, Success: {}",
                    userId, operation, resource, success);
        } catch (Exception e) {
            logger.error("Failed to record audit log", e);
        }
    }

    /**
     * 敏感数据加密保护
     *
     * @param sensitiveData 敏感数据
     * @return 加密数据
     */
    public static EncryptedData encryptSensitiveData(Object sensitiveData) {
        if (sensitiveData == null) {
            logger.warn("Invalid sensitive data for encryption");
            return null;
        }

        try {
            String dataString = sensitiveData.toString();
            byte[] dataBytes = dataString.getBytes(StandardCharsets.UTF_8);

            // 生成密钥
            KeyGenerator keyGenerator = KeyGenerator.getInstance(securityConfig.getEncryptionAlgorithm());
            keyGenerator.init(securityConfig.getKeySize());
            SecretKey secretKey = keyGenerator.generateKey();

            // 加密数据
            Cipher cipher = Cipher.getInstance(securityConfig.getEncryptionAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(dataBytes);

            logger.debug("Encrypted sensitive data, size: {} bytes", encryptedBytes.length);
            return new EncryptedData(encryptedBytes, securityConfig.getEncryptionAlgorithm());
        } catch (Exception e) {
            logger.error("Failed to encrypt sensitive data", e);
            return null;
        }
    }

    /**
     * 敏感数据解密
     *
     * @param encryptedData 加密数据
     * @param secretKey     密钥
     * @return 解密后的数据
     */
    public static String decryptSensitiveData(EncryptedData encryptedData, SecretKey secretKey) {
        if (encryptedData == null || secretKey == null) {
            logger.warn("Invalid parameters for decryption");
            return null;
        }

        try {
            Cipher cipher = Cipher.getInstance(encryptedData.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(encryptedData.getEncryptedBytes());

            String decryptedData = new String(decryptedBytes, StandardCharsets.UTF_8);
            logger.debug("Decrypted sensitive data, size: {} bytes", decryptedBytes.length);
            return decryptedData;
        } catch (Exception e) {
            logger.error("Failed to decrypt sensitive data", e);
            return null;
        }
    }

    /**
     * 密码哈希
     *
     * @param password 密码
     * @return 哈希值
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(securityConfig.getHashAlgorithm());
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // 转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Hash algorithm not available: {}", securityConfig.getHashAlgorithm(), e);
            throw new RuntimeException("Hash algorithm error", e);
        }
    }

    /**
     * 验证密码强度
     *
     * @param password 密码
     * @return 是否符合强度要求
     */
    public static boolean validatePasswordStrength(String password) {
        if (!securityConfig.isEnablePasswordPolicy()) {
            return true; // 密码策略未启用，视为通过
        }

        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }

        // 检查密码复杂度（至少包含大小写字母、数字和特殊字符）
        boolean hasUpper = Pattern.compile("[A-Z]").matcher(password).find();
        boolean hasLower = Pattern.compile("[a-z]").matcher(password).find();
        boolean hasDigit = Pattern.compile("[0-9]").matcher(password).find();
        boolean hasSpecial = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]").matcher(password).find();

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    /**
     * 创建用户
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param password 密码
     * @return 是否创建成功
     */
    public static boolean createUser(String userId, String username, String password) {
        if (userId == null || userId.isEmpty() ||
                username == null || username.isEmpty() ||
                password == null || password.isEmpty()) {
            logger.warn("Invalid user creation parameters");
            return false;
        }

        try {
            // 验证密码强度
            if (!validatePasswordStrength(password)) {
                logger.warn("Password does not meet strength requirements");
                return false;
            }

            // 检查用户是否已存在
            if (userStore.containsKey(username)) {
                logger.warn("User already exists: {}", username);
                return false;
            }

            // 哈希密码
            String hashedPassword = hashPassword(password);

            // 创建用户
            User user = new User(userId, username, hashedPassword);
            userStore.put(username, user);

            logger.info("User created successfully: {}", username);
            return true;
        } catch (Exception e) {
            logger.error("Failed to create user: {}", username, e);
            return false;
        }
    }

    /**
     * 锁定用户账户
     *
     * @param username 用户名
     * @return 是否锁定成功
     */
    public static boolean lockUserAccount(String username) {
        if (username == null || username.isEmpty()) {
            logger.warn("Invalid username for account locking");
            return false;
        }

        User user = userStore.get(username);
        if (user == null) {
            logger.warn("User not found for account locking: {}", username);
            return false;
        }

        user.setLocked(true);
        user.setLockoutTime(System.currentTimeMillis());
        logger.info("User account locked: {}", username);
        return true;
    }

    /**
     * 解锁用户账户
     *
     * @param username 用户名
     * @return 是否解锁成功
     */
    public static boolean unlockUserAccount(String username) {
        if (username == null || username.isEmpty()) {
            logger.warn("Invalid username for account unlocking");
            return false;
        }

        User user = userStore.get(username);
        if (user == null) {
            logger.warn("User not found for account unlocking: {}", username);
            return false;
        }

        user.setLocked(false);
        user.setLockoutTime(0);
        clearLoginAttempts(username); // 同时清除登录尝试记录
        logger.info("User account unlocked: {}", username);
        return true;
    }

    /**
     * 获取审计日志
     *
     * @param limit 限制数量
     * @return 审计日志列表
     */
    public static List<AuditLog> getAuditLogs(int limit) {
        if (limit <= 0) {
            return new ArrayList<>(auditLogs);
        }

        int size = auditLogs.size();
        int fromIndex = Math.max(0, size - limit);
        return new ArrayList<>(auditLogs.subList(fromIndex, size));
    }

    /**
     * 关闭访问控制器
     */
    public static void shutdown() {
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            logger.info("AccessController shutdown completed");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            logger.warn("AccessController shutdown interrupted");
        }
    }

    /**
     * 权限类型枚举
     */
    public enum PermissionType {
        READ("Read permission"),
        WRITE("Write permission"),
        EXECUTE("Execute permission"),
        ADMIN("Administrative permission");

        private final String description;

        PermissionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 安全配置类
     */
    public static class SecurityConfig {
        private String hashAlgorithm = DEFAULT_HASH_ALGORITHM;
        private String encryptionAlgorithm = DEFAULT_ENCRYPTION_ALGORITHM;
        private int keySize = DEFAULT_KEY_SIZE;
        private long sessionTimeout = DEFAULT_SESSION_TIMEOUT;
        private int maxLoginAttempts = MAX_LOGIN_ATTEMPTS;
        private long lockoutDuration = LOCKOUT_DURATION;
        private boolean enableAuditLogging = true;
        private boolean enableSessionManagement = true;
        private boolean enablePasswordPolicy = true;

        // Getters and Setters
        public String getHashAlgorithm() {
            return hashAlgorithm;
        }

        public SecurityConfig setHashAlgorithm(String hashAlgorithm) {
            this.hashAlgorithm = hashAlgorithm;
            return this;
        }

        public String getEncryptionAlgorithm() {
            return encryptionAlgorithm;
        }

        public SecurityConfig setEncryptionAlgorithm(String encryptionAlgorithm) {
            this.encryptionAlgorithm = encryptionAlgorithm;
            return this;
        }

        public int getKeySize() {
            return keySize;
        }

        public SecurityConfig setKeySize(int keySize) {
            this.keySize = keySize;
            return this;
        }

        public long getSessionTimeout() {
            return sessionTimeout;
        }

        public SecurityConfig setSessionTimeout(long sessionTimeout) {
            this.sessionTimeout = sessionTimeout;
            return this;
        }

        public int getMaxLoginAttempts() {
            return maxLoginAttempts;
        }

        public SecurityConfig setMaxLoginAttempts(int maxLoginAttempts) {
            this.maxLoginAttempts = maxLoginAttempts;
            return this;
        }

        public long getLockoutDuration() {
            return lockoutDuration;
        }

        public SecurityConfig setLockoutDuration(long lockoutDuration) {
            this.lockoutDuration = lockoutDuration;
            return this;
        }

        public boolean isEnableAuditLogging() {
            return enableAuditLogging;
        }

        public SecurityConfig setEnableAuditLogging(boolean enableAuditLogging) {
            this.enableAuditLogging = enableAuditLogging;
            return this;
        }

        public boolean isEnableSessionManagement() {
            return enableSessionManagement;
        }

        public SecurityConfig setEnableSessionManagement(boolean enableSessionManagement) {
            this.enableSessionManagement = enableSessionManagement;
            return this;
        }

        public boolean isEnablePasswordPolicy() {
            return enablePasswordPolicy;
        }

        public SecurityConfig setEnablePasswordPolicy(boolean enablePasswordPolicy) {
            this.enablePasswordPolicy = enablePasswordPolicy;
            return this;
        }
    }

    /**
     * 认证结果类
     */
    public static class AuthenticationResult {
        private final boolean success;
        private final String sessionId;
        private final String userId;
        private final String message;
        private final long timestamp;

        public AuthenticationResult(boolean success, String sessionId, String userId, String message) {
            this.success = success;
            this.sessionId = sessionId;
            this.userId = userId;
            this.message = message != null ? message : "";
            this.timestamp = System.currentTimeMillis();
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getUserId() {
            return userId;
        }

        public String getMessage() {
            return message;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return "AuthenticationResult{" +
                    "success=" + success +
                    ", userId='" + userId + '\'' +
                    ", message='" + message + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }

    /**
     * 权限检查结果类
     */
    public static class PermissionCheckResult {
        private final boolean granted;
        private final String message;
        private final long timestamp;

        public PermissionCheckResult(boolean granted, String message) {
            this.granted = granted;
            this.message = message != null ? message : "";
            this.timestamp = System.currentTimeMillis();
        }

        // Getters
        public boolean isGranted() {
            return granted;
        }

        public String getMessage() {
            return message;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return "PermissionCheckResult{" +
                    "granted=" + granted +
                    ", message='" + message + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }

    /**
     * 加密数据类
     */
    public static class EncryptedData {
        private final byte[] encryptedBytes;
        private final String algorithm;
        private final long timestamp;

        public EncryptedData(byte[] encryptedBytes, String algorithm) {
            this.encryptedBytes = encryptedBytes != null ? Arrays.copyOf(encryptedBytes, encryptedBytes.length) : new byte[0];
            this.algorithm = algorithm != null ? algorithm : "";
            this.timestamp = System.currentTimeMillis();
        }

        // Getters
        public byte[] getEncryptedBytes() {
            return Arrays.copyOf(encryptedBytes, encryptedBytes.length);
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return "EncryptedData{" +
                    "algorithm='" + algorithm + '\'' +
                    ", timestamp=" + timestamp +
                    ", dataSize=" + encryptedBytes.length + " bytes" +
                    '}';
        }
    }

    /**
     * 审计日志类
     */
    public static class AuditLog {
        private final String userId;
        private final String operation;
        private final String resource;
        private final String details;
        private final String ipAddress;
        private final long timestamp;
        private final boolean success;

        public AuditLog(String userId, String operation, String resource, String details,
                        String ipAddress, boolean success) {
            this.userId = userId != null ? userId : "";
            this.operation = operation != null ? operation : "";
            this.resource = resource != null ? resource : "";
            this.details = details != null ? details : "";
            this.ipAddress = ipAddress != null ? ipAddress : "";
            this.timestamp = System.currentTimeMillis();
            this.success = success;
        }

        // Getters
        public String getUserId() {
            return userId;
        }

        public String getOperation() {
            return operation;
        }

        public String getResource() {
            return resource;
        }

        public String getDetails() {
            return details;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public boolean isSuccess() {
            return success;
        }

        @Override
        public String toString() {
            return "AuditLog{" +
                    "userId='" + userId + '\'' +
                    ", operation='" + operation + '\'' +
                    ", resource='" + resource + '\'' +
                    ", ipAddress='" + ipAddress + '\'' +
                    ", timestamp=" + new Date(timestamp) +
                    ", success=" + success +
                    '}';
        }
    }

    /**
     * 用户类
     */
    public static class User {
        private final String userId;
        private final String username;
        private final String hashedPassword;
        private final Set<String> roles;
        private final Map<String, Object> attributes;
        private final long createTime;
        private volatile boolean locked;
        private volatile long lockoutTime;

        public User(String userId, String username, String hashedPassword) {
            this.userId = userId != null ? userId : "";
            this.username = username != null ? username : "";
            this.hashedPassword = hashedPassword != null ? hashedPassword : "";
            this.roles = ConcurrentHashMap.newKeySet();
            this.attributes = new ConcurrentHashMap<>();
            this.createTime = System.currentTimeMillis();
            this.locked = false;
            this.lockoutTime = 0;
        }

        // Getters and Setters
        public String getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getHashedPassword() {
            return hashedPassword;
        }

        public Set<String> getRoles() {
            return new HashSet<>(roles);
        }

        public void addRole(String role) {
            roles.add(role);
        }

        public void removeRole(String role) {
            roles.remove(role);
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public void setAttribute(String key, Object value) {
            attributes.put(key, value);
        }

        public Object getAttribute(String key) {
            return attributes.get(key);
        }

        public long getCreateTime() {
            return createTime;
        }

        public boolean isLocked() {
            return locked;
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
        }

        public long getLockoutTime() {
            return lockoutTime;
        }

        public void setLockoutTime(long lockoutTime) {
            this.lockoutTime = lockoutTime;
        }

        @Override
        public String toString() {
            return "User{" +
                    "userId='" + userId + '\'' +
                    ", username='" + username + '\'' +
                    ", roles=" + roles +
                    ", locked=" + locked +
                    '}';
        }
    }

    /**
     * 会话类
     */
    public static class Session {
        private final String sessionId;
        private final String userId;
        private final long createTime;
        private final String ipAddress;
        private final Map<String, Object> attributes;
        private volatile long lastAccessTime;

        public Session(String sessionId, String userId, String ipAddress) {
            this.sessionId = sessionId != null ? sessionId : UUID.randomUUID().toString();
            this.userId = userId != null ? userId : "";
            this.createTime = System.currentTimeMillis();
            this.lastAccessTime = createTime;
            this.ipAddress = ipAddress != null ? ipAddress : "";
            this.attributes = new ConcurrentHashMap<>();
        }

        // Getters and Setters
        public String getSessionId() {
            return sessionId;
        }

        public String getUserId() {
            return userId;
        }

        public long getCreateTime() {
            return createTime;
        }

        public long getLastAccessTime() {
            return lastAccessTime;
        }

        public void updateLastAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public Map<String, Object> getAttributes() {
            return new HashMap<>(attributes);
        }

        public void setAttribute(String key, Object value) {
            attributes.put(key, value);
        }

        public Object getAttribute(String key) {
            return attributes.get(key);
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - lastAccessTime > securityConfig.getSessionTimeout();
        }

        @Override
        public String toString() {
            return "Session{" +
                    "sessionId='" + sessionId + '\'' +
                    ", userId='" + userId + '\'' +
                    ", createTime=" + new Date(createTime) +
                    ", lastAccessTime=" + new Date(lastAccessTime) +
                    ", ipAddress='" + ipAddress + '\'' +
                    ", expired=" + isExpired() +
                    '}';
        }
    }

    /**
     * 登录尝试记录类
     */
    private static class LoginAttempt {
        private int attemptCount;
        private long lastAttemptTime;
        private boolean locked;

        public LoginAttempt() {
            this.attemptCount = 0;
            this.lastAttemptTime = System.currentTimeMillis();
            this.locked = false;
        }

        // Getters and Setters
        public int getAttemptCount() {
            return attemptCount;
        }

        public void incrementAttemptCount() {
            this.attemptCount++;
        }

        public long getLastAttemptTime() {
            return lastAttemptTime;
        }

        public void updateLastAttemptTime() {
            this.lastAttemptTime = System.currentTimeMillis();
        }

        public boolean isLocked() {
            return locked;
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
        }
    }

    /**
     * 会话管理器
     */
    private static class SessionManager {
        private final Map<String, Session> sessions = new ConcurrentHashMap<>();
        private final SecureRandom random = new SecureRandom();

        /**
         * 创建会话
         *
         * @param userId    用户ID
         * @param ipAddress IP地址
         * @return 会话对象
         */
        public Session createSession(String userId, String ipAddress) {
            String sessionId = generateSessionId();
            Session session = new Session(sessionId, userId, ipAddress);
            sessions.put(sessionId, session);
            logger.debug("Session created for user {}: {}", userId, sessionId);
            return session;
        }

        /**
         * 获取会话
         *
         * @param sessionId 会话ID
         * @return 会话对象
         */
        public Session getSession(String sessionId) {
            Session session = sessions.get(sessionId);
            if (session != null && session.isExpired()) {
                sessions.remove(sessionId);
                logger.debug("Expired session removed: {}", sessionId);
                return null;
            }
            return session;
        }

        /**
         * 销毁会话
         *
         * @param sessionId 会话ID
         * @return 是否销毁成功
         */
        public boolean destroySession(String sessionId) {
            Session session = sessions.remove(sessionId);
            if (session != null) {
                logger.debug("Session destroyed: {}", sessionId);
                return true;
            }
            return false;
        }

        /**
         * 清理过期会话
         */
        public void cleanupExpiredSessions() {
            long now = System.currentTimeMillis();
            int expiredCount = 0;

            Iterator<Map.Entry<String, Session>> iterator = sessions.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Session> entry = iterator.next();
                Session session = entry.getValue();
                if (session.isExpired()) {
                    iterator.remove();
                    expiredCount++;
                }
            }

            if (expiredCount > 0) {
                logger.debug("Cleaned up {} expired sessions", expiredCount);
            }
        }

        /**
         * 生成会话ID
         *
         * @return 会话ID
         */
        private String generateSessionId() {
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);

            try {
                MessageDigest digest = MessageDigest.getInstance(DEFAULT_HASH_ALGORITHM);
                byte[] hash = digest.digest(bytes);

                StringBuilder sb = new StringBuilder();
                for (byte b : hash) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            } catch (NoSuchAlgorithmException e) {
                // Fallback to UUID
                return UUID.randomUUID().toString();
            }
        }
    }
}
