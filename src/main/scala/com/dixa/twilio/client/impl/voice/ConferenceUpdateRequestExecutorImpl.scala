package com.dixa.twilio.client.impl.voice

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString, QueryParamBuilder}
import com.dixa.twilio.client.voice.ConferenceUpdateRequestExecutor
import com.dixa.twilio.client.voice.ConferenceUpdateRequestExecutor.ConferenceUpdateException
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
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
      .buildForPostParams

    createHttpRequestFor(
      s"/2010-04-01/Accounts/${req.accountSid}/Conferences/${req.conferenceSid}.json",
      connSettings
    ).map(_.withEntity(HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, params)))
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
      req: ConferenceUpdateRequestExecutor.ConferenceUpdateRequest,
      httpReq: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[ConferenceUpdateRequestExecutor.ConferenceUpdateException, Conference] = {
    httpResponse.status match {
      case StatusCodes.OK =>
        parseEntityAs[ConferenceJsonRep.TwilioConferenceJsonResp](entity).map(_.toModel)
      case _ => buildResultForUnhandledResponse(req, httpReq, httpResponse, entity)
    }
  }

}

private object ConferenceUpdateRequestExecutorImpl {
  private val statusParamKey         = "Status"
  private val announceUrlParamKey    = "AnnounceUrl"
  private val announceMethodParamKey = "AnnounceMethod"
}
