package com.im.qtech.service.msg.disruptor.wb;

import com.im.qtech.service.msg.entity.EqpReverseInfo;
import com.im.qtech.service.msg.persist.kafka.DeadLetterQueueService;
import com.im.qtech.service.msg.service.IEqpReverseInfoService;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.im.common.thread.core.SmartThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
public class WbOlpChkEventHandler implements EventHandler<WbOlpChkEvent>, LifecycleAware {

    private final List<EqpReverseInfo> buffer = Collections.synchronizedList(new ArrayList<>());
    private final Object bufferLock = new Object();
    private final IEqpReverseInfoService service;
    private final DeadLetterQueueService dlqService;
    private final SmartThreadPoolExecutor databaseExecutor;
    @Value("${wb.olp.chk.batch.size:100}")
    private int BATCH_SIZE = 100;
    @Value("${wb.olp.chk.flush.interval.seconds:5}")
    private int FLUSH_INTERVAL_SECONDS = 5;
    @Value("${wb.olp.chk.max.buffer.size:1000}")
    private int MAX_BUFFER_SIZE = 1000;
    private ScheduledExecutorService scheduler;

    @Autowired
    public WbOlpChkEventHandler(
            IEqpReverseInfoService service,
            DeadLetterQueueService dlqService,
            @Qualifier("importantTaskExecutor") SmartThreadPoolExecutor databaseExecutor) {
        this.service = service;
        this.dlqService = dlqService;
        // 使用自定义的线程池框架
        this.databaseExecutor = databaseExecutor;
    }

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
            synchronized (bufferLock) {
                // 背压控制
                if (buffer.size() >= MAX_BUFFER_SIZE) {
                    log.warn(">>>>> Buffer size exceeded limit, sending data to DLQ to prevent memory overflow");
                    dlqService.sendWbOlpChkToDLQ(data);
                    return;
                }

                buffer.add(data);
                if (buffer.size() >= BATCH_SIZE || endOfBatch) {
                    flush();
                }
            }
        } catch (Exception e) {
            log.error(">>>>> Error processing WbOlpChk: {}", data, e);
            dlqService.sendWbOlpChkToDLQ(data);
        } finally {
            event.clear();
        }
    }

    /**
     * 批量落库并清空缓冲区（带异常处理）
     */
    private void flush() {
        List<EqpReverseInfo> toPersist;

        synchronized (bufferLock) {
            if (buffer.isEmpty()) return;
            toPersist = new ArrayList<>(buffer);
            buffer.clear();
        }

        try {
            // 添加去重逻辑：根据业务主键去重，保留最新数据
            List<EqpReverseInfo> deduplicatedData = deduplicateData(toPersist);

            // 使用线程池框架执行数据库操作
            CompletableFuture<Boolean> upsertedDoris = executeWithThreadPool(() ->
                    service.upsertDorisBatchAsync(deduplicatedData));
            CompletableFuture<Boolean> addedDoris = executeWithThreadPool(() ->
                    service.addDorisBatchAsync(deduplicatedData));
            CompletableFuture<Boolean> upsertedOracle = executeWithThreadPool(() ->
                    service.upsertOracleBatchAsync(deduplicatedData));

            // 设置超时时间以避免永久阻塞
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    upsertedDoris.exceptionally(ex -> {
                        logErrorAndSendToDLQ("Doris Upsert", ex, deduplicatedData);
                        return false;
                    }),
                    addedDoris.exceptionally(ex -> {
                        logErrorAndSendToDLQ("Doris Add", ex, deduplicatedData);
                        return false;
                    }),
                    upsertedOracle.exceptionally(ex -> {
                        logErrorAndSendToDLQ("Oracle Upsert", ex, deduplicatedData);
                        return false;
                    })
            );

            // 加入超时限制
            allFutures.get(30, TimeUnit.SECONDS); // 可配置为更合适的值

            log.info(">>>>> 成功落库 [{}] 条 WbOlpChk 数据", deduplicatedData.size());
        } catch (TimeoutException e) {
            log.error(">>>>> 批量落库超时，写入 DLQ，数量：{}", toPersist.size(), e);
            toPersist.forEach(dlqService::sendWbOlpChkToDLQ);
        } catch (Exception e) {
            log.error(">>>>> 批量落库失败，写入 DLQ，数量：{}", toPersist.size(), e);
            toPersist.forEach(dlqService::sendWbOlpChkToDLQ);
        }
    }

    private CompletableFuture<Boolean> executeWithThreadPool(
            java.util.function.Supplier<CompletableFuture<Boolean>> supplier) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        databaseExecutor.execute(() -> {
            try {
                CompletableFuture<Boolean> result = supplier.get();
                result.whenComplete((boolResult, throwable) -> {
                    if (throwable != null) {
                        future.completeExceptionally(throwable);
                    } else {
                        future.complete(boolResult);
                    }
                });
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    // 提取公共的日志+DLQ方法
    private void logErrorAndSendToDLQ(String operationName, Throwable ex, List<EqpReverseInfo> data) {
        log.error(">>>>> {} 失败，部分数据写入 DLQ，数量：{}", operationName, data.size(), ex);
        data.forEach(dlqService::sendWbOlpChkToDLQ);
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
                uniqueMap.put(item.getSimId() + "|" + item.getSource(), item)
        );

        // Java 21的SequencedCollection支持
        return uniqueMap.values().stream().toList();
    }

    @Override
    public void onStart() {
        log.info(">>>>> Disruptor handler started with batch size: {}, max buffer size: {}",
                BATCH_SIZE, MAX_BUFFER_SIZE);
    }

    @Override
    public void onShutdown() {
        flush();
        log.info(">>>>> Disruptor handler shut down");
    }
}
