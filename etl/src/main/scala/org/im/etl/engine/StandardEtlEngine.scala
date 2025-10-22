package org.im.etl.engine

import org.im.etl.core.{EtlContext, EtlJob}
import org.im.etl.exception.EtlException
import org.slf4j.LoggerFactory

/**
 * 标准ETL引擎实现
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/22
 */
class StandardEtlEngine extends EtlEngine {
  private val logger = LoggerFactory.getLogger(classOf[StandardEtlEngine])

  override def executeJob(job: EtlJob, context: EtlContext): Either[EtlException, Unit] = {
    try {
      logger.info(s"Executing ETL job: ${job.name}")
      val result = job.execute(context)
      result match {
        case Right(_) =>
          logger.info(s"ETL job ${job.name} executed successfully")
        case Left(error) =>
          logger.error(s"ETL job ${job.name} failed", error)
      }
      result
    } catch {
      case e: Exception =>
        val etlException = EtlException(s"Failed to execute job: ${job.name}", e)
        logger.error(s"ETL job ${job.name} failed with exception", e)
        Left(etlException)
    }
  }

  override def executeJobs(jobs: Seq[EtlJob], context: EtlContext): Seq[Either[EtlException, Unit]] = {
    jobs.map(job => executeJob(job, context))
  }
}
