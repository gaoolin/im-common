package com.im.qtech.datadev.wb.usage.source

import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.im.etl.component.DataSource
import org.im.etl.exception.EtlException

/**
 * JDBC数据源实现
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/22
 */

class JdbcDataSource(sparkSession: SparkSession) extends DataSource {
  override type ReadConfig = Map[String, Any]

  override def read(config: ReadConfig): Either[EtlException, Dataset[Row]] = {
    try {
      val url = config.getOrElse("url", "").toString
      val user = config.getOrElse("user", "").toString
      val password = config.getOrElse("password", "").toString
      val driver = config.getOrElse("driver", "").toString
      val query = config.getOrElse("query", "").toString

      // 设置JDBC驱动
      Class.forName(driver)

      val dataset = sparkSession.read
        .format("jdbc")
        .option("url", url)
        .option("user", user)
        .option("password", password)
        .option("driver", driver)
        .option("dbtable", s"($query) tmp")
        .load()

      Right(dataset)
    } catch {
      case e: Exception =>
        Left(EtlException(s"Failed to read from JDBC source: ${e.getMessage}", e))
    }
  }
}