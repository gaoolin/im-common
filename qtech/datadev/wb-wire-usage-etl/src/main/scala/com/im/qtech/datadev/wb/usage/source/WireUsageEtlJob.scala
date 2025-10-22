package com.im.qtech.datadev.wb.usage.source

import com.im.qtech.datadev.wb.usage.config.DppConfigManager
import com.im.qtech.datadev.wb.usage.util.ImHttpClient
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.im.etl.core.{AbstractEtlJob, EtlContext}
import org.im.etl.exception.EtlException
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsError, JsSuccess, Json}

import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}

/**
 * 金线使用ETL作业实现
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/22
 */
class WireUsageEtlJob extends AbstractEtlJob {
  private val logger = LoggerFactory.getLogger(classOf[WireUsageEtlJob])

  // 从配置管理器获取配置
  private val configManager = DppConfigManager.getInstance()

  // Doris配置
  private val SOURCE_DORIS_URL = configManager.getString("jdbc.source.mysql.url")
  private val SOURCE_DORIS_USER = configManager.getString("jdbc.source.mysql.user")
  private val SOURCE_DORIS_PWD = configManager.getString("jdbc.source.mysql.pwd")
  private val MYSQL_JDBC_DRIVER = configManager.getString("jdbc.mysql.driver")

  // 目标配置
  private val TARGET_DORIS_URL = configManager.getString("jdbc.target.mysql.url")
  private val TARGET_DORIS_USER = configManager.getString("jdbc.target.mysql.user")
  private val TARGET_DORIS__PWD = configManager.getString("jdbc.target.mysql.pwd")
  private val RES_DORIS_TABLE_NAME = configManager.getString("doris.target.table.name")

  // 服务URL配置
  private val IM_SERVICE_URL_SPARK_JOB_DT = configManager.getString("spark.job.service.dt.url")
  private val IM_SERVICE_URL_SPARK_JOB_DT_PROD = configManager.getString("spark.job.service.dt.url.prod")
  private val IM_SERVICE_URL_SPARK_JOB_SQL = configManager.getString("spark.job.service.sql.url")
  private val IM_SERVICE_URL_SPARK_JOB_SQL_PROD = configManager.getString("spark.job.service.sql.url.prod")

  override def name: String = "WireUsageEtlJob"

  override protected def validate(context: EtlContext): Either[EtlException, Unit] = {
    // 在这里可以添加参数验证逻辑
    Right(())
  }

  override protected def extract(context: EtlContext): Either[EtlException, Any] = {
    try {
      // 获取Spark会话
      val sparkSession = context.getAttribute[SparkSession]("sparkSession")
        .getOrElse(throw new IllegalStateException("SparkSession not found in context"))

      // 获取master URL
      val masterUrl = sparkSession.sparkContext.master

      // HTTP请求头
      val header = ImHttpClient.buildHeaders(Map("Content-Type" -> "application/json"))

      // 根据master URL选择URL
      val jobDtUrl = if (masterUrl == "local[*]") IM_SERVICE_URL_SPARK_JOB_DT else IM_SERVICE_URL_SPARK_JOB_DT_PROD
      val jobSqlUrl = if (masterUrl == "local[*]") IM_SERVICE_URL_SPARK_JOB_SQL else IM_SERVICE_URL_SPARK_JOB_SQL_PROD

      // 获取jobDt
      val responseJobDt: Either[Throwable, String] = ImHttpClient.sendGet(jobDtUrl, header).toEither
      val responseBody = responseJobDt match {
        case Right(value) => value
        case Left(error) => throw error
      }
      val responseJsonJobDt = Json.parse(responseBody)
      val jobDtOpt = (responseJsonJobDt \ "data" \ "jobDt").validate[String]

      val jobDt = jobDtOpt match {
        case JsSuccess(jobDtValue, _) =>
          logger.info(s">>>>> Retrieved job date: $jobDtValue")
          jobDtValue
        case JsError(errors) =>
          throw new Exception(">>>>> Failed to parse job date from response: " + errors.toString)
      }

      // 获取jobSql
      val responseJobSql: Either[Throwable, String] = ImHttpClient.sendGet(jobSqlUrl, header).toEither
      val responseJobSqlBody = responseJobSql match {
        case Right(value) => value
        case Left(error) => throw error
      }
      val responseJsonJobSql = Json.parse(responseJobSqlBody)
      val jobSqlOpt = (responseJsonJobSql \ "data").validate[String]
      val jobSql = jobSqlOpt match {
        case JsSuccess(jobSqlValue, _) =>
          val sql = jobSqlValue.replace(":start_time", s"'$jobDt'")
          logger.info(s">>>>> Retrieved job sql: $sql")
          sql
        case JsError(errors) =>
          throw new Exception(">>>>> Failed to parse job sql from response: " + errors.toString)
      }

      logger.info(s">>>>> 本次作业开始时间为：$jobDt")
      logger.info(s">>>>> Executing SQL query: $jobSql")

      // 使用JDBC数据源读取数据
      val jdbcDataSource = new JdbcDataSource(sparkSession)
      val jdbcConfig = Map(
        "url" -> SOURCE_DORIS_URL,
        "user" -> SOURCE_DORIS_USER,
        "password" -> SOURCE_DORIS_PWD,
        "driver" -> MYSQL_JDBC_DRIVER,
        "query" -> jobSql
      )

      val extractedData = jdbcDataSource.read(jdbcConfig) match {
        case Right(data) => data
        case Left(error) => throw error
      }

      // 将jobDt和jobSql添加到上下文中供后续步骤使用
      val enrichedContext = context
        .withAttribute("jobDt", jobDt)
        .withAttribute("jobSql", jobSql)
        .withAttribute("jobDtUrl", jobDtUrl)
        .withAttribute("header", header)

      Right((extractedData, enrichedContext))
    } catch {
      case e: Exception =>
        Left(EtlException(s"Failed to extract data: ${e.getMessage}", e))
    }
  }

