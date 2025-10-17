package org.im.config.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * 标准加密服务实现
 * 提供对称加密和解密功能
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/16
 */
public class StandardCryptoService implements CryptoService {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    private final SecretKey secretKey;

    /**
     * 使用默认密钥创建加密服务
     */
    public StandardCryptoService() {
        // 在实际应用中，应该从安全的地方获取密钥，而不是硬编码
        this.secretKey = generateKey();
    }

    /**
     * 使用指定密钥创建加密服务
     *
     * @param keyBase64 Base64编码的密钥
     */
    public StandardCryptoService(String keyBase64) {
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * 生成随机密钥
     *
     * @return 生成的密钥
     */
    private SecretKey generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(128);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate key", e);
        }
    }

    @Override
    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * 获取Base64编码的密钥字符串，用于存储或传输
     *
     * @return Base64编码的密钥
     */
    public String getKeyAsBase64() {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
}