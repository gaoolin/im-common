package com.im.qtech.service.msg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.im.qtech.service.config.dynamic.DS;
import com.im.qtech.service.config.dynamic.DSContextHolder;
import com.im.qtech.service.config.dynamic.DSName;
import com.im.qtech.service.msg.entity.EqpLstParsedForOracle;
import com.im.qtech.service.msg.mapper.EqpLstParsedForOracleMapper;
import com.im.qtech.service.msg.service.IEqpLstParsedForOracle;
import org.im.common.thread.core.SmartThreadPoolExecutor;
import org.im.common.thread.core.ThreadPoolSingleton;
import org.im.common.thread.task.TaskPriority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/11/15
 */

@DS(DSName.THIRD)
@Service
public class EqpLstParsedForOracleImpl extends ServiceImpl<EqpLstParsedForOracleMapper, EqpLstParsedForOracle> implements IEqpLstParsedForOracle {

    private final SmartThreadPoolExecutor databaseExecutor;

    @Autowired
    public EqpLstParsedForOracleImpl(@Qualifier("importantTaskExecutor") SmartThreadPoolExecutor databaseExecutor) {
        // 使用自定义的线程池框架
        this.databaseExecutor = databaseExecutor;
    }

    @Override
    public CompletableFuture<Boolean> saveAsync(EqpLstParsedForOracle entity) {
        // 关键：在主线程中获取当前数据源设置
        DSName currentDS = DSContextHolder.get();

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        databaseExecutor.execute(() -> {
            try {
                // 关键：在异步线程中恢复数据源设置
                DSContextHolder.set(currentDS);
                boolean result = save(entity);
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            } finally {
                // 清理上下文避免内存泄漏
                DSContextHolder.clear();
            }
        });

        return future.orTimeout(30, TimeUnit.SECONDS);
    }
}

