package models

import play.api.libs.json._

case class Link(url: String, title: String, description: String, id: String, time: String, tags: String, shared:String, toRead:String)

object Link extends {
  implicit val format = Json.format[Link]
}
