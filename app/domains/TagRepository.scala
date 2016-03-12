package domains

import play.api.libs.json._

object TagRepository extends RedisRepository{
  def findAll() = {
    redis.zrangeWithScore("tags").get.toMap
  }

  // return links having ALL specified tags
  def findLinks(tags: Array[String]) = {
    Json.arr(tags)
  }
}
