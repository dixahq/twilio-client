// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.{HttpMethod, PositiveInteger}
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.dtmf.DtmfString
import com.dixa.twilio.model.iam.{TwilioAccount, TwimlApplication}
import com.dixa.twilio.model.twiml.Response
import com.dixa.twilio.model.voice.{Call, Trunk}

// In the documentation it looks like Twilio is returning a full call here, but reality most of the fields are null, so
// we cannot really map it to our call representation. For that reason, this should only return a Call.Sid.
// If we find that we need some of the few extra information from what they return, then we would need to create
// a custom return type for it.
// Example of what they return: {"date_updated": null, "price_unit": "USD", "parent_call_sid": null, "caller_name": null, "duration": null, "from": "+4569918190", "to": "+4522334455", "annotation": null, "answered_by": null, "sid": "CAf782cd44b45895ca16b1afb750c3f33f", "queue_time": "0", "price": null, "api_version": "2010-04-01", "status": "queued", "direction": "outbound-api", "start_time": null, "date_created": null, "from_formatted": "+4569918190", "group_sid": null, "trunk_sid": null, "forwarded_from": null, "uri": "/2010-04-01/Accounts/ACa7b77b3aab0e606a40be53388a9a95ca/Calls/CAf782cd44b45895ca16b1afb750c3f33f.json", "account_sid": "ACa7b77b3aab0e606a40be53388a9a95ca", "end_time": null, "to_formatted": "+4522334455", "phone_number_sid": "PN6973c038ea67ea37c1c98940b282d65b", "subresource_uris": {"feedback": "/2010-04-01/Accounts/ACa7b77b3aab0e606a40be53388a9a95ca/Calls/CAf782cd44b45895ca16b1afb750c3f33f/Feedback.json", "user_defined_messages": "/2010-04-01/Accounts/ACa7b77b3aab0e606a40be53388a9a95ca/Calls/CAf782cd44b45895ca16b1afb750c3f33f/UserDefinedMessages.json", "notifications": "/2010-04-01/Accounts/ACa7b77b3aab0e606a40be53388a9a95ca/Calls/CAf782cd44b45895ca16b1afb750c3f33f/Notifications.json", "recordings": "/2010-04-01/Accounts/ACa7b77b3aab0e606a40be53388a9a95ca/Calls/CAf782cd44b45895ca16b1afb750c3f33f/Recordings.json", "streams": "/2010-04-01/Accounts/ACa7b77b3aab0e606a40be53388a9a95ca/Calls/CAf782cd44b45895ca16b1afb750c3f33f/Streams.json", "payments": "/2010-04-01/Accounts/ACa7b77b3aab0e606a40be53388a9a95ca/Calls/CAf782cd44b45895ca16b1afb750c3f33f/Payments.json", "user_defined_message_subscriptions": "/2010-04-01/Accounts/ACa7b77b3aab0e606a40be53388a9a95ca/Calls/CAf782cd44b45895ca16b1afb750c3f33f/UserDefinedMessageSubscriptions.json", "siprec": "/2010-04-01/Accounts/ACa7b77b3aab0e606a40be53388a9a95ca/Calls/CAf782cd44b45895ca16b1afb750c3f33f/Siprec.json", "events": "/2010-04-01/Accounts/ACa7b77b3aab0e606a40be53388a9a95ca/Calls/CAf782cd44b45895ca16b1afb750c3f33f/Events.json", "feedback_summaries": "/2010-04-01/Accounts/ACa7b77b3aab0e606a40be53388a9a95ca/Calls/FeedbackSummary.json"}}
trait CallCreateRequestExecutor
    extends SingleRequestExecutor[
      CallCreateRequestExecutor.CallCreateRequest,
      CallCreateRequestExecutor.CallCreateException,
      Call.Sid,
      CallCreateRequestExecutor.CallCreateRequest.BuilderStartState
    ] {

  import CallCreateRequestExecutor._

  override final protected type ApiExceptionWrapper = CallCreateException.Api

  override final protected type UnspecifiedException = CallCreateException.Unspecified

  override protected def createBuilderStartState()
      : CallCreateRequestExecutor.CallCreateRequest.BuilderStartState =
    CallCreateRequestExecutor.CallCreateRequest.Builder.empty
}

object CallCreateRequestExecutor {

  sealed trait CallCreateRequest {

    def accountSid: TwilioAccount.Sid

    def to: Call.CallerId

