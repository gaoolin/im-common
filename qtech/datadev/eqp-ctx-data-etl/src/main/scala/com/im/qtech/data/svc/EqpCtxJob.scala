package com.im.qtech.data.svc

import com.im.qtech.data.config.DppConfigManager
import com.im.qtech.data.dpp.SparkInitConf
import com.im.qtech.data.dpp.conf.UnifiedHadoopConfig
import org.apache.spark.SparkConf
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.sql.SparkSession
import org.im.common.batch.monitor.JobMetrics
import org.im.etl.core.EtlContext
import org.im.etl.engine.{EtlJobManager, StandardEtlEngine}
import org.slf4j.LoggerFactory

/**
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/28
 */

object EqpCtxJob {
  private val logger = LoggerFactory.getLogger("EqpCtxJob")

  def main(args: Array[String]): Unit = {
    // 创建作业指标实例
    val jobMetrics = new JobMetrics("EqpCtxJob")
    jobMetrics.start()

    logger.info(">>>>> 开始执行设备上下文数据ETL作业！")

    var sparkSession: SparkSession = null
    var sc: JavaSparkContext = null

    try {
      // 使用统一的Spark配置管理器
      val sparkConf: SparkConf = SparkInitConf.initSparkConfigs("Equipment Context Data ETL Job")

      // 根据local模式设置运行模式
      val configManager = DppConfigManager.getInstance()
      val localMode = configManager.getString("spark.mode", "cluster")

      if ("local".equals(localMode)) {
        sparkConf.setMaster("local[*]")
        // 设置临时目录避免权限问题
        sparkConf.set("spark.local.dir", "D:\\spark-tmp")
      } else {
        // 在dolphinscheduler上运行时使用yarn模式
        sparkConf.setMaster("yarn")
      }

      // 设置其他Spark配置
      sparkConf.set("spark.default.parallelism", "4")
        .set("spark.sql.caseSensitive", "false")
        .set("spark.sql.analyzer.failAmbiguousSelfJoin", "false")

      // 创建 SparkSession 并传递配置
      sparkSession = SparkSession.builder()
        .config(sparkConf)
        .getOrCreate()

      logger.info(">>>>> Spark session initialized with app name: {}", sparkConf.get("spark.app.name"))

      // 获取 JavaSparkContext
      sc = new JavaSparkContext(sparkSession.sparkContext)

      // 设置 Hadoop 配置
      val hadoopConf = UnifiedHadoopConfig.createHadoopConfiguration()
      sparkSession.sparkContext.hadoopConfiguration.addResource(hadoopConf)

      // 创建ETL引擎和作业管理器
      val engine = new StandardEtlEngine()
      val jobManager = new EtlJobManager(engine)

      // 创建ETL作业并注册
      val eqpCtxJob = new EqpCtxEtlJob()
      jobManager.registerJob(eqpCtxJob, null)

      // 创建ETL上下文并添加SparkSession
      val context = EtlContext()
        .withAttribute("sparkSession", sparkSession)

      // 执行作业
      val result = jobManager.executeJobByName(eqpCtxJob.name, context)

      result match {
        case Right(_) =>
          logger.info(">>>>> ETL作业执行成功！")
          jobMetrics.finish()
        case Left(error) =>
          logger.error(">>>>> ETL作业执行失败！", error)
          jobMetrics.fail(error)
          throw error
      }
    } catch {
      case e: Exception =>
        logger.error("作业执行失败", e)
        jobMetrics.fail(e)
        return
    } finally {
      if (sparkSession != null) {
        sparkSession.close()
      }
      if (sc != null) {
        sc.stop()
      }

      // 记录作业指标
      logger.info(">>>>> 作业指标统计: {}", jobMetrics.toString)
    }
  }
}

