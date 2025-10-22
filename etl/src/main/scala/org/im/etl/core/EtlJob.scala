package org.im.etl.core

import org.im.etl.exception.EtlException

/**
 * ETL作业特质，定义ETL作业的基本接口
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/22
 */
trait EtlJob {
  def name: String

  def status: JobStatus.JobStatus

  /**
   * 执行ETL作业
   *
   * @param context ETL上下文
   * @return 执行结果，Right表示成功，Left表示失败
   */
  def execute(context: EtlContext): Either[EtlException, Unit]
}
