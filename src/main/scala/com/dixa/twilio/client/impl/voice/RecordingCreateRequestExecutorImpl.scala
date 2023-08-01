package com.dixa.twilio.client.impl.voice

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.impl._
import com.dixa.twilio.client.voice.RecordingCreateRequestExecutor
import com.dixa.twilio.client.voice.RecordingCreateRequestExecutor.RecordingCreateException
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.voice.Recording

import scala.concurrent.ExecutionContext

private[client] class RecordingCreateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends RecordingCreateRequestExecutor {

  import RecordingCreateRequestExecutorImpl._
  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: RecordingCreateRequestExecutor.RecordingCreateRequest
  ): Either[RecordingCreateRequestExecutor.RecordingCreateException, HttpRequest] = {
    val params = QueryParamBuilder.empty
      .withCollectionParam(
        recordingStatusCallbackEventParamKey,
        req.recordingStatusCallbackEvent.getOrElse(Set.empty)
      )
      .withOptionalParam(recordingStatusCallbackParamKey, req.recordingStatusCallback)
      .withOptionalParam(recordingStatusCallbackMethodParamKey, req.recordingStatusCallbackMethod)
      .withOptionalParam(trimParamKey, req.trim)
      .withOptionalParam(recordingChannelsParamKey, req.recordingChannels)
      .withOptionalParam(recordingTrackParamKey, req.recordingTrack)
      .buildForPostParams

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/Calls/${req.callSid}/Recordings.json",
      connSettings
    ).map(
      _.withEntity(
        HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, params)
      )
    )
  }

  override protected def parseHttpResponse(
      request: RecordingCreateRequestExecutor.RecordingCreateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[RecordingCreateRequestExecutor.RecordingCreateException, Recording] = {
    httpResponse.status match {
      case StatusCodes.Created | StatusCodes.OK =>
        parseEntityAs[RecordingJsonRep](entity).map(j => j.toModel)
      case StatusCodes.NotFound => buildResultForNotFoundResponse(request, entity)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
  }

  private def buildResultForNotFoundResponse(
      req: RecordingCreateRequestExecutor.RecordingCreateRequest,
      entity: HttpEntityString
  ) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).left
      .map(e => RecordingCreateException.Unspecified(e))
      .flatMap { decoded =>
        decoded.code match {
          case 20404L =>
            Left(RecordingCreateException.CallNotFound(req.accountSid, req.callSid))
          case other =>
            Left(
              RecordingCreateException.Unspecified(
                s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                  s"$other represent. Full error entity from Twilio: $entity"
              )
            )
        }
      }
  }

  override protected def mapApiException(apiException: ApiException): RecordingCreateException.Api =
    RecordingCreateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): RecordingCreateException.Unspecified = RecordingCreateException.Unspecified(msg, cause)
}

private object RecordingCreateRequestExecutorImpl {
  private val recordingStatusCallbackEventParamKey  = "RecordingStatusCallbackEvent"
  private val recordingChannelsParamKey             = "RecordingChannels"
  private val recordingStatusCallbackParamKey       = "RecordingStatusCallback"
  private val recordingStatusCallbackMethodParamKey = "RecordingStatusCallbackMethod"
  private val recordingTrackParamKey                = "RecordingTrack"
  private val trimParamKey                          = "Trim"
}
