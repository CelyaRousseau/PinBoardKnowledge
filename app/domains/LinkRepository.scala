package domains

import javax.inject.Inject
import com.redis.RedisClientPool

class LinkRepository @Inject() (pool: RedisClientPool)  {

  def getLinks() : List[String] = {
    pool.withClient {
      client => {
        val links : List[String] = client.zrange("links").get
        links
      }
    }
  }
}
