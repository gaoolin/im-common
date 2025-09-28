package com.im.aa.inspection.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.im.aa.inspection.entity.param.EqLstParsed;
import com.im.aa.inspection.entity.tpl.QtechEqLstTpl;
import com.im.aa.inspection.entity.tpl.QtechEqLstTplInfo;
import org.im.cache.core.Cache;
import org.im.util.json.JsonMapperProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache utility class for handling AaListParams-related cache operations using im-common cache components.
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/29
 */
public class CacheUtil {
    private static final Logger logger = LoggerFactory.getLogger(CacheUtil.class);
    private static final ObjectMapper objectMapper = JsonMapperProvider.getSharedInstance();

    // 单例实例
    private static volatile CacheUtil instance;

    private final Cache<String, String> eqLstTplInfoCache;
    private final Cache<String, String> eqLstTplCache;

    // 私有构造函数防止外部实例化
    private CacheUtil(Cache<String, String> eqLstTplInfoCache, Cache<String, String> eqLstTplCache) {
        this.eqLstTplInfoCache = eqLstTplInfoCache;
        this.eqLstTplCache = eqLstTplCache;
    }

    /**
     * 获取CacheUtil单例实例
     *
     * @param eqLstTplInfoCache 模板信息缓存
     * @param eqLstTplCache     模板缓存
     * @return CacheUtil单例实例
     */
    public static CacheUtil getInstance(Cache<String, String> eqLstTplInfoCache, Cache<String, String> eqLstTplCache) {
        if (instance == null) {
            synchronized (CacheUtil.class) {
                if (instance == null) {
                    instance = new CacheUtil(eqLstTplInfoCache, eqLstTplCache);
                }
            }
        }
        return instance;
    }

    /**
     * 获取已存在的单例实例
     *
     * @return CacheUtil实例，如果未初始化则返回null
     */
    public static CacheUtil getInstance() {
        return instance;
    }

    public void saveMsgToHash(Map<String, EqLstParsed> messages, String hashKey) {
        // 由于im-common的Cache接口不支持Hash操作，我们需要改变实现方式
        // 将整个Map序列化为一个字符串存储
        if (messages == null || hashKey == null) {
            logger.warn(">>>>> Null parameter provided to saveMsgToHash: messages={}, hashKey={}", messages, hashKey);
            return;
        }

        try {
            String jsonString = objectMapper.writeValueAsString(messages);
            eqLstTplCache.put(hashKey, jsonString);
        } catch (JsonProcessingException e) {
            logger.error(">>>>> JSON serialization failed for messages map with hashKey: {}", hashKey, e);
        }
    }

    public EqLstParsed getMsgFromHash(String hashKey, String messageId) {
        if (hashKey == null || messageId == null) {
            logger.warn(">>>>> Null parameter provided to getMessageFromHash: hashKey={}, messageId={}", hashKey, messageId);
            return null;
        }

        // 从序列化的Map中获取特定消息
        Map<String, EqLstParsed> messages = getAllMsgFromHash(hashKey);
        return messages.get(messageId);
    }

    public Map<String, EqLstParsed> getAllMsgFromHash(String hashKey) {
        if (hashKey == null) {
            logger.warn(">>>>> Null hashKey provided to getAllMsgFromHash");
            return new HashMap<>();
        }

        String jsonString = eqLstTplCache.get(hashKey);
        if (jsonString != null) {
            try {
                // 使用objectMapper的类型引用来反序列化复杂类型
                return objectMapper.readValue(jsonString,
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, EqLstParsed.class));
            } catch (JsonProcessingException e) {
                logger.error(">>>>> JSON deserialization failed for hash key: {}", hashKey, e);
            } catch (IllegalArgumentException e) {
                logger.error(">>>>> Illegal argument during JSON deserialization for hash key: {}", hashKey, e);
            } catch (Exception e) {
                logger.error(">>>>> Unexpected error during JSON deserialization for hash key: {}", hashKey, e);
            }
        }
        return new HashMap<>();
    }

    public void saveParamsTpl(String name, QtechEqLstTpl message) {
        if (name == null || message == null) {
            logger.warn(">>>>> Null parameter provided to saveParamsTpl: name={}, message={}", name, message);
            return;
        }

        try {
            String jsonString = objectMapper.writeValueAsString(message);
            eqLstTplCache.put(name, jsonString);
        } catch (JsonProcessingException e) {
            logger.error(">>>>> JSON serialization failed for QtechEqLstTpl with name: {}", name, e);
        }
    }

    public void saveParamsTplInfo(String name, QtechEqLstTplInfo message) {
        if (name == null || message == null) {
            logger.warn(">>>>> Null parameter provided to saveParamsTplInfo: name={}, message={}", name, message);
            return;
        }

        try {
            String jsonString = objectMapper.writeValueAsString(message);
            eqLstTplInfoCache.put(name, jsonString);
        } catch (JsonProcessingException e) {
            logger.error(">>>>> JSON serialization failed for QtechEqLstTplInfo with name: {}", name, e);
        }
    }

    public QtechEqLstTpl getParamsTpl(String name) {
        if (name == null) {
            logger.warn(">>>>> Null name provided to getParamsTpl");
            return null;
        }

        String jsonString = eqLstTplCache.get(name);
        if (jsonString != null) {
            try {
                return objectMapper.readValue(jsonString, QtechEqLstTpl.class);
            } catch (JsonProcessingException e) {
                logger.error(">>>>> JSON deserialization failed for QtechEqLstTpl with name: {}", name, e);
            }
        }
        return null;
    }

    public QtechEqLstTplInfo getParamsTplInfo(String name) {
        if (name == null) {
            logger.warn(">>>>> Null name provided to getParamsTplInfo");
            return null;
        }

        String jsonString = eqLstTplInfoCache.get(name);
        if (jsonString != null) {
            try {
                return objectMapper.readValue(jsonString, QtechEqLstTplInfo.class);
            } catch (JsonProcessingException e) {
                logger.error(">>>>> JSON deserialization failed for QtechEqLstTplInfo with name: {}", name, e);
            }
        }
        return null;
    }
}
