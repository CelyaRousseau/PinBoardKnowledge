package controllers

import javax.inject.Inject
import domains.LinkRepository
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

class LinksController @Inject()(linkRepository: LinkRepository) extends Controller {

  def findAll(limit: Int, offset: Int) = Action { request =>
    Ok(Json.toJson(linkRepository.findAll(limit, offset)))
  }

  def find(filters: Option[String], query: Option[String]) = Action {
    /*  val tags : Array[String] = filters match {
        case Some(filter) => filter.split("+")
        case None =>
      }

      Ok(Json.toJson(linkRepository.search(tags)))*/
    Ok("blop")
  }

  def create() = Action(parse.json) { request =>
    linkRepository.create(request.body)
    Ok("Links Created")
  }
}
