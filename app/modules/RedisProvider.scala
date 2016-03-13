package modules

import com.google.inject.{AbstractModule, Provides}
import com.redis.RedisClientPool
import play.Configuration

class RedisProvider extends AbstractModule {

  @Provides
  def provideRedisClientPool(configuration: Configuration) = {
    new RedisClientPool(
      configuration.getString("redis.host"),
      configuration.getInt("redis.port"),
      secret = Option(configuration.getString("redis.secret"))
    )
  }

  override def configure(): Unit = {}
}
