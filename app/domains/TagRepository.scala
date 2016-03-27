package domains

import javax.inject.Inject

import com.redis.RedisClientPool
import play.api.libs.json._

class TagRepository @Inject() (pool: RedisClientPool)  {
  def findAll() : List[String] = {
    pool.withClient {
      client => {
        client.zrange("tags").get
      }
    }
  }

  // return links having ALL specified tags
  def findLinks(tags: Array[String]) = {
    Json.arr(tags) // @todo: find them in redis
  }

  def create(tags: List[String]) = {
    pool.withClient {
      client => {
        tags map {
          client.zincrby("tags", 1, _).get
        }
      }
    }
  }
}
