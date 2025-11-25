package com.im.qtech.service.msg.service.impl;

import com.im.qtech.data.dto.param.WbOlpRawData;
import com.im.qtech.service.config.dynamic.DS;
import com.im.qtech.service.config.dynamic.DSContextHolder;
import com.im.qtech.service.config.dynamic.DSName;
import com.im.qtech.service.msg.mapper.WbOlpRawDataMapper;
import com.im.qtech.service.msg.service.IWbOlpRawDataService;
import org.im.common.thread.core.SmartThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/23 13:39:12
 */
@Service
public class WbOlpRawDataServiceImpl implements IWbOlpRawDataService {
    private static final Logger logger = LoggerFactory.getLogger(WbOlpRawDataServiceImpl.class);
    private static final long DATABASE_TIMEOUT_SECONDS = 30L;

    private final WbOlpRawDataMapper wbOlpRawDataMapper;
    private final SmartThreadPoolExecutor databaseExecutor;

    @Autowired
    public WbOlpRawDataServiceImpl(
            WbOlpRawDataMapper wbOlpRawDataMapper,
            @Qualifier("importantTaskExecutor") SmartThreadPoolExecutor databaseExecutor) {
        this.wbOlpRawDataMapper = wbOlpRawDataMapper;
        this.databaseExecutor = databaseExecutor;
    }

    @DS(DSName.SECOND)
    @Override
    public int addWbOlpRawDataBatch(List<WbOlpRawData> wbOlpRawDataList) {
        if (wbOlpRawDataList == null || wbOlpRawDataList.isEmpty()) {
            return 0;
        }
        return wbOlpRawDataMapper.addWbOlpRawDataBatch(wbOlpRawDataList);
    }

    @DS(DSName.SECOND)
    @Override
    public CompletableFuture<Boolean> addWbOlpRawDataBatchAsync(List<WbOlpRawData> list) {
        if (list == null || list.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        DSName currentDS = DSContextHolder.get();

        // 使用 CompletableFuture.supplyAsync 更简洁的方式
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        DSContextHolder.set(currentDS);
                        int result = wbOlpRawDataMapper.addWbOlpRawDataBatch(list);
                        return result > 0;
                    } catch (Exception e) {
                        logger.error(">>>>> 异步批量插入WbOlpRawData数据失败, 数据量: {}", list.size(), e);
                        throw new RuntimeException("数据库操作失败: " + e.getMessage(), e);
                    }
                }, databaseExecutor)
                .whenComplete((result, throwable) -> {
                    // 清理上下文
                    DSContextHolder.clear();
                })
                .orTimeout(DATABASE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

}
