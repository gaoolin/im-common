package org.im.common.serde.protobuf;

import com.google.protobuf.ByteString;

/**
 * Protobuf 工具类
 * <p>
 * 提供 Protobuf 相关的辅助方法
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/09
 */
public class ProtoUtils {

    /**
     * 检查字符串是否为空
     *
     * @param value 字符串值
     * @return 是否为空
     */
    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    /**
     * 检查 ByteString 是否为空
     *
     * @param value ByteString 值
     * @return 是否为空
     */
    public static boolean isEmpty(ByteString value) {
        return value == null || value.isEmpty();
    }
}

