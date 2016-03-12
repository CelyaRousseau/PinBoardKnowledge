package modules

import com.google.inject.{AbstractModule, Provides}
import com.redis.RedisClientPool
import play.Configuration

class RedisProvider extends AbstractModule {

  @Provides
  def provideRedisClientPool(configuration: Configuration) = {
    new RedisClientPool(configuration.getString("redis.host"), configuration.getString("redis.port").toInt)
  }

  override def configure(): Unit = {}
}
