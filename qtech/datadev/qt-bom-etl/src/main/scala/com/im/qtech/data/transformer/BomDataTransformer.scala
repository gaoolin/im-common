package com.im.qtech.data.transformer

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Dataset, Row}
import org.im.etl.component.DataTransformer
import org.im.etl.exception.EtlException

/**
 * BOM数据转换器
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/30
 */
class BomDataTransformer extends DataTransformer {
  override def transform(input: Any): Either[EtlException, Dataset[Row]] = {
    try {
      input match {
        case dataset: Dataset[Row] =>
          val transformedData = dataset.select(
            col("ID"),
            col("UPN"),
            col("STEPS"),
            col("REPLACE_CPN"),
            col("SOURCE"),
            col("NAME"),
            col("SPEC"),
            col("UNIT"),
            col("QTY"),
            col("WASTE"),
            col("POSITION"),
            col("STATUS").alias("BOM_STATUS"),
            col("TOPLIMIT"),
            col("ID1"),
            col("MDATE"),
            col("MUSER"),
            col("IS_DO"),
            col("REMARKS"),
            col("VERSION"),
            col("OP_CODE"),
            col("SERIES_CODE"),
            col("PART_CODE"),
            col("PART_NAME"),
            col("PART_SPEC"),
            col("TYPE"),
            col("TYPE2"),
            col("PIXEL"),
            col("PROCESS_SPEC"),
            current_timestamp().alias("UPDATE_TIME")
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
