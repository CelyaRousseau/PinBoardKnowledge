package controllers

import controllers.Application._
import play.api.libs.json._
import play.api.mvc._
import domains._

object LinksController {
  def findAll(limit: Int, offset: Int) = Action {
    implicit request =>
      Ok(Json.toJson(LinkRepository.findAll(limit, offset)))
  }

  def find(tags: String) = Action {
    implicit request =>
      Ok(Json.toJson(TagRepository.findLinks(tags.split(","))))
  }
}
