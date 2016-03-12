package controllers

import controllers.Application._
import domains._
import play.api.libs.json._
import play.api.mvc._

object TagsController {
  def findAll() = Action {
    implicit request =>
      Ok(Json.toJson(TagRepository.findAll()))
  }
}
