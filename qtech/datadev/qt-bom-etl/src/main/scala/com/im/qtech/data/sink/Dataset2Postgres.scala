package com.im.qtech.data.sink

import org.apache.spark.sql.{Dataset, Row}
import org.slf4j.LoggerFactory

import java.sql.{Connection, DriverManager, PreparedStatement, Timestamp}
import java.util.Properties
import java.time.LocalDateTime

/**
 * Dataset到PostgreSQL的数据写入实现
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/31
 */
class Dataset2Postgres(
    dataset: Dataset[Row],
    jdbcUrl: String,
    driverClass: String,
    tableName: String,
    props: Properties,
    batchSize: Int,
    enableCache: Boolean
) {

  private val logger = LoggerFactory.getLogger(classOf[Dataset2Postgres])

  def doInsert(): Unit = {
    try {
      // 注册JDBC驱动
      Class.forName(driverClass)

      // 如果启用缓存，则缓存数据集
      val data = if (enableCache) dataset.cache() else dataset

      // PostgreSQL的UPSERT语法 (ON CONFLICT)
      val columns = data.columns
      val columnList = columns.mkString(", ")
      val placeholders = columns.map(_ => "?").mkString(", ")

      // 根据mes_aa_bom表的唯一索引调整冲突键
      val conflictColumns = "id, upn"
      val updatePlaceholders = columns.filterNot(col => col == "id" || col == "upn")
        .map(col => s"$col = EXCLUDED.$col").mkString(", ")

      // 构建UPSERT语句
      val upsertSQL =
        s"""INSERT INTO $tableName ($columnList) VALUES ($placeholders)
           |ON CONFLICT ($conflictColumns)
           |DO UPDATE SET $updatePlaceholders""".stripMargin

      // 收集数据并批量插入
      val rows = data.collect()
      var connection: Connection = null
      var statement: PreparedStatement = null

      try {
        connection = DriverManager.getConnection(jdbcUrl, props)
        connection.setAutoCommit(false)

        statement = connection.prepareStatement(upsertSQL)
        var count = 0

        rows.foreach { row =>
          // 设置参数值
          for (i <- columns.indices) {
            val columnName = columns(i)
            val value = row.get(i)

            // 根据列名和数据类型设置适当的值
            columnName match {
              case "qty" =>
                // qty是FLOAT类型
                statement.setObject(i + 1, value)
              case "id1" | "is_do" =>
                // 整数类型字段
                statement.setObject(i + 1, value)
              case col if col.endsWith("date") || col == "update_time" =>
                // 时间戳字段处理
                if (value != null) {
                  statement.setTimestamp(i + 1, Timestamp.valueOf(LocalDateTime.now()))
                } else {
                  statement.setNull(i + 1, java.sql.Types.TIMESTAMP)
                }
              case _ =>
                // 其他字段使用默认处理
                statement.setObject(i + 1, value)
            }
          }

          statement.addBatch()
          count += 1

          // 批量执行
          if (count % batchSize == 0) {
            statement.executeBatch()
            connection.commit()
            logger.info(s">>>>> Upserted $count rows into PostgreSQL table $tableName")
          }
        }

        // 执行剩余的批次
        if (count % batchSize != 0) {
          statement.executeBatch()
          connection.commit()
        }

        logger.info(s">>>>> Successfully upserted total $count rows into PostgreSQL table $tableName")
      } finally {
        if (statement != null) statement.close()
        if (connection != null) connection.close()
      }
    } catch {
      case e: Exception =>
        logger.error(s">>>>> Failed to upsert data into PostgreSQL table $tableName: ${e.getMessage}", e)
        throw e
    }
  }
}
