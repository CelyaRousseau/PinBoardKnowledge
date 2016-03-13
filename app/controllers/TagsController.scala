package controllers

import javax.inject.Inject
import play.api.mvc._
import domains.TagRepository

class TagsController @Inject()(TagRepository: TagRepository) extends Controller {

  def findAll() = Action { request =>
    Ok(TagRepository.findAll().toString())
  }
}
