// src/main/scala/com/im/qtech/data/svc/EqpCtxEtlJob.scala

package com.im.qtech.data.svc

import com.im.qtech.data.config.{DppConfigManager, TargetType}
import com.im.qtech.data.sink.{DorisDataSink, OracleDataSink, PostgresDataSink}
import com.im.qtech.data.source.DorisDataSource
import com.im.qtech.data.transformer.EqpCtxDataTransformer
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.im.etl.core.{AbstractEtlJob, EtlContext}
import org.im.etl.exception.EtlException
import org.slf4j.LoggerFactory

import scala.collection.mutable.ListBuffer

/**
 * 设备上下文数据ETL作业实现
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/28
 */

class EqpCtxEtlJob extends AbstractEtlJob {
  private val logger = LoggerFactory.getLogger(classOf[EqpCtxEtlJob])

  // 同步任务配置
  private case class SyncTaskConfig(
                             sourceTable: String,
                             targetTable: String,
                             targetType: TargetType, // DORIS, ORACLE or POSTGRESQL
                             primaryKey: String,
                             keepTempTable: Boolean
                           )

  override def name: String = "EqpCtxJob"

  override protected def validate(context: EtlContext): Either[EtlException, Unit] = {
    Right(())
  }

  override protected def extract(context: EtlContext): Either[EtlException, Any] = {
    try {
      // 获取Spark会话
      val sparkSession = context.getAttribute[SparkSession]("sparkSession")
        .getOrElse(throw new IllegalStateException("SparkSession not found in context"))

      // 定义同步任务配置
      val taskConfigs = List(
        // Doris -> Doris
        SyncTaskConfig("QT_MODEL_EMS.T_PBOX_INFO", "qtech_eq_ods.ems_t_pbox_info", TargetType.DORIS, "PBOX_ID", false),
        SyncTaskConfig("QT_MODEL_EMS.V_CODE_NAME", "qtech_eq_ods.ems_v_code_name", TargetType.DORIS, "HIERARCHY_SHOW_CODE", false),
        SyncTaskConfig("QT_MODEL_EMS.T_COLLECTOR_PROGRAM", "qtech_eq_ods.ems_t_collector_program", TargetType.DORIS, "ID", false),
        SyncTaskConfig("QT_MODEL_EMS.T_DEVICE_CALCGD1JH1U82GWWIONK", "qtech_eq_ods.ems_t_device", TargetType.DORIS, "ID", false),

        // Doris -> Oracle
        SyncTaskConfig("QT_MODEL_EMS.T_PBOX_INFO", "IMBIZ.EMS_T_PBOX_INFO", TargetType.ORACLE, "PBOX_ID", true),
        SyncTaskConfig("QT_MODEL_EMS.V_CODE_NAME", "IMBIZ.EMS_V_CODE_NAME", TargetType.ORACLE, "HIERARCHY_SHOW_CODE", true),
        SyncTaskConfig("QT_MODEL_EMS.T_COLLECTOR_PROGRAM", "IMBIZ.EMS_T_COLLECTOR_PROGRAM", TargetType.ORACLE, "ID", true),
        SyncTaskConfig("QT_MODEL_EMS.T_DEVICE_CALCGD1JH1U82GWWIONK", "IMBIZ.EMS_T_DEVICE", TargetType.ORACLE, "ID", true),

        // Doris -> PostgreSQL
        SyncTaskConfig("QT_MODEL_EMS.T_COLLECTOR_PROGRAM", "ems_t_collector_program", TargetType.POSTGRESQL, "ID", false),
        SyncTaskConfig("QT_MODEL_EMS.V_CODE_NAME", "ems_v_code_name", TargetType.POSTGRESQL, "HIERARCHY_SHOW_CODE", false),
        SyncTaskConfig("QT_MODEL_EMS.T_PBOX_INFO", "ems_t_pbox_info", TargetType.POSTGRESQL, "PBOX_ID", false),
        SyncTaskConfig("QT_MODEL_EMS.T_DEVICE_CALCGD1JH1U82GWWIONK", "ems_t_device", TargetType.POSTGRESQL, "ID", false)
      )

      val results = ListBuffer[(Dataset[Row], SyncTaskConfig)]()

      // 使用Doris数据源读取每个表的数据
      val dorisDataSource = new DorisDataSource(sparkSession)

      for (config <- taskConfigs) {
        val dorisConfig = Map(
          "url" -> DppConfigManager.getInstance().getString("jdbc.doris.source.url"),
          "user" -> DppConfigManager.getInstance().getString("jdbc.doris.source.user"),
          "password" -> DppConfigManager.getInstance().getString("jdbc.doris.source.pwd"),
          "driver" -> DppConfigManager.getInstance().getString("jdbc.doris.source.driver"),
          "dbtable" -> config.sourceTable
        )

        dorisDataSource.read(dorisConfig) match {
          case Right(data) =>
            results += ((data, config))
            logger.info(s">>>>> Successfully extracted data from ${config.sourceTable}")
          case Left(error) =>
            logger.error(s">>>>> Failed to extract data from ${config.sourceTable}: ${error.getMessage}")
            throw error
        }
      }

      Right(results.toList)
    } catch {
      case e: Exception =>
        Left(EtlException(s"Failed to extract data: ${e.getMessage}", e))
    }
  }

