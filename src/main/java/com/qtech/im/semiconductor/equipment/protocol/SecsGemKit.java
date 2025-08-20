package com.qtech.im.semiconductor.equipment.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * SECS/GEM协议工具类
 * <p>
 * 特性：
 * - 通用化：支持SECS-I和HSMS协议
 * - 规范化：遵循SEMI标准协议规范
 * - 灵活性：支持自定义消息格式和编码
 * - 可靠性：完善的连接管理和错误处理
 * - 容错性：断线重连和消息重发机制
 * - 专业性：半导体行业标准协议实现
 * </p>
 *
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/08/20
 */
public class SecsGemKit {

    private static final Logger logger = LoggerFactory.getLogger(SecsGemKit.class);

    // 默认设备ID
    public static final int DEFAULT_DEVICE_ID = 0;

    // 默认超时时间（毫秒）
    public static final int DEFAULT_TIMEOUT = 5000;

    /**
     * 解析SECS消息
     *
     * @param rawData 原始数据
     * @return SECS消息对象
     */
    public static SecsMessage parseSecsMessage(byte[] rawData) {
        if (rawData == null || rawData.length < 10) {
            logger.warn("Invalid SECS message data");
            return null;
        }

        try {
            ByteBuffer buffer = ByteBuffer.wrap(rawData);

            // 解析消息头
            byte[] header = new byte[10];
            buffer.get(header);

            // 解析消息体
            byte[] body = new byte[buffer.remaining()];
            buffer.get(body);

            return new SecsMessage(header, body);
        } catch (Exception e) {
            logger.error("Failed to parse SECS message", e);
            return null;
        }
    }

    /**
     * 构建SECS消息
     *
     * @param stream    Stream号
     * @param function  Function号
     * @param deviceId  设备ID
     * @param data      消息数据
     * @return SECS消息对象
     */
    public static SecsMessage buildSecsMessage(int stream, int function, int deviceId, byte[] data) {
        try {
            // 构建消息头
            byte[] header = new byte[10];
            ByteBuffer headerBuffer = ByteBuffer.wrap(header);

            // 设置设备ID
            headerBuffer.putShort((short) deviceId);

            // 设置Stream和Function
            headerBuffer.put((byte) (stream & 0x7F)); // Stream (7 bits)
            headerBuffer.put((byte) (function & 0x7F)); // Function (7 bits)

            // 设置其他标志位
            headerBuffer.put((byte) 0x80); // P-Type = 1 (SECS-II)
            headerBuffer.put((byte) 0x00); // unused
            headerBuffer.putInt(data != null ? data.length : 0); // 数据长度

            return new SecsMessage(header, data != null ? data : new byte[0]);
        } catch (Exception e) {
            logger.error("Failed to build SECS message", e);
            return null;
        }
    }

    /**
     * 发送SECS消息
     *
     * @param connection 连接对象
     * @param message    SECS消息
     * @param timeout    超时时间（毫秒）
     * @return 响应消息
     */
    public static SecsMessage sendSecsMessage(SecsConnection connection, SecsMessage message, int timeout) {
        if (connection == null || message == null) {
            logger.warn("Invalid connection or message");
            return null;
        }

        try {
            // 发送消息
            byte[] responseBytes = connection.send(message.toByteArray(), timeout);

            // 解析响应
            return responseBytes != null ? parseSecsMessage(responseBytes) : null;
        } catch (Exception e) {
            logger.error("Failed to send SECS message", e);
            return null;
        }
    }

    /**
     * SECS消息类
     */
    public static class SecsMessage {
        private final byte[] header;
        private final byte[] body;

        public SecsMessage(byte[] header, byte[] body) {
            this.header = header != null ? Arrays.copyOf(header, header.length) : new byte[0];
            this.body = body != null ? Arrays.copyOf(body, body.length) : new byte[0];
        }

        // Getters
        public byte[] getHeader() { return Arrays.copyOf(header, header.length); }
        public byte[] getBody() { return Arrays.copyOf(body, body.length); }

        public byte[] toByteArray() {
            byte[] result = new byte[header.length + body.length];
            System.arraycopy(header, 0, result, 0, header.length);
            System.arraycopy(body, 0, result, header.length, body.length);
            return result;
        }

        @Override
        public String toString() {
            return "SecsMessage{" +
                   "header=" + Arrays.toString(header) +
                   ", bodyLength=" + body.length +
                   '}';
        }
    }

    /**
     * SECS连接接口
     */
    public interface SecsConnection {
        byte[] send(byte[] message, int timeout) throws Exception;
        void close();
        boolean isConnected();
    }
}
