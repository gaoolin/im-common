package com.im.qtech.chk;

import com.im.aa.inspection.config.EqpLstRedisCacheConfig;
import com.im.aa.inspection.serde.EqLstProtobufMapper;
import com.im.qtech.common.dto.param.EqLstPOJO;
import org.im.cache.core.Cache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Base64;

import static org.junit.Assert.*;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/10/10
 */
public class EqLstProtoRedisTest {

    private EqpLstRedisCacheConfig cacheConfig;
    private Cache<String, String> eqLstByteStringCache; // 使用String类型缓存

    @Before
    public void setUp() {
        // 初始化缓存配置
        cacheConfig = new EqpLstRedisCacheConfig();
        // 使用String类型的缓存替代byte[]类型缓存
        eqLstByteStringCache = cacheConfig.getEqLstTplCache(); // 或者创建专门的String缓存
    }

    @After
    public void tearDown() {
        // 清理测试数据
        if (eqLstByteStringCache != null) {
            eqLstByteStringCache.close();
        }
    }

    @Test
    public void testSerializeAndStoreToRedis() {
        // 创建测试对象
        EqLstPOJO originalObj = createTestEqLstPOJO();

        // 序列化对象
        byte[] serializedData = EqLstProtobufMapper.serialize(originalObj);
        assertNotNull("序列化数据不应为null", serializedData);
        assertTrue("序列化数据应不为空", serializedData.length > 0);

        // 将byte[]转换为Base64字符串后存储到Redis
        String base64Data = Base64.getEncoder().encodeToString(serializedData);
        String key = "test:EqLstPOJO:" + System.currentTimeMillis();
        eqLstByteStringCache.put(key, base64Data);

        // 验证存储成功
        assertTrue("数据应成功存储到缓存中", eqLstByteStringCache.containsKey(key));
    }

    @Test
    public void testRetrieveFromRedisAndDeserialize() {
        // 创建测试对象并序列化
        EqLstPOJO originalObj = createTestEqLstPOJO();
        byte[] serializedData = EqLstProtobufMapper.serialize(originalObj);

        // 将byte[]转换为Base64字符串后存储到Redis
        String base64Data = Base64.getEncoder().encodeToString(serializedData);
        String key = "test:EqLstPOJO:" + System.currentTimeMillis();
        eqLstByteStringCache.put(key, base64Data);

        // 从Redis获取Base64编码的数据
        String retrievedBase64Data = eqLstByteStringCache.get(key);
        assertNotNull("从Redis获取的数据不应为null", retrievedBase64Data);

        // 将Base64字符串解码为byte[]
        byte[] retrievedData = Base64.getDecoder().decode(retrievedBase64Data);

        // 反序列化
        EqLstPOJO deserializedObj = EqLstProtobufMapper.deserialize(retrievedData);
        assertNotNull("反序列化对象不应为null", deserializedObj);

        // 验证对象内容
        assertEquals("module字段应相等", originalObj.getModule(), deserializedObj.getModule());
        assertEquals("aa1字段应相等", originalObj.getAa1(), deserializedObj.getAa1());
        assertEquals("sid字段应相等", originalObj.getSid(), deserializedObj.getSid());
    }

    @Test
    public void testRoundTripSerialization() {
        // 创建测试对象
        EqLstPOJO originalObj = createTestEqLstPOJO();

        // 序列化并转换为Base64字符串后存储到Redis
        byte[] serializedData = EqLstProtobufMapper.serialize(originalObj);
        String base64Data = Base64.getEncoder().encodeToString(serializedData);
        String key = "test:EqLstPOJO:" + System.currentTimeMillis();
        eqLstByteStringCache.put(key, base64Data);

        // 从Redis获取并解码为byte[]后反序列化
        String retrievedBase64Data = eqLstByteStringCache.get(key);
        byte[] retrievedData = Base64.getDecoder().decode(retrievedBase64Data);
        EqLstPOJO deserializedObj = EqLstProtobufMapper.deserialize(retrievedData);

        // 验证完整往返过程
        assertEquals("原始对象和反序列化对象的module字段应相等",
                originalObj.getModule(), deserializedObj.getModule());
        assertEquals("原始对象和反序列化对象的aa1字段应相等",
                originalObj.getAa1(), deserializedObj.getAa1());
        assertEquals("原始对象和反序列化对象的sid字段应相等",
                originalObj.getSid(), deserializedObj.getSid());
    }

    private EqLstPOJO createTestEqLstPOJO() {
        EqLstPOJO obj = new EqLstPOJO();
        obj.setModule("test-module");
        obj.setAa1("test-aa1-value");
        obj.setAa2("test-aa2-value");
        obj.setSid("test-sid-value");
        obj.setBackToPosition("test-position");
        obj.setBlemish("test-blemish");
        return obj;
    }
}
