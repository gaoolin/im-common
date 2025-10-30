package com.im.qtech.data.sink

import com.im.qtech.data.config.DppConfigManager
import org.apache.spark.sql.{Dataset, Row, SaveMode}
import org.im.etl.component.DataSink
import org.im.etl.exception.EtlException

import java.util.Properties

/**
 * Doris数据目标实现
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/28
 */

class DorisDataSink extends DataSink {
  override type WriteConfig = Map[String, Any]

  private val configManager = DppConfigManager.getInstance()

  override def write(data: Any, config: WriteConfig): Either[EtlException, Unit] = {
    try {
      data match {
        case dataset: Dataset[Row] =>
          val targetTable = config.getOrElse("targetTable", "").toString

          // 从配置管理器获取Doris连接配置
          val dorisUrl = configManager.getString("jdbc.doris.target.url")
          val dorisUser = configManager.getString("jdbc.doris.target.user")
          val dorisPassword = configManager.getString("jdbc.doris.target.pwd")
          val dorisDriver = configManager.getString("jdbc.doris.target.driver")

          // 注册驱动
          Class.forName(dorisDriver)

          val props = new Properties()
          props.setProperty("user", dorisUser)
          props.setProperty("password", dorisPassword)

          dataset.write
            .format("jdbc")
            .option("url", dorisUrl)
            .option("user", dorisUser)
            .option("password", dorisPassword)
            .option("driver", dorisDriver)
            .option("dbtable", targetTable)
            .mode(SaveMode.Append)
            .save()

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
