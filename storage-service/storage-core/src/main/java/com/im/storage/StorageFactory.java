package com.im.storage;

import com.im.storage.v1.StorageServiceV1;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/10
 */

public interface StorageFactory {
    StorageServiceV1 create(StorageProperties properties);
}
