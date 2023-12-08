package com.dixa.twilio.client

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model.{HttpMethod, HttpRequest}
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl.TwilioClientPickler.Reader
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString, TwilioUri}

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

trait RequestExecutor[Req, Err <: RuntimeException] {

  protected def http: HttpExt

  protected implicit def materializer: Materializer

  /** Execution context to use for Async operations. No blocking operation will be run on this. */
  protected implicit def executionContext: ExecutionContext

  /** Type for the request specific wrapper for an ApiException.
    *
    * All implementations is expected to have there own Exception ADT, where one of the possible
    * values should be a ApiException wrapper
    */
  protected type ApiExceptionWrapper <: Err with RequestExecutor.ApiExceptionWrapper

  /** Type for the request specific UnspecifiedException.
    *
    * All implementations is expected to have there own Exception ADT, where one of the possible
    * values should be a UnspecifiedException for representing all the error cases, that does not
    * have it own type for representing it.
    */
  protected type UnspecifiedException <: Err

  /** Specify the sub domain in twilio, that this API request is against. */
  protected def subDomain: ApiSubDomain

  /** Specify the Http method that this API request uses */
  protected def method: HttpMethod

  /** Build the http request.
    *
    * Implementations should provide this for building the HttpRequest for the request represented
    * by the concrete implementation.
    */
  protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: Req
  ): Either[Err, HttpRequest]

  /** Convert an ApiException into the request specific Exception.
    */
  protected def mapApiException(apiException: ApiException): ApiExceptionWrapper

  /** Create the request specific Unspecified exception. */
  protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UnspecifiedException

  protected final def createUnspecifiedException(
      msg: String,
      cause: Throwable
  ): UnspecifiedException = createUnspecifiedException(Some(msg), Some(cause))

  protected final def createUnspecifiedException(msg: String): UnspecifiedException =
    createUnspecifiedException(Some(msg), None)

  protected final def createUnspecifiedException(cause: Throwable): UnspecifiedException =
    createUnspecifiedException(None, Some(cause))

  /** Helper method for creating a HttpRequest for a path, converting errors to unspecified errors.
    */
  protected final def createHttpRequestFor(
      path: String,
      connectionSettings: TwilioConnectionSettings
  ): Either[Err, HttpRequest] = TwilioUri
    .createPath(subDomain, method, path)
    .flatMap(_.createHttpRequest(connectionSettings))
    .left
    .map(createUnspecifiedException("Error creating HttpRequest", _))

  /** Helper method for parsing entity as Json, wrapping errors in UnspecifiedException. */
  protected final def parseEntityAs[A: ClassTag: Reader](
      entity: HttpEntityString
  ): Either[UnspecifiedException, A] = {
    entity.parse[A]().left.map(e => createUnspecifiedException(None, Some(e)))
  }

}

object RequestExecutor {
  trait ApiExceptionWrapper {
    def cause: ApiException
  }
}
