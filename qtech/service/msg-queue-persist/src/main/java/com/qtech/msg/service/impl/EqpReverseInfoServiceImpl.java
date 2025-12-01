package com.qtech.msg.service.impl;

import com.im.qtech.data.dto.reverse.EqpReversePOJO;
import com.qtech.im.exception.DataAccessException;
import com.qtech.msg.common.dynamic.DataSourceNames;
import com.qtech.msg.common.dynamic.DataSourceSwitch;
import com.qtech.msg.mapper.EqReverseCtrlInfoMapper;
import com.qtech.msg.service.IEqpReverseInfoService;
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
    private final EqReverseCtrlInfoMapper eqReverseCtrlInfoMapper;

    @Autowired
    public EqpReverseInfoServiceImpl(EqReverseCtrlInfoMapper eqReverseCtrlInfoMapper) {
        this.eqReverseCtrlInfoMapper = eqReverseCtrlInfoMapper;
    }

    @DataSourceSwitch(name = DataSourceNames.FIRST)
    @Async
    @Override
    public CompletableFuture<Integer> upsertOracleAsync(EqpReversePOJO pojo) {

        if (pojo != null) {
            if (!Objects.equals(pojo.getSource(), "aa-list")) {
                logger.error(">>>>> EqpReverseInfoServiceImpl.upsertOracleAsync error: {}", pojo.getSource());
            }

            try {
                eqReverseCtrlInfoMapper.upsertOracle(pojo);
                return CompletableFuture.completedFuture(1);
            } catch (Exception e) {
                logger.error(">>>>> EqpReverseInfoServiceImpl.upsertOracleAsync error: {}", e.getMessage());
                return CompletableFuture.completedFuture(0);
            }
        }
        return CompletableFuture.completedFuture(0);
    }

    @DataSourceSwitch(name = DataSourceNames.SECOND)
    @Async
    @Override
    public CompletableFuture<Integer> upsertDorisAsync(EqpReversePOJO pojo) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        try {
            eqReverseCtrlInfoMapper.upsertDoris(pojo);
            future.complete(1);
        } catch (Exception e) {
            logger.error(">>>>> EqpReverseInfoServiceImpl.upsertDoris error: {}", e.getMessage());
            future.completeExceptionally(e);
        }
        return future;
    }

    @DataSourceSwitch(name = DataSourceNames.SECOND)
    @Async
    @Override
    public CompletableFuture<Integer> addEqpLstDorisAsync(EqpReversePOJO pojo) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        try {
            int result = eqReverseCtrlInfoMapper.addAaListDoris(pojo);
            future.complete(1);
        } catch (Exception e) {
            logger.error(">>>>> EqpReverseInfoServiceImpl.addAaListBatchDoris error: {}", e.getMessage());
            future.completeExceptionally(e);
        }
        return future;
    }

    @DataSourceSwitch(name = DataSourceNames.SECOND)
    @Async
    @Override
    public CompletableFuture<Integer> addWbOlpChkDorisAsync(EqpReversePOJO pojo) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        try {
            int result = eqReverseCtrlInfoMapper.addWbOlpChkDoris(pojo);
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
    @DataSourceSwitch(name = DataSourceNames.FIRST)
    @Override
    public int upsertOracleBatch(List<EqpReversePOJO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }

        try {
            return eqReverseCtrlInfoMapper.upsertOracleBatch(list);
        } catch (Exception e) {
            logger.error(">>>>> EqpReverseInfoServiceImpl.upsertOracleBatch error: {}", e.getMessage(), e);
            throw new DataAccessException("DB_UPSERT_ERROR", "批量 upsertOracle 错误");
        }
    }

    /**
     * @param list
     * @return
     */
    @DataSourceSwitch(name = DataSourceNames.SECOND)
    @Override
    public int addWbOlpChkDorisBatch(List<EqpReversePOJO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }

        try {
            return eqReverseCtrlInfoMapper.addWbOlpChkDorisBatch(list);
        } catch (Exception e) {
            logger.error(">>>>> EqpReverseInfoServiceImpl.addWbOlpChkDorisBatch error: {}", e.getMessage(), e);
            throw new DataAccessException("DB_INSERT_ERROR", "批量 addWbOlpChkDoris 错误");
        }
    }
}
