package org.im.etl.engine

import org.im.etl.config.EtlConfig
import org.im.etl.core.{EtlContext, EtlJob, JobStatus}
import org.im.etl.exception.EtlException
import org.slf4j.LoggerFactory

import scala.collection.mutable

/**
 * ETL作业管理器，提供作业注册、管理和调度功能
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/22
 */
class EtlJobManager(engine: EtlEngine) {
  private val logger = LoggerFactory.getLogger(classOf[EtlJobManager])
  private val jobs = mutable.Map[String, EtlJob]()
  private val jobConfigs = mutable.Map[String, EtlConfig]()

  /**
   * 注册ETL作业
   *
   * @param job    ETL作业
   * @param config 作业配置
   */
  def registerJob(job: EtlJob, config: EtlConfig): Unit = {
    jobs.put(job.name, job)
    jobConfigs.put(job.name, config)
    logger.info(s"Registered ETL job: ${job.name}")
  }

  /**
   * 根据作业名称获取作业
   *
   * @param jobName 作业名称
   * @return 作业实例
   */
  def getJob(jobName: String): Option[EtlJob] = jobs.get(jobName)

  /**
   * 获取所有注册的作业名称
   *
   * @return 作业名称列表
   */
  def getJobNames: Seq[String] = jobs.keys.toSeq

  /**
   * 执行指定名称的作业
   *
   * @param jobName 作业名称
   * @param context ETL上下文
   * @return 执行结果
   */
  def executeJobByName(jobName: String, context: EtlContext): Either[EtlException, Unit] = {
    jobs.get(jobName) match {
      case Some(job) =>
        engine.executeJob(job, context)
      case None =>
        val error = EtlException(s"Job not found: $jobName")
        logger.error(s"Job not found: $jobName")
        Left(error)
    }
  }

  /**
   * 执行所有注册的作业
   *
   * @param context ETL上下文
   * @return 执行结果列表
   */
  def executeAllJobs(context: EtlContext): Map[String, Either[EtlException, Unit]] = {
    jobs.map { case (name, job) =>
      name -> engine.executeJob(job, context)
    }.toMap
  }

  /**
   * 获取作业状态
   *
   * @param jobName 作业名称
   * @return 作业状态
   */
  def getJobStatus(jobName: String): Option[JobStatus.JobStatus] = {
    jobs.get(jobName).map(_.status)
  }

  /**
   * 移除作业
   *
   * @param jobName 作业名称
   */
  def removeJob(jobName: String): Unit = {
    jobs.remove(jobName)
    jobConfigs.remove(jobName)
    logger.info(s"Removed ETL job: $jobName")
  }
}
