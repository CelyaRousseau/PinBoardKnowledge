package controllers

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc._
import domains.TagRepository

class TagsController @Inject()(TagRepository: TagRepository) extends Controller {

  def findAll(pattern: Option[String]) = Action { request =>
    pattern match {
      case Some(p) => Ok(Json.toJson(TagRepository.findByPattern(pattern)))
      case None => Ok(Json.toJson(TagRepository.findAll()))
    }
  }
}
