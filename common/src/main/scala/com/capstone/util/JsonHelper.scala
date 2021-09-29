package com.capstone.util

/**
  * Helper to parse Json
  */
import java.time.OffsetDateTime

import io.circe._
import net.liftweb.json.{
  JNothing,
  JValue,
  Serialization,
  parse => liftParser,
  _
}

trait JsonHelper {

  protected def parse(value: String): JValue = liftParser(value)

  implicit val formats
    : Formats = Serialization.formats(NoTypeHints) + new OffsetDateTimeserializer + new JsonSerializer
  //implicit protected val formats: DefaultFormats.type = DefaultFormats
  protected def write[T <: AnyRef](value: T): String =
    Serialization.write(value)

  implicit protected def extractOrEmptyString(json: JValue): String =
    json match {
      case JNothing => ""
      case data     => data.extract[String]
    }
}

class JsonSerializer extends Serializer[Json] {
  override def deserialize(
      implicit format: Formats): PartialFunction[(TypeInfo, JValue), Json] = ???

  override def serialize(
      implicit format: Formats): PartialFunction[Any, JValue] = {
    case j: Json =>
      JString(j.toString().split('\n').map(_.trim.filter(_ >= ' ')).mkString)
  }
}

class OffsetDateTimeserializer extends Serializer[OffsetDateTime] {
  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case x: OffsetDateTime => JString(x.toString)
  }

  override def deserialize(implicit format: Formats)
    : PartialFunction[(TypeInfo, JValue), OffsetDateTime] = ???
}
