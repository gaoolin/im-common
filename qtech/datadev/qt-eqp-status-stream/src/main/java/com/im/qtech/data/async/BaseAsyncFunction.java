package com.im.qtech.data.async;

import com.im.qtech.data.model.EqNetworkStatus;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * author :  gaozhilin
 * email  :  gaoolin@gmail.com
 * date   :  2025/08/01 09:41:36
 * desc   :
 * 异步函数基类，统一使用 AsyncExecutorProvider 线程池
 * 支持超时处理，统一日志和指标管理
 *
 * @param <IN>  输入类型
 * @param <OUT> 输出类型
 */
public abstract class BaseAsyncFunction<IN, OUT> extends RichAsyncFunction<IN, OUT> {

    private static final Logger logger = LoggerFactory.getLogger(BaseAsyncFunction.class);

    // 超时时间，默认30秒，可子类构造时设置
    private final long timeoutMs;

    // 线程池引用 - 使用高并发线程池处理外部操作
    protected transient ExecutorService executor;

    // 指标计数器
    protected transient Counter successCounter;
    protected transient Counter timeoutCounter;
    protected transient Counter failureCounter;

    public BaseAsyncFunction() {
        this(30000L); // 增加默认超时时间到30秒
    }

    public BaseAsyncFunction(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        // 使用高并发线程池处理外部异步操作（如数据库访问）
        this.executor = AsyncExecutorProvider.getHighConcurrencyExecutor();

        // 初始化指标
        successCounter = getRuntimeContext().getMetricGroup().counter("asyncSuccessCount");
        timeoutCounter = getRuntimeContext().getMetricGroup().counter("asyncTimeoutCount");
        failureCounter = getRuntimeContext().getMetricGroup().counter("asyncFailureCount");

        logger.info("BaseAsyncFunction opened with timeout {} ms", timeoutMs);
    }

    @Override
    public void asyncInvoke(final IN input, final ResultFuture<OUT> resultFuture) {
        try {
            // 直接在 Flink 线程中启动异步任务
            CompletableFuture.supplyAsync(() -> {
                        try {
                            return asyncInvokeInternal(input);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }, executor) // 使用自己的线程池执行实际的异步操作
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            failureCounter.inc();
                            logger.error("Async invoke failure for input: {}", input, throwable);
                            onFailure(input, resultFuture, throwable);
                        } else {
                            successCounter.inc();
                            resultFuture.complete(Collections.singletonList(result));
                        }
                    });
        } catch (Exception e) {
            // 同步处理异常
            failureCounter.inc();
            logger.error("Failed to submit async task for input: {}", input, e);
            onFailure(input, resultFuture, e);
        }
    }

    /**
     * 具体异步处理实现，子类必须实现
     *
     * @param input 输入
     * @return 结果
     * @throws Exception 异常
     */
    protected abstract OUT asyncInvokeInternal(IN input) throws Exception;

    /**
     * 超时回调，默认返回原始输入（如果是 EqNetworkStatus），子类可重写
     *
     * @param input        输入
     * @param resultFuture 结果回调
     */
    protected void onTimeout(IN input, ResultFuture<OUT> resultFuture) {
        if (input instanceof EqNetworkStatus && isEqNetworkStatusOutput()) {
            resultFuture.complete(Collections.singletonList((OUT) input));
        } else {
            resultFuture.complete(Collections.emptyList());
        }
    }

    /**
     * 异常回调，默认返回原始输入（如果是 EqNetworkStatus），子类可重写
     *
     * @param input        输入
     * @param resultFuture 结果回调
     * @param ex           异常
     */
    protected void onFailure(IN input, ResultFuture<OUT> resultFuture, Throwable ex) {
        if (input instanceof EqNetworkStatus && isEqNetworkStatusOutput()) {
            resultFuture.complete(Collections.singletonList((OUT) input));
        } else {
            resultFuture.complete(Collections.emptyList());
        }
    }

    @Override
    public void timeout(IN input, ResultFuture<OUT> resultFuture) throws Exception {
        // 此方法不会被触发，因为 CompletableFuture 已经控制了超时
        // 保留此方法以防未来框架行为变化
        timeoutCounter.inc();
        logger.warn("Flink async timeout for input: {}", input);
        onTimeout(input, resultFuture);
    }

    @Override
    public void close() throws Exception {
        super.close();
        // 这里线程池统一管理，不主动关闭
    }

    // 类型安全检查辅助方法
    private boolean isEqNetworkStatusOutput() {
        return EqNetworkStatus.class.isAssignableFrom(getOutputClass());
    }

    // 获取泛型 OUT 的实际类型（需子类实现）
    protected abstract Class<OUT> getOutputClass();
}