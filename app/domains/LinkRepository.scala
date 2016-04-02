package domains

import javax.inject.Inject

import com.redis.RedisClientPool
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.libs.json.{JsArray, JsValue, Json}

class LinkRepository @Inject()(pool: RedisClientPool, tagRepository: TagRepository) {

  def findAllWithParameters(limit: Int, offset: Int, q: Option[String], f: Option[String], intersect: Option[Boolean]): JsValue = {
    val links: List[JsValue] = (q, f) match {
    val links: List[JsValue] = (q, t) match {
      case (None, None) => findAll
      case (Some(query), None) => findAllFilteredByQuery(query.split(",").toList)
      case (None, Some(tag)) => findAllFilteredByTags(tag.split(",").toList, intersect)
      case (Some(query), Some(tag)) => findAllFilteredByTagsAndQuery(query.split(",").toList, tag.split(",").toList, intersect)
    }

    Json.obj(
      "count" -> links.length,
      "links" -> links.slice(offset, offset + limit)
    )
  }

  def findAll: List[JsValue] = {
    pool.withClient {
      client => {
        client.zrange("links").get.map(link => Json.parse(link))
      }
    }
  }

  def findAllFilteredByQuery(query: List[String], links: Option[List[JsValue]] = None): List[JsValue] = {
    val pattern = "(?i).*" + query.head + ".*" + query.tail.map(word => "|.*" + word + ".*").mkString

    links match {
      case Some(link) => {
        link.collect({
          case (jsonLink) if (jsonLink \ "description").as[String].matches(pattern)
            || (jsonLink \ "extended").as[String].matches(pattern) => jsonLink
        })
      }
      case None => {
        pool.withClient {
          client => {
            client.zrange("links").get
              .map { link => Json.parse(link) }
              .collect({
                case (link) if (link \ "description").as[String].matches(pattern)
                  || (link \ "extended").as[String].matches(pattern) => link
              })
          }
        }
      }
    }
  }

  def findAllFilteredByTags(filters: List[String], intersect: Option[Boolean]): List[JsValue] = {
    pool.withClient {
      client => {
        val tags = filters.map { tag =>
          "tags:" + tag
        }

        intersect match {
          case Some(true) => client.sinter(tags.head, tags.tail: _*)
          case _ => client.sunion(tags.head, tags.tail: _*)
        }

      }.get
    }.map(link => Json.parse(link.get)).toList
  }

  def findAllFilteredByTagsAndQuery(query: String, filters: List[String], intersect: Option[Boolean]): List[JsValue] = {
    pool.withClient {
      client => {
        val pattern = "(?i).*" + query + ".*"

        findAllFilteredByTags(filters, intersect).collect({
          case (link) if (link \ "description").as[String].matches(pattern)
            || (link \ "extended").as[String].matches(pattern) => link
        })
      }
    }
  def findAllFilteredByTagsAndQuery(query: List[String], tags: List[String], intersect: Option[Boolean]): List[JsValue] = {
    findAllFilteredByQuery(query, Some(findAllFilteredByTags(tags, intersect)))
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
}
