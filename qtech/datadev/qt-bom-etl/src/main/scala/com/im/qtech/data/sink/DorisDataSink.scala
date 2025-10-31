package com.im.qtech.data.sink

import com.im.qtech.data.config.BomConfigManager
import org.apache.spark.sql.{Dataset, Row}
import org.im.etl.component.DataSink
import org.im.etl.exception.EtlException

import java.util.Properties

/**
 * Doris数据目标实现
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/30
 */
class DorisDataSink extends DataSink {
  override type WriteConfig = Map[String, Any]

  private val configManager = BomConfigManager.getInstance()

  override def write(data: Any, config: WriteConfig): Either[EtlException, Unit] = {
    try {
      data match {
        case dataset: Dataset[Row] =>
          // 从配置管理器获取Doris连接配置
          val dorisUrl = configManager.getString("jdbc.target.mysql.url")
          val dorisUser = configManager.getString("jdbc.target.mysql.user")
          val dorisPassword = configManager.getString("jdbc.target.mysql.pwd")
          val dorisDriver = configManager.getString("jdbc.mysql.driver")

          val props = new Properties()
          props.setProperty("user", dorisUser)
          props.setProperty("password", dorisPassword)

          val tableName = configManager.getString("doris.target.table.name")
          val batchSize = config.getOrElse("batchSize", 2000).toString.toInt
          val enableCache = config.getOrElse("enableCache", false).toString.toBoolean

          new Dataset2Doris(dataset, dorisUrl, dorisDriver, tableName, props, batchSize, enableCache).doInsert()
          Right(())
        case _ =>
          Left(EtlException("Input data is not a Dataset[Row]"))
      }
    } catch {
      case e: Exception =>
        Left(EtlException(s"Failed to write to Doris: ${e.getMessage}", e))
    }
  }
}