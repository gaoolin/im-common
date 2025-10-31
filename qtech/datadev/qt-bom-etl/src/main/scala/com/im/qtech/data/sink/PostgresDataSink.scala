package com.im.qtech.data.sink

import com.im.qtech.data.config.BomConfigManager
import org.apache.spark.sql.{Dataset, Row}
import org.im.etl.component.DataSink
import org.im.etl.exception.EtlException

import java.util.Properties

/**
 * PostgreSQL数据目标实现
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/31
 */

class PostgresDataSink extends DataSink {
  override type WriteConfig = Map[String, Any]

  private val configManager = BomConfigManager.getInstance()

  override def write(data: Any, config: WriteConfig): Either[EtlException, Unit] = {
    try {
      data match {
        case dataset: Dataset[Row] =>
          // 从配置管理器获取PostgreSQL连接配置
          val postgresUrl = configManager.getString("jdbc.postgres.url")
          val postgresUser = configManager.getString("jdbc.postgres.user")
          val postgresPassword = configManager.getString("jdbc.postgres.pwd")
          val postgresDriver = configManager.getString("jdbc.postgres.driver")

          val props = new Properties()
          props.setProperty("user", postgresUser)
          props.setProperty("password", postgresPassword)

          val tableName = config.getOrElse("table", "mes_aa_bom").toString
          val batchSize = config.getOrElse("batchSize", 2000).toString.toInt
          val enableCache = config.getOrElse("enableCache", false).toString.toBoolean

          new Dataset2Postgres(dataset, postgresUrl, postgresDriver, tableName, props, batchSize, enableCache).doInsert()
          Right(())
        case _ =>
          Left(EtlException("Input data is not a Dataset[Row]"))
      }
    } catch {
      case e: Exception =>
        Left(EtlException(s"Failed to write to PostgreSQL: ${e.getMessage}", e))
    }
  }
}