package com.im.qtech.data.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.im.common.json.JsonMapperProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;


/**
 * 分流逻辑
 * <p>
 * 负责字符串消息流的分流
 * </p>
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/05/26 15:00:59
 */
public class DeviceTypeSplitter {
    private static final Logger logger = LoggerFactory.getLogger(DeviceTypeSplitter.class);
    private static final List<String> IMPORTANT_KEYWORDS = Arrays.asList("WB", "DB", "HM", "AA");
    private static final ObjectMapper objectMapper = JsonMapperProvider.getSharedInstance();

    private static final OutputTag<EqNetworkStatus> importantTag =
            new OutputTag<EqNetworkStatus>("important") {
            };

    public static SplitStreams split(DataStream<String> input) {
        SingleOutputStreamOperator<EqNetworkStatus> mainStream = input.process(new ProcessFunction<String, EqNetworkStatus>() {
            @Override
            public void processElement(String msg, Context ctx, Collector<EqNetworkStatus> out) {
                try {
                    if (msg == null || msg.trim().isEmpty()) {
                        logger.warn("Received empty message");
                        return;
                    }

                    EqNetworkStatus record = objectMapper.readValue(msg, EqNetworkStatus.class);

                    if (record == null) {
                        logger.warn("Parsed null record from message: {}", msg);
                        return;
                    }

                    // 检查必要字段
                    if (record.getDeviceType() == null) {
                        logger.warn("Device type is null for message: {}", msg);
                        out.collect(record);
                        return;
                    }

                    String type = record.getDeviceType().toUpperCase();
                    if (IMPORTANT_KEYWORDS.contains(type)) {
                        ctx.output(importantTag, record);
                    } else {
                        out.collect(record);
                    }
                } catch (Exception e) {
                    logger.error("Failed to parse JSON message: {}", msg, e);
                    // 可以选择将错误消息发送到侧输出流进行进一步处理
                    // ctx.output(ERROR_TAG, msg);
                }
            }
        });

        return new SplitStreams(
                mainStream.getSideOutput(importantTag),
                mainStream
        );
    }

    @Getter
    public static class SplitStreams {
        private final DataStream<EqNetworkStatus> importantStream;
        private final DataStream<EqNetworkStatus> normalStream;

        public SplitStreams(DataStream<EqNetworkStatus> important, DataStream<EqNetworkStatus> normal) {
            this.importantStream = important;
            this.normalStream = normal;
        }

    }
}