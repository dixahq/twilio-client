package com.dixa.twilio.client.phonenumber

import com.dixa.twilio.client.RequestExecutor.ApiExceptionWrapper
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.{EnumWithTwilioString, HttpMethod}
import com.dixa.twilio.model.callback.CallbackUrl
import com.dixa.twilio.model.dtmf.DtmfString
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.OutgoingCallerId.FriendlyName
import com.dixa.twilio.model.phonenumber.{OutgoingCallerId, PhoneNumberE164}
import com.dixa.twilio.model.voice.Call

import scala.collection.immutable

trait OutgoingCallerIdCreateRequestExecutor
    extends SingleRequestExecutor[
      OutgoingCallerIdCreateRequestExecutor.OutgoingCallerIdCreateRequest,
      OutgoingCallerIdCreateRequestExecutor.OutgoingCallerIdCreateException,
      OutgoingCallerIdCreateRequestExecutor.OutgoingCallerIdCreateResponse
    ] {

  import OutgoingCallerIdCreateRequestExecutor._

  override final protected type ApiExceptionWrapper = OutgoingCallerIdCreateException.Api

  override final protected type UnspecifiedException = OutgoingCallerIdCreateException.Unspecified

}

object OutgoingCallerIdCreateRequestExecutor {

  final case class OutgoingCallerIdCreateResponse(
      accountSid: TwilioAccount.Sid,
      phoneNumber: PhoneNumberE164,
      friendlyName: Option[OutgoingCallerId.FriendlyName],
      validationCode: DtmfString.OnlyDtmfDigits,
      callSid: Call.Sid
  )

  sealed abstract class CallDelay(
      override val twilioString: String,
  ) extends EnumWithTwilioString.EnumEntry

  object CallDelay extends EnumWithTwilioString[CallDelay] {
    override val values: immutable.IndexedSeq[CallDelay] = findValues
    case object Seconds0  extends CallDelay("0")
    case object Seconds1  extends CallDelay("1")
    case object Seconds2  extends CallDelay("2")
    case object Seconds3  extends CallDelay("3")
    case object Seconds4  extends CallDelay("4")
    case object Seconds5  extends CallDelay("5")
    case object Seconds6  extends CallDelay("6")
    case object Seconds7  extends CallDelay("7")
    case object Seconds8  extends CallDelay("8")
    case object Seconds9  extends CallDelay("9")
    case object Seconds10 extends CallDelay("10")
    case object Seconds11 extends CallDelay("11")
    case object Seconds12 extends CallDelay("12")
    case object Seconds13 extends CallDelay("13")
    case object Seconds14 extends CallDelay("14")
    case object Seconds15 extends CallDelay("15")
    case object Seconds16 extends CallDelay("16")
    case object Seconds17 extends CallDelay("17")
    case object Seconds18 extends CallDelay("18")
    case object Seconds19 extends CallDelay("19")
    case object Seconds20 extends CallDelay("20")
    case object Seconds21 extends CallDelay("21")
    case object Seconds22 extends CallDelay("22")
    case object Seconds23 extends CallDelay("23")
    case object Seconds24 extends CallDelay("24")
    case object Seconds25 extends CallDelay("25")
    case object Seconds26 extends CallDelay("26")
    case object Seconds27 extends CallDelay("27")
    case object Seconds28 extends CallDelay("28")
    case object Seconds29 extends CallDelay("29")
    case object Seconds30 extends CallDelay("30")
    case object Seconds31 extends CallDelay("31")
    case object Seconds32 extends CallDelay("32")
    case object Seconds33 extends CallDelay("33")
    case object Seconds34 extends CallDelay("34")
    case object Seconds35 extends CallDelay("35")
    case object Seconds36 extends CallDelay("36")
    case object Seconds37 extends CallDelay("37")
    case object Seconds38 extends CallDelay("38")
    case object Seconds39 extends CallDelay("39")
    case object Seconds40 extends CallDelay("40")
    case object Seconds41 extends CallDelay("41")
    case object Seconds42 extends CallDelay("42")
    case object Seconds43 extends CallDelay("43")
    case object Seconds44 extends CallDelay("44")
    case object Seconds45 extends CallDelay("45")
    case object Seconds46 extends CallDelay("46")
    case object Seconds47 extends CallDelay("47")
    case object Seconds48 extends CallDelay("48")
    case object Seconds49 extends CallDelay("49")
    case object Seconds50 extends CallDelay("50")
    case object Seconds51 extends CallDelay("51")
    case object Seconds52 extends CallDelay("52")
    case object Seconds53 extends CallDelay("53")
    case object Seconds54 extends CallDelay("54")
    case object Seconds55 extends CallDelay("55")
    case object Seconds56 extends CallDelay("56")
    case object Seconds57 extends CallDelay("57")
    case object Seconds58 extends CallDelay("58")
    case object Seconds59 extends CallDelay("59")
    case object Seconds60 extends CallDelay("60")
  }

  sealed trait OutgoingCallerIdCreateRequest {
    def accountSid: TwilioAccount.Sid
    def phoneNumber: PhoneNumberE164
    def friendlyName: Option[OutgoingCallerId.FriendlyName]
    def callDelay: Option[CallDelay]
    def extension: Option[DtmfString.OnlyDtmfDigits]
    def statusCallback: Option[CallbackUrl.OutgoingCallerIdVerificationUrl]
    def statusCallbackMethod: Option[HttpMethod]
  }

  private final case class OutgoingCallerIdCreateRequestImpl(
      accountSid: TwilioAccount.Sid,
      phoneNumber: PhoneNumberE164,
      friendlyName: Option[OutgoingCallerId.FriendlyName],
      callDelay: Option[CallDelay],
      extension: Option[DtmfString.OnlyDtmfDigits],
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
        callDelay: Option[CallDelay],
        extension: Option[DtmfString.OnlyDtmfDigits],
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
          callDelay: CallDelay
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
          extension: DtmfString.OnlyDtmfDigits
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

    final case class ValidationCodeFormatException(dtmfStringExceptionMessage: String)
        extends RuntimeException(
          s"Validation failed parsing do to DtmfString exception: $dtmfStringExceptionMessage"
        )
        with OutgoingCallerIdCreateException
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
