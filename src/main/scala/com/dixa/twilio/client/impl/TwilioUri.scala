package com.dixa.twilio.client.impl

import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.model.{HttpMethod, HttpRequest, Uri}
import com.dixa.twilio.client.TwilioConnectionSettings

/** Represent a URI for a request to perform agains the Twilio API
  *
  * An instance of this class, can be used to construct HttpRequest instance for the URI, that can
  * then be extended with an entity and headers as needed by the request.
  *
  * There are two concrete implementations:
  *
  * [[com.dixa.twilio.client.impl.TwilioUri.TwilioUrl]] for representing a full URL, including
  * specified domain name, and thereby ignoring the domain name form the
  * [[com.dixa.twilio.client.TwilioConnectionSettings]]. This is typically used, when we as a result
  * from one request, receives an full URL for getting sub resources.
  *
  * [[com.dixa.twilio.client.impl.TwilioUri.TwilioPath]] for representing the URI as a ApiSubDomain
  * and a path. Full URL will the be consturcted by using the baseHostName from
  * [[com.dixa.twilio.client.TwilioConnectionSettings]]. This is what we should use, in most cases,
  * as it more flexible and easy to stub/mock in tests.
  */
private[impl] sealed abstract class TwilioUri {

  def subDomain: ApiSubDomain

  def createHttpRequest(connSettings: TwilioConnectionSettings): HttpRequest
}

private[impl] object TwilioUri {

  private[impl] final case class TwilioPath(
      subDomain: ApiSubDomain,
      method: HttpMethod,
      path: String
  ) extends TwilioUri {

    require(path.startsWith("/"), "NextPagePath.path must start must be a path starting with a /")

    override def createHttpRequest(connSettings: TwilioConnectionSettings): HttpRequest = {
      val hostname = connSettings.hostNameFor(subDomain)
      val url      = s"${connSettings.protocol}://$hostname:${connSettings.port}$path"
      TwilioUrl(method, url, subDomain).createHttpRequest(connSettings)
    }
  }

  // This one needs to take a subdomain as well, even thoug it does not use it for
  // constructing the HttpRequest. But it needs to carry it from places where this class
  // is used, to later stages where a Path may be used. Also it typically no problem,
  // as this is used to represent URL we get from twilio, after performing a request
  // based on a Path and a Subdomain, so we will have the information when creating
  // instances anyway.
  private[impl] final case class TwilioUrl(
      method: HttpMethod,
      uri: Uri,
      subDomain: ApiSubDomain
  ) extends TwilioUri {

    require(uri.isAbsolute, "NextPageUri.uri must be absolute uri starting with a protocol.")

    override def createHttpRequest(connSettings: TwilioConnectionSettings): HttpRequest = {
      HttpRequest(method, uri).addHeader(
        Authorization(
          BasicHttpCredentials(connSettings.accountSid.toString, connSettings.authToken.asString)
        )
      )
    }
  }

  /** Autodetect if the provided urlOrPath is TwilioUrl or TwilioPath.
    *
    * Usefully when receiving sub resources from Twilio, where we do not now if they are specified
    * as a path or full URL.
    */
  private[impl] def autoDetect(
      urlOrPath: String,
      methods: HttpMethod,
      fallbackSubDomain: ApiSubDomain
  ): TwilioUri = {
    if (urlOrPath.startsWith("http")) TwilioUrl(methods, urlOrPath, fallbackSubDomain)
    else if (urlOrPath.startsWith("/")) TwilioPath(fallbackSubDomain, methods, urlOrPath)
    else TwilioPath(fallbackSubDomain, methods, s"/$urlOrPath")
  }
}
