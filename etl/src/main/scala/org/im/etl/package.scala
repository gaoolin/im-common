package org.im

/**
 * ETL框架包对象，定义公共类型别名和隐式转换
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/22
 */
package object etl {
  // 类型别名
  type EtlContext = org.im.etl.core.EtlContext
  type EtlException = org.im.etl.exception.EtlException
  type JobStatus = org.im.etl.core.JobStatus.JobStatus

  // 伴生对象引用
  val JobStatus = org.im.etl.core.JobStatus

  // 引擎相关类型
  type EtlEngine = org.im.etl.engine.EtlEngine
  type StandardEtlEngine = org.im.etl.engine.StandardEtlEngine
  type EtlJobManager = org.im.etl.engine.EtlJobManager
}
