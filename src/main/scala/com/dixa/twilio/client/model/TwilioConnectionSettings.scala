package com.dixa.twilio.client.model

import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.{HttpMethod, HttpMethods, HttpRequest}

final case class TwilioConnectionSettings(
    host: String,
    port: Int,
    useHttps: Boolean,
    accountSid: String,
    authToken: String
) {

  private val protocol: String = useHttps match {
    case true  => "https"
    case false => "http"
  }

  private[client] def createBaseRequest(
      method: HttpMethod = HttpMethods.GET,
      pathOrUri: String,
  ): HttpRequest = {
    val safeUri =
      if (pathOrUri.startsWith("http")) pathOrUri
      else {
        val pathWithSlashPrefix = if (pathOrUri.startsWith("/")) pathOrUri else s"/$pathOrUri"
        s"$protocol://$host:$port$pathWithSlashPrefix"
      }
    HttpRequest(method, safeUri).addHeader(
      Authorization(
        BasicHttpCredentials(accountSid, authToken)
      )
    )
  }
}
