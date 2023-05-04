package com.dixa.twilio.client.phonenumber

import akka.Done
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber
import com.dixa.twilio.model.phonenumber.TwilioPhoneNumber

/** Delete an incoming phone number from twilio account.
  *
  * An IncomingPhoneNumber instance resource represents a Twilio phone number provisioned from
  * Twilio, ported or hosted to Twilio.
  *
  * @see
  *   https://www.twilio.com/docs/phone-numbers/api/incomingphonenumber-resource#delete-an-incomingphonenumber-resource
  */
trait IncomingPhoneNumberDeleteRequestExecutor
    extends SingleRequestExecutor[
      IncomingPhoneNumberDeleteRequestExecutor.IncomingPhoneNumberDeleteRequest,
      IncomingPhoneNumberDeleteRequestExecutor.IncomingPhoneNumberDeleteException,
      Done
    ] {

  import IncomingPhoneNumberDeleteRequestExecutor._

  override protected final type ApiExceptionWrapper = IncomingPhoneNumberDeleteException.Api

  override protected final type UnspecifiedException =
    IncomingPhoneNumberDeleteException.Unspecified

}

object IncomingPhoneNumberDeleteRequestExecutor {

  /** Request representation of deleting an incoming phone number.
    *
    * This request does not have any complex requirements, and hence no build function is needed.
    * You should just create a plain instance of this, via the apply method in the companion object.
    */
  final case class IncomingPhoneNumberDeleteRequest(
      accountSid: TwilioAccount.Sid,
      phoneNumberId: TwilioPhoneNumber.Sid
  )

  sealed trait IncomingPhoneNumberDeleteException extends RuntimeException
  object IncomingPhoneNumberDeleteException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with IncomingPhoneNumberDeleteException

    final case class PhoneNumberNotFound(
        accountSid: TwilioAccount.Sid,
        phoneNumberSid: phonenumber.TwilioPhoneNumber.Sid
    ) extends RuntimeException(
          s"Incoming phone number with sid $phoneNumberSid was not found in account: $accountSid"
        )
        with IncomingPhoneNumberDeleteException

    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to delete incoming numbers"
          ),
          cause.orNull
        )
        with IncomingPhoneNumberDeleteException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Exception) = this(None, Some(cause))
    }
  }
}
