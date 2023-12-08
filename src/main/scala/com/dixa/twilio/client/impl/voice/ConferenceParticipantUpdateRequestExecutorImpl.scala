package com.dixa.twilio.client.impl.voice

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.voice.ConferenceParticipantUpdateRequestExecutor
import com.dixa.twilio.client.voice.ConferenceParticipantUpdateRequestExecutor.{
  ConferenceParticipantUpdateException,
  ConferenceParticipantUpdateRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.Conference

import scala.concurrent.ExecutionContext

private[client] class ConferenceParticipantUpdateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends ConferenceParticipantUpdateRequestExecutor {

  import ConferenceParticipantUpdateRequestExecutorImpl._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: ConferenceParticipantUpdateRequest
  ): Either[ConferenceParticipantUpdateException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withOptionalBooleanParam(mutedParamKey, req.muted)
      .withOptionalBooleanParam(holdParamKey, req.hold)
      .withOptionalParam(holdUrlParamKey, req.holdUrl)
      .withOptionalParam(holdMethodParamKey, req.holdMethod)
      .withOptionalParam(announceUrlParamKey, req.announceUrl)
      .withOptionalParam(announceMethodParamKey, req.announceMethod)
      .withOptionalParam(waitUrlParamKey, req.waitUrl)
      .withOptionalParam(waitMethodParamKey, req.waitMethod)
      .withOptionalBooleanParam(beepOnExitParamKey, req.beepOnExit)
      .withOptionalBooleanParam(endConferenceOnExitParamKey, req.endConferenceOnExit)
      .withOptionalParam(callSidToCouchParamKey, req.callSidToCoach)
      .buildForPostParams

    val participantId = req.participantId.fold(_.twilioString, _.twilioString)
    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/Conferences/${req.conferenceSid}/Participants/$participantId.json",
      connSettings
    ).map(
      _.withEntity(HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, params))
    )
  }

  override protected def mapApiException(
      apiException: ApiException
  ): ConferenceParticipantUpdateException.Api =
    ConferenceParticipantUpdateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): ConferenceParticipantUpdateException.Unspecified =
    ConferenceParticipantUpdateException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      req: ConferenceParticipantUpdateRequest,
      httpReq: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[ConferenceParticipantUpdateException, Conference.Participant] = {
    httpResponse.status match {
      case StatusCodes.OK       => parseEntityAs[ParticipantJsonRep](entity).map(_.toModel)
      case StatusCodes.NotFound => buildResultForNotFoundResponse(req, entity)
      case _ => buildResultForUnhandledResponse(req, httpReq, httpResponse, entity)
    }
  }

  private def buildResultForNotFoundResponse(
      req: ConferenceParticipantUpdateRequest,
      entity: HttpEntityString
  ) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => ConferenceParticipantUpdateException.Unspecified(e))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            Left(
              ConferenceParticipantUpdateException
                .ParticipantNotFound(req.accountSid, req.conferenceSid, req.participantId)
            )
          case other =>
            Left(
              ConferenceParticipantUpdateException.Unspecified(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represent. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }
}

private object ConferenceParticipantUpdateRequestExecutorImpl {
  private val mutedParamKey               = "Muted"
  private val holdParamKey                = "Hold"
  private val holdUrlParamKey             = "HoldUrl"
  private val holdMethodParamKey          = "HoldMethod"
  private val announceUrlParamKey         = "AnnounceUrl"
  private val announceMethodParamKey      = "AnnounceMethod"
  private val waitUrlParamKey             = "WaitUrl"
  private val waitMethodParamKey          = "WaitMethod"
  private val beepOnExitParamKey          = "BeepOnExit"
  private val endConferenceOnExitParamKey = "EndConferenceOnExit"
  private val callSidToCouchParamKey      = "CallSidToCoach"
}
