package domains

import javax.inject.Inject

import com.redis.RedisClient._
import com.redis.RedisClientPool
import models.Tag
import play.api.libs.json._

class TagRepository @Inject()(pool: RedisClientPool) {
  def findByPattern(pattern: Option[String]) = {
    pool.withClient {
      client => {
        client.zrangeWithScore("tags", sortAs = DESC).get
          .filter {
            _._1.startsWith(pattern.get)
          }
          .map { tag => Json.toJson(new Tag(tag._1, tag._2.toInt)) }
      }
    }
  }

  def findAll(): List[JsValue] = {
    pool.withClient {
      client => {
        client.zrangeWithScore("tags", sortAs = DESC).get
      }
    }.map { tag =>
      val filter: Tag = new Tag(tag._1, tag._2.toInt)
      Json.toJson(filter)
    }
  }

  // return links having ALL specified tags
  def findLinks(tags: Array[String]) = {
    Json.arr(tags) // @todo: find them in redis
  }

  def createOrUpdate(tags: List[String]) = {
    pool.withClient {
      client => {
        tags foreach { tag => client.zincrby("tags", 1, tag) }
      }
    }
  }

  def createLinkForTag(tagName: String, link: JsValue) = {
    pool.withClient {
      client => {
        client.sadd("tags:" + tagName, link)
      }
    }
  }
}
