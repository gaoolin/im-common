package com.qtech.msg.service;

import com.im.qtech.data.dto.param.WbOlpRawData;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/23 13:38:51
 */

public interface IWbOlpRawDataService {
    public int addWbOlpRawDataBatch(List<WbOlpRawData> wbOlpRawDataList);

    public CompletableFuture<Integer> addWbOlpRawDataBatchAsync(List<WbOlpRawData> wbOlpRawDataList);
}
