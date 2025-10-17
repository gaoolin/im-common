package org.im.config.security;

import org.im.config.ConfigSource;

import java.util.Map;
import java.util.Set;

/**
 * 加密配置源装饰器
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/16
 */

public class EncryptedConfigSource implements ConfigSource {
    private final ConfigSource delegate;
    private final CryptoService cryptoService;

    public EncryptedConfigSource(ConfigSource delegate, CryptoService cryptoService) {
        this.delegate = delegate;
        this.cryptoService = cryptoService;
    }

    @Override
    public String getProperty(String key) {
        String encryptedValue = delegate.getProperty(key);
        if (encryptedValue != null && encryptedValue.startsWith("ENC(") && encryptedValue.endsWith(")")) {
            String encryptedData = encryptedValue.substring(4, encryptedValue.length() - 1);
            return cryptoService.decrypt(encryptedData);
        }
        return encryptedValue;
    }

    // 委托其他方法到原始配置源
    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Map<String, String> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public Set<String> getPropertyNames() {
        return delegate.getPropertyNames();
    }

    @Override
    public void refresh() {
        delegate.refresh();
    }

    @Override
    public boolean isAvailable() {
        return delegate.isAvailable();
    }

    @Override
    public int getPriority() {
        return delegate.getPriority();
    }
}