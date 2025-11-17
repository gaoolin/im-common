package com.im.inspection.util;

import com.google.gson.Gson;
import org.im.common.exception.type.common.BusinessException;
import org.im.exception.constants.ErrorCode;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CustomClassLoader extends ClassLoader {
    private PublicKey publicKey;
    private TokenData tokenData;
    private SecretKeySpec aesKeySpec;
    private JarFile jarFile;

    public CustomClassLoader(ClassLoader parent, Path publicKeyPath, Path tokenFilePath, JarFile jarFile) throws Exception {
        super(parent);
        this.jarFile = jarFile;
        loadPublicKey(publicKeyPath);
        loadTokenData(tokenFilePath);
        validateToken();
    }

    private void loadPublicKey(Path publicKeyPath) throws Exception {
        byte[] keyBytes = Files.readAllBytes(publicKeyPath);
        byte[] decodedKey = Base64.getDecoder().decode(keyBytes);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
        this.publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private void loadTokenData(Path tokenFilePath) throws Exception {
        byte[] tokenContent = Files.readAllBytes(tokenFilePath);
        byte[] encryptedAesKey = new byte[256]; // RSA 密钥大小为 2048 位 = 256 字节
        byte[] aesKeyBytes = new byte[16]; // AES 密钥大小为 128 位 = 16 字节

        System.arraycopy(tokenContent, 0, encryptedAesKey, 0, encryptedAesKey.length);
        System.arraycopy(tokenContent, encryptedAesKey.length, aesKeyBytes, 0, aesKeyBytes.length);

        byte[] aesKey = decryptAesKey(encryptedAesKey);
        this.aesKeySpec = new SecretKeySpec(aesKey, "AES");

        this.tokenData = new Gson().fromJson(new String(aesKeyBytes), TokenData.class);
    }

    private byte[] decryptAesKey(byte[] encryptedAesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        return cipher.doFinal(encryptedAesKey);
    }

    private void validateToken() throws Exception {
        if (new Date().after(this.tokenData.getExpirationDate())) {
            throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED, "Token has expired.");
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] classBytes = loadClassBytes(name);
            if (classBytes == null) {
                throw new ClassNotFoundException("Class not found: " + name);
            }
            return defineClass(name, classBytes, 0, classBytes.length);
        } catch (Exception e) {
            throw new ClassNotFoundException("Class not found: " + name, e);
        }
    }

    private byte[] loadClassBytes(String name) throws Exception {
        String entryName = name.replace('.', '/') + ".class";
        JarEntry entry = jarFile.getJarEntry(entryName);
        if (entry != null) {
            try (InputStream inputStream = jarFile.getInputStream(entry)) {
                byte[] encryptedBytes = readAllBytes(inputStream);
                return decryptClassBytes(encryptedBytes);
            }
        }
        return null;
    }

    private byte[] decryptClassBytes(byte[] encryptedBytes) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, aesKeySpec);
        return cipher.doFinal(encryptedBytes);
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len;
        while ((len = inputStream.read(data)) != -1) {
            buffer.write(data, 0, len);
        }
        return buffer.toByteArray();
    }
}