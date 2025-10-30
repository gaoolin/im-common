package com.im.qtech.data.sink

import com.im.qtech.data.config.DppConfigManager
import org.apache.spark.sql.{Dataset, Row, SaveMode, SparkSession}
import org.im.etl.component.DataSink
import org.im.etl.exception.EtlException

import java.sql.{Connection, DriverManager, Statement}
import java.util.Properties

/**
 * Oracle数据目标实现
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/28
 */

class OracleDataSink(sparkSession: SparkSession) extends DataSink {
  override type WriteConfig = Map[String, Any]

  private val configManager = DppConfigManager.getInstance()

  override def write(data: Any, config: WriteConfig): Either[EtlException, Unit] = {
    try {
      data match {
        case dataset: Dataset[Row] =>
          val targetTable = config.getOrElse("targetTable", "").toString
          val primaryKey = config.getOrElse("primaryKey", "").toString
          val keepTempTable = config.getOrElse("keepTempTable", false).toString.toBoolean

          // 从配置管理器获取Oracle连接配置
          val oracleUrl = configManager.getString("jdbc.oracle.url")
          val oracleUser = configManager.getString("jdbc.oracle.user")
          val oraclePassword = configManager.getString("jdbc.oracle.pwd")
          val oracleDriver = configManager.getString("jdbc.oracle.driver")

          // 注册驱动
          Class.forName(oracleDriver)

          // 写入临时表
          val tmpTable = targetTable + "_TMP"

          // 清空临时表
          clearTable(tmpTable, oracleUrl, oracleUser, oraclePassword)

          // 写入数据到临时表
          val props = new Properties()
          props.setProperty("user", oracleUser)
          props.setProperty("password", oraclePassword)

          dataset.write
            .format("jdbc")
            .option("url", oracleUrl)
            .option("user", oracleUser)
            .option("password", oraclePassword)
            .option("driver", oracleDriver)
            .option("dbtable", tmpTable)
            .mode(SaveMode.Append)
            .save()

          // 执行MERGE操作
          val mergeSql = buildMergeSql(targetTable, tmpTable, primaryKey, dataset)
          executeOracleMerge(oracleUrl, oracleUser, oraclePassword, mergeSql)

          // 删除临时表（除非保留标志开启）
          if (!keepTempTable) {
            dropTempTable(tmpTable, oracleUrl, oracleUser, oraclePassword)
          }

          Right(())
        case _ =>
          Left(EtlException("Input data is not a Dataset[Row]"))
      }
    } catch {
      case e: Exception =>
        Left(EtlException(s"Failed to write to Oracle: ${e.getMessage}", e))
    }
  }

  private def clearTable(tableName: String, url: String, user: String, password: String): Unit = {
    var connection: Connection = null
    var statement: Statement = null

    try {
      connection = DriverManager.getConnection(url, user, password)
      statement = connection.createStatement()
      statement.execute(s"TRUNCATE TABLE $tableName")
    } finally {
      if (statement != null) statement.close()
      if (connection != null) connection.close()
    }
  }

  private def dropTempTable(tmpTable: String, url: String, user: String, password: String): Unit = {
    val sql = s"BEGIN EXECUTE IMMEDIATE 'DROP TABLE $tmpTable'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;"
    var connection: Connection = null
    var statement: Statement = null

    try {
      connection = DriverManager.getConnection(url, user, password)
      statement = connection.createStatement()
      statement.execute(sql)
    } finally {
      if (statement != null) statement.close()
      if (connection != null) connection.close()
    }
  }

  private def executeOracleMerge(url: String, user: String, password: String, sql: String): Unit = {
    var connection: Connection = null
    var statement: Statement = null

    try {
      connection = DriverManager.getConnection(url, user, password)
      statement = connection.createStatement()
      statement.executeUpdate(sql)
    } finally {
      if (statement != null) statement.close()
      if (connection != null) connection.close()
    }
  }

  private def buildMergeSql(targetTable: String, tmpTable: String, primaryKey: String, dataset: Dataset[Row]): String = {
    val columns = dataset.columns
    val updateFields = columns.filter(!_.equalsIgnoreCase(primaryKey))
      .map(col => s"""t."$col" = s."$col"""")
      .mkString(", ")

    val allFields = columns.map(col => s""""$col"""").mkString(", ")
    val allValues = columns.map(col => s"""s."$col"""").mkString(", ")

    s"""MERGE INTO $targetTable t
       |USING $tmpTable s
       |ON (t."$primaryKey" = s."$primaryKey")
       |WHEN MATCHED THEN UPDATE SET
       |  $updateFields
       |WHEN NOT MATCHED THEN INSERT (
       |  $allFields
       |) VALUES (
       |  $allValues
       |)""".stripMargin
  }
}
