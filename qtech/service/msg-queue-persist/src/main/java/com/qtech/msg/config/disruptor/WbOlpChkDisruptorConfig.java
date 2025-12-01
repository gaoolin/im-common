package com.qtech.msg.config.disruptor;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/04/24 15:24:54
 * desc   :
 * <p>
 * 优雅关闭 disruptor，防止内存泄漏或线程挂起
 * <p>
 * 若仍希望记录总处理数，建议将此方法改到 Producer 或 Handler 内部，并结合 Counter
 */

/*
@Configuration
public class WbOlpChkDisruptorConfig {

    private static final int RING_BUFFER_SIZE = 1024;

    private Disruptor<WbOlpChkEvent> disruptor;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private WbOlpChkEventHandler wbOlpChkEventHandler;

    @Bean
    @Qualifier("wbOlpChkDisruptor")
    public Disruptor<WbOlpChkEvent> disruptor() {
        ThreadFactory threadFactory = runnable -> {
            Thread t = Executors.defaultThreadFactory().newThread(runnable);
            t.setName("disruptor-worker-thread");
            return t;
        };

        disruptor = new Disruptor<>(
                WbOlpChkEvent::new,
                RING_BUFFER_SIZE,
                threadFactory,
                ProducerType.MULTI,
                new BlockingWaitStrategy()
        );

        disruptor.setDefaultExceptionHandler(new FatalExceptionHandler());
        disruptor.handleEventsWith(wbOlpChkEventHandler);
        disruptor.start();

        return disruptor;
    }

    @Bean
    public RingBuffer<WbOlpRawDataEvent> ringBuffer(Disruptor<WbOlpRawDataEvent> disruptor) {
        return disruptor.getRingBuffer();
    }

    */
/**
 * 优雅关闭 disruptor，防止内存泄漏或线程挂起
 *//*

    @PreDestroy
    public void shutdown() {
        if (disruptor != null) {
            disruptor.shutdown();
        }
    }

    */
/**
 * 若仍希望记录总处理数，建议将此方法改到 Producer 或 Handler 内部，并结合 Counter
 *//*

    public void countProcessed() {
        meterRegistry.counter("wb_olp_raw_data.processed.total").increment();
    }
}
*/