  override protected def transform(context: EtlContext, data: Any): Either[EtlException, Any] = {
    try {
      data match {
        case dataList: List[(Dataset[Row], SyncTaskConfig)] =>
          val transformer = new EqpCtxDataTransformer()
          val results = ListBuffer[(Dataset[Row], SyncTaskConfig)]()

          for ((dataset, config) <- dataList) {
            transformer.transform(dataset) match {
              case Right(transformedData) =>
                results += ((transformedData, config))
                logger.info(s">>>>> Successfully transformed data for ${config.sourceTable}")
              case Left(error) =>
                logger.error(s">>>>> Failed to transform data for ${config.sourceTable}: ${error.getMessage}")
                throw error
            }
          }

          Right(results.toList)
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
        case dataList: List[(Dataset[Row], SyncTaskConfig)] =>
          // 获取Spark会话用于Oracle sink
          val sparkSession = context.getAttribute[SparkSession]("sparkSession")
            .getOrElse(throw new IllegalStateException("SparkSession not found in context"))

          for ((dataset, config) <- dataList) {
            config.targetType match {
              case TargetType.DORIS =>
                val dorisSink = new DorisDataSink()
                val dorisConfig = Map(
                  "targetTable" -> config.targetTable
                )

                dorisSink.write(dataset, dorisConfig) match {
                  case Right(_) =>
                    logger.info(s">>>>> Successfully loaded data to Doris table ${config.targetTable}")
                  case Left(error) =>
                    logger.error(s">>>>> Failed to load data to Doris table ${config.targetTable}: ${error.getMessage}")
                    throw error
                }

              case TargetType.ORACLE =>
                val oracleSink = new OracleDataSink(sparkSession)
                val oracleConfig = Map(
                  "targetTable" -> config.targetTable,
                  "primaryKey" -> config.primaryKey,
                  "keepTempTable" -> config.keepTempTable
                )

                oracleSink.write(dataset, oracleConfig) match {
                  case Right(_) =>
                    logger.info(s">>>>> Successfully loaded data to Oracle table ${config.targetTable}")
                  case Left(error) =>
                    logger.error(s">>>>> Failed to load data to Oracle table ${config.targetTable}: ${error.getMessage}")
                    throw error
                }

              case TargetType.POSTGRESQL =>
                val postgresSink = new PostgresDataSink()
                val postgresConfig = Map(
                  "targetTable" -> config.targetTable,
                  "primaryKey" -> config.primaryKey,
                  "keepTempTable" -> config.keepTempTable
                )

                postgresSink.write(dataset, postgresConfig) match {
                  case Right(_) =>
                    logger.info(s">>>>> Successfully loaded data to PostgreSQL table ${config.targetTable}")
                  case Left(error) =>
                    logger.error(s">>>>> Failed to load data to PostgreSQL table ${config.targetTable}: ${error.getMessage}")
                    throw error
                }

              case _ =>
                throw new IllegalArgumentException(s"Unsupported target type: ${config.targetType}")
            }
          }

          Right(())
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
    logger.error(s">>>>> Error in EqpCtxJob: ${exception.getMessage}", exception)
  }
}