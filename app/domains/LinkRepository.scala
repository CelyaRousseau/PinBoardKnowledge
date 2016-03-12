package domains

import java.util.UUID

import play.api.libs.json._

object LinkRepository {
  def findAll(limit: Int, offset: Int) = {
    // @todo: find links on redis
    Json.arr(
      Json.obj(
        "id" -> 1,
        "url" -> "http://www.scala-lang.org",
        "title" -> "scala ... cool ... you know ...",
        "tags" -> List("scala")
      ),
      Json.obj(
        "id" -> 2,
        "url" -> "https://www.javascript.com",
        "title" -> "javascript is awesome",
        "description" -> "nope",
        "tags" -> List("javascript")
      )
    )
  }
}
