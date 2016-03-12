package domains

import play.api.libs.json._

object TagRepository {
  // @todo: find on redis
  def findAll() = {
    Json.obj(
      "scala" -> 10,
      "js" -> 100
    )
  }

  // return links having ALL specified tags
  def findLinks(tags: Array[String]) = {
    Json.arr(tags)
  }
}
