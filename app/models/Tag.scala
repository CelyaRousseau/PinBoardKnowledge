package models

import play.api.libs.json.Json


case class Tag(name:String, count:Int)

object Tag {
  implicit val format = Json.format[Tag]
}
