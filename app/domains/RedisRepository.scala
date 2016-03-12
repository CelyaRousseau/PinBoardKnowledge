package domains

import com.redis.RedisClient

trait RedisRepository {
  val redis = new RedisClient("localhost", 6379) // @todo: use env var
}
