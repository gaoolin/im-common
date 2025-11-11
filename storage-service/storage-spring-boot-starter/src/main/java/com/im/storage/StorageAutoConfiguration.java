package com.im.storage;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/10
 */

@Configuration
@Import({CephClientConfig.class}) // 实际可根据 im.storage.type 决定导入哪个配置
public class StorageAutoConfiguration {
    // 如果需要，提供条件化的 bean 创建，或在 web 模块注入 CephS3StorageService bean
}
