package org.im.common.batch.engine;

import org.im.common.batch.config.BatchConfig;
import org.im.common.batch.processor.BatchProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * 标准批处理引擎
 * <p>
 * 基于标准批处理器实现的批处理引擎
 * </p>
 *
 * @param <T> 处理数据类型
 * @author gaozhilin
 * @version 1.0
 * @email gaoolin@gmail.com
 * @date 2025/10/17
 * @since 1.0
 */

public class StandardBatchEngine<T> {

    private static final Logger logger = LoggerFactory.getLogger(StandardBatchEngine.class);

    private final BatchConfig config;
    private final String engineName;

    /**
     * 构造函数
     *
     * @param engineName 引擎名称
     * @param config     批处理配置
     */
    public StandardBatchEngine(String engineName, BatchConfig config) {
        this.engineName = engineName != null ? engineName : "StandardBatchEngine";
        this.config = config != null ? config : new BatchConfig();
    }

    /**
     * 构造函数（使用默认配置）
     *
     * @param engineName 引擎名称
     */
    public StandardBatchEngine(String engineName) {
        this(engineName, null);
    }

    /**
     * 执行批处理任务
     *
     * @param data      待处理数据列表
     * @param processor 数据处理器
     * @return 批处理结果列表
     */
    public List<BatchProcessor.BatchResult<T>> executeBatch(List<T> data, Consumer<List<T>> processor) {
        logger.info("Starting batch processing with engine: {}", engineName);

        try {
            List<BatchProcessor.BatchResult<T>> results = BatchProcessor.processInBatches(data, processor, config);
            logger.info("Batch processing completed with {} results", results.size());
            return results;
        } catch (Exception e) {
            logger.error("Batch processing failed in engine: {}", engineName, e);
            throw new RuntimeException("Batch processing failed", e);
        }
    }

    /**
     * 获取批处理配置
     *
     * @return 批处理配置
     */
    public BatchConfig getConfig() {
        return config;
    }

    /**
     * 获取引擎名称
     *
     * @return 引擎名称
     */
    public String getEngineName() {
        return engineName;
    }
}
