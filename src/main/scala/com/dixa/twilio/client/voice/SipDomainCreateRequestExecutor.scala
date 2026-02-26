package com.dixa.twilio.client.voice

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.TwilioPhoneNumber
import com.dixa.twilio.model.voice.{ByocTrunk, SipDomain}
import com.dixa.twilio.model.{HttpMethod, SidAbstract}

trait SipDomainCreateRequestExecutor
    extends SingleRequestExecutor[
      SipDomainCreateRequestExecutor.SipDomainCreateRequest,
      SipDomainCreateRequestExecutor.SipDomainCreateException,
      SipDomain,
      SipDomainCreateRequestExecutor.SipDomainCreateRequest.BuilderStartState
    ] {

  override protected type ApiExceptionWrapper =
    SipDomainCreateRequestExecutor.SipDomainCreateException.Api

  override protected type UnspecifiedException =
    SipDomainCreateRequestExecutor.SipDomainCreateException.Unspecified

  override protected def createBuilderStartState()
      : SipDomainCreateRequestExecutor.SipDomainCreateRequest.BuilderStartState =
    SipDomainCreateRequestExecutor.SipDomainCreateRequest.Builder.empty
}

object SipDomainCreateRequestExecutor {

  sealed trait SipDomainCreateRequest {
    def accountSid: TwilioAccount.Sid
    def domainName: SipDomain.DomainName
    def friendlyName: Option[SipDomain.FriendlyName]
    def voiceUrl: Option[CallbackUrl.VoiceUrl]
    def voiceMethod: Option[HttpMethod]
    def voiceFallbackUrl: Option[CallbackUrl.VoiceFallbackUrl]
    def voiceFallbackMethod: Option[HttpMethod]
    def voiceStatusCallbackUrl: Option[CallbackUrl.VoiceStatusCallbackUrl]
    def voiceStatusCallbackMethod: Option[HttpMethod]
    def sipRegistration: Option[Boolean]
    def emergencyCallingEnabled: Option[Boolean]
    def secure: Option[Boolean]
    def byocTrunkSid: Option[SidAbstract]
    def emergencyCallerSid: Option[SidAbstract]
  }

  object SipDomainCreateRequest {

    type BuilderStartState = Builder[
      PhantomTypes.AccountSidSetFalse,
      PhantomTypes.DomainNameSetFalse,
      PhantomTypes.VoiceUrlSetFalse,
      PhantomTypes.VoiceFallbackurlSetFalse,
      PhantomTypes.VoiceStatusCallbackSetFalse
    ]

    /** Phantom types used to enforce compile time constraints on the Builder */
    object PhantomTypes {

      sealed trait AccountSidSet
      sealed trait AccountSidSetTrue  extends AccountSidSet
      sealed trait AccountSidSetFalse extends AccountSidSet

      sealed trait DomainNameSet
      sealed trait DomainNameSetTrue  extends DomainNameSet
      sealed trait DomainNameSetFalse extends DomainNameSet

      sealed trait VoiceUrlSet
      sealed trait VoiceUrlSetTrue  extends VoiceUrlSet
      sealed trait VoiceUrlSetFalse extends VoiceUrlSet

      sealed trait VoiceFallbackUrlSet
      sealed trait VoiceFallbackUrlSetTrue  extends VoiceFallbackUrlSet
      sealed trait VoiceFallbackurlSetFalse extends VoiceFallbackUrlSet

      sealed trait VoiceStatusCallbackSet
      sealed trait VoiceStatusCallbackSetTrue  extends VoiceStatusCallbackSet
      sealed trait VoiceStatusCallbackSetFalse extends VoiceStatusCallbackSet
    }

