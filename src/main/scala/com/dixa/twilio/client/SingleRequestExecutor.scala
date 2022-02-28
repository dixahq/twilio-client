package com.dixa.twilio.client

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse, StatusCodes}
import akka.stream.Materializer

import scala.concurrent.{ExecutionContext, Future}

/** Base trait for an executor that is able and ready to fire a specific request in different ways.
  *
  * Different users have different preferences when it comes to error handling. So an instance of
  * this, is ready to perform a specific request, but allows the user to decide how he prefers the
  * response.
  *
  * @tparam Req
  *   The Request type that is ready to be executed by this instance.
  * @tparam Err
  *   The Err type that the request might produce.
  * @tparam Success
  *   The type of a successfully response.
  */
trait SingleRequestExecutor[Req, Err <: RuntimeException, Success] {

  /** Run the request, with typesafe error handling
    *
    * Always return a Successfully future, and communicates errors of the request as part of the
    * return type, in form as an Either.
    *
    * All the Error ADT used in the safe versions, are also exception, so a request would always be
    * failed with the same error, no matter if you run safe or unsafe, it is only a matter of how
    * the that error is communicated.
    */
  final def run(connSettings: TwilioConnectionSettings, req: Req): Future[Either[Err, Success]] =
    Future {
      execWithCheckForApiException(httpReq(connSettings, req), connSettings.timeouts).map {
        apiErrorOrResp =>
          apiErrorOrResp.left
            .map(mapApiException)
            .flatMap(HttpReqRespAndEntity =>
              parseHttpResponse(
                req,
                HttpReqRespAndEntity._1,
                HttpReqRespAndEntity._2,
                HttpReqRespAndEntity._3
              )
            )
      }
    }.flatten.recover { case e: Exception =>
      Left(
        createUnspecifiedException(
          Some(s"Uncaught Exception thrown when parsing httpResponse for request: $req"),
          Some(e)
        )
      )
    }

  /** Run the request, returning failed Future on errors.
    *
    * All the Error ADT used in the safe versions, are also exception, so a request would always be
    * failed with the same error, no matter if you run safe or unsafe, it is only a matter of how
    * the that error is communicated.
    */
  final def unsafeRun(connSettings: TwilioConnectionSettings, req: Req): Future[Success] =
    run(connSettings, req).map(_.fold(e => throw e, res => res))

  protected def http: HttpExt

  protected implicit def materializer: Materializer

  /** Execution context to use for Async operations. No blocking operation will be run on this. */
  protected implicit def executionContext: ExecutionContext

  /** Type for the request specific wrapper for an ApiException.
    *
    * All implementations is expected to have there own Exception ADT, where one one of the possible
    * values should be a ApiException wrapper
    */
  protected type ApiExceptionWrapper <: Err

  /** Type for the request specific UnspecifiedException.
    *
    * All implementations is expected to have there own Exception ADT, where one one of the possible
    * values should be a UnspecifiedException for representing all the error cases, that does not
    * have it own type for representing it.
    */
  protected type UnspecifiedException <: Err

  /** Build the http response.
    *
    * Implementations should provide this for building the HttpRequest for the request represented
    * by the concrete implementation.
    */
  protected def httpReq(connSettings: TwilioConnectionSettings, req: Req): HttpRequest

  /** Convert an ApiException into the request specific Exception. */
  protected def mapApiException(apiException: ApiException): ApiExceptionWrapper

  /** Create the request specific Unspecified exception. */
  protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Exception]
  ): UnspecifiedException

  /** Parse the response of the http request.
    *
    * Some parsing will already have happened before this method is called, as the response will
    * already have been checked for the common errors that all Twilio APIs can return, and if one
    * was found, this method would not be called at all. Instead the [[mapApiException]] method will
    * be used to map it into an Err.
    *
    * The implementation of this method, should still check for errors, but it would only have to
    * check for errors specific to the request.
    *
    * The entity will already be read fully into memory when this is called. This is why it is
    * parsed in separately as a HttpEntity.Strict. This allows for easy use by implementations,
    * without needing to worry about double reading the entity, or forgetting to either read it or
    * discard it in the first place.
    *
    * It should try to return all possible errors as a Left, but in case it slips, and ends up
    * throwing an Exception, then SingleRequestExecutor will make sure to map the exception into the
    * UndefinedException type of the request.
    *
    * @return
    *   Left in case of errors, right in case of success.
    * @param request
    *   The Req instance that that the HttpRequest was build upon
    * @param httpRequest
    *   The HttpRequest that the httpResponse is a response to
    * @param httpResponse
    *   The HttpResponse to parse (Entity has already been read to a Strict)
    * @param entity
    *   The Strict version of the Http entity.
    */
  protected def parseHttpResponse(
      request: Req,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntity.Strict
  ): Either[Err, Success]

  private def execWithCheckForApiException(
      httpReq: HttpRequest,
      timeouts: TwilioConnectionSettings.Timeouts
  ): Future[Either[ApiException, (HttpRequest, HttpResponse, HttpEntity.Strict)]] = {
    for {
      httpResp <- http.singleRequest(httpReq)
      entity   <- httpResp.entity.toStrict(timeouts.requestEntityTimeout)
      result = httpResp.status match {
        case StatusCodes.Unauthorized => Left(ApiException.AuthenticationException())
        // Fill in more cases here as we find them
        case _ => Right((httpReq, httpResp, entity))
      }
    } yield result
  }
}
