package com.im.storage;

import com.im.storage.v1.StorageServiceV1;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/10
 */

public class StorageRouter {

    private final Map<String, StorageServiceV1> v1Impls = new ConcurrentHashMap<>();

    public void registerV1(StorageType type, StorageServiceV1 service) {
        v1Impls.put(type.name(), service);
    }

    public StorageServiceV1 getV1(StorageType type) {
        return v1Impls.get(type.name());
    }
}
