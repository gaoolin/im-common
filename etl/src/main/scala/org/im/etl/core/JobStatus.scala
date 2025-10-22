package org.im.etl.core

/**
 * ETL作业状态枚举
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/22
 */
object JobStatus extends Enumeration {
  type JobStatus = Value
  val Pending, Running, Success, Failed, Stopped = Value
}
