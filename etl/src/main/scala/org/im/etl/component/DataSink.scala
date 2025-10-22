package org.im.etl.component

import org.im.etl.exception.EtlException

/**
 * 数据目标特质，定义数据加载的接口
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/22
 */
trait DataSink {
  /**
   * 写入数据配置
   */
  type WriteConfig = Map[String, Any]

  /**
   * 将数据写入目标
   *
   * @param data   待写入的数据
   * @param config 写入配置
   * @return 写入结果
   */
  def write(data: Any, config: WriteConfig): Either[EtlException, Unit]
}
