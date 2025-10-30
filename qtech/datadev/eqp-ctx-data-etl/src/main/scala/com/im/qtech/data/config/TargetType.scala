package com.im.qtech.data.config

/**
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @date 2025/10/30
 */

sealed trait TargetType

object TargetType {
  case object DORIS extends TargetType

  case object ORACLE extends TargetType

  case object POSTGRESQL extends TargetType

  // 用于从字符串解析枚举值
  def fromString(value: String): TargetType = value.toUpperCase match {
    case "DORIS" => DORIS
    case "ORACLE" => ORACLE
    case "POSTGRESQL" => POSTGRESQL
    case _ => throw new IllegalArgumentException(s"Unsupported target type: $value")
  }

  // 用于获取枚举值的字符串表示
  def toString(targetType: TargetType): String = targetType match {
    case DORIS => "DORIS"
    case ORACLE => "ORACLE"
    case POSTGRESQL => "POSTGRESQL"
  }
}
