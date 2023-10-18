package com.dixa.twilio.client.general

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.general.Application
import com.dixa.twilio.model.iam.TwilioAccount

trait ApplicationCreateRequestExecutor
    extends SingleRequestExecutor[
      ApplicationCreateRequestExecutor.ApplicationCreateRequest,
      ApplicationCreateRequestExecutor.ApplicationCreateException,
      Application
    ] {

  override protected type ApiExceptionWrapper =
    ApplicationCreateRequestExecutor.ApplicationCreateException.Api

  override protected type UnspecifiedException =
    ApplicationCreateRequestExecutor.ApplicationCreateException.Unspecified
}

object ApplicationCreateRequestExecutor {

  sealed trait ApplicationCreateRequest {
    def accountSid: TwilioAccount.Sid
    def voiceUrl: Option[CallbackUrl.VoiceUrl]
    def voiceMethod: Option[HttpMethod]
    def voiceFallbackUrl: Option[CallbackUrl.VoiceFallbackUrl]
    def voiceFallbackMethod: Option[HttpMethod]
    def statusCallback: Option[CallbackUrl.ApplicationStatusCallback]
    def statusCallbackMethod: Option[HttpMethod]
    def voiceCallerIdLookup: Option[Boolean]
    def smsUrl: Option[CallbackUrl.SmsUrl]
    def smsMethod: Option[HttpMethod]
    def smsFallbackUrl: Option[CallbackUrl.SmsFallbackUrl]
    def smsFallbackMethod: Option[HttpMethod]
    def smsStatusCallback: Option[CallbackUrl.SmsStatusCallback]
    def messageStatusCallback: Option[CallbackUrl.MessageStatusCallback]
    def friendlyName: Option[Application.FriendlyName]
    def publicApplicationConnectEnabled: Option[Boolean]
  }

  object ApplicationCreateRequest {

    type BuilderStartState = Builder[
      PhantomTypes.AccountSidSetFalse,
      PhantomTypes.VoiceUrlSetFalse,
      PhantomTypes.VoiceFallbackurlSetFalse,
      PhantomTypes.StatusCallbackSetFalse,
      PhantomTypes.SmsUrlSetFalse,
      PhantomTypes.SmsFallbackUrlSetFalse
    ]

    /** Phantom types used to enforce compile time constraints on the Builder */
    object PhantomTypes {

      sealed trait AccountSidSet
      sealed trait AccountSidSetTrue  extends AccountSidSet
      sealed trait AccountSidSetFalse extends AccountSidSet

      sealed trait VoiceUrlSet
      sealed trait VoiceUrlSetTrue  extends VoiceUrlSet
      sealed trait VoiceUrlSetFalse extends VoiceUrlSet

      sealed trait VoiceFallbackUrlSet
      sealed trait VoiceFallbackUrlSetTrue  extends VoiceFallbackUrlSet
      sealed trait VoiceFallbackurlSetFalse extends VoiceFallbackUrlSet

      sealed trait StatusCallbackSet
      sealed trait StatusCallbackSetTrue  extends StatusCallbackSet
      sealed trait StatusCallbackSetFalse extends StatusCallbackSet

      sealed trait SmsUrlSet
      sealed trait SmsUrlSetTrue  extends SmsUrlSet
      sealed trait SmsUrlSetFalse extends SmsUrlSet

      sealed trait SmsFallbackUrlSet
      sealed trait SmsFallbackUrlSetTrue  extends SmsFallbackUrlSet
      sealed trait SmsFallbackUrlSetFalse extends SmsFallbackUrlSet
    }

