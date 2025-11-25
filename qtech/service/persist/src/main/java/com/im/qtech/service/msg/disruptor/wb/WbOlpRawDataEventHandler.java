package com.im.qtech.service.msg.disruptor.wb;

import com.im.qtech.data.dto.param.WbOlpRawData;
import com.im.qtech.service.msg.persist.kafka.DeadLetterQueueService;
import com.im.qtech.service.msg.service.IWbOlpRawDataService;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import lombok.extern.slf4j.Slf4j;
import org.im.common.thread.core.SmartThreadPoolExecutor;
import org.im.common.thread.core.ThreadPoolSingleton;
import org.im.common.thread.task.TaskPriority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
    private final List<WbOlpRawData> buffer = new ArrayList<>();
    private final IWbOlpRawDataService repository;
    private final DeadLetterQueueService dlqService;
    private final SmartThreadPoolExecutor databaseExecutor;
    private final Object bufferLock = new Object();
    @Value("${wb.olp.raw.data.batch.size:500}")
    private int BATCH_SIZE = 500;
    @Value("${wb.olp.raw.data.max.buffer.size:5000}")
    private int MAX_BUFFER_SIZE = 5000;

    @Autowired
    public WbOlpRawDataEventHandler(
            IWbOlpRawDataService repository,
            DeadLetterQueueService dlqService) {
        this.repository = repository;
        this.dlqService = dlqService;
        // 使用您自定义的线程池框架中的重要任务线程池
        this.databaseExecutor = ThreadPoolSingleton.getInstance()
                .getTaskDispatcher()
                .getExecutor(TaskPriority.IMPORTANT);
    }

    @Override
    public void onEvent(WbOlpRawDataEvent event, long sequence, boolean endOfBatch) {
        WbOlpRawData data = event.getData();
        try {
            synchronized (bufferLock) {
                // 背压控制：防止缓冲区过大影响内存
                if (buffer.size() >= MAX_BUFFER_SIZE) {
                    log.warn(">>>>> Buffer size exceeded limit, sending data to DLQ to prevent memory overflow");
                    dlqService.sendWbOlpRawDataToDLQ(data);
                    return;
                }

                buffer.add(data);
                if (buffer.size() >= BATCH_SIZE || endOfBatch) {
                    flush();
                }
            }
        } catch (Exception e) {
            log.error(">>>>> Error processing WbOlpRawDataRecord: {}", data, e);
            dlqService.sendWbOlpRawDataToDLQ(data);
        } finally {
            event.clear();
        }
    }

    private void flush() {
        List<WbOlpRawData> toFlush;
        synchronized (bufferLock) {
            if (buffer.isEmpty()) {
                return;
            }
            toFlush = new ArrayList<>(buffer);
            buffer.clear();
        }

        try {
            // 使用自定义线程池执行数据库操作，避免阻塞 Disruptor 处理线程
            CompletableFuture<Boolean> async = repository.addWbOlpRawDataBatchAsync(toFlush);
            async.handle((result, throwable) -> {
                if (throwable != null) {
                    log.error(">>>>> Error saving batch to database, batch size: {}", toFlush.size(), throwable);
                    toFlush.forEach(dlqService::sendWbOlpRawDataToDLQ);
                } else {
                    log.debug(">>>>> Successfully processed batch of {} records", toFlush.size());
                }
                return null;
            });
            // 移除 orTimeout，或者正确处理其异常
        } catch (Exception e) {
            log.error(">>>>> Error initiating batch save to database, batch size: {}", toFlush.size(), e);
            toFlush.forEach(dlqService::sendWbOlpRawDataToDLQ);
        }
    }

    @Override
    public void onStart() {
        log.info(">>>>> Disruptor handler started with batch size: {}, max buffer size: {}",
                BATCH_SIZE, MAX_BUFFER_SIZE);
    }

    @Override
    public void onShutdown() {
        flush(); // 关闭前清空缓冲
        log.info(">>>>> Disruptor handler shut down");
    }
}
