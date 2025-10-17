package com.im.inspection.dpp.algorithm;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import static com.im.inspection.dpp.algorithm.Algorithm.doIntegrate;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/03 20:41:22
 */

public class Check {
    // 安全方法，每次调用时进行签名验证
    public static Dataset<Row> doCheck(Dataset<Row> processedDf, Dataset<Row> stdModels) throws Exception {
        // extracted();
        // 核心算法逻辑
        return doIntegrate(processedDf, stdModels);
    }
}