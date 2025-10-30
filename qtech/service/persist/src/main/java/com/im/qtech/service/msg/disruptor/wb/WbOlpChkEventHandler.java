package com.im.qtech.service.msg.disruptor.wb;

import com.im.qtech.service.msg.entity.EqpReverseInfo;
import com.im.qtech.service.msg.persist.olp.DeadLetterQueueService;
import com.im.qtech.service.msg.service.IEqpReverseInfoService;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/04/24 15:31:03
 */

@Slf4j
@Service
public class WbOlpChkEventHandler implements EventHandler<WbOlpChkEvent>, LifecycleAware {

    private static final int BATCH_SIZE = 100;
    private static final int FLUSH_INTERVAL_SECONDS = 5;
    private final List<EqpReverseInfo> buffer = Collections.synchronizedList(new ArrayList<>());
    private ScheduledExecutorService scheduler;

    @Autowired
    private IEqpReverseInfoService service;

    @Autowired
    private DeadLetterQueueService dlqService;

    @PostConstruct
    public void init() {
        // 定时任务：定期 flush 缓冲数据，防止遗漏
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                flush(); // 周期性落库
            } catch (Exception e) {
                log.error(">>>>> 定时落库失败", e);
            }
        }, FLUSH_INTERVAL_SECONDS, FLUSH_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void destroy() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        flush(); // JVM 退出前补一波
    }

    @Override
    public void onEvent(WbOlpChkEvent event, long sequence, boolean endOfBatch) {
        EqpReverseInfo data = event.getData();
        try {
            buffer.add(data);
            if (buffer.size() >= BATCH_SIZE || endOfBatch) {
                flush();
            }
        } catch (Exception e) {
            log.error(">>>>> Error processing WbOlpChk: {}", data, e);
            dlqService.sendWbOlpChkToDLQ(data);
        } finally {
            event.clear(); // 防止内存滞留
        }
    }

    /**
     * 批量落库并清空缓冲区（带异常处理）
     */
    private void flush() {
        List<EqpReverseInfo> toPersist;

        synchronized (buffer) {
            if (buffer.isEmpty()) return;

            toPersist = new ArrayList<>(buffer);
            buffer.clear();
        }

        try {
            // 添加去重逻辑：根据业务主键去重，保留最新数据
            List<EqpReverseInfo> deduplicatedData = deduplicateData(toPersist);

            // service.addWbOlpChkDorisBatch(deduplicatedData);
            service.upsertPGBatch(deduplicatedData);
            log.info(">>>>> 成功落库 [{}] 条 WbOlpChk 数据", deduplicatedData.size());
        } catch (Exception e) {
            log.error(">>>>> 批量落库失败，写入 DLQ，数量：{}", toPersist.size(), e);
            toPersist.forEach(dlqService::sendWbOlpChkToDLQ);
        }
    }

    /**
     * 根据业务主键对数据进行去重
     *
     * @param data 待去重的数据列表
     * @return 去重后的数据列表
     */
    private List<EqpReverseInfo> deduplicateData(List<EqpReverseInfo> data) {
        Map<String, EqpReverseInfo> uniqueMap = new LinkedHashMap<>();

        data.forEach(item ->
                uniqueMap.put(item.getSimId(), item)
        );

        // Java 21的SequencedCollection支持
        return uniqueMap.values().stream().toList();
    }

    @Override
    public void onStart() {
        log.info(">>>>> Disruptor handler started");
    }

    @Override
    public void onShutdown() {
        flush();
        log.info(">>>>> Disruptor handler shut down");
    }
}
