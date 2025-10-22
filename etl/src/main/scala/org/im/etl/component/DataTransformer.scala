package org.im.etl.component

import org.im.etl.exception.EtlException

/**
 * 数据转换器特质，定义数据转换的接口
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/22
 */
trait DataTransformer {
  /**
   * 转换数据
   *
   * @param input 输入数据
   * @return 转换后的数据
   */
  def transform(input: Any): Either[EtlException, Any]
}
