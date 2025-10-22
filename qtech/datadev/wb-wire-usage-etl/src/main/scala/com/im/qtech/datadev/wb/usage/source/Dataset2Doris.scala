package com.im.qtech.datadev.wb.usage.source

import org.apache.spark.sql.{Dataset, Row}
import org.slf4j.LoggerFactory

import java.sql.{Connection, DriverManager, PreparedStatement}
import java.util.Properties

/**
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/22
 */

class Dataset2Doris(
                     dataset: Dataset[Row],
                     jdbcUrl: String,
                     driverClass: String,
                     tableName: String,
                     props: Properties,
                     batchSize: Int,
                     enableCache: Boolean
                   ) {

  private val logger = LoggerFactory.getLogger(classOf[Dataset2Doris])

  def doInsert(): Unit = {
    try {
      // 注册JDBC驱动
      Class.forName(driverClass)

      // 如果启用缓存，则缓存数据集
      val data = if (enableCache) dataset.cache() else dataset

      // 获取数据集的列信息
      val columns = data.columns
      val columnList = columns.mkString(", ")
      val placeholders = columns.map(_ => "?").mkString(", ")

      // 构建INSERT语句
      val insertSQL = s"INSERT INTO $tableName ($columnList) VALUES ($placeholders)"

      // 收集数据并批量插入
      val rows = data.collect()
      var connection: Connection = null
      var statement: PreparedStatement = null

      try {
        connection = DriverManager.getConnection(jdbcUrl, props)
        connection.setAutoCommit(false)

        statement = connection.prepareStatement(insertSQL)
        var count = 0

        rows.foreach { row =>
          // 设置参数值
          for (i <- columns.indices) {
            val value = row.get(i)
            statement.setObject(i + 1, value)
          }

          statement.addBatch()
          count += 1

          // 批量执行
          if (count % batchSize == 0) {
            statement.executeBatch()
            connection.commit()
            logger.info(s">>>>> Inserted $count rows into Doris table $tableName")
          }
        }

        // 执行剩余的批次
        if (count % batchSize != 0) {
          statement.executeBatch()
          connection.commit()
        }

        logger.info(s">>>>> Successfully inserted total $count rows into Doris table $tableName")
      } finally {
        if (statement != null) statement.close()
        if (connection != null) connection.close()
      }
    } catch {
      case e: Exception =>
        logger.error(s">>>>> Failed to insert data into Doris table $tableName: ${e.getMessage}", e)
        throw e
    }
  }
}