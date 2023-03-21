package com.dixa.twilio.client.impl.voice

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.impl.voice.ConferenceReadRequestExecutorImpl.{
  dateCreatedParamKey,
  dateUpdatedParamKey,
  friendlyNameParamKey,
  statusParamKey
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString, QueryParamBuilder}
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

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ConferenceReadRequestExecutor.ConferenceReadRequest
  ): Either[ConferenceReadRequestExecutor.ConferenceReadException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withOptionalDateParam(dateCreatedParamKey, req.dateCreated)
      .withOptionalDateParam(dateUpdatedParamKey, req.dateUpdated)
      .withOptionalParam(friendlyNameParamKey, req.friendlyName)
      .withOptionalParam(statusParamKey, req.status)
      .build

    createHttpRequestFor(
      s"/2010-04-01/Accounts/${req.accountSid}/Conferences.json$params",
      connSettings
    )
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
    println(s"Conference Read responseEntity: $responseEntity")
    responseEntity.parse[ConferenceListJsonRep]() match {
      case Left(ex) =>
        List(Left(ConferenceReadException.Unspecified(Some(ex.cause.getMessage), Some(ex.cause))))
      case Right(listJsonRep) => listJsonRep.conferences.map { _.toModel }.map { Right(_) }
    }

  }

}

private object ConferenceReadRequestExecutorImpl {
  private val dateCreatedParamKey  = "DateCreated"
  private val dateUpdatedParamKey  = "DateUpdated"
  private val friendlyNameParamKey = "FriendlyName"
  private val statusParamKey       = "Status"
}
