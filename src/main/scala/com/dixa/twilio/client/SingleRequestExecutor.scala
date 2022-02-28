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

  protected def http: HttpExt

  protected implicit def materializer: Materializer

  protected implicit def executionContext: ExecutionContext

  /** Run the request, with typesafe error handling
    *
    * Always return a Successfull future, and communicates erros of the request as part of the
    * return type, in form as an Either.
    *
    * All the Error ADT used in the safe versions, are also exception, so a request would always be
    * failed with the same error, no matter if you run safe or unsafe, it is only a matter of how
    * the that error is communicated.
    */
  def run(connSettings: TwilioConnectionSettings, req: Req): Future[Either[Err, Success]]

  /** Run the request, returning failed Future on errors.
    *
    * All the Error ADT used in the safe versions, are also exception, so a request would always be
    * failed with the same error, no matter if you run safe or unsafe, it is only a matter of how
    * the that error is communicated.
    */
  final def unsafeRun(connSettings: TwilioConnectionSettings, req: Req): Future[Success] =
    run(connSettings, req).map(_.fold(e => throw e, res => res))

  /** Will execute provided HttpRequest, and check the response for common API errors.
    *
    * This helps implementations in 2 ways:
    *
    * 1) It checks the response for the common errors all the Twilio APIs can return, and returns
    * them as a Left(ApiException), that implementations easily can left map into ther own Exception
    * wrapper for the ApiException
    *
    * 2) It reads in the entity, and return it as part of the successfully response as a
    * HttpEntity.Strict. This makes the entity easy to handle for implementations, as they then do
    * not need to remember either reading or discarding it. Also prevents them from trying to read
    * it in a second time.
    */
  protected def execWithCheckForApiException(
      httpReq: HttpRequest,
      timeouts: TwilioConnectionSettings.Timeouts
  ): Future[Either[ApiException, (HttpResponse, HttpEntity.Strict)]] = {
    for {
      httpResp <- http.singleRequest(httpReq)
      entity   <- httpResp.entity.toStrict(timeouts.requestEntityTimeout)
      result = httpResp.status match {
        case StatusCodes.Unauthorized => Left(ApiException.AuthenticationException())
        // Fill in more cases here as we find them
        case _ => Right(httpResp -> entity)
      }
    } yield result
  }
}
