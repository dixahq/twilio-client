package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.twiml.Response
import com.dixa.twilio.model.voice.Call

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
    def accountSid: TwilioAccount.Sid

    def to: Call.CallerId

    def from: Call.CallerId

    def method: Option[HttpMethod]

    def fallbackUrl: Option[CallbackUrl]

    def fallbackMethod: Option[HttpMethod]

    def statusCallback: Option[CallbackUrl]

    def statusCallbackEvent: Option[HttpMethod]

    def statusCallbackMethod: Option[HttpMethod]

    def sendDigits: Option[String]

    def timeout: Option[Int]

    def record: Option[Boolean]

    def recordingChannels: Option[String]

    def recordingStatusCallback: Option[String]

    def recordingStatusCallbackMethod: Option[String]

    def sipAuthUsername: Option[String]

    def sipAuthPassword: Option[String]

//    def machineDetection: Option[]

//    def machineDetectionTimeout: Option[]

    def recordingStatusCallbackEvent: Option[String]

    def trim: Option[String]

    def callerId: Option[String]

//    def machineDetectionSpeechThreshold: Option[]

//    def machineDetectionSpeechEndThreshold: Option[]

//    def machineDetectionSilenceTimeout: Option[]

//    def asyncAmd: Option[]

//    def asyncAmdStatusCallback: Option[]

//    def asyncAmdStatusCallbackMethod: Option[]

    def byoc: Option[String]

    def callReason: Option[String]

    def callToken: Option[String]

    def recordingTrack: Option[String]

    def timeLimit: Option[Call.TimeLimit]

    // Required if Twiml ApplicationSid is not passed
    def url: Option[CallbackUrl]

    // Required if Url ApplicationSid is not passed
    def twiml: Option[Response.Verified]

    // Required if Url Twiml is not passed
    def applicationSid: Option[String]
  }

  private final case class CallCreateRequestImpl(
      accountSid: TwilioAccount.Sid,
      to: Call.CallerId,
      from: Call.CallerId,
      method: Option[HttpMethod],
      fallbackUrl: Option[CallbackUrl],
      fallbackMethod: Option[HttpMethod],
      statusCallback: Option[CallbackUrl],
      statusCallbackEvent: Option[HttpMethod],
      statusCallbackMethod: Option[HttpMethod],
      sendDigits: Option[String],
      timeout: Option[Int],
      record: Option[Boolean],
      recordingChannels: Option[String],
      recordingStatusCallback: Option[String],
      recordingStatusCallbackMethod: Option[String],
      sipAuthUsername: Option[String],
      sipAuthPassword: Option[String],
      recordingStatusCallbackEvent: Option[String],
      trim: Option[String],
      callerId: Option[String],
      byoc: Option[String],
      callReason: Option[String],
      callToken: Option[String],
      recordingTrack: Option[String],
      timeLimit: Option[Call.TimeLimit],
      url: Option[CallbackUrl],
      twiml: Option[Response.Verified],
      applicationSid: Option[String]
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
