package com.im.qtech.data.transformer

import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{Dataset, Row}
import org.im.etl.component.DataTransformer
import org.im.etl.exception.EtlException

import java.sql.Timestamp

/**
 * 设备上下文数据转换器
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/28
 */

class EqpCtxDataTransformer extends DataTransformer {
  override def transform(input: Any): Either[EtlException, Dataset[Row]] = {
    try {
      input match {
        case dataset: Dataset[Row] =>
          // 添加更新时间字段
          val currentTime = new Timestamp(System.currentTimeMillis())
          val transformedData = dataset.withColumn("UPDATE_TIME", lit(currentTime))
          Right(transformedData)
        case _ =>
          Left(EtlException("Input data is not a Dataset[Row]"))
      }
    } catch {
      case e: Exception =>
        Left(EtlException(s"Failed to transform data: ${e.getMessage}", e))
    }
  }
}