    def from: Call.CallerId

    def method: Option[HttpMethod]

    def fallbackUrl: Option[CallbackUrl.VoiceFallbackUrl]

    def fallbackMethod: Option[HttpMethod]

    def statusCallback: Option[CallbackUrl.VoiceStatusCallbackUrl]

    def statusCallbackEvents: Option[Seq[Call.ProgressEvent]]

    def statusCallbackMethod: Option[HttpMethod]

    def sendDigits: Option[DtmfString]

    def timeout: Option[Call.Timeout]

    def record: Option[Boolean]

    def recordingChannels: Option[Call.RecordingChannels]

    def recordingStatusCallback: Option[CallbackUrl.RecordingStatusCallbackUrl]

    def recordingStatusCallbackEvents: Option[Seq[Call.RecordingEvent]]

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

    def asyncAmdStatusCallback: Option[CallbackUrl.AsyncAmdStatusCallbackUrl]

    def asyncAmdStatusCallbackMethod: Option[HttpMethod]

    def byoc: Option[Trunk.Sid]

    def callReason: Option[Call.Reason]

    def callToken: Option[Call.Token]

    def timeLimit: Option[Call.TimeLimit]

    def url: Option[CallbackUrl]

    def twiml: Option[Response]

    def applicationSid: Option[TwimlApplication.Sid]
  }

  private final case class CallCreateRequestImpl(
      accountSid: TwilioAccount.Sid,
      to: Call.CallerId,
      from: Call.CallerId,
      method: Option[HttpMethod],
      fallbackUrl: Option[CallbackUrl.VoiceFallbackUrl],
      fallbackMethod: Option[HttpMethod],
      statusCallback: Option[CallbackUrl.VoiceStatusCallbackUrl],
      statusCallbackEvents: Option[Seq[Call.ProgressEvent]],
      statusCallbackMethod: Option[HttpMethod],
      sendDigits: Option[DtmfString],
      timeout: Option[Call.Timeout],
      record: Option[Boolean],
      recordingChannels: Option[Call.RecordingChannels],
      recordingStatusCallback: Option[CallbackUrl.RecordingStatusCallbackUrl],
      recordingStatusCallbackEvents: Option[Seq[Call.RecordingEvent]],
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
      asyncAmdStatusCallback: Option[CallbackUrl.AsyncAmdStatusCallbackUrl],
      asyncAmdStatusCallbackMethod: Option[HttpMethod],
      byoc: Option[Trunk.Sid],
      callReason: Option[Call.Reason],
      callToken: Option[Call.Token],
      timeLimit: Option[Call.TimeLimit],
      url: Option[CallbackUrl.VoiceUrl],
      twiml: Option[Response],
      applicationSid: Option[TwimlApplication.Sid]
  ) extends CallCreateRequest

  object CallCreateRequest {

    /** Phantom type used to require account sid to be supplied before build can be called */
    sealed trait AccountSidAttributeSet

    sealed trait AccountSidAttributeSetTrue extends AccountSidAttributeSet

    sealed trait AccountSidAttributeSetFalse extends AccountSidAttributeSet

    /** Phantom type used to require to caller id to be supplied before build can be called */
    sealed trait ToCallerIdAttributeSet

    sealed trait ToCallerIdAttributeSetTrue extends ToCallerIdAttributeSet

    sealed trait ToCallerIdAttributeSetFalse extends ToCallerIdAttributeSet

    /** Phantom type used to require from caller id to be supplied before build can be called */
    sealed trait FromCallerIdAttributeSet

    sealed trait FromCallerIdAttributeSetTrue extends FromCallerIdAttributeSet

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

    sealed trait HasUrlForMethodSetTrue extends HasUrlForMethodSet

    sealed trait HasUrlForMethodSetFalse extends HasUrlForMethodSet

    sealed trait HasFallbackUrlForMethodSet

    sealed trait HasFallbackUrlForMethodSetTrue extends HasFallbackUrlForMethodSet

    sealed trait HasFallbackUrlForMethodSetFalse extends HasFallbackUrlForMethodSet

    sealed trait HasStatusCallbackUrlForMethodSet

    sealed trait HasStatusCallbackUrlForMethodTrue extends HasStatusCallbackUrlForMethodSet

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

    /** Require record to be set because other record attributes will be useless without it */
    sealed trait HasRecordForRecordAttributesSet

