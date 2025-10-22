package org.im.etl.config

import scala.concurrent.duration._

/**
 * ETL配置案例类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/22
 */
case class EtlConfig(
                      jobId: String = "",
                      jobName: String = "",
                      parameters: Map[String, Any] = Map.empty,
                      retryConfig: RetryConfig = RetryConfig(),
                      monitoringConfig: MonitoringConfig = MonitoringConfig()
                    )

/**
 * 重试配置
 */
case class RetryConfig(
                        maxAttempts: Int = 3,
                        delay: FiniteDuration = 1.second,
                        exponentialBackoff: Boolean = false
                      )

/**
 * 监控配置
 */
case class MonitoringConfig(
                             enabled: Boolean = true,
                             metricsPrefix: String = "etl"
                           )
