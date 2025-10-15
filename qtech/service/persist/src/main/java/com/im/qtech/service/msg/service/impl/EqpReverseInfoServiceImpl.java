package com.im.qtech.service.msg.service.impl;

import com.im.qtech.service.msg.entity.EqpReverseInfo;
import com.im.qtech.service.msg.mapper.EqpReverseInfoMapper;
import com.im.qtech.service.msg.service.IEqpReverseInfoService;
import org.im.exception.type.data.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/13 17:28:21
 */

@Service
public class EqpReverseInfoServiceImpl implements IEqpReverseInfoService {
    private static final Logger logger = LoggerFactory.getLogger(EqpReverseInfoServiceImpl.class);
    private final EqpReverseInfoMapper eqpReverseInfoMapper;

    @Autowired
    public EqpReverseInfoServiceImpl(EqpReverseInfoMapper eqpReverseInfoMapper) {
        this.eqpReverseInfoMapper = eqpReverseInfoMapper;
    }

    @Async
    @Override
    public CompletableFuture<Integer> upsertOracleAsync(EqpReverseInfo EqpReverseInfo) {

        if (EqpReverseInfo != null) {
            if (!Objects.equals(EqpReverseInfo.getSource(), "aa-list")) {
                logger.error(">>>>> EqpReverseInfoServiceImpl.upsertOracleAsync error: {}", EqpReverseInfo.getSource());
            }

            try {
                eqpReverseInfoMapper.upsertOracle(EqpReverseInfo);
                return CompletableFuture.completedFuture(1);
            } catch (Exception e) {
                logger.error(">>>>> EqpReverseInfoServiceImpl.upsertOracleAsync error: {}", e.getMessage());
                return CompletableFuture.completedFuture(0);
            }
        }
        return CompletableFuture.completedFuture(0);
    }

    @Async
    @Override
    public CompletableFuture<Integer> upsertDorisAsync(EqpReverseInfo EqpReverseInfo) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        try {
            eqpReverseInfoMapper.upsertDoris(EqpReverseInfo);
            future.complete(1);
        } catch (Exception e) {
            logger.error(">>>>> EqpReverseInfoServiceImpl.upsertDoris error: {}", e.getMessage());
            future.completeExceptionally(e);
        }
        return future;
    }

    @Async
    @Override
    public CompletableFuture<Integer> addAaListDorisAsync(EqpReverseInfo EqpReverseInfo) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        try {
            int result = eqpReverseInfoMapper.addAaListDoris(EqpReverseInfo);
            future.complete(1);
        } catch (Exception e) {
            logger.error(">>>>> EqpReverseInfoServiceImpl.addAaListBatchDoris error: {}", e.getMessage());
            future.completeExceptionally(e);
        }
        return future;
    }

    @Async
    @Override
    public CompletableFuture<Integer> addWbOlpChkDorisAsync(EqpReverseInfo EqpReverseInfo) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        try {
            int result = eqpReverseInfoMapper.addWbOlpChkDoris(EqpReverseInfo);
            future.complete(1);
        } catch (Exception e) {
            logger.error(">>>>> EqpReverseInfoServiceImpl.addWbOlpChkBatchDoris error: {}", e.getMessage());
            future.completeExceptionally(e);
        }
        return future;
    }

    /**
     * @param list
     * @return
     */
    @Override
    public int upsertOracleBatch(List<EqpReverseInfo> list) {
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }

        try {
            return eqpReverseInfoMapper.upsertOracleBatch(list);
        } catch (Exception e) {
            logger.error(">>>>> EqpReverseInfoServiceImpl.upsertOracleBatch error: {}", e.getMessage(), e);
            throw new DataAccessException("DB_UPSERT_ERROR", "批量 upsertOracle 错误");
        }
    }

    /**
     * @param list
     * @return
     */
    @Override
    public int addWbOlpChkDorisBatch(List<EqpReverseInfo> list) {
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }

        try {
            return eqpReverseInfoMapper.addWbOlpChkDorisBatch(list);
        } catch (Exception e) {
            logger.error(">>>>> EqpReverseInfoServiceImpl.addWbOlpChkDorisBatch error: {}", e.getMessage(), e);
            throw new DataAccessException("DB_INSERT_ERROR", "批量 addWbOlpChkDoris 错误");
        }
    }
}
