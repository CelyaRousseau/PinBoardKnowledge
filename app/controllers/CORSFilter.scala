package controllers

import play.api.mvc.{Result, RequestHeader, Filter}
import play.api.Play.{configuration, current}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CORSFilter extends Filter {
  def isPreFlight(rh: RequestHeader) = rh.method.toLowerCase.equals("options") && rh.headers.get("Access-Control-Request-Method").nonEmpty

  def apply(f: (RequestHeader) => Future[Result])(request: RequestHeader): Future[Result] = {
    val preflightAllowHeaders = "Access-Control-Allow-Headers" -> request.headers.get("Access-Control-Request-Headers").getOrElse("")
    val defaultAllowHeaders =  "Access-Control-Allow-Headers" -> configuration.getString("access_control.allow_headers").getOrElse("")
    val headers = Seq(
      "Access-Control-Allow-Origin" -> configuration.getString("access_control.allow_origin").orElse(request.headers.get("Origin")).getOrElse(""),
      "Access-Control-Allow-Methods" -> configuration.getString("access_control.allow_methods").orElse(request.headers.get("Origin")).getOrElse(""),
      "Access-Control-Allow-Credentials" -> "true"
    )

    if (isPreFlight(request)) {
      return Future.successful(Default.Ok.withHeaders(headers :+ preflightAllowHeaders: _*))
    }

    f(request).map{_.withHeaders(headers :+ defaultAllowHeaders: _*)}
  }
}