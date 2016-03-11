package controllers

import controllers.Application._
import play.api.mvc._

object LinksController {
  def findAll(limit: Int, offset: Int) = Action {
    Ok("TODO")
  }

  def findById(linkId: java.util.UUID) = Action {
    Ok("TODO")
  }
}