    final class Builder[
        AccountSidSet <: PhantomTypes.AccountSidSet,
        VoiceUrlSet <: PhantomTypes.VoiceUrlSet,
        VoiceFallbackUrlSet <: PhantomTypes.VoiceFallbackUrlSet,
        StatusCallbackSet <: PhantomTypes.StatusCallbackSet,
        SmsUrlSet <: PhantomTypes.SmsUrlSet,
        SmsFallbackUrlSet <: PhantomTypes.SmsFallbackUrlSet
    ] private[ApplicationCreateRequest] (
        accountSid: Option[TwilioAccount.Sid],
        voiceUrl: Option[CallbackUrl.VoiceUrl],
        voiceMethod: Option[HttpMethod],
        voiceFallbackUrl: Option[CallbackUrl.VoiceFallbackUrl],
        voiceFallbackMethod: Option[HttpMethod],
        statusCallback: Option[CallbackUrl.ApplicationStatusCallback],
        statusCallbackMethod: Option[HttpMethod],
        voiceCallerIdLookup: Option[Boolean],
        smsUrl: Option[CallbackUrl.SmsUrl],
        smsMethod: Option[HttpMethod],
        smsFallbackUrl: Option[CallbackUrl.SmsFallbackUrl],
        smsFallbackMethod: Option[HttpMethod],
        smsStatusCallback: Option[CallbackUrl.SmsStatusCallback],
        messageStatusCallback: Option[CallbackUrl.MessageStatusCallback],
        friendlyName: Option[Application.FriendlyName],
        publicApplicationConnectEnabled: Option[Boolean]
    ) {

      private def copy[
          NewAccountSidSet <: PhantomTypes.AccountSidSet,
          NewVoiceUrlSet <: PhantomTypes.VoiceUrlSet,
          NewVoiceFallbackUrlSet <: PhantomTypes.VoiceFallbackUrlSet,
          NewStatusCallbackSet <: PhantomTypes.StatusCallbackSet,
          NewSmsUrlSet <: PhantomTypes.SmsUrlSet,
          NewSmsFallbackUrlSet <: PhantomTypes.SmsFallbackUrlSet
      ](
          accountSid: Option[TwilioAccount.Sid] = accountSid,
          voiceUrl: Option[CallbackUrl.VoiceUrl] = voiceUrl,
          voiceMethod: Option[HttpMethod] = voiceMethod,
          voiceFallbackUrl: Option[CallbackUrl.VoiceFallbackUrl] = voiceFallbackUrl,
          voiceFallbackMethod: Option[HttpMethod] = voiceFallbackMethod,
          statusCallback: Option[CallbackUrl.ApplicationStatusCallback] = statusCallback,
          statusCallbackMethod: Option[HttpMethod] = statusCallbackMethod,
          voiceCallerIdLookup: Option[Boolean] = voiceCallerIdLookup,
          smsUrl: Option[CallbackUrl.SmsUrl] = smsUrl,
          smsMethod: Option[HttpMethod] = smsMethod,
          smsFallbackUrl: Option[CallbackUrl.SmsFallbackUrl] = smsFallbackUrl,
          smsFallbackMethod: Option[HttpMethod] = smsFallbackMethod,
          smsStatusCallback: Option[CallbackUrl.SmsStatusCallback] = smsStatusCallback,
          messageStatusCallback: Option[CallbackUrl.MessageStatusCallback] = messageStatusCallback,
          friendlyName: Option[Application.FriendlyName] = friendlyName,
          publicApplicationConnectEnabled: Option[Boolean] = publicApplicationConnectEnabled
      ): Builder[
        NewAccountSidSet,
        NewVoiceUrlSet,
        NewVoiceFallbackUrlSet,
        NewStatusCallbackSet,
        NewSmsUrlSet,
        NewSmsFallbackUrlSet
      ] =
        new Builder(
          accountSid,
          voiceUrl,
          voiceMethod,
          voiceFallbackUrl,
          voiceFallbackMethod,
          statusCallback,
          statusCallbackMethod,
          voiceCallerIdLookup,
          smsUrl,
          smsMethod,
          smsFallbackUrl,
          smsFallbackMethod,
          smsStatusCallback,
          messageStatusCallback,
          friendlyName,
          publicApplicationConnectEnabled
        )

      private type BuilderWithSameTypes =
        Builder[
          AccountSidSet,
          VoiceUrlSet,
          VoiceFallbackUrlSet,
          StatusCallbackSet,
          SmsUrlSet,
          SmsFallbackUrlSet
        ]

      /** The SID of the Account that will create the resource. */
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[
        PhantomTypes.AccountSidSetTrue,
        VoiceUrlSet,
        VoiceFallbackUrlSet,
        StatusCallbackSet,
        SmsUrlSet,
        SmsFallbackUrlSet
      ] =
        copy(accountSid = Some(accountSid))

      /** The URL we should call when the phone number assigned to this application receives a call.
        */
      def withVoiceUrl(
          voiceUrl: CallbackUrl.VoiceUrl
      ): Builder[
        AccountSidSet,
        PhantomTypes.VoiceUrlSetTrue,
        VoiceFallbackUrlSet,
        StatusCallbackSet,
        SmsUrlSet,
        SmsFallbackUrlSet
      ] =
        copy(voiceUrl = Some(voiceUrl))

      /** HttpMethod used for he voiceUrl callbacks.
        *
        * Can only be set, if [[withVoiceUrl]] has been called first.
        */
      def withVoiceMethod(voiceMethod: HttpMethod)(
          implicit voiceUrlSetEv: VoiceUrlSet =:= PhantomTypes.VoiceUrlSetTrue
      ): BuilderWithSameTypes = copy(voiceMethod = Some(voiceMethod))

      /** The URL that we should call when an error occurs retrieving or executing the TwiML
        * requested by voiceUrl.
        */
      def withVoiceFallbackUrl(
          voiceFallbackUrl: CallbackUrl.VoiceFallbackUrl
      ): Builder[
        AccountSidSet,
        VoiceUrlSet,
        PhantomTypes.VoiceFallbackUrlSetTrue,
        StatusCallbackSet,
        SmsUrlSet,
        SmsFallbackUrlSet
      ] =
        copy(voiceFallbackUrl = Some(voiceFallbackUrl))

      /** HttpMethod used for he fallbackVoiceUrl callbacks.
        *
        * Can only be set, if [[withVoiceFallbackUrl]] has been called first.
        */
      def withVoiceFallbackMethod(voiceFallbackMethod: HttpMethod)(
          implicit
          voiceFallbackUrlSetEv: VoiceFallbackUrlSet =:= PhantomTypes.VoiceFallbackUrlSetTrue
      ): BuilderWithSameTypes =
        copy(voiceFallbackMethod = Some(voiceFallbackMethod))

      /** The URL we should call using the status_callback_method to send status information to your
        * application.
        */
      def withStatusCallback(statusCallback: CallbackUrl.ApplicationStatusCallback): Builder[
        AccountSidSet,
        VoiceUrlSet,
        VoiceFallbackUrlSet,
        PhantomTypes.StatusCallbackSetTrue,
        SmsUrlSet,
        SmsFallbackUrlSet
      ] =
        copy(statusCallback = Some(statusCallback))

      /** HttpMethod used for he status callback.
        *
        * Can only be set, if [[withStatusCallback]] has been called first.
        */
      def withStatusCallbackMethod(statusCallbackMethod: HttpMethod)(
          implicit statusCallbackSetEv: StatusCallbackSet =:= PhantomTypes.StatusCallbackSetTrue
      ): BuilderWithSameTypes =
        copy(statusCallbackMethod = Some(statusCallbackMethod))

      /** Whether we should look up the caller's caller-ID name from the CNAM database (additional
        * charges apply). Can be: true or false.
        */
      def withVoiceCallerIdLookup(voiceCallerIdLookup: Boolean): BuilderWithSameTypes =
        copy(voiceCallerIdLookup = Some(voiceCallerIdLookup))

      /** The URL we should call when the phone number receives an incoming SMS message. */
      def withSmsUrl(smsUrl: CallbackUrl.SmsUrl): Builder[
        AccountSidSet,
        VoiceUrlSet,
        VoiceFallbackUrlSet,
        StatusCallbackSet,
        PhantomTypes.SmsUrlSetTrue,
        SmsFallbackUrlSet
      ] =
        copy(smsUrl = Some(smsUrl))

      /** HttpMethod used for the sms url.
        *
        * Can only be set, if [[withSmsUrl]] has been called first.
        */
      def withSmsMethod(smsMethod: HttpMethod)(
          implicit smsUrlSetEv: SmsUrlSet =:= PhantomTypes.SmsUrlSetTrue
      ): BuilderWithSameTypes = copy(smsMethod = Some(smsMethod))

      /** The URL that we should call when an error occurs while retrieving or executing the TwiML
        * from sms_url.
        */
      def withSmsFallbackUrl(smsFallbackUrl: CallbackUrl.SmsFallbackUrl): Builder[
        AccountSidSet,
        VoiceUrlSet,
        VoiceFallbackUrlSet,
        StatusCallbackSet,
        SmsUrlSet,
        PhantomTypes.SmsFallbackUrlSetTrue
      ] =
        copy(smsFallbackUrl = Some(smsFallbackUrl))

      /** HttpMethod used for the sms url.
        *
        * Can only be set, if [[withSmsUrl]] has been called first.
        */
      def withSmsFallbackMethod(smsFallbackMethod: HttpMethod)(
          implicit smsFallbackUrlSetEv: SmsFallbackUrlSet =:= PhantomTypes.SmsFallbackUrlSetTrue
      ): BuilderWithSameTypes =
        copy(smsFallbackMethod = Some(smsFallbackMethod))

      /** The URL we should call using a POST method to send status information about SMS messages
        * sent by the application.
        */
      def withSmsStatusCallback(
          smsStatusCallback: CallbackUrl.SmsStatusCallback
      ): BuilderWithSameTypes =
        copy(smsStatusCallback = Some(smsStatusCallback))

      /** The URL we should call using a POST method to send message status information to your
        * application.
        */
      def withMessageStatusCallback(
          messageStatusCallback: CallbackUrl.MessageStatusCallback
      ): BuilderWithSameTypes =
        copy(messageStatusCallback = Some(messageStatusCallback))

      /** A descriptive string that you create to describe the new application. It can be up to 64
        * characters long.
        */
      def withFriendlyName(friendlyName: Application.FriendlyName): BuilderWithSameTypes =
        copy(friendlyName = Some(friendlyName))

      /** Whether to allow other Twilio accounts to dial this applicaton using Dial verb. Can be:
        * true or false.
        */
      def withPublicApplicationConnectEnabled(
          publicApplicationConnectEnabled: Boolean
      ): BuilderWithSameTypes =
        copy(publicApplicationConnectEnabled = Some(publicApplicationConnectEnabled))

      def build()(
          implicit accountSidSetEv: AccountSidSet =:= PhantomTypes.AccountSidSetTrue
      ): ApplicationCreateRequest = RequestImpl(
        accountSid.get,
        voiceUrl,
        voiceMethod,
        voiceFallbackUrl,
        voiceFallbackMethod,
        statusCallback,
        statusCallbackMethod,
        voiceCallerIdLookup,
        smsUrl,
        smsMethod,
        smsFallbackUrl,
        smsFallbackMethod,
        smsStatusCallback,
        messageStatusCallback,
        friendlyName,
        publicApplicationConnectEnabled
      )
    }

