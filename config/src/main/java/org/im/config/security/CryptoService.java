package org.im.config.security;

/**
 * 加密服务接口
 * 定义加密和解密操作
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/16
 */
public interface CryptoService {

    /**
     * 加密明文
     *
     * @param plainText 明文
     * @return 加密后的密文
     */
    String encrypt(String plainText);

    /**
     * 解密密文
     *
     * @param encryptedText 密文
     * @return 解密后的明文
     */
    String decrypt(String encryptedText);
}