    final class Builder[
        AccountSidSet <: PhantomTypes.AccountSidSet,
        DomainNameSet <: PhantomTypes.DomainNameSet,
        VoiceUrlSet <: PhantomTypes.VoiceUrlSet,
        VoiceFallbackUrlSet <: PhantomTypes.VoiceFallbackUrlSet,
        StatusCallbackSet <: PhantomTypes.VoiceStatusCallbackSet
    ] private[SipDomainCreateRequest] (
        accountSid: Option[TwilioAccount.Sid],
        domainName: Option[SipDomain.DomainName],
        friendlyName: Option[SipDomain.FriendlyName],
        voiceUrl: Option[CallbackUrl.VoiceUrl],
        voiceMethod: Option[HttpMethod],
        voiceFallbackUrl: Option[CallbackUrl.VoiceFallbackUrl],
        voiceFallbackMethod: Option[HttpMethod],
        voiceStatusCallbackUrl: Option[CallbackUrl.VoiceStatusCallbackUrl],
        voiceStatusCallbackMethod: Option[HttpMethod],
        sipRegistration: Option[Boolean],
        emergencyCallingEnabled: Option[Boolean],
        secure: Option[Boolean],
        byocTrunkSid: Option[SidAbstract],
        emergencyCallerSid: Option[SidAbstract]
    ) {

      private def copy[
          NewAccountSidSet <: PhantomTypes.AccountSidSet,
          NewDomainNameSet <: PhantomTypes.DomainNameSet,
          NewVoiceUrlSet <: PhantomTypes.VoiceUrlSet,
          NewVoiceFallbackUrlSet <: PhantomTypes.VoiceFallbackUrlSet,
          NewStatusCallbackSet <: PhantomTypes.VoiceStatusCallbackSet
      ](
          accountSid: Option[TwilioAccount.Sid] = accountSid,
          domainName: Option[SipDomain.DomainName] = domainName,
          friendlyName: Option[SipDomain.FriendlyName] = friendlyName,
          voiceUrl: Option[CallbackUrl.VoiceUrl] = voiceUrl,
          voiceMethod: Option[HttpMethod] = voiceMethod,
          voiceFallbackUrl: Option[CallbackUrl.VoiceFallbackUrl] = voiceFallbackUrl,
          voiceFallbackMethod: Option[HttpMethod] = voiceFallbackMethod,
          voiceStatusCallbackUrl: Option[CallbackUrl.VoiceStatusCallbackUrl] =
            voiceStatusCallbackUrl,
          voiceStatusCallbackMethod: Option[HttpMethod] = voiceStatusCallbackMethod,
          sipRegistration: Option[Boolean] = sipRegistration,
          emergencyCallingEnabled: Option[Boolean] = emergencyCallingEnabled,
          secure: Option[Boolean] = secure,
          byocTrunkSid: Option[SidAbstract] = byocTrunkSid,
          emergencyCallerSid: Option[SidAbstract] = emergencyCallerSid
      ): Builder[
        NewAccountSidSet,
        NewDomainNameSet,
        NewVoiceUrlSet,
        NewVoiceFallbackUrlSet,
        NewStatusCallbackSet
      ] =
        new Builder(
          accountSid,
          domainName,
          friendlyName,
          voiceUrl,
          voiceMethod,
          voiceFallbackUrl,
          voiceFallbackMethod,
          voiceStatusCallbackUrl,
          voiceStatusCallbackMethod,
          sipRegistration,
          emergencyCallingEnabled,
          secure,
          byocTrunkSid,
          emergencyCallerSid
        )

      private type BuilderWithSameTypes =
        Builder[
          AccountSidSet,
          DomainNameSet,
          VoiceUrlSet,
          VoiceFallbackUrlSet,
          StatusCallbackSet
        ]

      /** The SID of the Account that will create the resource. */
      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[
        PhantomTypes.AccountSidSetTrue,
        DomainNameSet,
        VoiceUrlSet,
        VoiceFallbackUrlSet,
        StatusCallbackSet
      ] =
        copy(accountSid = Some(accountSid))

      def withDomainName(domainName: SipDomain.DomainName): Builder[
        AccountSidSet,
        PhantomTypes.DomainNameSetTrue,
        VoiceUrlSet,
        VoiceFallbackUrlSet,
        StatusCallbackSet
      ] = copy(domainName = Some(domainName))

      /** A descriptive string that you create to describe the new application. It can be up to 64
        * characters long.
        */
      def withFriendlyName(friendlyName: SipDomain.FriendlyName): BuilderWithSameTypes =
        copy(friendlyName = Some(friendlyName))

      /** The URL we should when the domain receives a call. */
      def withVoiceUrl(
          voiceUrl: CallbackUrl.VoiceUrl
      ): Builder[
        AccountSidSet,
        DomainNameSet,
        PhantomTypes.VoiceUrlSetTrue,
        VoiceFallbackUrlSet,
        StatusCallbackSet
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
        DomainNameSet,
        VoiceUrlSet,
        PhantomTypes.VoiceFallbackUrlSetTrue,
        StatusCallbackSet
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
      def withVoiceStatusCallbackUrl(
          voiceStatusCallbackUrl: CallbackUrl.VoiceStatusCallbackUrl
      ): Builder[
        AccountSidSet,
        DomainNameSet,
        VoiceUrlSet,
        VoiceFallbackUrlSet,
        PhantomTypes.VoiceStatusCallbackSetTrue
      ] =
        copy(voiceStatusCallbackUrl = Some(voiceStatusCallbackUrl))

      /** HttpMethod used for he status callback.
        *
        * Can only be set, if [[withVoiceStatusCallbackUrl]] has been called first.
        */
      def withVoiceStatusCallbackMethod(voiceStatusCallbackMethod: HttpMethod)(
          implicit
          voiceStatusCallbackSetEv: StatusCallbackSet =:= PhantomTypes.VoiceStatusCallbackSetTrue
      ): BuilderWithSameTypes =
        copy(voiceStatusCallbackMethod = Some(voiceStatusCallbackMethod))

      /** Whether to allow SIP Endpoints to register with the domain to receive calls.
        *
        * Can be true or false. true allows SIP Endpoints to register with the domain to receive
        * calls, false does not.
        */
      def withSipRegistration(sipRegistration: Boolean): BuilderWithSameTypes =
        copy(sipRegistration = Some(sipRegistration))

      /** Whether emergency calling is enabled for the domain.
        *
        * If enabled, allows emergency calls on the domain from phone numbers with validated
        * addresses.
        */
      def withEmergencyCallingEnabled(emergencyCallingEnabled: Boolean): BuilderWithSameTypes =
        copy(emergencyCallingEnabled = Some(emergencyCallingEnabled))

      /** Whether secure SIP is enabled for the domain.
        *
        * If enabled, TLS will be enforced and SRTP will be negotiated on all incoming calls to this
        * sip domain.
        */
      def withSecure(secure: Boolean): BuilderWithSameTypes =
        copy(secure = Some(secure))

      /** The SID of the BYOC Trunk(Bring Your Own Carrier) resource that the Sip Domain will be
        * associated with.
        *
        * As of now Byoc Trunk are not supported by this library, and as such we don't hava special
        * Sid type for it, and this method therefore takes any kind of Sid.
        */
      def withByocTrunkSid(byocTrunkSid: ByocTrunk.Sid): BuilderWithSameTypes =
        copy(byocTrunkSid = Some(byocTrunkSid))

      /** Whether an emergency caller sid is configured for the domain. If present, this phone
        * number will be used as the callback for the emergency call.
        *
        * As of now EmergencyCaller are not supported by this library, and as such we don't hava
        * special Sid type for it, and this method therefore takes any kind of Sid.
        */
      def withEmergencyCallerSid(emergencyCallerSid: TwilioPhoneNumber.Sid): BuilderWithSameTypes =
        copy(emergencyCallerSid = Some(emergencyCallerSid))

      def build()(
          implicit accountSidSetEv: AccountSidSet =:= PhantomTypes.AccountSidSetTrue,
          domainNameSetEv: DomainNameSet =:= PhantomTypes.DomainNameSetTrue
      ): SipDomainCreateRequest = RequestImpl(
        accountSid.get,
        domainName.get,
        friendlyName,
        voiceUrl,
        voiceMethod,
        voiceFallbackUrl,
        voiceFallbackMethod,
        voiceStatusCallbackUrl,
        voiceStatusCallbackMethod,
        sipRegistration,
        emergencyCallingEnabled,
        secure,
        byocTrunkSid,
        emergencyCallerSid
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
          None
        )
    }

    def build(fun: BuilderStartState => SipDomainCreateRequest): SipDomainCreateRequest =
      fun(Builder.empty)
  }

