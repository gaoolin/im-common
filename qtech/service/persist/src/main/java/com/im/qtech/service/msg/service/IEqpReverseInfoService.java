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
    CompletableFuture<Integer> upsertOracleAsync(EqpReverseInfo EqpReverseInfo);

    CompletableFuture<Integer> upsertPostgresAsync(EqpReverseInfo EqpReverseInfo);

    CompletableFuture<Integer> upsertDorisAsync(EqpReverseInfo EqpReverseInfo);

    CompletableFuture<Integer> addAaListDorisAsync(EqpReverseInfo EqpReverseInfo);

    CompletableFuture<Integer> addWbOlpChkDorisAsync(EqpReverseInfo EqpReverseInfo);

    int upsertOracleBatch(List<EqpReverseInfo> list);

    int upsertPostgresBatch(List<EqpReverseInfo> list);

    int addWbOlpChkDorisBatch(List<EqpReverseInfo> list);
}
