package domains

import javax.inject.Inject

import com.fasterxml.jackson.databind.JsonNode
import com.redis.RedisClientPool
import play.libs.Json

class LinkRepository @Inject() (pool: RedisClientPool)  {

  def getLinks() : List[JsonNode] = {
    pool.withClient {
      client => {
        client.zrange("links").get
      }
    }.map { link =>
      Json.parse(link)
    }
  }

  def addLinks(links :Json) = {
    // TODO : Generify Links and push them in redis sortedSet [links]
  }
}