    sealed trait HasRecordForRecordAttributesSetTrue extends HasRecordForRecordAttributesSet

    sealed trait HasRecordForRecordAttributesSetFalse extends HasRecordForRecordAttributesSet

    /** Require asyncAmd to be set because other asyncAmd attributes will be useless without it */
    sealed trait HasAsyncAmdForAsyncAmdAttributesSet

    sealed trait HasAsyncAmdForAsyncAmdAttributesSetTrue extends HasAsyncAmdForAsyncAmdAttributesSet

    sealed trait HasAsyncAmdForAsyncAmdAttributesSetFalse
        extends HasAsyncAmdForAsyncAmdAttributesSet

    // url and appSid: url is ignored if app sid is set
    // url and twiml: twiml is ignored if url is set
    // twiml and appSid: probably (?) twiml is ignored if app sid is set

    /** Allows to set only one of the url, twiml or applicationSid attributes */
    sealed trait HasUrlOrTwimlOrApplicationSidSet

    sealed trait HasUrlOrTwimlOrApplicationSidTrue extends HasUrlOrTwimlOrApplicationSidSet

    sealed trait HasUrlOrTwimlOrApplicationSidFalse extends HasUrlOrTwimlOrApplicationSidSet

    // From ignored attribute's point of view - I am ignored
    sealed trait IsIgnoredBecauseApplicationSidAttributeSet

    sealed trait IsIgnoredBecauseApplicationSidSetTrue
        extends IsIgnoredBecauseApplicationSidAttributeSet

    sealed trait IsIgnoredBecauseApplicationSidSetFalse
        extends IsIgnoredBecauseApplicationSidAttributeSet

    // From application sid point of view - they are ignored
    sealed trait AttributeIgnoredBecauseApplicationSidAttributeSet

    sealed trait MethodIgnoredBecauseApplicationSidSetTrue
        extends AttributeIgnoredBecauseApplicationSidAttributeSet

    sealed trait UrlIgnoredBecauseApplicationSidSetTrue
        extends AttributeIgnoredBecauseApplicationSidAttributeSet

    sealed trait FallbackUrlIgnoredBecauseApplicationSidSetTrue
        extends AttributeIgnoredBecauseApplicationSidAttributeSet

    sealed trait FallbackMethodIgnoredBecauseApplicationSidSetTrue
        extends AttributeIgnoredBecauseApplicationSidAttributeSet

    sealed trait StatusCallbackIgnoredBecauseApplicationSidSetTrue
        extends AttributeIgnoredBecauseApplicationSidAttributeSet

    sealed trait StatusCallbackMethodIgnoredBecauseApplicationSidSetTrue
        extends AttributeIgnoredBecauseApplicationSidAttributeSet

    sealed trait StatusCallbackEventsIgnoredBecauseApplicationSidSetTrue
        extends AttributeIgnoredBecauseApplicationSidAttributeSet

    sealed trait AttributeIgnoredBecauseApplicationSidSetFalse
        extends AttributeIgnoredBecauseApplicationSidAttributeSet

    // From machine detection point of view - I am ignored because sendDigits is here
    sealed trait IsIgnoredBecauseSendDigitsAttributeSet

    sealed trait IsIgnoredBecauseSendDigitsSetTrue extends IsIgnoredBecauseSendDigitsAttributeSet

    sealed trait IsIgnoredBecauseSendDigitsSetFalse extends IsIgnoredBecauseSendDigitsAttributeSet

    // From sendDigits point of view - they (machine detection) are ignored
    sealed trait MachineDetectionIgnoredBecauseSendDigitsAttributeSet

    sealed trait MachineDetectionIgnoredBecauseSendDigitsSetTrue
        extends MachineDetectionIgnoredBecauseSendDigitsAttributeSet

