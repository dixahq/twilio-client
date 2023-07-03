package com.dixa.twilio.client.impl.voice

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{
  ContentTypes,
  HttpEntity,
  HttpMethod,
  HttpMethods,
  HttpRequest,
  HttpResponse,
  StatusCodes
}
import akka.stream.Materializer
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.client.impl.{ApiSubDomain, ApiVersion, HttpEntityString, QueryParamBuilder}
import com.dixa.twilio.client.voice.CallCreateRequestExecutor
import com.dixa.twilio.client.voice.CallCreateRequestExecutor.CallCreateException
import com.dixa.twilio.model.voice.Call

import scala.concurrent.ExecutionContext

private[client] class CallCreateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext,
    apiVersion: ApiVersion
) extends CallCreateRequestExecutor {

  import CallCreateRequestExecutorImpl._
  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: CallCreateRequestExecutor.CallCreateRequest
  ): Either[CallCreateRequestExecutor.CallCreateException, HttpRequest] = {
    val paramsRequired =
      QueryParamBuilder.empty.withParam(toParamKey, req.to).withParam(fromParamKey, req.from)
    // Twilio requires to send status events as separate params
    val paramsWithStatusCallbackEventSeqOpt =
      req.statusCallbackEvents
        .map(events =>
          events.foldLeft(paramsRequired)((params, event) =>
            params.withParam(statusCallbackEventParamKey, event)
          )
        )
        .getOrElse(paramsRequired)
    // Twilio requires recording events to be sent in a single param and in a string separated by spaces
    val paramsWithRecordingStatusCallbackEventSeqOpt =
      req.recordingStatusCallbackEvents
        .map(events =>
          paramsWithStatusCallbackEventSeqOpt
            .withParam(
              recordingStatusCallbackEventParamKey,
              events.map(_.twilioString).mkString(" ")
            )
        )
        .getOrElse(paramsWithStatusCallbackEventSeqOpt)
    val paramsFull = paramsWithRecordingStatusCallbackEventSeqOpt
      .withOptionalParam(methodParamKey, req.method)
      .withOptionalParam(fallbackUrlParamKey, req.fallbackUrl)
      .withOptionalParam(fallbackMethodParamKey, req.fallbackMethod)
      .withOptionalParam(statusCallbackParamKey, req.statusCallback)
      .withOptionalParam(statusCallbackMethodParamKey, req.statusCallbackMethod)
      .withOptionalParam(sendDigitsParamKey, req.sendDigits)
      .withOptionalParam(timeoutParamKey, req.timeout)
      .withOptionalBooleanParam(recordParamKey, req.record)
      .withOptionalParam(recordingChannelsParamKey, req.recordingChannels)
      .withOptionalParam(recordingStatusCallbackParamKey, req.recordingStatusCallback)
      .withOptionalParam(recordingStatusCallbackMethodParamKey, req.recordingStatusCallbackMethod)
      .withOptionalParam(recordingTrackParamKey, req.recordingTrack)
      .withOptionalParam(sipAuthUsernameParamKey, req.sipAuthUsername)
      .withOptionalParam(sipAuthPasswordParamKey, req.sipAuthPassword)
      .withOptionalParam(machineDetectionParamKey, req.machineDetection)
      .withOptionalParam(machineDetectionTimeoutParamKey, req.machineDetectionTimeout)
      .withOptionalParam(
        machineDetectionSpeechThresholdParamKey,
        req.machineDetectionSpeechThreshold
      )
      .withOptionalParam(
        machineDetectionSpeechEndThresholdParamKey,
        req.machineDetectionSpeechEndThreshold
      )
      .withOptionalParam(machineDetectionSilenceTimeoutParamKey, req.machineDetectionSilenceTimeout)
      .withOptionalParam(trimParamKey, req.trim)
      .withOptionalParam(callerIdParamKey, req.callerId)
      .withOptionalBooleanParam(asyncAmdParamKey, req.asyncAmd)
      .withOptionalParam(asyncAmdStatusCallbackParamKey, req.asyncAmdStatusCallback)
      .withOptionalParam(asyncAmdStatusCallbackMethodParamKey, req.asyncAmdStatusCallbackMethod)
      .withOptionalParam(byocParamKey, req.byoc)
      .withOptionalParam(callReasonParamKey, req.callReason)
      .withOptionalParam(callTokenParamKey, req.callToken)
      .withOptionalParam(timeLimitParamKey, req.timeLimit)
      .withOptionalParam(urlParamKey, req.url)
      .withOptionalParam(twimlParamKey, req.twiml)
      .withOptionalParam(applicationSidParamKey, req.applicationSid)

    val params = paramsFull.buildForPostParams

    createHttpRequestFor(
      s"/${apiVersion.twilioString}/Accounts/${req.accountSid}/Calls.json",
      connSettings
    ).map(
      _.withEntity(
        HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, params)
      )
    )
  }

  override protected def parseHttpResponse(
      request: CallCreateRequestExecutor.CallCreateRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[CallCreateRequestExecutor.CallCreateException, Call] = {
    httpResponse.status match {
      case StatusCodes.OK =>
        parseEntityAs[CallJsonRep](entity).map(_.toModel)
      case _ => buildResultForUnhandledResponse(request, httpRequest, httpResponse, entity)
    }
  }

  override protected def mapApiException(apiException: ApiException): CallCreateException.Api =
    CallCreateException.Api(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): CallCreateException.Unspecified = CallCreateException.Unspecified(msg, cause)
}

