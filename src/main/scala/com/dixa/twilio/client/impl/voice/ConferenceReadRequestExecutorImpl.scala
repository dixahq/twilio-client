package com.dixa.twilio.client.impl.voice

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.impl.voice.ConferenceJsonResp.TwilioConferenceJsonResp
import com.dixa.twilio.client.impl.voice.ConferenceReadRequestExecutorImpl.{accountSidParamKey, dateCreatedParamKey, dateUpdatedParamKey, friendlyNameParamKey, statusParamKey}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString, ListJsonRep, QueryParamBuilder}
import com.dixa.twilio.client.voice.ConferenceReadRequestExecutor
import com.dixa.twilio.client.voice.ConferenceReadRequestExecutor.ConferenceReadException
import com.dixa.twilio.model.voice.Conference

import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

class ConferenceReadRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ConferenceReadRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  /** Build the http request.
    *
    * Implementations should provide this for building the HttpRequest for the request represented
    * by the concrete implementation.
    */
  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ConferenceReadRequestExecutor.ConferenceReadRequest
  ): Either[ConferenceReadRequestExecutor.ConferenceReadException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withParam(accountSidParamKey, req.accountSid)
      .withOptionalDateParam(dateCreatedParamKey, req.dateCreated)
      .withOptionalDateParam(dateUpdatedParamKey, req.dateUpdated)
      .withOptionalParam(friendlyNameParamKey, req.friendlyName)
      .withOptionalParam(statusParamKey, req.status)
      .buildForPostParams

    createHttpRequestFor(
      s"/2010-04-01/Accounts/${req.accountSid}/Conferences.json",
      connSettings
    ).map(_.withEntity(HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, params)))
  }

  override protected def mapApiException(apiException: ApiException): ConferenceReadException.Api =
    ConferenceReadException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): ConferenceReadException.Unspecified = ConferenceReadException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: ConferenceReadRequestExecutor.ConferenceReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[ConferenceReadRequestExecutor.ConferenceReadException, Conference]] = {
    responseEntity.parse[ListJsonRep[TwilioConferenceJsonResp]]() match {
      case Left(ex) =>
        List(Left(ConferenceReadException.Unspecified(Some(ex.cause.getMessage), Some(ex.cause))))
      case Right(listJsonRep) => listJsonRep.messages.map{_.toModel}
    }

  }

}

private object ConferenceReadRequestExecutorImpl {
  private val accountSidParamKey   = "AccountSid"
  private val dateCreatedParamKey  = "DateCreated"
  private val dateUpdatedParamKey  = "DateUpdated"
  private val friendlyNameParamKey = "FriendlyName"
  private val statusParamKey       = "Status"
}
