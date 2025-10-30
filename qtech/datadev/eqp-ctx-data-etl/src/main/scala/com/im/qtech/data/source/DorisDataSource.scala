package com.im.qtech.data.source

import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.im.etl.component.DataSource
import org.im.etl.exception.EtlException

/**
 * Doris数据源实现
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/28
 */

class DorisDataSource(sparkSession: SparkSession) extends DataSource {
  override type ReadConfig = Map[String, Any]

  override def read(config: ReadConfig): Either[EtlException, Dataset[Row]] = {
    try {
      val url = config.getOrElse("url", "").toString
      val user = config.getOrElse("user", "").toString
      val password = config.getOrElse("password", "").toString
      val driver = config.getOrElse("driver", "").toString
      val dbtable = config.getOrElse("dbtable", "").toString

      // 设置JDBC驱动
      Class.forName(driver)

      val dataset = sparkSession.read
        .format("jdbc")
        .option("url", url)
        .option("user", user)
        .option("password", password)
        .option("driver", driver)
        .option("dbtable", dbtable)
        .load()

      Right(dataset)
    } catch {
      case e: Exception =>
        Left(EtlException(s"Failed to read from Doris source: ${e.getMessage}", e))
    }
  }
}