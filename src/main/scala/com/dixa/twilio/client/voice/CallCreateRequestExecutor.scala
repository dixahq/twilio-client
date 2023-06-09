package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.{HttpMethod, PositiveInteger}
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.dtmf.DtmfString
import com.dixa.twilio.model.iam.{Application, TwilioAccount}
import com.dixa.twilio.model.twiml.Response
import com.dixa.twilio.model.voice.Call.RecordingChannels
import com.dixa.twilio.model.voice.{Call, Trunk}

trait CallCreateRequestExecutor
    extends SingleRequestExecutor[
      CallCreateRequestExecutor.CallCreateRequest,
      CallCreateRequestExecutor.CallCreateException,
      Call
    ] {

  import CallCreateRequestExecutor._

  override final protected type ApiExceptionWrapper = CallCreateException.Api

  override final protected type UnspecifiedException = CallCreateException.Unspecified

}

object CallCreateRequestExecutor {

  sealed trait CallCreateRequest {

    // Required
    def accountSid: TwilioAccount.Sid

    // Required
    def to: Call.CallerId

    // Required
    def from: Call.CallerId

    def method: Option[HttpMethod]

    def fallbackUrl: Option[CallbackUrl]

    def fallbackMethod: Option[HttpMethod]

    def statusCallback: Option[CallbackUrl]

    def statusCallbackEvent: Option[Call.ProgressEvent]

    def statusCallbackMethod: Option[HttpMethod]

    def sendDigits: Option[DtmfString]

    def timeout: Option[Call.Timeout]

    def record: Option[Boolean]

    def recordingChannels: Option[RecordingChannels]

    def recordingStatusCallback: Option[CallbackUrl]

    def recordingStatusCallbackEvent: Option[Call.RecordingEvent]

    def recordingStatusCallbackMethod: Option[HttpMethod]

    def recordingTrack: Option[Call.RecordingTrack]

    def sipAuthUsername: Option[Trunk.Username]

    def sipAuthPassword: Option[Trunk.Password]

    def machineDetection: Option[Call.MachineDetection]

    def machineDetectionTimeout: Option[PositiveInteger]

    def machineDetectionSpeechThreshold: Option[Call.MachineDetectionSpeechThreshold]

    def machineDetectionSpeechEndThreshold: Option[Call.MachineDetectionSpeechEndThreshold]

    def machineDetectionSilenceTimeout: Option[Call.MachineDetectionSilenceTimeout]

    def trim: Option[Call.Trim]

    def callerId: Option[Call.CallerId]

    def asyncAmd: Option[Boolean]

    def asyncAmdStatusCallback: Option[CallbackUrl]

    def asyncAmdStatusCallbackMethod: Option[HttpMethod]

    def byoc: Option[Trunk.Sid]

    def callReason: Option[Call.Reason]

    def callToken: Option[Call.Token]

    def timeLimit: Option[Call.TimeLimit]

    // Required if Twiml ApplicationSid is not passed
    def url: Option[CallbackUrl]

    // Required if Url ApplicationSid is not passed
    def twiml: Option[Response.Verified]

    // Required if Url Twiml is not passed
    def applicationSid: Option[Application.Sid]
  }

  private final case class CallCreateRequestImpl(
      accountSid: TwilioAccount.Sid,
      to: Call.CallerId,
      from: Call.CallerId,
      method: Option[HttpMethod],
      fallbackUrl: Option[CallbackUrl],
      fallbackMethod: Option[HttpMethod],
      statusCallback: Option[CallbackUrl],
      statusCallbackEvent: Option[Call.ProgressEvent],
      statusCallbackMethod: Option[HttpMethod],
      sendDigits: Option[DtmfString],
      timeout: Option[Call.Timeout],
      record: Option[Boolean],
      recordingChannels: Option[Call.RecordingChannels],
      recordingStatusCallback: Option[CallbackUrl],
      recordingStatusCallbackEvent: Option[Call.RecordingEvent],
      recordingStatusCallbackMethod: Option[HttpMethod],
      recordingTrack: Option[Call.RecordingTrack],
      sipAuthUsername: Option[Trunk.Username],
      sipAuthPassword: Option[Trunk.Password],
      machineDetection: Option[Call.MachineDetection],
      machineDetectionTimeout: Option[PositiveInteger],
      machineDetectionSpeechThreshold: Option[Call.MachineDetectionSpeechThreshold],
      machineDetectionSpeechEndThreshold: Option[Call.MachineDetectionSpeechEndThreshold],
      machineDetectionSilenceTimeout: Option[Call.MachineDetectionSilenceTimeout],
      trim: Option[Call.Trim],
      callerId: Option[Call.CallerId],
      asyncAmd: Option[Boolean],
      asyncAmdStatusCallback: Option[CallbackUrl],
      asyncAmdStatusCallbackMethod: Option[HttpMethod],
      byoc: Option[Trunk.Sid],
      callReason: Option[Call.Reason],
      callToken: Option[Call.Token],
      timeLimit: Option[Call.TimeLimit],
      url: Option[CallbackUrl],
      twiml: Option[Response.Verified],
      applicationSid: Option[Application.Sid]
  ) extends CallCreateRequest {}

  sealed trait CallCreateException extends RuntimeException

  object CallCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with CallCreateException
        with ApiExceptionWrapper

    final case class CallNotFound(accountSid: TwilioAccount.Sid, callSid: Call.Sid)
        extends RuntimeException(s"Call with sid $callSid was not found in account: $accountSid")
        with CallCreateException

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to create call"
          ),
          cause.orNull
        )
        with CallCreateException

    object Unspecified {
      def apply(msg: String) = new Unspecified(Some(msg), None)

      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