    object Builder {
      val empty =
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
          None
        )
    }

    def build(fun: BuilderStartState => ApplicationCreateRequest): ApplicationCreateRequest =
      fun(Builder.empty)
  }

  private final case class RequestImpl(
      accountSid: TwilioAccount.Sid,
      voiceUrl: Option[CallbackUrl.VoiceUrl],
      voiceMethod: Option[HttpMethod],
      voiceFallbackUrl: Option[CallbackUrl.VoiceFallbackUrl],
      voiceFallbackMethod: Option[HttpMethod],
      statusCallback: Option[CallbackUrl.ApplicationStatusCallback],
      statusCallbackMethod: Option[HttpMethod],
      voiceCallerIdLookup: Option[Boolean],
      smsUrl: Option[CallbackUrl.SmsUrl],
      smsMethod: Option[HttpMethod],
      smsFallbackUrl: Option[CallbackUrl.SmsFallbackUrl],
      smsFallbackMethod: Option[HttpMethod],
      smsStatusCallback: Option[CallbackUrl.SmsStatusCallback],
      messageStatusCallback: Option[CallbackUrl.MessageStatusCallback],
      friendlyName: Option[Application.FriendlyName],
      publicApplicationConnectEnabled: Option[Boolean]
  ) extends ApplicationCreateRequest

  sealed trait ApplicationCreateException extends RuntimeException

  object ApplicationCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with ApplicationCreateException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to create application"
          ),
          cause.orNull
        )
        with ApplicationCreateException

    object Unspecified {
      def apply(msg: String) = new Unspecified(Some(msg), None)

      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }

}
