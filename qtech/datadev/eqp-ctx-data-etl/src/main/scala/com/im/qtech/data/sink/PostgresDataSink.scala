package com.im.qtech.data.sink

import com.im.qtech.data.config.DppConfigManager
import org.apache.spark.sql.{Dataset, Row, SaveMode}
import org.slf4j.LoggerFactory

import java.sql.{Connection, DriverManager, ResultSet}

class PostgresDataSink {

  private val logger = LoggerFactory.getLogger(classOf[PostgresDataSink])
  private val configManager = DppConfigManager.getInstance()
  private val jdbcUrl = configManager.getString("jdbc.postgres.url")
  private val jdbcUser = configManager.getString("jdbc.postgres.user")
  private val jdbcPassword = configManager.getString("jdbc.postgres.pwd")

  private def getConnection(): Connection = {

    DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword)
  }

  /** 主入口写方法 */
  def write(dataset: Dataset[Row], config: Map[String, Any]): Either[Throwable, Unit] = {
    try {
      val targetTable = config("targetTable").toString
      val primaryKey = config("primaryKey").toString
      val keepTempTable = config.getOrElse("keepTempTable", false).asInstanceOf[Boolean]

      val tmpTable = s"${targetTable}_tmp"

      val df = dataset.toDF(dataset.columns.map(_.toLowerCase): _*) // 全部转小写列名

      val conn = getConnection()
      try {
        // 如果临时表不存在，创建
        if (!tableExists(conn, tmpTable)) {
          logger.info(s"Temporary table $tmpTable does not exist, creating from $targetTable")
          createTempTable(conn, targetTable, tmpTable)
        } else {
          // 临时表存在，先清空
          executeSql(conn, s"TRUNCATE TABLE $tmpTable")
        }
      } finally conn.close()

      // 写入临时表
      df.write
        .format("jdbc")
        .option("url", jdbcUrl)
        .option("user", jdbcUser)
        .option("password", jdbcPassword)
        .option("dbtable", tmpTable)
        .mode(SaveMode.Append)
        .save()

      // 执行 UPSERT
      val upsertSql = buildUpsertSql(targetTable, tmpTable, primaryKey, df)
      val upsertConn = getConnection()
      try {
        executeSql(upsertConn, upsertSql)
      } finally upsertConn.close()

      // 删除临时表，如果配置不保留
      if (!keepTempTable) {
        val dropConn = getConnection()
        try {
          executeSql(dropConn, s"DROP TABLE IF EXISTS $tmpTable")
        } finally dropConn.close()
      }

      Right(())
    } catch {
      case e: Throwable => Left(e)
    }
  }

  /** 检查表是否存在 */
  private def tableExists(conn: Connection, table: String): Boolean = {
    val rs: ResultSet = conn.getMetaData.getTables(null, null, table, Array("TABLE"))
    try rs.next() finally rs.close()
  }

  /** 根据目标表创建临时表 */
  private def createTempTable(conn: Connection, targetTable: String, tmpTable: String): Unit = {
    val sql = s"CREATE TABLE $tmpTable AS TABLE $targetTable WITH NO DATA"
    executeSql(conn, sql)
  }

  /** 执行 SQL */
  private def executeSql(conn: Connection, sql: String): Unit = {
    val stmt = conn.createStatement()
    try {
      logger.info(s"Executing PostgreSQL SQL:\n$sql")
      stmt.execute(sql)
    } finally stmt.close()
  }

  /** 构建 UPSERT SQL，列名全小写 */
  private def buildUpsertSql(targetTable: String, tmpTable: String, primaryKey: String, dataset: Dataset[Row]): String = {
    val cols = dataset.columns.map(_.toLowerCase)

    val updateSets = cols.filterNot(_ == primaryKey.toLowerCase)
      .map(c => s""""$c" = EXCLUDED."$c"""")
      .mkString(", ")

    s"""
       |INSERT INTO $targetTable (${cols.map(c => s""""$c"""").mkString(", ")})
       |SELECT ${cols.map(c => s""""$c"""").mkString(", ")} FROM $tmpTable
       |ON CONFLICT ("${primaryKey.toLowerCase}")
       |DO UPDATE SET $updateSets
       |""".stripMargin
  }
}