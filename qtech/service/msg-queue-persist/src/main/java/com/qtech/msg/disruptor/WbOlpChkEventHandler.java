package com.qtech.msg.disruptor;

import com.im.qtech.data.dto.reverse.EqpReversePOJO;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import com.qtech.msg.kafka.olp.DeadLetterQueueService;
import com.qtech.msg.service.IEqpReverseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private final List<EqpReversePOJO> buffer = Collections.synchronizedList(new ArrayList<>());
    private ScheduledExecutorService scheduler;

    @Autowired
    private IEqpReverseInfoService eqReverseCtrlInfoService;

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
        EqpReversePOJO data = event.getData();
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
        List<EqpReversePOJO> toPersist;

        synchronized (buffer) {
            if (buffer.isEmpty()) return;

            toPersist = new ArrayList<>(buffer);
            buffer.clear();
        }

        try {
            eqReverseCtrlInfoService.addWbOlpChkDorisBatch(toPersist);
            eqReverseCtrlInfoService.upsertOracleBatch(toPersist);
            log.info(">>>>> 成功落库 [{}] 条 WbOlpChk 数据", toPersist.size());
        } catch (Exception e) {
            log.error(">>>>> 批量落库失败，写入 DLQ，数量：{}", toPersist.size(), e);
            toPersist.forEach(dlqService::sendWbOlpChkToDLQ);
        }
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