  override protected def transform(context: EtlContext, data: Any): Either[EtlException, Any] = {
    try {
      data match {
        case (dataset: Dataset[Row], enrichedContext: EtlContext) =>
          // 使用数据转换器进行转换
          val transformer = new WireUsageDataTransformer()
          val transformedData = transformer.transform(dataset) match {
            case Right(data) => data
            case Left(error) => throw error
          }

          Right((transformedData, enrichedContext))
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
        case (dataset: Dataset[Row], enrichedContext: EtlContext) =>
          // 计算下次作业时间
          val nextJobDtDataset = dataset.select(max(col("create_date")).alias("JOB_DT"))

          if (!nextJobDtDataset.isEmpty) {
            // 使用Doris数据目标写入数据
            val dorisSink = new DorisDataSink()
            val dorisConfig = Map(
              "driver" -> MYSQL_JDBC_DRIVER,
              "url" -> TARGET_DORIS_URL,
              "table" -> RES_DORIS_TABLE_NAME,
              "user" -> TARGET_DORIS_USER,
              "password" -> TARGET_DORIS__PWD,
              "charset" -> "UTF-8"
            )

            dorisSink.write(dataset, dorisConfig) match {
              case Right(_) =>
                // 更新作业时间
                val jobDtTimestamp = nextJobDtDataset.collect()(0).getAs[Timestamp]("JOB_DT")
                val targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())
                val formattedJobDtStr = LocalDateTime.ofInstant(jobDtTimestamp.toInstant, ZoneId.systemDefault()).format(targetFormatter)

                val jobDtUrl = enrichedContext.getAttribute[String]("jobDtUrl")
                  .getOrElse(throw new IllegalStateException("jobDtUrl not found in context"))
                val header = enrichedContext.getAttribute[Map[String, String]]("header")
                  .getOrElse(throw new IllegalStateException("header not found in context"))

                val params: Map[String, Any] = Map("jobDt" -> formattedJobDtStr)
                val putParams = ImHttpClient.buildJsonBody(params)

                val responseUpdateDt = ImHttpClient.sendPut(jobDtUrl, putParams, header) match {
                  case scala.util.Success(value) => value
                  case scala.util.Failure(exception) => throw new RuntimeException("Failed to update job date", exception)
                }

                logger.info(s">>>>> 更新作业时间成功，返回结果：$responseUpdateDt")

                // 记录插入的数据条数
                val cnt = dataset.cache().count()
                logger.info(s">>>>> 数据插入 doris 完成，共${cnt}条！")

                Right(())
              case Left(error) =>
                throw error
            }
          } else {
            logger.error(">>>>> 下次作业时间不规范！")
            Left(EtlException("Invalid next job date"))
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
    logger.error(s">>>>> Error in WireUsageEtlJob: ${exception.getMessage}", exception)
  }
}
