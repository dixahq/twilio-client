package com.dixa.twilio.client.phonenumber

import akka.Done
import com.dixa.twilio.client.phonenumber.OutgoingCallerIdDeleteRequestExecutor.OutgoingCallerIdDeleteException
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.phonenumber.OutgoingCallerId

trait OutgoingCallerIdDeleteRequestExecutor
    extends SingleRequestExecutor[
      OutgoingCallerIdDeleteRequestExecutor.OutgoingCallerIdDeleteRequest,
      OutgoingCallerIdDeleteRequestExecutor.OutgoingCallerIdDeleteException,
      Done
    ] {
  override protected final type ApiExceptionWrapper = OutgoingCallerIdDeleteException.Api

  override protected final type UnspecifiedException = OutgoingCallerIdDeleteException.Unspecified

}

object OutgoingCallerIdDeleteRequestExecutor {
  final case class OutgoingCallerIdDeleteRequest(
      accountSid: TwilioAccount.Sid,
      outGoingCallerId: OutgoingCallerId.Sid
  )

  sealed trait OutgoingCallerIdDeleteException extends RuntimeException
  object OutgoingCallerIdDeleteException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with OutgoingCallerIdDeleteException
    final case class Unspecified(msg: Option[String], cause: Option[Exception])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to read incoming numbers"
          ),
          cause.orNull
        )
        with OutgoingCallerIdDeleteException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Exception) = this(None, Some(cause))
    }
  }
}
