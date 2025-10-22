package com.im.qtech.datadev.wb.usage.config

import org.im.config.ConfigurationManager
import org.im.config.impl.DefaultConfigurationManager

/**
 * 应用统一配置管理器
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/16
 */
object DppConfigManager {
  private lazy val instance: ConfigurationManager = new DefaultConfigurationManager()

  def getInstance(): ConfigurationManager = instance
}
