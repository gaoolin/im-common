package org.im.etl.core

/**
 * ETL上下文，用于在作业执行过程中传递参数和状态
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/22
 */
case class EtlContext(
                       parameters: Map[String, Any] = Map.empty,
                       attributes: Map[String, Any] = Map.empty
                     ) {

  def getParameter[T](key: String): Option[T] = parameters.get(key).asInstanceOf[Option[T]]

  def getAttribute[T](key: String): Option[T] = attributes.get(key).asInstanceOf[Option[T]]

  def withParameter(key: String, value: Any): EtlContext =
    this.copy(parameters = parameters + (key -> value))

  def withAttribute(key: String, value: Any): EtlContext =
    this.copy(attributes = attributes + (key -> value))
}
