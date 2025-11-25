package com.im.qtech.service.config.disruptor;

import com.im.qtech.service.msg.disruptor.wb.WbOlpChkEvent;
import com.im.qtech.service.msg.disruptor.wb.WbOlpChkEventHandler;
import com.im.qtech.service.msg.disruptor.wb.WbOlpRawDataEvent;
import com.im.qtech.service.msg.disruptor.wb.WbOlpRawDataEventHandler;
import com.lmax.disruptor.FatalExceptionHandler;
import com.lmax.disruptor.LiteBlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ThreadFactory;

/**
 * 配置 Disruptor 框架以实现高性能的异步批处理
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/04/24 10:06:31
 */

@Configuration
public class DisruptorConfig {

    private static final int RING_BUFFER_SIZE = 1024;

    private Disruptor<WbOlpRawDataEvent> wbOlpRawDataEventDisruptor;

    private Disruptor<WbOlpChkEvent> wbOlpChkEventDisruptor;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private WbOlpRawDataEventHandler wbOlpRawDataEventHandler;

    @Autowired
    private WbOlpChkEventHandler wbOlpChkEventHandler;


    @Bean
    @Qualifier("wbOlpRawDataDisruptor")
    public Disruptor<WbOlpRawDataEvent> wbOlpRawDataDisruptor() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private int counter = 0;

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "disruptor-wb-raw-" + counter++);
                t.setDaemon(true);
                return t;
            }
        };

        wbOlpRawDataEventDisruptor = new Disruptor<>(WbOlpRawDataEvent::new,
            RING_BUFFER_SIZE,
            threadFactory,
            ProducerType.MULTI,
            new LiteBlockingWaitStrategy());

        wbOlpRawDataEventDisruptor.setDefaultExceptionHandler(new FatalExceptionHandler());
        wbOlpRawDataEventDisruptor.handleEventsWith(wbOlpRawDataEventHandler);
        wbOlpRawDataEventDisruptor.start();

        return wbOlpRawDataEventDisruptor;
    }

    @Bean
    @Qualifier("wbOlpChkDisruptor")
    public Disruptor<WbOlpChkEvent> wbOlpChkDisruptor() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private int counter = 0;

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "disruptor-wb-chk-" + counter++);
                t.setDaemon(true);
                return t;
            }
        };

        wbOlpChkEventDisruptor = new Disruptor<>(
                WbOlpChkEvent::new,
                RING_BUFFER_SIZE,
                threadFactory,
                ProducerType.MULTI,
                new LiteBlockingWaitStrategy()
        );

        wbOlpChkEventDisruptor.setDefaultExceptionHandler(new FatalExceptionHandler());
        wbOlpChkEventDisruptor.handleEventsWith(wbOlpChkEventHandler);
        wbOlpChkEventDisruptor.start();

        return wbOlpChkEventDisruptor;
    }

    @Bean
    @Qualifier("wbOlpRawDataRingBuffer")
    public RingBuffer<WbOlpRawDataEvent> wbOlpRawDataRingBuffer(Disruptor<WbOlpRawDataEvent> disruptor) {
        return disruptor.getRingBuffer();
    }

    @Bean
    @Qualifier("wbOlpChkRingBuffer")
    public RingBuffer<WbOlpChkEvent> wbOlpChkRingBuffer(Disruptor<WbOlpChkEvent> disruptor) {
        return disruptor.getRingBuffer();
    }

    @Bean
    public ApplicationRunner disruptorMonitor(
            @Qualifier("wbOlpRawDataDisruptor") Disruptor<WbOlpRawDataEvent> rawDataDisruptor,
            @Qualifier("wbOlpChkDisruptor") Disruptor<WbOlpChkEvent> chkDisruptor) {
        return args -> {
            RingBuffer<WbOlpRawDataEvent> rawRingBuffer = rawDataDisruptor.getRingBuffer();
            RingBuffer<WbOlpChkEvent> chkRingBuffer = chkDisruptor.getRingBuffer();

            meterRegistry.gauge("disruptor.raw.ringbuffer.remaining", rawRingBuffer,
                rb -> rb.getBufferSize() - rb.getCursor());
            meterRegistry.gauge("disruptor.chk.ringbuffer.remaining", chkRingBuffer,
                rb -> rb.getBufferSize() - rb.getCursor());
        };
    }

    /**
     * 优雅关闭 disruptor，防止内存泄漏或线程挂起
     */
    @PreDestroy
    public void shutdown() {
        if (wbOlpRawDataEventDisruptor != null) {
            wbOlpRawDataEventDisruptor.halt();
            wbOlpRawDataEventDisruptor.shutdown();
        }

        if (wbOlpChkEventDisruptor != null) {
            wbOlpChkEventDisruptor.halt();
            wbOlpChkEventDisruptor.shutdown();
        }
    }

    /**
     * 若仍希望记录总处理数，建议将此方法改到 Producer 或 Handler 内部，并结合 Counter
     */
    public void countProcessed() {
        meterRegistry.counter("wb_olp_raw_data.processed.total").increment();
    }
}
