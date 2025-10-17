package com.im.inspection.dpp.algorithm;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import static com.im.inspection.dpp.algorithm.Check.doCheck;

/**
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2024/08/04 01:11:31
 */


public class Transfer {
    public static Dataset<Row> doChk(Dataset<Row> processedDf, Dataset<Row> stdModels) throws Exception {
        return doCheck(processedDf, stdModels);
    }
}
