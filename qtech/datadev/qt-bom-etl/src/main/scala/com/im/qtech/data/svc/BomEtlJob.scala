package com.im.qtech.data.svc

import com.im.qtech.data.config.BomConfigManager
import com.im.qtech.data.sink.{DorisDataSink, PostgresDataSink}
import com.im.qtech.data.source.JdbcDataSource
import com.im.qtech.data.transformer.BomDataTransformer
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.im.common.batch.monitor.JobMetrics
import org.im.etl.core.{AbstractEtlJob, EtlContext}
import org.im.etl.exception.EtlException
import org.slf4j.LoggerFactory

/**
 * BOM ETL作业实现
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/30
 */
class BomEtlJob extends AbstractEtlJob {
  private val logger = LoggerFactory.getLogger(classOf[BomEtlJob])

  // 从配置管理器获取配置
  private val configManager = BomConfigManager.getInstance()

  // Oracle配置
  private val ORACLE_JDBC_DRIVER = configManager.getString("jdbc.oracle.driver")
  private val ORACLE_JDBC_URL = configManager.getString("jdbc.oracle.url")
  private val ORACLE_USER = configManager.getString("jdbc.oracle.user")
  private val ORACLE_PWD = configManager.getString("jdbc.oracle.pwd")

  // 目标配置
  private val TARGET_DORIS_URL = configManager.getString("jdbc.target.mysql.url")
  private val TARGET_DORIS_USER = configManager.getString("jdbc.target.mysql.user")
  private val TARGET_DORIS_PWD = configManager.getString("jdbc.target.mysql.pwd")
  private val MYSQL_JDBC_DRIVER = configManager.getString("jdbc.mysql.driver")
  private val RES_DORIS_TABLE_NAME = configManager.getString("doris.target.table.name")

  // PostgreSQL配置
  private val POSTGRES_JDBC_DRIVER = configManager.getString("jdbc.postgres.driver")
  private val POSTGRES_JDBC_URL = configManager.getString("jdbc.postgres.url")
  private val POSTGRES_USER = configManager.getString("jdbc.postgres.user")
  private val POSTGRES_PWD = configManager.getString("jdbc.postgres.pwd")

  override def name: String = "BomEtlJob"

  override protected def validate(context: EtlContext): Either[EtlException, Unit] = {
    // 在这里可以添加参数验证逻辑
    Right(())
  }

  override protected def extract(context: EtlContext): Either[EtlException, Any] = {
    try {
      // 获取Spark会话
      val sparkSession = context.getAttribute[SparkSession]("sparkSession")
        .getOrElse(throw new IllegalStateException("SparkSession not found in context"))

      // 构建查询SQL - 需要使用原始项目的SQL
      val bomSql =
        """select distinct id, steps, replace_cpn, upn, source, name, spec, unit, qty, waste, position, status, toplimit, cast(id1 as integer) as id1, nvl(ta.mdate, tb.mdate) mdate, nvl(tb.muser, ta.muser) muser, cast(is_do as integer) as is_do, remarks, version, op_code, series_code, part_code, part_name, part_spec, type, type2, pixel, process_spec
          |from (select id, steps, replace_cpn, upn, source, name, spec, unit, cast(qty as DOUBLE PRECISION) as qty, waste, position, status, toplimit, id1, mdate, muser, is_do, remarks, version, op_code, series_code
          |      from aa_bom ta
          |      where upn in ('640700000002', '640700000008', '640700000001', '6-9999B2655QT', '640700000004', '640700000005', '6-999901450QT',
          |                    '640700000007', 'S4201000020F')) ta
          |         inner join i_material tb on ta.id = tb.part_code
          |where IS_DO = 1 and length(part_spec) = lengthb(part_spec) and REPLACE_CPN = '主料'""".stripMargin

      logger.info(s">>>>> Executing SQL query: $bomSql")

      // 使用JDBC数据源读取数据
      val jdbcDataSource = new JdbcDataSource(sparkSession)
      val jdbcConfig = Map(
        "url" -> ORACLE_JDBC_URL,
        "user" -> ORACLE_USER,
        "password" -> ORACLE_PWD,
        "driver" -> ORACLE_JDBC_DRIVER,
        "query" -> bomSql
      )

      val extractedData = jdbcDataSource.read(jdbcConfig) match {
        case Right(data) => data
        case Left(error) => throw error
      }

      Right(extractedData)
    } catch {
      case e: Exception =>
        Left(EtlException(s"Failed to extract data: ${e.getMessage}", e))
    }
  }

  override protected def transform(context: EtlContext, data: Any): Either[EtlException, Any] = {
    try {
      data match {
        case dataset: Dataset[Row] =>
          // 使用数据转换器进行转换
          val transformer = new BomDataTransformer()
          val transformedData = transformer.transform(dataset) match {
            case Right(data) => data
            case Left(error) => throw error
          }

          Right(transformedData)
        case _ =>
          Left(EtlException("Invalid data format for transformation"))
      }
    } catch {
      case e: Exception =>
        Left(EtlException(s"Failed to transform data: ${e.getMessage}", e))
    }
  }

  override protected def load(context: EtlContext, data: Any): Either[EtlException, Unit] = {
    try {
      data match {
        case dataset: Dataset[Row] =>
          // 使用Doris数据目标写入数据
          val dorisSink = new DorisDataSink()
          val dorisConfig = Map(
            "batchSize" -> 2000,
            "driver" -> MYSQL_JDBC_DRIVER,
            "url" -> TARGET_DORIS_URL,
            "table" -> RES_DORIS_TABLE_NAME,
            "user" -> TARGET_DORIS_USER,
            "password" -> TARGET_DORIS_PWD,
            "charset" -> "UTF-8"
          )

          dorisSink.write(dataset, dorisConfig) match {
            case Right(_) =>
              logger.info(">>>>> Data successfully written to Doris")
            case Left(error) =>
              logger.error(">>>>> Failed to write data to Doris", error)
              throw error
          }

          // 使用PostgreSQL数据目标写入数据
          val postgresSink = new PostgresDataSink()
          val postgresConfig = Map(
            "batchSize" -> 2000,
            "driver" -> POSTGRES_JDBC_DRIVER,
            "url" -> POSTGRES_JDBC_URL,
            "table" -> "mes_aa_bom",
            "user" -> POSTGRES_USER,
            "password" -> POSTGRES_PWD
          )

          postgresSink.write(dataset, postgresConfig) match {
            case Right(_) =>
              // 记录插入的数据条数
              val cnt = dataset.cache().count()
              logger.info(s">>>>> AA BOM数据插入PostgreSQL完成，共${cnt}条！")

              // 更新JobMetrics中的processedRecords
              context.getAttribute[JobMetrics]("jobMetrics") match {
                case Some(metrics) => metrics.setProcessedRecords(cnt)
                case None => logger.warn("JobMetrics not found in context")
              }

              Right(())
            case Left(error) =>
              throw error
          }
        case _ =>
          Left(EtlException("Invalid data format for loading"))
      }
    } catch {
      case e: Exception =>
        Left(EtlException(s"Failed to load data: ${e.getMessage}", e))
    }
  }

  override protected def handleError(exception: EtlException, context: EtlContext): Unit = {
    super.handleError(exception, context)
    logger.error(s">>>>> Error in BomEtlJob: ${exception.getMessage}", exception)
  }
}
