package org.im.etl.component

import org.im.etl.exception.EtlException

/**
 * 数据源特质，定义数据抽取的接口
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/22
 */
trait DataSource {
  /**
   * 读取数据配置
   */
  type ReadConfig = Map[String, Any]

  /**
   * 从数据源读取数据
   *
   * @param config 读取配置
   * @return 读取的数据
   */
  def read(config: ReadConfig): Either[EtlException, Any]
}