    sealed trait MachineDetectionIgnoredBecauseSendDigitsSetFalse
        extends MachineDetectionIgnoredBecauseSendDigitsAttributeSet

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
        HasRecordForRecordAttributesSetFalse,
        HasAsyncAmdForAsyncAmdAttributesSetFalse,
        HasUrlOrTwimlOrApplicationSidFalse,
        IsIgnoredBecauseApplicationSidSetFalse,
        AttributeIgnoredBecauseApplicationSidSetFalse,
        IsIgnoredBecauseSendDigitsSetFalse,
        MachineDetectionIgnoredBecauseSendDigitsSetFalse
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
        RecordForRecordAttributesSet <: HasRecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet <: HasAsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid <: HasUrlOrTwimlOrApplicationSidSet,
        IsIgnoredBecauseApplicationSidSet <: IsIgnoredBecauseApplicationSidAttributeSet,
        AttributeIgnoredBecauseApplicationSidSet <: AttributeIgnoredBecauseApplicationSidAttributeSet,
        IsIgnoredBecauseSendDigitsSet <: IsIgnoredBecauseSendDigitsAttributeSet,
        MachineDetectionIgnoredBecauseSendDigitsSet <: MachineDetectionIgnoredBecauseSendDigitsAttributeSet
    ] private[CallCreateRequest] (
        accountSid: Option[TwilioAccount.Sid],
        to: Option[Call.CallerId],
        from: Option[Call.CallerId],
        method: Option[HttpMethod],
        fallbackUrl: Option[CallbackUrl.VoiceFallbackUrl],
        fallbackMethod: Option[HttpMethod],
        statusCallback: Option[CallbackUrl.VoiceStatusCallbackUrl],
        statusCallbackEvents: Option[Seq[Call.ProgressEvent]],
        statusCallbackMethod: Option[HttpMethod],
        sendDigits: Option[DtmfString],
        timeout: Option[Call.Timeout],
        record: Option[Boolean],
        recordingChannels: Option[Call.RecordingChannels],
        recordingStatusCallback: Option[CallbackUrl.RecordingStatusCallbackUrl],
        recordingStatusCallbackEvents: Option[Seq[Call.RecordingEvent]],
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
        asyncAmdStatusCallback: Option[CallbackUrl.AsyncAmdStatusCallbackUrl],
        asyncAmdStatusCallbackMethod: Option[HttpMethod],
        byoc: Option[Trunk.Sid],
        callReason: Option[Call.Reason],
        callToken: Option[Call.Token],
        timeLimit: Option[Call.TimeLimit],
        url: Option[CallbackUrl.VoiceUrl],
        twiml: Option[Response],
        applicationSid: Option[TwimlApplication.Sid]
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          Some(accountSid),
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          Some(to),
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          Some(from),
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
          implicit ev: UrlAndMethod =:= HasUrlForMethodSetTrue,
          ev2: IsIgnoredBecauseApplicationSidSet =:= IsIgnoredBecauseApplicationSidSetFalse
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        MethodIgnoredBecauseApplicationSidSetTrue,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          Some(method),
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
          fallbackUrl: CallbackUrl.VoiceFallbackUrl
      )(
          implicit ev: IsIgnoredBecauseApplicationSidSet =:= IsIgnoredBecauseApplicationSidSetFalse
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        FallbackUrlIgnoredBecauseApplicationSidSetTrue,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          Some(fallbackUrl),
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
          implicit ev: FallbackUrlAndMethod =:= HasFallbackUrlForMethodSetTrue,
          ev2: IsIgnoredBecauseApplicationSidSet =:= IsIgnoredBecauseApplicationSidSetFalse
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        FallbackMethodIgnoredBecauseApplicationSidSetTrue,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          Some(fallbackMethod),
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
          statusCallback: CallbackUrl.VoiceStatusCallbackUrl
      )(
          implicit ev: IsIgnoredBecauseApplicationSidSet =:= IsIgnoredBecauseApplicationSidSetFalse
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        StatusCallbackIgnoredBecauseApplicationSidSetTrue,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          Some(statusCallback),
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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

      def withStatusCallbackEvents(
          statusCallbackEvents: Seq[Call.ProgressEvent]
      )(
          implicit ev: IsIgnoredBecauseApplicationSidSet =:= IsIgnoredBecauseApplicationSidSetFalse
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        StatusCallbackEventsIgnoredBecauseApplicationSidSetTrue,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          Some(statusCallbackEvents),
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
          implicit ev: StatusCallbackUrlAndMethod =:= HasStatusCallbackUrlForMethodTrue,
          ev2: IsIgnoredBecauseApplicationSidSet =:= IsIgnoredBecauseApplicationSidSetFalse
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        StatusCallbackMethodIgnoredBecauseApplicationSidSetTrue,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          Some(statusCallbackMethod),
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
      )(
          implicit
          ev: MachineDetectionIgnoredBecauseSendDigitsSet =:= MachineDetectionIgnoredBecauseSendDigitsSetFalse
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSetTrue,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          Some(sendDigits),
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          Some(timeout),
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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

      /** record by default is false */
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
        HasRecordForRecordAttributesSetTrue,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          Some(record),
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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

      /** recordingChannels by default is mono */
      def withRecordingChannels(
          recordingChannels: Call.RecordingChannels
      )(
          implicit ev: RecordForRecordAttributesSet =:= HasRecordForRecordAttributesSetTrue
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          Some(recordingChannels),
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
          recordingStatusCallback: CallbackUrl.RecordingStatusCallbackUrl
      )(
          implicit ev: RecordForRecordAttributesSet =:= HasRecordForRecordAttributesSetTrue
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          Some(recordingStatusCallback),
          recordingStatusCallbackEvents,
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

      /** recordingStatusCallbackEvents by default is completed */
      def withRecordingStatusCallbackEvents(
          recordingStatusCallbackEvents: Seq[Call.RecordingEvent]
      )(
          implicit ev: RecordForRecordAttributesSet =:= HasRecordForRecordAttributesSetTrue
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          Some(recordingStatusCallbackEvents),
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
      )(
          implicit ev: RecordForRecordAttributesSet =:= HasRecordForRecordAttributesSetTrue
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
      )(
          implicit ev: IsIgnoredBecauseSendDigitsSet =:= IsIgnoredBecauseSendDigitsSetFalse
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSetTrue
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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

      /** machineDetectionTimeout by default is 30 seconds */
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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

      /** machineDetectionSpeechThreshold by default is 2400 */
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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

      /** machineDetectionSpeechEndThreshold by default is 1200 */
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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

      /** machineDetectionSilenceTimeout by default is 5000 */
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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

      /** trim by default is trim-silence */
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
        RecordForRecordAttributesSet,
        HasAsyncAmdForAsyncAmdAttributesSetTrue,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
          asyncAmdStatusCallback: CallbackUrl.AsyncAmdStatusCallbackUrl
      )(
          implicit ev: AsyncAmdForAsyncAmdAttributesSet =:= HasAsyncAmdForAsyncAmdAttributesSetTrue
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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

      /** Constraints depend on account and configuration */
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        UrlOrTwimlOrApplicationSid,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
          url: CallbackUrl.VoiceUrl
      )(
          implicit ev: UrlOrTwimlOrApplicationSid =:= HasUrlOrTwimlOrApplicationSidFalse,
          ev2: IsIgnoredBecauseApplicationSidSet =:= IsIgnoredBecauseApplicationSidSetFalse
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        HasUrlOrTwimlOrApplicationSidTrue,
        IsIgnoredBecauseApplicationSidSet,
        UrlIgnoredBecauseApplicationSidSetTrue,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
          twiml: Response
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        HasUrlOrTwimlOrApplicationSidTrue,
        IsIgnoredBecauseApplicationSidSet,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
          applicationSid: TwimlApplication.Sid
      )(
          implicit ev: UrlOrTwimlOrApplicationSid =:= HasUrlOrTwimlOrApplicationSidFalse,
          ev2: AttributeIgnoredBecauseApplicationSidSet =:= AttributeIgnoredBecauseApplicationSidSetFalse
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
        RecordForRecordAttributesSet,
        AsyncAmdForAsyncAmdAttributesSet,
        HasUrlOrTwimlOrApplicationSidTrue,
        IsIgnoredBecauseApplicationSidSetTrue,
        AttributeIgnoredBecauseApplicationSidSet,
        IsIgnoredBecauseSendDigitsSet,
        MachineDetectionIgnoredBecauseSendDigitsSet
      ] = {
        new Builder(
          accountSid,
          to,
          from,
          method,
          fallbackUrl,
          fallbackMethod,
          statusCallback,
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
          statusCallbackEvents,
          statusCallbackMethod,
          sendDigits,
          timeout,
          record,
          recordingChannels,
          recordingStatusCallback,
          recordingStatusCallbackEvents,
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
      fun(Builder.empty)

    object Builder {
      def empty: BuilderStartState =
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
    }
  }

  sealed trait CallCreateException extends RuntimeException

  object CallCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with CallCreateException
        with ApiExceptionWrapper

    final case class AccountNotAllowedToCallNumber(
        accountSid: TwilioAccount.Sid,
        to: Call.CallerId,
        from: Call.CallerId
    ) extends IllegalStateException(
          s"Account $accountSid is not allowed to call number $to from number $from - more_info https://www.twilio.com/docs/errors/21216"
        )
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
