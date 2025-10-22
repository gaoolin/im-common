package com.im.qtech.service.msg.disruptor.wb;

import com.im.qtech.data.dto.param.WbOlpRawData;
import com.im.qtech.service.msg.kafka.olp.DeadLetterQueueService;
import com.im.qtech.service.msg.service.IWbOlpRawDataService;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Disruptor 事件处理器
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/04/24 10:20:38
 */
@Slf4j
@Service
public class WbOlpRawDataEventHandler implements EventHandler<WbOlpRawDataEvent>, LifecycleAware {
    private static final int BATCH_SIZE = 100;
    private final List<WbOlpRawData> buffer = new ArrayList<>();
    @Autowired
    private IWbOlpRawDataService repository;

    @Autowired
    private DeadLetterQueueService dlqService;

    @Override
    public void onEvent(WbOlpRawDataEvent event, long sequence, boolean endOfBatch) {
        WbOlpRawData data = event.getData();
        try {
            buffer.add(data);
            if (buffer.size() >= BATCH_SIZE || endOfBatch) {
                flush();
            }
        } catch (Exception e) {
            log.error(">>>>> Error processing WbOlpRawDataRecord: {}", data, e);
            dlqService.sendWbOlpRawDataToDLQ(data); // 死信队列记录
        } finally {
            // 确保数据释放，防止内存滞留
            event.clear();
        }
    }

    private void flush() {
        try {
            if (!buffer.isEmpty()) {
                repository.addWbOlpRawDataBatch(buffer);
                buffer.clear();
            }
        } catch (Exception e) {
            log.error(">>>>> Error saving batch to database", e);
            buffer.forEach(dlqService::sendWbOlpRawDataToDLQ);
            buffer.clear();
        }
    }

    @Override
    public void onStart() {
        log.info(">>>>> Disruptor handler started");
    }

    @Override
    public void onShutdown() {
        flush(); // 关闭前清空缓冲
        log.info(">>>>> Disruptor handler shut down");
    }
}

