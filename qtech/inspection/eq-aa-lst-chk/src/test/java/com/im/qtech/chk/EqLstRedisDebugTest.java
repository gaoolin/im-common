package com.im.qtech.chk;

import com.im.aa.inspection.config.EqpLstRedisCacheConfig;
import com.im.aa.inspection.serde.EqLstProtobufMapper;
import com.im.qtech.common.dto.param.EqLstPOJO;
import org.im.cache.core.Cache;
import org.junit.Test;

import java.util.Base64;

/**
 * 用于调试Redis中实际存储的数据内容
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/10/10
 */
public class EqLstRedisDebugTest {

    @Test
    public void testPrintObjectFromRedis() {
        // 初始化缓存配置
        EqpLstRedisCacheConfig cacheConfig = new EqpLstRedisCacheConfig();
        Cache<String, String> cache = cacheConfig.getEqLstByteStringCache();

        // 使用提供的key获取数据（不要包含prefix）
        String key = "debug:test:EqLstPOJO:1760063145100";

        // 从Redis获取Base64编码的数据
        String base64Data = cache.get(key);

        if (base64Data != null) {
            System.out.println("Redis中存储的Base64数据:");
            System.out.println(base64Data);

            try {
                // 解码Base64数据
                byte[] decodedData = Base64.getDecoder().decode(base64Data);

                // 反序列化为Java对象
                EqLstPOJO obj = EqLstProtobufMapper.deserialize(decodedData);

                if (obj != null) {
                    System.out.println("\n完整对象内容:");
                    System.out.println(obj.toString());

                    System.out.println("\n对象字段详情:");
                    System.out.println("Module: " + obj.getModule());
                    System.out.println("Aa1: " + obj.getAa1());
                    System.out.println("Sid: " + obj.getSid());
                    // 可以添加更多关键字段的打印
                } else {
                    System.out.println("反序列化结果为null");
                }
            } catch (Exception e) {
                System.err.println("处理数据时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Redis中未找到key: " + key);
        }

        // 清理资源
        cache.close();
    }
}