  private final case class RequestImpl(
      accountSid: TwilioAccount.Sid,
      domainName: SipDomain.DomainName,
      friendlyName: Option[SipDomain.FriendlyName],
      voiceUrl: Option[CallbackUrl.VoiceUrl],
      voiceMethod: Option[HttpMethod],
      voiceFallbackUrl: Option[CallbackUrl.VoiceFallbackUrl],
      voiceFallbackMethod: Option[HttpMethod],
      voiceStatusCallbackUrl: Option[CallbackUrl.VoiceStatusCallbackUrl],
      voiceStatusCallbackMethod: Option[HttpMethod],
      sipRegistration: Option[Boolean],
      emergencyCallingEnabled: Option[Boolean],
      secure: Option[Boolean],
      byocTrunkSid: Option[SidAbstract],
      emergencyCallerSid: Option[SidAbstract]
  ) extends SipDomainCreateRequest

  sealed trait SipDomainCreateException extends RuntimeException

  object SipDomainCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with SipDomainCreateException
        with ApiExceptionWrapper

    /** Returned when Twilio rejects the request with error code: 21232: Invalid Domain.
      *
      * @see
      *   https://www.twilio.com/docs/api/errors/21232
      */
    final case class InvalidDomainName(domainName: SipDomain.DomainName)
        extends RuntimeException(
          s"Cannot create SIP domain. Domain name is invalid: '$domainName'"
        )
        with SipDomainCreateException

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to create application"
          ),
          cause.orNull
        )
        with SipDomainCreateException

    object Unspecified {
      def apply(msg: String) = new Unspecified(Some(msg), None)

      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }

}
