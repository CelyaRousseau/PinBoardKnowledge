package domains

import javax.inject.Inject

import com.fasterxml.jackson.databind.JsonNode
import com.redis.RedisClientPool
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.libs.json.{Json, JsArray, JsValue}
import play.libs.Json
import play.libs.Json

class LinkRepository @Inject()(pool: RedisClientPool, tagRepository: TagRepository) {

  def findAll(limit: Int, offset: Int): List[JsonNode] = {
    pool.withClient {
      client => {
        client.zrange("links", offset, offset + limit).get
      }
    }.map { link =>
      Json.parse(link)
    }
  }

  def addLinks(links: JsValue) = {
    val fmt: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-dd-MM'T'HH:mm:ss'Z")
    var tags = collection.mutable.Map[String, Int]().withDefaultValue(0)
    pool.withClient {
      client => {
        links.asInstanceOf[JsArray].value foreach { link =>
          val datetime: DateTime = new DateTime((link \ "time").as[String])
          client.zadd("links", datetime.getMillis, link).get
          (link \ "tags").as[String].split(" ").foreach { tag =>
            tags += (tag -> (tags.getOrElse(tag, 0) + 1))
          }
        }
        tagRepository.addTags(tags)
      }
    }
  }
}
