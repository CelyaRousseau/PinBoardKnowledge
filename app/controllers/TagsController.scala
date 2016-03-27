package controllers

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc._
import domains.TagRepository

class TagsController @Inject()(TagRepository: TagRepository) extends Controller {

  def findAll() = Action { request =>
    Ok(Json.toJson(TagRepository.findAll()))
  }
}
