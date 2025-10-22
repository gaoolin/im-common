package com.im.qtech.datadev.wb.usage.util

import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods._
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory

import java.io.IOException
import scala.util.{Failure, Try}

/**
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/22
 */

object ImHttpClient {
  private val logger = LoggerFactory.getLogger(getClass)
  private val requestConfig = RequestConfig.custom()
    .setConnectTimeout(5000)
    .setSocketTimeout(5000)
    .build()

  private val httpClient = HttpClients.custom()
    .setDefaultRequestConfig(requestConfig)
    .build()

  def sendGet(url: String, headers: Map[String, String] = Map.empty): Try[String] = {
    val request = new HttpGet(url)
    addHeaders(request, headers)
    executeRequest(request)
  }

  def sendPost(url: String, body: String, headers: Map[String, String] = Map.empty): Try[String] = {
    val request = new HttpPost(url)
    addHeaders(request, headers)
    request.setEntity(new StringEntity(body))
    executeRequest(request)
  }

  def sendPut(url: String, body: String, headers: Map[String, String] = Map.empty): Try[String] = {
    val request = new HttpPut(url)
    addHeaders(request, headers)
    request.setEntity(new StringEntity(body))
    executeRequest(request)
  }

  def sendDelete(url: String, headers: Map[String, String] = Map.empty): Try[String] = {
    val request = new HttpDelete(url)
    addHeaders(request, headers)
    executeRequest(request)
  }

  private def addHeaders(request: HttpRequestBase, headers: Map[String, String]): Unit = {
    headers.foreach { case (key, value) =>
      request.addHeader(key, value)
    }
  }

  private def executeRequest(request: HttpRequestBase): Try[String] = {
    Try {
      val response = httpClient.execute(request)
      try {
        val statusCode = response.getStatusLine.getStatusCode
        if (statusCode >= 200 && statusCode < 300) {
          val entity = response.getEntity
          if (entity != null) {
            EntityUtils.toString(entity)
          } else {
            null
          }
        } else {
          val errorMsg = s"Request failed with status code: $statusCode"
          logger.warn(errorMsg)
          throw new IOException(errorMsg)
        }
      } finally {
        response.close()
      }
    }.recoverWith {
      case e: IOException =>
        logger.error(s"Error executing request: ${e.getMessage}", e)
        Failure(e)
    }
  }

  def buildJsonBody(bodyParams: Map[String, Any]): String = {
    bodyParams.map { case (key, value) =>
      s""""$key":"$value""""
    }.mkString("{", ",", "}")
  }

  def buildHeaders(headers: Map[String, String]): Map[String, String] = {
    headers
  }
}