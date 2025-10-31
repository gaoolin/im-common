package com.im.qtech.data.config

import org.im.config.ConfigurationManager
import org.im.config.impl.DefaultConfigurationManager

/**
 * BOM应用配置管理器
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/30
 */
object BomConfigManager {
  private lazy val instance: ConfigurationManager = new DefaultConfigurationManager()

  def getInstance(): ConfigurationManager = instance
}