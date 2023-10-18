package com.dixa.twilio.client.phonenumber

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.HttpMethod
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.OutgoingCallerId.FriendlyName
import com.dixa.twilio.model.phonenumber.{
  OutgoingCallerId,
  OutgoingCallerIdCreateResponse,
  PhoneNumberE164
}

trait OutgoingCallerIdCreateRequestExecutor
    extends SingleRequestExecutor[
      OutgoingCallerIdCreateRequestExecutor.OutgoingCallerIdCreateRequest,
      OutgoingCallerIdCreateRequestExecutor.OutgoingCallerIdCreateException,
      OutgoingCallerIdCreateResponse
    ] {

  import OutgoingCallerIdCreateRequestExecutor._

  override final protected type ApiExceptionWrapper = OutgoingCallerIdCreateException.Api

  override final protected type UnspecifiedException = OutgoingCallerIdCreateException.Unspecified

}

object OutgoingCallerIdCreateRequestExecutor {

  sealed trait OutgoingCallerIdCreateRequest {
    def accountSid: TwilioAccount.Sid
    def phoneNumber: PhoneNumberE164
    def friendlyName: Option[OutgoingCallerId.FriendlyName]
    def callDelay: Option[OutgoingCallerId.CallDelay]
    def extension: Option[OutgoingCallerId.Extension]
    def statusCallback: Option[CallbackUrl.OutgoingCallerIdVerificationUrl]
    def statusCallbackMethod: Option[HttpMethod]
  }

  private final case class OutgoingCallerIdCreateRequestImpl(
      accountSid: TwilioAccount.Sid,
      phoneNumber: PhoneNumberE164,
      friendlyName: Option[OutgoingCallerId.FriendlyName],
      callDelay: Option[OutgoingCallerId.CallDelay],
      extension: Option[OutgoingCallerId.Extension],
      statusCallback: Option[CallbackUrl.OutgoingCallerIdVerificationUrl],
      statusCallbackMethod: Option[HttpMethod],
  ) extends OutgoingCallerIdCreateRequest

  object OutgoingCallerIdCreateRequest {

    /** Phantom type used to require account sid to be supplied before build can be called */
    sealed trait AccountSidAttributeSet
    sealed trait AccountSidAttributeSetTrue  extends AccountSidAttributeSet
    sealed trait AccountSidAttributeSetFalse extends AccountSidAttributeSet

    /** Phantom type used to require phone number to be supplied before build can be called */
    sealed trait PhoneNumberAttributeSet
    sealed trait PhoneNumberAttributeSetTrue  extends PhoneNumberAttributeSet
    sealed trait PhoneNumberAttributeSetFalse extends PhoneNumberAttributeSet

    /** Phantom type used to require callback url is supplied before build can be called */
    sealed trait HasUrlForMethodSet
    sealed trait HasUrlForMethodSetTrue  extends HasUrlForMethodSet
    sealed trait HasUrlForMethodSetFalse extends HasUrlForMethodSet

    type BuilderStartState =
      Builder[
        AccountSidAttributeSetFalse,
        PhoneNumberAttributeSetFalse,
        HasUrlForMethodSetTrue
      ]

