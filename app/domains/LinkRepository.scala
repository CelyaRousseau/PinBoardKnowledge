package domains

import javax.inject.Inject

import com.redis.RedisClientPool
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

class LinkRepository @Inject()(pool: RedisClientPool, tagRepository: TagRepository) {
  /**
    * Find all Links according to parameters sent with pagination
    *
    * @param limit     Int     Limit for pagination
    * @param offset    Int     Offset for pagination
    * @param q         String  Query
    * @param t         String  Tags
    * @param intersect Boolean Boolean to set search with intersection
    * @return
    */
  def findAllWithParameters(limit: Int, offset: Int, q: Option[String], t: Option[String], intersect: Option[Boolean]): JsValue = {
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

  /**
    * Find all Links without any parameters
    *
    * @return
    */
  def findAll: List[JsValue] = {
    pool.withClient {
      client => {
        client.zrange("links").get.map(link => Json.parse(link))
      }
    }
  }

  /**
    * Find all links containing specific query from redis or links given
    *
    * @param query String                word(s) to search
    * @param links Option[List[JsValue]] optionnal list of links
    * @return
    */
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

  /**
    * Find all links contained in tags with or without intersection
    *
    * @param tags      List[String]  List of tags
    * @param intersect Boolean       Boolean to set search with intersection
    * @return
    */
  def findAllFilteredByTags(tags: List[String], intersect: Option[Boolean]): List[JsValue] = {
    pool.withClient {
      client => {
        val keys = tags.map { tag =>
          "tags:" + tag
        }

        intersect match {
          case Some(true) => client.sinter(keys.head, keys.tail: _*)
          case _ => client.sunion(keys.head, keys.tail: _*)
        }

      }.get
    }.map(link => Json.parse(link.get)).toList
  }

  /**
    * Find all links contained in filters with or without intersection and matching with query given
    *
    * @param query     String        word(s) to search
    * @param tags      List[String]  List of tags
    * @param intersect Boolean       Boolean to set search with intersection
    * @return
    */
  def findAllFilteredByTagsAndQuery(query: List[String], tags: List[String], intersect: Option[Boolean]): List[JsValue] = {
    findAllFilteredByQuery(query, Some(findAllFilteredByTags(tags, intersect)))
  }

  /**
    * Create a link in redis and save associated tags
    *
    * @param JSONLink JsValue JSON Link -> [{title: String, description: String, url: String, tags: Array[String]}
    * @return
    */
  def create(JSONLink: JsValue) = {
    DateTimeFormat.forPattern("yyyy-dd-MM'T'HH:mm:ss'Z")
    pool.withClient {
      client => {
        val link = JSONLink.asInstanceOf[JsValue]
        val tags = (link \ "tags").as[List[String]]
        val createdAt: DateTime = JSONLink.as[JsObject].keys.contains("time") match {
          case true => new DateTime((link \ "time").as[String])
          case _ => new DateTime()
        }

        // add link in links Zset
        client.zadd("links", createdAt.getMillis, link)

        // add link in tags:`tag` Set
        tags.foreach { tag => tagRepository.createLinkForTag(tag, link) }

        // increment tags in tags Zset
        tagRepository.createOrUpdate(tags)
      }
    }
  }

  /**
    * Create links given in redis as well as create or update associated tags
    *
    * @param JSONLinks JsValue List Of Link
    * @return
    */
  def createFromImport(JSONLinks: JsValue) = {
    DateTimeFormat.forPattern("yyyy-dd-MM'T'HH:mm:ss'Z")
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
        tagRepository.createOrUpdateFromImport(tags)
      }
    }
  }
}
