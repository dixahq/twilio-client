package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.{HttpMethod, PositiveInteger}
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.dtmf.DtmfString
import com.dixa.twilio.model.iam.{Application, TwilioAccount}
import com.dixa.twilio.model.twiml.Response
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

    def accountSid: TwilioAccount.Sid

    def to: Call.CallerId

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

    def recordingChannels: Option[Call.RecordingChannels]

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

    def url: Option[CallbackUrl]

    def twiml: Option[Response.Verified]

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
  ) extends CallCreateRequest

  object CallCreateRequest {

    /** Phantom type used to require account sid to be supplied before build can be called */
    sealed trait AccountSidAttributeSet
    sealed trait AccountSidAttributeSetTrue  extends AccountSidAttributeSet
    sealed trait AccountSidAttributeSetFalse extends AccountSidAttributeSet

    /** Phantom type used to require to caller id to be supplied before build can be called */
    sealed trait ToCallerIdAttributeSet
    sealed trait ToCallerIdAttributeSetTrue  extends ToCallerIdAttributeSet
    sealed trait ToCallerIdAttributeSetFalse extends ToCallerIdAttributeSet

    /** Phantom type used to require from caller id to be supplied before build can be called */
    sealed trait FromCallerIdAttributeSet
    sealed trait FromCallerIdAttributeSetTrue  extends FromCallerIdAttributeSet
    sealed trait FromCallerIdAttributeSetFalse extends FromCallerIdAttributeSet

    /** Phantom type used to require one of url, twiml or applicationSid to be supplied before build
      * can be called
      */
    sealed trait OneOfUrlOrTwimlOrApplicationSidAttributeSet
    sealed trait OneOfUrlOrTwimlOrApplicationSidAttributeSetTrue
        extends OneOfUrlOrTwimlOrApplicationSidAttributeSet
    sealed trait OneOfUrlOrTwimlOrApplicationSidAttributeSetFalse
        extends OneOfUrlOrTwimlOrApplicationSidAttributeSet

    sealed trait HasUrlForMethodSet
    sealed trait HasUrlForMethodSetTrue  extends HasUrlForMethodSet
    sealed trait HasUrlForMethodSetFalse extends HasUrlForMethodSet

    sealed trait HasFallbackUrlForMethodSet
    sealed trait HasFallbackUrlForMethodSetTrue  extends HasFallbackUrlForMethodSet
    sealed trait HasFallbackUrlForMethodSetFalse extends HasFallbackUrlForMethodSet

    sealed trait HasStatusCallbackUrlForMethodSet
    sealed trait HasStatusCallbackUrlForMethodTrue  extends HasStatusCallbackUrlForMethodSet
    sealed trait HasStatusCallbackUrlForMethodFalse extends HasStatusCallbackUrlForMethodSet

    sealed trait HasRecordingStatusCallbackUrlForMethodSet
    sealed trait HasRecordingStatusCallbackUrlForMethodTrue
        extends HasRecordingStatusCallbackUrlForMethodSet
    sealed trait HasRecordingStatusCallbackUrlForMethodFalse
        extends HasRecordingStatusCallbackUrlForMethodSet

    sealed trait HasAsyncAmdStatusCallbackUrlForMethodSet
    sealed trait HasAsyncAmdStatusCallbackUrlForMethodTrue
        extends HasAsyncAmdStatusCallbackUrlForMethodSet
    sealed trait HasAsyncAmdStatusCallbackUrlForMethodFalse
        extends HasAsyncAmdStatusCallbackUrlForMethodSet

    sealed trait HasUrlOrTwimlOrApplicationSidSet
    sealed trait HasUrlOrTwimlOrApplicationSidTrue  extends HasUrlOrTwimlOrApplicationSidSet
    sealed trait HasUrlOrTwimlOrApplicationSidFalse extends HasUrlOrTwimlOrApplicationSidSet

    type BuilderStartState =
      Builder[
        AccountSidAttributeSetFalse,
        ToCallerIdAttributeSetFalse,
        FromCallerIdAttributeSetFalse,
        OneOfUrlOrTwimlOrApplicationSidAttributeSetFalse,
        HasUrlForMethodSetFalse,
        HasFallbackUrlForMethodSetFalse,
        HasStatusCallbackUrlForMethodFalse,
        HasRecordingStatusCallbackUrlForMethodFalse,
        HasAsyncAmdStatusCallbackUrlForMethodFalse,
        HasUrlOrTwimlOrApplicationSidFalse
      ]

    final class Builder[
        AccountSidSet <: AccountSidAttributeSet,
        ToCallerIdSet <: ToCallerIdAttributeSet,
        FromCallerIdSet <: FromCallerIdAttributeSet,
        OneOfUrlOrTwimlOrApplicationSidSet <: OneOfUrlOrTwimlOrApplicationSidAttributeSet,
        UrlAndMethod <: HasUrlForMethodSet,
        FallbackUrlAndMethod <: HasFallbackUrlForMethodSet,
        StatusCallbackUrlAndMethod <: HasStatusCallbackUrlForMethodSet,
        RecordingStatusCallbackUrlAndMethod <: HasRecordingStatusCallbackUrlForMethodSet,
        AsyncAmdStatusCallbackUrlAndMethod <: HasAsyncAmdStatusCallbackUrlForMethodSet,
        UrlOrTwimlOrApplicationSid <: HasUrlOrTwimlOrApplicationSidSet
    ] private[CallCreateRequest] (
        accountSid: Option[TwilioAccount.Sid],
        to: Option[Call.CallerId],
        from: Option[Call.CallerId],
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
    ) {

      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[
        AccountSidAttributeSetTrue,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          Some(accountSid),
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withToCallerId(
          to: Call.CallerId
      ): Builder[
        AccountSidSet,
        ToCallerIdAttributeSetTrue,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          Some(to),
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withFromCallerId(
          from: Call.CallerId
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdAttributeSetTrue,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          Some(from),
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withMethod(method: HttpMethod)(
          implicit ev: UrlAndMethod =:= HasUrlForMethodSetTrue
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          Some(method),
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withFallbackUrl(
          fallbackUrl: CallbackUrl
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        HasFallbackUrlForMethodSetTrue,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          Some(fallbackUrl),
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withFallbackMethod(
          fallbackMethod: HttpMethod
      )(
          implicit ev: FallbackUrlAndMethod =:= HasFallbackUrlForMethodSetTrue
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          Some(fallbackMethod),
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withStatusCallback(
          statusCallback: CallbackUrl
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        HasStatusCallbackUrlForMethodTrue,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          Some(statusCallback),
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withStatusCallbackEvent(
          statusCallbackEvent: Call.ProgressEvent
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          Some(statusCallbackEvent),
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withStatusCallbackMethod(
          statusCallbackMethod: HttpMethod
      )(
          implicit ev: StatusCallbackUrlAndMethod =:= HasStatusCallbackUrlForMethodTrue
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          Some(statusCallbackMethod),
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withSendDigits(
          sendDigits: DtmfString
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          Some(sendDigits),
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withTimeout(
          timeout: Call.Timeout
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          Some(timeout),
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withRecord(
          record: Boolean
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          Some(record),
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withRecordingChannels(
          recordingChannels: Call.RecordingChannels
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          Some(recordingChannels),
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withRecordingStatusCallback(
          recordingStatusCallback: CallbackUrl
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        HasRecordingStatusCallbackUrlForMethodTrue,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          Some(recordingStatusCallback),
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withRecordingStatusCallbackEvent(
          recordingStatusCallbackEvent: Call.RecordingEvent
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          Some(recordingStatusCallbackEvent),
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withRecordingStatusCallbackMethod(
          recordingStatusCallbackMethod: HttpMethod
      )(
          implicit
          ev: RecordingStatusCallbackUrlAndMethod =:= HasRecordingStatusCallbackUrlForMethodTrue
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          Some(recordingStatusCallbackMethod),
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withRecordingTrack(
          recordingTrack: Call.RecordingTrack
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          Some(recordingTrack),
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withSipAuthUsername(
          sipAuthUsername: Trunk.Username
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          Some(sipAuthUsername),
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withSipAuthPassword(
          sipAuthPassword: Trunk.Password
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          Some(sipAuthPassword),
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withMachineDetection(
          machineDetection: Call.MachineDetection
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          Some(machineDetection),
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withMachineDetectionTimeout(
          machineDetectionTimeout: PositiveInteger
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          Some(machineDetectionTimeout),
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withMachineDetectionSpeechThreshold(
          machineDetectionSpeechThreshold: Call.MachineDetectionSpeechThreshold
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          Some(machineDetectionSpeechThreshold),
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withMachineDetectionSpeechEndThreshold(
          machineDetectionSpeechEndThreshold: Call.MachineDetectionSpeechEndThreshold
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          Some(machineDetectionSpeechEndThreshold),
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withMachineDetectionSilenceTimeout(
          machineDetectionSilenceTimeout: Call.MachineDetectionSilenceTimeout
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          Some(machineDetectionSilenceTimeout),
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withTrim(
          trim: Call.Trim
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          Some(trim),
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withCallerId(
          callerId: Call.CallerId
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          Some(callerId),
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withAsyncAmd(
          asyncAmd: Boolean
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          Some(asyncAmd),
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withAsyncAmdStatusCallback(
          asyncAmdStatusCallback: CallbackUrl
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        HasAsyncAmdStatusCallbackUrlForMethodTrue,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          Some(asyncAmdStatusCallback),
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withAsyncAmdStatusCallbackMethod(
          asyncAmdStatusCallbackMethod: HttpMethod
      )(
          implicit
          ev: AsyncAmdStatusCallbackUrlAndMethod =:= HasAsyncAmdStatusCallbackUrlForMethodTrue
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          Some(asyncAmdStatusCallbackMethod),
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withByoc(
          byoc: Trunk.Sid
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          Some(byoc),
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withCallReason(
          callReason: Call.Reason
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          Some(callReason),
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withCallToken(
          callToken: Call.Token
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          Some(callToken),
          timeLimit,
          url,
          twiml,
          applicationSid
        )
      }

      def withTimeLimit(
          timeLimit: Call.TimeLimit
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidSet,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        UrlOrTwimlOrApplicationSid
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          Some(timeLimit),
          url,
          twiml,
          applicationSid
        )
      }

      def withUrl(
          url: CallbackUrl
      )(
          implicit ev: UrlOrTwimlOrApplicationSid =:= HasUrlOrTwimlOrApplicationSidFalse
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidAttributeSetTrue,
        HasUrlForMethodSetTrue,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        HasUrlOrTwimlOrApplicationSidTrue
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          Some(url),
          twiml,
          applicationSid
        )
      }

      def withTwiml(
          twiml: Response.Verified
      )(
          implicit ev: UrlOrTwimlOrApplicationSid =:= HasUrlOrTwimlOrApplicationSidFalse
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidAttributeSetTrue,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        HasUrlOrTwimlOrApplicationSidTrue
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          Some(twiml),
          applicationSid
        )
      }

      def withApplicationSid(
          applicationSid: Application.Sid
      )(
          implicit ev: UrlOrTwimlOrApplicationSid =:= HasUrlOrTwimlOrApplicationSidFalse
      ): Builder[
        AccountSidSet,
        ToCallerIdSet,
        FromCallerIdSet,
        OneOfUrlOrTwimlOrApplicationSidAttributeSetTrue,
        UrlAndMethod,
        FallbackUrlAndMethod,
        StatusCallbackUrlAndMethod,
        RecordingStatusCallbackUrlAndMethod,
        AsyncAmdStatusCallbackUrlAndMethod,
        HasUrlOrTwimlOrApplicationSidTrue
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          Some(applicationSid)
        )
      }

      def build()(
          implicit ev: AccountSidSet =:= AccountSidAttributeSetTrue,
          ev2: ToCallerIdSet =:= ToCallerIdAttributeSetTrue,
          ev3: FromCallerIdSet =:= FromCallerIdAttributeSetTrue,
          ev4: OneOfUrlOrTwimlOrApplicationSidSet =:= OneOfUrlOrTwimlOrApplicationSidAttributeSetTrue
      ): CallCreateRequest =
        CallCreateRequestImpl(
          accountSid.get,
          to.get,
          from.get,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvent,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvent,
          recordingStatusCallbackMethod,
          recordingTrack,
          sipAuthUsername,
          sipAuthPassword,
          machineDetection,
          machineDetectionTimeout,
          machineDetectionSpeechThreshold,
          machineDetectionSpeechEndThreshold,
          machineDetectionSilenceTimeout,
          trim,
          callerId,
          asyncAmd,
          asyncAmdStatusCallback,
          asyncAmdStatusCallbackMethod,
          byoc,
          callReason,
          callToken,
          timeLimit,
          url,
          twiml,
          applicationSid
        )
    }

    def build(fun: BuilderStartState => CallCreateRequest): CallCreateRequest =
      fun(
        new BuilderStartState(
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None
        )
      )
  }

  sealed trait CallCreateException extends RuntimeException

  object CallCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with CallCreateException
        with ApiExceptionWrapper

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
