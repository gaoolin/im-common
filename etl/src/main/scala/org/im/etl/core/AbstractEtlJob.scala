package org.im.etl.core

import org.im.etl.exception.EtlException
import org.slf4j.LoggerFactory

/**
 * 抽象ETL作业模板，提供ETL作业的标准执行流程
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/22
 */
abstract class AbstractEtlJob extends EtlJob {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private var currentStatus: JobStatus.JobStatus = JobStatus.Pending

  override def execute(context: EtlContext): Either[EtlException, Unit] = {
    try {
      logger.info(s"Starting ETL job: ${name}")
      currentStatus = JobStatus.Running

      validate(context) match {
        case Left(error) =>
          currentStatus = JobStatus.Failed
          logger.error(s"Validation failed for job: ${name}", error)
          return Left(error)
        case Right(_) => // Validation passed
      }

      extract(context) match {
        case Left(error) =>
          currentStatus = JobStatus.Failed
          logger.error(s"Extract phase failed for job: ${name}", error)
          return Left(error)
        case Right(extractedData) =>
          transform(context, extractedData) match {
            case Left(error) =>
              currentStatus = JobStatus.Failed
              logger.error(s"Transform phase failed for job: ${name}", error)
              return Left(error)
            case Right(transformedData) =>
              load(context, transformedData) match {
                case Left(error) =>
                  currentStatus = JobStatus.Failed
                  logger.error(s"Load phase failed for job: ${name}", error)
                  return Left(error)
                case Right(_) =>
                  currentStatus = JobStatus.Success
                  logger.info(s"ETL job completed successfully: ${name}")
                  return Right(())
              }
          }
      }
    } catch {
      case e: Exception =>
        currentStatus = JobStatus.Failed
        val etlException = EtlException(s"Unexpected error in ETL job: ${name}", e)
        handleError(etlException, context)
        logger.error(s"ETL job failed: ${name}", e)
        Left(etlException)
    }
  }

  override def status: JobStatus.JobStatus = currentStatus

  /**
   * 验证上下文参数
   *
   * @param context ETL上下文
   * @return 验证结果
   */
  protected def validate(context: EtlContext): Either[EtlException, Unit]

  /**
   * 抽取数据
   *
   * @param context ETL上下文
   * @return 抽取的数据集
   */
  protected def extract(context: EtlContext): Either[EtlException, Any]

  /**
   * 转换数据
   *
   * @param context ETL上下文
   * @param data    原始数据
   * @return 转换后的数据
   */
  protected def transform(context: EtlContext, data: Any): Either[EtlException, Any]

  /**
   * 加载数据
   *
   * @param context ETL上下文
   * @param data    转换后的数据
   * @return 加载结果
   */
  protected def load(context: EtlContext, data: Any): Either[EtlException, Unit]

  /**
   * 处理错误
   *
   * @param exception 异常
   * @param context   ETL上下文
   */
  protected def handleError(exception: EtlException, context: EtlContext): Unit = {
    logger.error(s"Error in ETL job: ${name}", exception)
  }
}