    final class Builder[
        AccountSidSet <: AccountSidAttributeSet,
        PhoneNumberSet <: PhoneNumberAttributeSet,
        UrlForMethod <: HasUrlForMethodSet
    ] private[OutgoingCallerIdCreateRequest] (
        accountSid: Option[TwilioAccount.Sid],
        phoneNumber: Option[PhoneNumberE164],
        friendlyName: Option[OutgoingCallerId.FriendlyName],
        callDelay: Option[OutgoingCallerId.CallDelay],
        extension: Option[OutgoingCallerId.Extension],
        statusCallback: Option[CallbackUrl.OutgoingCallerIdVerificationUrl],
        statusCallbackMethod: Option[HttpMethod],
    ) {

      def withAccountSid(
          accountSid: TwilioAccount.Sid
      ): Builder[
        AccountSidAttributeSetTrue,
        PhoneNumberSet,
        UrlForMethod
      ] = {
        new Builder(
          Some(accountSid),
          phoneNumber,
          friendlyName,
          callDelay,
          extension,
          statusCallback,
          statusCallbackMethod
        )
      }

      def withPhoneNumber(
          phoneNumber: PhoneNumberE164
      ): Builder[
        AccountSidSet,
        PhoneNumberAttributeSetTrue,
        UrlForMethod
      ] = {
        new Builder(
          accountSid,
          Some(phoneNumber),
          friendlyName,
          callDelay,
          extension,
          statusCallback,
          statusCallbackMethod
        )
      }

      def withFriendlyName(
          friendlyName: FriendlyName
      ): Builder[
        AccountSidSet,
        PhoneNumberSet,
        UrlForMethod
      ] = {
        new Builder(
          accountSid,
          phoneNumber,
          Some(friendlyName),
          callDelay,
          extension,
          statusCallback,
          statusCallbackMethod,
        )
      }

      def withCallDelay(
          callDelay: OutgoingCallerId.CallDelay
      ): Builder[
        AccountSidSet,
        PhoneNumberSet,
        UrlForMethod
      ] = {
        new Builder(
          accountSid,
          phoneNumber,
          friendlyName,
          Some(callDelay),
          extension,
          statusCallback,
          statusCallbackMethod,
        )
      }

      def withExtension(
          extension: OutgoingCallerId.Extension
      ): Builder[
        AccountSidSet,
        PhoneNumberSet,
        UrlForMethod
      ] = {
        new Builder(
          accountSid,
          phoneNumber,
          friendlyName,
          callDelay,
          Some(extension),
          statusCallback,
          statusCallbackMethod,
        )
      }

      def withCallback(
          statusCallback: CallbackUrl.OutgoingCallerIdVerificationUrl
      ): Builder[
        AccountSidSet,
        PhoneNumberSet,
        HasUrlForMethodSetTrue
      ] = {
        new Builder(
          accountSid,
          phoneNumber,
          friendlyName,
          callDelay,
          extension,
          Some(statusCallback),
          statusCallbackMethod,
        )
      }

      def withCallbackMethod(
          statusCallbackMethod: HttpMethod
      )(
          implicit ev: UrlForMethod =:= HasUrlForMethodSetTrue
      ): Builder[
        AccountSidSet,
        PhoneNumberSet,
        UrlForMethod
      ] = {
        new Builder(
          accountSid,
          phoneNumber,
          friendlyName,
          callDelay,
          extension,
          statusCallback,
          Some(statusCallbackMethod),
        )
      }

      def build()(
          implicit ev: AccountSidSet =:= AccountSidAttributeSetTrue,
          ev2: PhoneNumberSet =:= PhoneNumberAttributeSetTrue,
          ev3: UrlForMethod =:= HasUrlForMethodSetTrue,
      ): OutgoingCallerIdCreateRequest =
        OutgoingCallerIdCreateRequestImpl(
          accountSid.get,
          phoneNumber.get,
          friendlyName,
          callDelay,
          extension,
          statusCallback,
          statusCallbackMethod,
        )
    }

    def build(
        fun: BuilderStartState => OutgoingCallerIdCreateRequest
    ): OutgoingCallerIdCreateRequest =
      fun(
        new BuilderStartState(
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

  sealed trait OutgoingCallerIdCreateException extends RuntimeException

  object OutgoingCallerIdCreateException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with OutgoingCallerIdCreateException
        with ApiExceptionWrapper

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to create outgoing caller id"
          ),
          cause.orNull
        )
        with OutgoingCallerIdCreateException

    object Unspecified {
      def apply(msg: String) = new Unspecified(Some(msg), None)

      def apply(cause: Throwable) = new Unspecified(Option(cause.getMessage), Some(cause))
    }
  }
}
