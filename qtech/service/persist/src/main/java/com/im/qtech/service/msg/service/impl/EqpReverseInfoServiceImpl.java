package com.im.qtech.service.msg.service.impl;

import com.im.qtech.service.config.dynamic.DS;
import com.im.qtech.service.config.dynamic.DSContextHolder;
import com.im.qtech.service.config.dynamic.DSName;
import com.im.qtech.service.msg.entity.EqpReverseInfo;
import com.im.qtech.service.msg.mapper.EqpReverseInfoMapper;
import com.im.qtech.service.msg.service.IEqpReverseInfoService;
import org.im.common.thread.core.SmartThreadPoolExecutor;
import org.im.exception.type.data.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class EqpReverseInfoServiceImpl implements IEqpReverseInfoService {
    private static final Logger logger = LoggerFactory.getLogger(EqpReverseInfoServiceImpl.class);
    private static final long DATABASE_TIMEOUT_SECONDS = 30L;

    private final EqpReverseInfoMapper mapper;
    private final SmartThreadPoolExecutor databaseExecutor;

    @Autowired
    public EqpReverseInfoServiceImpl(
            EqpReverseInfoMapper eqpReverseInfoMapper,
            @Qualifier("importantTaskExecutor") SmartThreadPoolExecutor databaseExecutor) {
        this.mapper = eqpReverseInfoMapper;
        // 使用自定义的线程池框架中的重要任务线程池
        this.databaseExecutor = databaseExecutor;
    }

    @Override
    @DS(DSName.THIRD)
    public CompletableFuture<Boolean> upsertOracleAsync(EqpReverseInfo eqpReverseInfo) {
        DSName currentDS = DSContextHolder.get();

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        databaseExecutor.execute(() -> {
            try {
                DSContextHolder.set(currentDS);
                if (eqpReverseInfo == null) {
                    future.complete(false);
                    return;
                }
                if (!Objects.equals(eqpReverseInfo.getSource(), "aa-list")) {
                    logger.error(">>>>> EqpReverseInfoServiceImpl.upsertOracleAsync error: {}", eqpReverseInfo.getSource());
                    future.complete(false);
                    return;
                }
                try {
                    mapper.upsertOracle(eqpReverseInfo);
                    future.complete(true);
                } catch (Exception e) {
                    logger.error(">>>>> EqpReverseInfoServiceImpl.upsertOracleAsync error: {}", e.getMessage(), e);
                    future.complete(false);
                }
            } catch (Exception e) {
                logger.error(">>>>> EqpReverseInfoServiceImpl.upsertOracleAsync error: {}", e.getMessage(), e);
                future.complete(false);
            }
        });

        return future.orTimeout(DATABASE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @DS(DSName.FIRST)
    @Override
    public CompletableFuture<Boolean> upsertPGAsync(EqpReverseInfo eqpReverseInfo) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        databaseExecutor.execute(() -> {
            if (eqpReverseInfo != null) {
                try {
                    mapper.upsertPostgres(eqpReverseInfo);
                    future.complete(true);
                } catch (Exception e) {
                    logger.error("upsertPGAsync failed", e);
                    future.complete(false);
                }
            } else {
                logger.warn("upsertPGAsync received null parameter");
                future.complete(false);
            }
        });

        return future.orTimeout(DATABASE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @DS(DSName.SECOND)
    @Override
    public CompletableFuture<Boolean> upsertDorisAsync(EqpReverseInfo eqpReverseInfo) {
        DSName currentDS = DSContextHolder.get();

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        databaseExecutor.execute(() -> {
            try {
                DSContextHolder.set(currentDS);
                if (eqpReverseInfo != null) {
                    boolean result = mapper.upsertDoris(eqpReverseInfo) > 0;
                    future.complete(result);
                } else {
                    future.complete(false);
                }
            } catch (Exception e) {
                logger.error("upsertDorisAsync failed", e);
                future.complete(false);
            }
        });

        return future.orTimeout(DATABASE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @DS(DSName.THIRD)
    @Override
    public CompletableFuture<Boolean> upsertOracleBatchAsync(List<EqpReverseInfo> list) {
        if (CollectionUtils.isEmpty(list)) {
            return CompletableFuture.completedFuture(false);
        }

        DSName currentDS = DSContextHolder.get();
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        databaseExecutor.execute(() -> {
            try {
                DSContextHolder.set(currentDS);
                boolean result = mapper.upsertOracleBatch(list) > 0;
                future.complete(result);
            } catch (Exception e) {
                logger.error("upsertOracleBatchAsync failed", e);
                future.complete(false);
            }
        });

        return future.orTimeout(DATABASE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public CompletableFuture<Boolean> upsertPGBatchAsync(List<EqpReverseInfo> list) {
        if (CollectionUtils.isEmpty(list)) {
            return CompletableFuture.completedFuture(false);
        }

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        databaseExecutor.execute(() -> {
            try {
                boolean result = mapper.upsertPostgresBatch(list) > 0;
                future.complete(result);
            } catch (Exception e) {
                logger.error("upsertPGBatchAsync failed", e);
                future.complete(false);
            }
        });

        return future.orTimeout(DATABASE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @DS(DSName.SECOND)
    @Override
    public CompletableFuture<Boolean> upsertDorisBatchAsync(List<EqpReverseInfo> list) {
        if (CollectionUtils.isEmpty(list)) {
            return CompletableFuture.completedFuture(false);
        }

        DSName currentDS = DSContextHolder.get();
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        databaseExecutor.execute(() -> {
            try {
                DSContextHolder.set(currentDS);
                boolean result = mapper.upsertDorisBatch(list) > 0;
                future.complete(result);
            } catch (Exception e) {
                logger.error("upsertDorisBatchAsync failed", e);
                future.complete(false);
            }
        });

        return future.orTimeout(DATABASE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @DS(DSName.THIRD)
    @Override
    public CompletableFuture<Boolean> addOracleBatchAsync(List<EqpReverseInfo> list) {
        if (CollectionUtils.isEmpty(list)) {
            return CompletableFuture.completedFuture(false);
        }

        DSName currentDS = DSContextHolder.get();
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        databaseExecutor.execute(() -> {
            try {
                DSContextHolder.set(currentDS);
                boolean result = mapper.addOracleBatch(list) > 0;
                future.complete(result);
            } catch (Exception e) {
                logger.error("addOracleBatchAsync failed", e);
                future.complete(false);
            }
        });

        return future.orTimeout(DATABASE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public CompletableFuture<Boolean> addPGBatchAsync(List<EqpReverseInfo> list) {
        if (CollectionUtils.isEmpty(list)) {
            return CompletableFuture.completedFuture(false);
        }

        DSName currentDS = DSContextHolder.get();
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        databaseExecutor.execute(() -> {
            try {
                DSContextHolder.set(currentDS);
                boolean result = mapper.addPostgresBatch(list) > 0;
                future.complete(result);
            } catch (Exception e) {
                logger.error("addPGBatchAsync failed", e);
                future.complete(false);
            }
        });

        return future.orTimeout(DATABASE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @DS(DSName.SECOND)
    @Override
    public CompletableFuture<Boolean> addDorisBatchAsync(List<EqpReverseInfo> list) {
        if (CollectionUtils.isEmpty(list)) {
            return CompletableFuture.completedFuture(false);
        }

        DSName currentDS = DSContextHolder.get();
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        databaseExecutor.execute(() -> {
            try {
                DSContextHolder.set(currentDS);
                boolean result = mapper.addDorisBatch(list) > 0;
                future.complete(result);
            } catch (Exception e) {
                logger.error("addDorisBatchAsync failed", e);
                future.complete(false);
            } finally {
                DSContextHolder.clear();
            }
        });

        return future.orTimeout(DATABASE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @DS(DSName.SECOND)
    @Override
    public CompletableFuture<Boolean> addDorisAsync(EqpReverseInfo eqpReverseInfo) {
        if (eqpReverseInfo == null) {
            return CompletableFuture.completedFuture(false);
        }

        DSName currentDS = DSContextHolder.get();
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        databaseExecutor.execute(() -> {
            try {
                DSContextHolder.set(currentDS);
                boolean result = mapper.addDorisAsync(eqpReverseInfo) > 0;
                future.complete(result);
            } catch (Exception e) {
                logger.error("addDorisAsync failed", e);
                future.complete(false);
            } finally {
                DSContextHolder.clear();
            }
        });

        return future.orTimeout(DATABASE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @DS(DSName.THIRD)
    @Override
    public int upsertOracleBatch(List<EqpReverseInfo> list) {
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }

        try {
            return mapper.upsertOracleBatch(list);
        } catch (Exception e) {
            logger.error(">>>>> EqpReverseInfoServiceImpl.upsertOracleBatch error: {}", e.getMessage(), e);
            throw new DataAccessException("DB_UPSERT_ERROR", "批量 upsertOracle 错误", e);
        }
    }

    @DS(DSName.FIRST)
    @Override
    public int upsertPGBatch(List<EqpReverseInfo> list) {
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }
        try {
            return mapper.upsertPostgresBatch(list);
        } catch (Exception e) {
            throw new DataAccessException("DB_UPSERT_ERROR", "批量 upsertPostgresBatch 错误", e);
        }
    }

    @DS(DSName.SECOND)
    @Override
    public int addWbOlpChkDorisBatch(List<EqpReverseInfo> list) {
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }

        try {
            return mapper.addWbOlpChkDorisBatch(list);
        } catch (Exception e) {
            logger.error(">>>>> EqpReverseInfoServiceImpl.addWbOlpChkDorisBatch error: {}", e.getMessage(), e);
            throw new DataAccessException("DB_INSERT_ERROR", "批量 addWbOlpChkDoris 错误");
        }
    }
}
