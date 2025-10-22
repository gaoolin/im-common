package com.im.qtech.datadev.wb.usage.source

import com.im.qtech.datadev.wb.usage.util.ImHttpClient
import org.im.etl.component.DataSource
import org.im.etl.exception.EtlException

import java.util
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/**
 * HTTP数据源实现
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/22
 */
class HttpDataSource extends DataSource {
  override type ReadConfig = Map[String, Any]

  override def read(config: ReadConfig): Either[EtlException, String] = {
    try {
      val url = config.getOrElse("url", "").toString
      val method = config.getOrElse("method", "GET").toString.toUpperCase
      val headers = config.getOrElse("headers", Map.empty[String, String]).asInstanceOf[Map[String, String]]
      val body = config.getOrElse("body", "").toString

      val javaHeaders = new util.HashMap[String, String]()
      headers.foreach { case (k, v) => javaHeaders.put(k, v) }

      // 将 HashMap 转换为 Scala Map 以适配 ImHttpClient 方法签名
      val scalaHeaders = javaHeaders.asScala.toMap

      val responseTry: Try[String] = method match {
        case "GET" =>
          ImHttpClient.sendGet(url, scalaHeaders)
        case "POST" =>
          ImHttpClient.sendPost(url, body, scalaHeaders)
        case "PUT" =>
          ImHttpClient.sendPut(url, body, scalaHeaders)
        case "DELETE" =>
          ImHttpClient.sendDelete(url, scalaHeaders)
        case _ =>
          throw new IllegalArgumentException(s"Unsupported HTTP method: $method")
      }

      responseTry match {
        case Success(value) => Right(value)
        case Failure(exception) => Left(EtlException(s"Failed to read from HTTP source: ${exception.getMessage}", exception))
      }
    } catch {
      case e: Exception =>
        Left(EtlException(s"Failed to read from HTTP source: ${e.getMessage}", e))
    }
  }
}
