package com.im.qtech.datadev.wb.usage.source

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Dataset, Row}
import org.im.etl.component.DataTransformer
import org.im.etl.exception.EtlException

/**
 * 金线使用数据转换器
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/22
 */

class WireUsageDataTransformer extends DataTransformer {
  override def transform(input: Any): Either[EtlException, Dataset[Row]] = {
    try {
      input match {
        case dataset: Dataset[Row] =>
          val transformedData = dataset.select(
            col("create_date").cast("timestamp"),
            col("factory_name"),
            col("workshop"),
            col("device_id"),
            col("prod_type"),
            col("device_m_id"),
            col("wire_size").cast("float"),
            col("machine_no"),
            col("device_type"),
            col("wire_cnt"),
            col("prod_cnt"),
            col("wire_usage"),
            col("yield"),
            col("biz_date"),
            col("time_period"),
            col("upload_time")
          )
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