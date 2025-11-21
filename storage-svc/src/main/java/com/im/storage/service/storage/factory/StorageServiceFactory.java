package com.im.storage.service.storage.factory;

import com.im.storage.service.StorageService;
import com.im.storage.service.storage.StorageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 存储服务工厂
 * 用于根据存储类型获取对应的存储服务实现
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/07
 */
@Component
public class StorageServiceFactory {

    @Autowired
    private Map<String, StorageService> storageServices;

    /**
     * 根据存储类型获取存储服务
     *
     * @param type 存储类型
     * @return 存储服务实现
     */
    public StorageService getStorageService(StorageType type) {
        String beanName = type.getType() + "StorageService";
        StorageService storageService = storageServices.get(beanName);
        if (storageService == null) {
            throw new IllegalArgumentException("Unsupported storage type: " + type);
        }
        return storageService;
    }

    /**
     * 根据存储类型字符串获取存储服务
     *
     * @param type 存储类型字符串
     * @return 存储服务实现
     */
    public StorageService getStorageService(String type) {
        return getStorageService(StorageType.fromString(type));
    }
}