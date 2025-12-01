package com.qtech.msg.service;

import com.im.qtech.data.dto.reverse.EqpReversePOJO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/13 17:27:36
 */

public interface IEqpReverseInfoService {
    CompletableFuture<Integer> upsertOracleAsync(EqpReversePOJO pojo);

    CompletableFuture<Integer> upsertDorisAsync(EqpReversePOJO pojo);

    CompletableFuture<Integer> addEqpLstDorisAsync(EqpReversePOJO pojo);

    CompletableFuture<Integer> addWbOlpChkDorisAsync(EqpReversePOJO pojo);

    int upsertOracleBatch(List<EqpReversePOJO> list);

    int addWbOlpChkDorisBatch(List<EqpReversePOJO> list);
}
