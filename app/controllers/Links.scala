package controllers

import javax.inject.Inject

import domain.links.LinkRepository
import play.api.mvc.{Action, Controller}

class Links @Inject()(linkRepository: LinkRepository) extends Controller {

  def getLinks() = Action { request =>
    Ok(linkRepository.getLinks().toString())
  }
}
