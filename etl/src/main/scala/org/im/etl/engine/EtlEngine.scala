package org.im.etl.engine

import org.im.etl.core.{EtlContext, EtlJob}
import org.im.etl.exception.EtlException

/**
 * ETL引擎特质，定义ETL作业执行的核心接口
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/22
 */
trait EtlEngine {
  /**
   * 执行单个ETL作业
   *
   * @param job     ETL作业
   * @param context ETL上下文
   * @return 执行结果
   */
  def executeJob(job: EtlJob, context: EtlContext): Either[EtlException, Unit]

  /**
   * 执行多个ETL作业
   *
   * @param jobs    ETL作业序列
   * @param context ETL上下文
   * @return 执行结果列表
   */
  def executeJobs(jobs: Seq[EtlJob], context: EtlContext): Seq[Either[EtlException, Unit]]
}
