package domains

import javax.inject.Inject

import com.redis.RedisClientPool
import models.Tag
import play.api.libs.json._

class TagRepository @Inject()(pool: RedisClientPool) {
  def findAll(): List[JsValue] = {
    pool.withClient {
      client => {
        client.zrangeWithScore("tags").get
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

  def createOrUpdate(tags: collection.mutable.Map[String, Int]) = {
    pool.withClient {
      client => {
        tags map { tag =>
          client.zincrby("tags", tag._2, tag._1).get
        }
      }
    }
  }

  def createLinkForTag(tagName: String, link: JsValue) = {
    pool.withClient {
      client => {
        client.sadd("tags:" + tagName, link).get
      }
    }
  }
}
