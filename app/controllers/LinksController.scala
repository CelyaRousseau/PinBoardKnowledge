package controllers

import javax.inject.Inject
import domains.LinkRepository
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

class LinksController @Inject()(linkRepository: LinkRepository) extends Controller {

  def findAll(limit: Int, offset: Int, filters: Option[String], q: Option[String]) = Action { request =>
    (q, filters) match {
      case (Some(query), Some(filter)) => Ok(Json.toJson(linkRepository.findAllFilteredByTagsAndQuery(limit, offset, query, filter.split(" ").toList)))
      case (Some(query), None) => Ok(Json.toJson(linkRepository.findAllFilteredByQuery(limit,offset,query)))
      case (None, Some(filter)) => Ok(Json.toJson(linkRepository.findAllFilteredByTags(limit, offset, filter.split(" ").toList)))
      case (None, None) => Ok(Json.toJson(linkRepository.findAll(limit, offset)))
    }
  }

  def create() = Action(parse.json) { request =>
    linkRepository.create(request.body)
    Ok("Links Created")
  }

  def findAllWithIntersect(limit: Int, offset: Int, filters: Option[String], q: Option[String]) = Action { request =>
    (q, filters) match {
      case (Some(query), Some(filter)) => Ok(Json.toJson(linkRepository.findAllFilteredWithIntersectByTagsAndQuery(limit, offset, query, filter.split(" ").toList)))
      case (Some(query), None) => Ok(Json.toJson(linkRepository.findAllFilteredByQuery(limit,offset,query)))
      case (None, Some(filter)) => Ok(Json.toJson(linkRepository.findAllWithIntersectByTags(limit, offset, filter.split(" ").toList)))
      case (None, None) => Ok(Json.toJson(linkRepository.findAll(limit, offset)))
    }
  }
}