private object CallCreateRequestExecutorImpl {
  private val toParamKey                                 = "To"
  private val fromParamKey                               = "From"
  private val methodParamKey                             = "Method"
  private val fallbackUrlParamKey                        = "FallbackUrl"
  private val fallbackMethodParamKey                     = "FallbackMethod"
  private val statusCallbackParamKey                     = "StatusCallback"
  private val statusCallbackEventParamKey                = "StatusCallbackEvent"
  private val statusCallbackMethodParamKey               = "StatusCallbackMethod"
  private val sendDigitsParamKey                         = "SendDigits"
  private val timeoutParamKey                            = "Timeout"
  private val recordParamKey                             = "Record"
  private val recordingChannelsParamKey                  = "RecordingChannels"
  private val recordingStatusCallbackParamKey            = "RecordingStatusCallback"
  private val recordingStatusCallbackEventParamKey       = "RecordingStatusCallbackEvent"
  private val recordingStatusCallbackMethodParamKey      = "RecordingStatusCallbackMethod"
  private val recordingTrackParamKey                     = "RecordingTrack"
  private val sipAuthUsernameParamKey                    = "SipAuthUsername"
  private val sipAuthPasswordParamKey                    = "SipAuthPassword"
  private val machineDetectionParamKey                   = "MachineDetection"
  private val machineDetectionTimeoutParamKey            = "MachineDetectionTimeout"
  private val machineDetectionSpeechThresholdParamKey    = "MachineDetectionSpeechThreshold"
  private val machineDetectionSpeechEndThresholdParamKey = "MachineDetectionSpeechEndThreshold"
  private val machineDetectionSilenceTimeoutParamKey     = "MachineDetectionSilenceTimeout"
  private val trimParamKey                               = "Trim"
  private val callerIdParamKey                           = "CallerId"
  private val asyncAmdParamKey                           = "AsyncAmd"
  private val asyncAmdStatusCallbackParamKey             = "AsyncAmdStatusCallback"
  private val asyncAmdStatusCallbackMethodParamKey       = "AsyncAmdStatusCallbackMethod"
  private val byocParamKey                               = "Byoc"
  private val callReasonParamKey                         = "CallReason"
  private val callTokenParamKey                          = "CallToken"
  private val timeLimitParamKey                          = "TimeLimit"
  private val urlParamKey                                = "Url"
  private val twimlParamKey                              = "Twiml"
  private val applicationSidParamKey                     = "ApplicationSid"
}
