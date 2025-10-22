package com.im.qtech.service.msg.service.impl;

import com.im.qtech.data.dto.param.WbOlpRawData;
import com.im.qtech.service.config.dynamic.DS;
import com.im.qtech.service.config.dynamic.DSName;
import com.im.qtech.service.msg.mapper.WbOlpRawDataMapper;
import com.im.qtech.service.msg.service.IWbOlpRawDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/23 13:39:12
 */
@Service
public class WbOlpRawDataServiceImpl implements IWbOlpRawDataService {
    private static final Logger logger = LoggerFactory.getLogger(WbOlpRawDataServiceImpl.class);
    private final WbOlpRawDataMapper wbOlpRawDataMapper;

    @Autowired
    public WbOlpRawDataServiceImpl(WbOlpRawDataMapper wbOlpRawDataMapper) {
        this.wbOlpRawDataMapper = wbOlpRawDataMapper;
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
    @Async
    @Override
    public CompletableFuture<Integer> addWbOlpRawDataBatchAsync(List<WbOlpRawData> wbOlpRawDataList) {
        if (wbOlpRawDataList == null || wbOlpRawDataList.isEmpty()) {
            return CompletableFuture.completedFuture(0);
        }

        CompletableFuture<Integer> future = new CompletableFuture<>();
        try {
            int result = wbOlpRawDataMapper.addWbOlpRawDataBatch(wbOlpRawDataList);
            future.complete(result);
        } catch (Exception e) {
            logger.error(">>>>> 异步批量插入WbOlpRawData数据失败: {}", e.getMessage());
            future.completeExceptionally(e);
        }
        return future;
    }
}
