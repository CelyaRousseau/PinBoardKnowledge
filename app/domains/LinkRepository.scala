package domains

import javax.inject.Inject

import com.redis.RedisClientPool
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.libs.json.{JsArray, JsValue, Json}

class LinkRepository @Inject()(pool: RedisClientPool, tagRepository: TagRepository) {

  def findAll(limit: Int, offset: Int): List[JsValue] = {
    pool.withClient {
      client => {
        client.zrange("links", offset, offset + limit).get
      }
    }.map { link =>
      Json.parse(link)
    }
  }

  def create(JSONLink: JsValue) = {
    val fmt: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-dd-MM'T'HH:mm:ss'Z")
    var tags = collection.mutable.Map[String, Int]().withDefaultValue(0)
    pool.withClient {
      client => {
        val link = JSONLink.asInstanceOf[JsValue]
        client.zadd("links", new DateTime().getMillis, link).get
        (link \ "tags").as[String].split(" ").foreach { tag =>
          tags += (tag -> (tags.getOrElse(tag, 0) + 1))
        }
        tagRepository.addTags(tags)
      }
    }
  }
}
