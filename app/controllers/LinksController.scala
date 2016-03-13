package controllers

import javax.inject.Inject
import domains.LinkRepository
import play.api.mvc.{Action, Controller}

class LinksController @Inject()(linkRepository: LinkRepository) extends Controller {

  def findAll(limit: Int, offset: Int) = Action { request =>
    Ok(linkRepository.getLinks().toString())
  }

  def find(tags: String) = play.mvc.Results.TODO
}
