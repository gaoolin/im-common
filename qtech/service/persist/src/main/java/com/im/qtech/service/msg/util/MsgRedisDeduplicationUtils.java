package com.im.qtech.service.msg.util;

import lombok.extern.slf4j.Slf4j;
import org.im.common.dt.Chronos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/04/29 16:18:43
 */

@Slf4j
@Service
public class MsgRedisDeduplicationUtils {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 构造带有 Redis Hash 标签的 key（避免 Redis Cluster 的 CROSSSLOT 报错）
     *
     * @param prefix   Redis 业务前缀，例如 "qtech:im:chk:wb:kafka"
     * @param hashPart 参与去重的 hash 值（如 SHA256 摘要）
     * @return Redis key
     */
    public static String buildDedupKey(String prefix, String hashPart) {
        return String.format("%s{%s}", prefix, hashPart);
    }

    /**
     * 使用普通 Redis 命令实现批量去重（无 Lua）
     *
     * @param keys       Redis key 列表（建议使用 buildDedupKey 构造）
     * @param expireTime 过期时间（单位：秒）
     * @return 实际新添加的 key 列表（未命中缓存的 key）
     */
    public List<String> filterNewKeys(List<String> keys, long expireTime) {
        List<String> result = new ArrayList<>();
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        for (String key : keys) {
            try {
                Boolean success = ops.setIfAbsent(key, Chronos.now().format(Chronos.getFormatter(Chronos.ISO_DATETIME_MS_FORMAT)), Duration.ofSeconds(expireTime));
                if (Boolean.TRUE.equals(success)) {
                    result.add(key);
                }
            } catch (Exception e) {
                log.warn(">>>>> 去重尝试失败，key = {}", key, e);
            }
        }

        return result;
    }
}
