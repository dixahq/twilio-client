// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.client.impl

import org.apache.pekko.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import org.apache.pekko.http.scaladsl.model.{HttpMethod, HttpRequest, Uri}
import com.dixa.twilio.client.TwilioConnectionSettings

import scala.util.Try

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
private[client] sealed abstract class TwilioUri {

  def subDomain: ApiSubDomain

  def createHttpRequest(
      connSettings: TwilioConnectionSettings
  ): Either[TwilioUri.HttpRequestCreationException, HttpRequest]

  final def createHttpRequestUnsafe(connectionSettings: TwilioConnectionSettings): HttpRequest =
    createHttpRequest(connectionSettings).toTry.get
}

private[client] object TwilioUri {

  sealed trait TwilioUriException extends RuntimeException

  final case class HttpRequestCreationException(cause: Throwable)
      extends RuntimeException("Exception trying to create HttpRequest", cause)
      with TwilioUriException

  sealed trait TwilioPathException extends TwilioUriException
  final case class TwilioPathMustStartWithASlashException(path: String)
      extends IllegalArgumentException(
        s"The path of a TwilioPath must start with a '/', but this path does not: $path"
      )
      with TwilioPathException

  sealed trait TwilioUrlExecption extends TwilioUriException
  final case class TwilioUrlMustBeAbsoluteException(uri: Uri)
      extends IllegalArgumentException(
        s"The uri of a TwilioUrl must be absolute, but this is not: $uri"
      )
      with TwilioUrlExecption

  sealed trait TwilioPath extends TwilioUri {
    def method: HttpMethod
    def path: String
  }

  /** Create a TwilioPath, handling errors as part of return type. */
  def createPath(
      subDomain: ApiSubDomain,
      method: HttpMethod,
      path: String
  ): Either[TwilioPathException, TwilioPath] = {
    if (!path.startsWith("/"))
      Left(TwilioPathMustStartWithASlashException(path))
    else
      Right(TwilioPathImpl(subDomain, method, path))
  }

  /** Creates a Twilio path, throwing exceptions on error */
  def createPathUnsafe(
      subDomain: ApiSubDomain,
      method: HttpMethod,
      path: String
  ): TwilioPath = createPath(subDomain, method, path).toTry.get

  private final case class TwilioPathImpl private (
      subDomain: ApiSubDomain,
      method: HttpMethod,
      path: String
  ) extends TwilioPath {

    override def createHttpRequest(
        connSettings: TwilioConnectionSettings
    ): Either[TwilioUri.HttpRequestCreationException, HttpRequest] = {
      val hostname = connSettings.hostNameFor(subDomain)
      val url      = s"${connSettings.protocol}://$hostname:${connSettings.endpoint.port}$path"
      TwilioUrlImpl(method, url, subDomain).createHttpRequest(connSettings)
    }
  }

  sealed trait TwilioUrl extends TwilioUri {
    def method: HttpMethod
    def uri: Uri
  }

  def createUrl(
      subDomain: ApiSubDomain,
      method: HttpMethod,
      uri: Uri,
  ): Either[TwilioUrlExecption, TwilioUrl] = {
    if (!uri.isAbsolute)
      Left(TwilioUrlMustBeAbsoluteException(uri))
    else
      Right(TwilioUrlImpl(method, uri, subDomain))
  }

  // This one needs to take a subdomain as well, even thoug it does not use it for
  // constructing the HttpRequest. But it needs to carry it from places where this class
  // is used, to later stages where a Path may be used. Also it typically no problem,
  // as this is used to represent URL we get from twilio, after performing a request
  // based on a Path and a Subdomain, so we will have the information when creating
  // instances anyway.
  private final case class TwilioUrlImpl(
      method: HttpMethod,
      uri: Uri,
      subDomain: ApiSubDomain
  ) extends TwilioUrl {
    override def createHttpRequest(
        connSettings: TwilioConnectionSettings
    ): Either[TwilioUri.HttpRequestCreationException, HttpRequest] = {
      Try {
        HttpRequest(method, uri).addHeader(
          Authorization(
            BasicHttpCredentials(connSettings.accountSid.toString, connSettings.authToken.asString)
          )
        )
      }.toEither.left.map(HttpRequestCreationException)
    }
  }

  /** Autodetect if the provided urlOrPath is TwilioUrl or TwilioPath.
    *
    * Usefully when receiving sub resources from Twilio, where we do not now if they are specified
    * as a path or full URL.
    */
  def autoDetect(
      urlOrPath: String,
      methods: HttpMethod,
      fallbackSubDomain: ApiSubDomain
  ): Either[TwilioUriException, TwilioUri] = {
    if (urlOrPath.startsWith("http")) createUrl(fallbackSubDomain, methods, urlOrPath)
    else if (urlOrPath.startsWith("/")) createPath(fallbackSubDomain, methods, urlOrPath)
    else createPath(fallbackSubDomain, methods, s"/$urlOrPath")
  }

  /** Autodetect if the provided urlOrPath is TwilioUrl or TwilioPath, throwing exceptions on error.
    *
    * Same as [[autoDetect]] but will throw exceptoins instead of returning them in an [[Either]]
    */
  def autoDetectUnsafe(
      urlOrPath: String,
      methods: HttpMethod,
      fallbackSubDomain: ApiSubDomain
  ): TwilioUri = autoDetect(urlOrPath, methods, fallbackSubDomain).toTry.get
}
