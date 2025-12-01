package com.qtech.msg.kafka.olp.bak;

import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2024/08/24 09:26:53
 * desc   :
 */


public class WbOlpChkDeduplicationTransformer implements ValueTransformer<String, String> {
    private final String stateStoreName;
    private KeyValueStore<String, String> stateStore;

    public WbOlpChkDeduplicationTransformer(String stateStoreName) {
        this.stateStoreName = stateStoreName;
    }

    @Override
    public void init(ProcessorContext context) {
        this.stateStore = (KeyValueStore<String, String>) context.getStateStore(stateStoreName);
    }

    @Override
    public String transform(String value) {
        // 获取上次的结果
        String previousValue = stateStore.get(value);

        if (previousValue == null) {
            // 如果之前不存在，则存储并返回当前值
            stateStore.put(value, value);
            return value;
        } else {
            // 如果已经存在，则返回null，表示去重
            return null;
        }
    }

    @Override
    public void close() {
        // 关闭时的清理操作
    }
}