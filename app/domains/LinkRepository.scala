package domains

import javax.inject.Inject

import com.redis.RedisClientPool
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.libs.json.{JsArray, JsValue, Json}

class LinkRepository @Inject()(pool: RedisClientPool, tagRepository: TagRepository) {

  def findAll(limit: Int, offset: Int) = {
    pool.withClient {
      client => {
        Json.obj(
          "count" -> client.zcount("links").get,
          "links" -> client.zrange("links", offset, offset + limit - 1).get.map(Json.parse(_))
        )
      }
    }
  }

  def findAllFilteredByTags(limit: Int, offset: Int, filters: List[String]) = {
    pool.withClient {
      client => {
        val filter = filters.map { filter =>
          "tags:" + filter
        }

        val allLinks = client.sinter(filter.head, filter.tail: _*).get

        Json.obj(
          "count" -> (allLinks.toList.length),
          "links" -> allLinks.map { link => Json.parse(link.get) }
        )
      }
    }
  }

  def create(JSONLinks: JsValue) = {
    val fmt: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-dd-MM'T'HH:mm:ss'Z")
    var tags = collection.mutable.Map[String, Int]().withDefaultValue(0)
    pool.withClient {
      client => {
        JSONLinks.asInstanceOf[JsArray].value foreach { link =>
          val datetime: DateTime = new DateTime((link \ "time").as[String])
          client.zadd("links", datetime.getMillis, link).get
          (link \ "tags").as[String].split(" ").foreach { tag =>
            tags += (tag -> (tags.getOrElse(tag, 0) + 1))
            tagRepository.createLinkForTag(tag, link)
          }
        }
        tagRepository.createOrUpdate(tags)
      }
    }
  }

  def search(tags: Array[String]): Unit = {

  }
}
