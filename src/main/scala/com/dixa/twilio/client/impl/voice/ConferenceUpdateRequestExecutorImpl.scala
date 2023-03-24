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
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString, QueryParamBuilder}
import com.dixa.twilio.client.voice.{ConferenceReadRequestExecutor, ConferenceUpdateRequestExecutor}
import com.dixa.twilio.client.voice.ConferenceReadRequestExecutor.ConferenceReadException
import com.dixa.twilio.client.voice.ConferenceUpdateRequestExecutor.ConferenceUpdateException
import com.dixa.twilio.client.{voice, ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.Conference
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

class ConferenceUpdateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends ConferenceUpdateRequestExecutor {

  import ConferenceUpdateRequestExecutorImpl._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ConferenceUpdateRequestExecutor.ConferenceUpdateRequest
  ): Either[ConferenceUpdateRequestExecutor.ConferenceUpdateException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withOptionalParam(statusParamKey, req.status)
      .withOptionalParam(announceUrlParamKey, req.announceUrl)
      .withOptionalParam(announceMethodParamKey, req.announceMethod)
      .build

    createHttpRequestFor(
      s"/2010-04-01/Accounts/${req.accountSid}/Conferences/${req.conferenceSid}.json$params",
      connSettings
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): ConferenceUpdateException.Api =
    ConferenceUpdateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): ConferenceUpdateException.Unspecified = ConferenceUpdateException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: ConferenceUpdateRequestExecutor.ConferenceUpdateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[Either[ConferenceUpdateRequestExecutor.ConferenceUpdateException, Conference]] = {
    responseEntity.parse[ConferenceListJsonRep]() match {
      case Left(ex) =>
        List(Left(ConferenceUpdateException.Unspecified(Some(ex.cause.getMessage), Some(ex.cause))))
      case Right(listJsonRep) => listJsonRep.conferences.map { _.toModel }.map { Right(_) }
    }

  }

}

private object ConferenceUpdateRequestExecutorImpl {
  private val statusParamKey         = "Status"
  private val announceUrlParamKey    = "AnnounceUrl"
  private val announceMethodParamKey = "AnnounceMethod"
}
