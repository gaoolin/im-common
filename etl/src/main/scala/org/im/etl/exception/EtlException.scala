package org.im.etl.exception

/**
 * ETL框架异常类
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/22
 */
case class EtlException(message: String, cause: Throwable = null) extends Exception(message, cause)
