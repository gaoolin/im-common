package com.im.qtech.service.msg.service;


import com.im.qtech.service.msg.entity.EqpReverseInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/13 17:27:36
 */

public interface IEqpReverseInfoService {


    CompletableFuture<Boolean> addDorisAsync(EqpReverseInfo EqpReverseInfo);

    int upsertOracleBatch(List<EqpReverseInfo> list);

    int upsertPGBatch(List<EqpReverseInfo> list);

    int addWbOlpChkDorisBatch(List<EqpReverseInfo> list);

    CompletableFuture<Boolean> upsertOracleAsync(EqpReverseInfo EqpReverseInfo);

    CompletableFuture<Boolean> upsertPGAsync(EqpReverseInfo EqpReverseInfo);

    CompletableFuture<Boolean> upsertDorisAsync(EqpReverseInfo EqpReverseInfo);

    CompletableFuture<Boolean> upsertOracleBatchAsync(List<EqpReverseInfo> list);

    CompletableFuture<Boolean> upsertPGBatchAsync(List<EqpReverseInfo> list);

    CompletableFuture<Boolean> upsertDorisBatchAsync(List<EqpReverseInfo> list);

    CompletableFuture<Boolean> addOracleBatchAsync(List<EqpReverseInfo> list);

    CompletableFuture<Boolean> addPGBatchAsync(List<EqpReverseInfo> list);

    CompletableFuture<Boolean> addDorisBatchAsync(List<EqpReverseInfo> list);


}
