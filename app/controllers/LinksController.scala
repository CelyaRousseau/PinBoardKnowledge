package controllers

import javax.inject.Inject
import domains.LinkRepository
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

class LinksController @Inject()(linkRepository: LinkRepository) extends Controller {

  def findAll(limit: Int, offset: Int, filters: Option[String], q: Option[String], intersect: Option[Boolean]) = Action { request =>
    Ok(Json.toJson(linkRepository.findAllWithParameters(limit, offset, q, filters, intersect)))
  }

  def create() = Action(parse.json) { request =>
    linkRepository.create(request.body)
    Ok("Links Created")
  }
}
