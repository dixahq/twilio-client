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

package com.dixa.twilio.client

import org.apache.pekko.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse, StatusCodes}
import com.dixa.twilio.client.impl.{DefaultApiErrorEntityJsonRep, HttpEntityString}

import scala.concurrent.Future

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
trait SingleRequestExecutor[Req, Err <: RuntimeException, Success, BuilderStartState]
    extends RequestExecutor[Req, Err] {

  /** Run the request, with typesafe error handling
    *
    * Always return a Successful future, and communicate errors of the request as part of the return
    * type, in form as an Either.
    *
    * All the Error ADT used in the safe versions, are also exception, so a request would always be
    * failed with the same error, no matter if you run safe or unsafe, it is only a matter of how
    * the error is communicated.
    *
    * Function can be overridden for the soul purpose of stubbing or mocking by scalatest, should
    * never be overridden in extended classes.
    */
  def run(connSettings: TwilioConnectionSettings, req: Req): Future[Either[Err, Success]] =
    Future {
      createHttpReq(connSettings, req) match {
        case Left(value)        => Future.successful(Left(value))
        case Right(httpRequest) =>
          execWithCheckForApiException(httpRequest, connSettings.timeouts).map { apiErrorOrResp =>
            apiErrorOrResp.left
              .map(mapApiException)
              .flatMap(HttpReqRespAndEntity =>
                parseHttpResponse(
                  req,
                  HttpReqRespAndEntity._1,
                  HttpReqRespAndEntity._2,
                  HttpEntityString(HttpReqRespAndEntity._3.data.utf8String)
                )
              )
          }
      }
    }.flatten.recover { case e: Exception =>
      Left(
        createUnspecifiedException(
          Some(s"Uncaught Exception thrown when parsing httpResponse for request: $req"),
          Some(e)
        )
      )
    }

  /** Build and Run the request inline, with typesafe error handling
    *
    * This lets you inline the request building, so you can call it like:
    * `client.endpointX.buildAndRun(connectionSettings, _.withY.withZ.build())`.
    *
    * Besides the inline request building, it works just as [[run]].
    */
  def buildAndRun(
      connSettings: TwilioConnectionSettings,
      requestBuilderFun: BuilderStartState => Req
  ): Future[Either[Err, Success]] = run(connSettings, requestBuilderFun(createBuilderStartState()))

  /** Run the request, returning failed Future on errors.
    *
    * All the Error ADT used in the safe versions, are also exception, so a request would always be
    * failed with the same error, no matter if you run safe or unsafe, it is only a matter of how
    * the error is communicated.
    *
    * Function can be overridden for the soul purpose of stubbing or mocking by scalatest, should
    * never be overridden in extended classes.
    */
  def unsafeRun(connSettings: TwilioConnectionSettings, req: Req): Future[Success] =
    run(connSettings, req).map(_.fold(e => throw e, res => res))

  /** Build and Run the request inline, returning failed Future on errors.
    *
    * This lets you inline the request building, so you can call it like:
    * `client.endpointX.buildAndUnsafeRun(connectionSettings, _.withY.withZ.build())`.
    *
    * Besides the inline request building, it works just as [[unsafeRun]].
    */
  def buildAndUnsafeRun(
      connSettings: TwilioConnectionSettings,
      requestBuilderFun: BuilderStartState => Req
  ): Future[Success] =
    unsafeRun(connSettings, requestBuilderFun(createBuilderStartState()))

  protected def createBuilderStartState(): BuilderStartState

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
    * When looking for errors, the [[buildResultForUnhandledResponse]] is an easy way to create a
    * willcard for the cases not handled.
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
    *   The entity parsed as a String using UTF-8
    */
  protected def parseHttpResponse(
      request: Req,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
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
        case StatusCodes.Conflict     =>
          parseEntityAs[DefaultApiErrorEntityJsonRep](
            HttpEntityString(entity.data.utf8String)
          ) match {
            case Right(DefaultApiErrorEntityJsonRep(20409L, message, more_info, status)) =>
              Left(
                ApiException.Conflict(
                  Option(s"Code: 20409, Message: $message, More info: $more_info, Status: $status")
                )
              )
            case _ => Right((httpReq, httpResp, entity))
          }
        // Fill in more cases here as we find them
        case _ => Right((httpReq, httpResp, entity))
      }
    } yield result
  }

  /** Helper method for creating a response to cases where we have no support for handling a
    * Responese.
    */
  protected final def buildResultForUnhandledResponse(
      request: Req,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[Err, Success] = {
    val msg =
      s"No support for handling response to $request, due to status code ${httpResponse.status} " +
        s"after firing $httpRequest. Full entity of response is: $entity"
    httpResponse.status match {
      case StatusCodes.NotFound => Left(mapApiException(ApiException.NotFound(msg)))
      case _                    => Left(createUnspecifiedException(Some(msg), None))
    }
  }
}
