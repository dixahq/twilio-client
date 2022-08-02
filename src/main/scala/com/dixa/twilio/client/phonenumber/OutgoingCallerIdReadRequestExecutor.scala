package com.dixa.twilio.client.phonenumber

import com.dixa.twilio.client.phonenumber.OutgoingCallerIdReadRequestExecutor.OutgoingCallerIdReadException
import com.dixa.twilio.client.{ApiException, MultipleResponseRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.{OutgoingCallerId, PhoneNumberE164}

trait OutgoingCallerIdReadRequestExecutor
    extends MultipleResponseRequestExecutor[
      OutgoingCallerIdReadRequestExecutor.OutgoingCallerIdReadRequest,
      OutgoingCallerIdReadRequestExecutor.OutgoingCallerIdReadException,
      OutgoingCallerId
    ] {
  override protected final type ApiExceptionWrapper = OutgoingCallerIdReadException.Api

  override protected final type UnspecifiedException = OutgoingCallerIdReadException.Unspecified

}

object OutgoingCallerIdReadRequestExecutor {
  final case class OutgoingCallerIdReadRequest(
      accountSid: TwilioAccount.Sid,
      filter: OutgoingCallerIdReadRequestFilter = OutgoingCallerIdReadRequestFilter()
  )

  final case class OutgoingCallerIdReadRequestFilter(
      phoneNumber: Option[PhoneNumberE164] = None,
      friendlyName: Option[OutgoingCallerId.FriendlyName] = None,
      pageSize: Int = 20
  )

  sealed trait OutgoingCallerIdReadException extends RuntimeException
  object OutgoingCallerIdReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with OutgoingCallerIdReadException
    final case class Unspecified(msg: Option[String], cause: Option[Exception])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to read incoming numbers"
          ),
          cause.orNull
        )
        with OutgoingCallerIdReadException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Exception) = this(None, Some(cause))
    }
  }
}
