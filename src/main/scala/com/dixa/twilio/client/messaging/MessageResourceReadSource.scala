package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.{ApiException, MultipleResponseSource}
import com.dixa.twilio.client.messaging.MessageResourceReadSource.MessageResourceReadException
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging._
import com.dixa.twilio.model.phonenumber.PhoneNumberE164


import java.time.Instant

trait MessageResourceReadSource
    extends MultipleResponseSource[
      MessageResourceReadSource.MessageResourceReadRequest,
      MessageResourceReadSource.MessageResourceReadException,
      MessageResource
    ] {

  override protected type ApiExceptionWrapper = MessageResourceReadException.Api

  override protected type UnspecifiedException = MessageResourceReadException.Unspecified
}

object MessageResourceReadSource {

  final case class MessageResourceReadRequest(
      accountSid: TwilioAccount.Sid,
      filter: MessageResourcesReadRequestFilter = MessageResourcesReadRequestFilter()
  )

  final case class MessageResourcesReadRequestFilter(
      to: Option[PhoneNumberE164] = None,
      from: Option[PhoneNumberE164] = None,
      dateSentAfter: Option[Instant] = None,
      dateSentBefore: Option[Instant] = None,
      pageSize: Int = 20
  )

  // TODO: msf - Figure out is Exceptions are valid for this request
  sealed trait MessageResourceReadException extends RuntimeException
  object MessageResourceReadException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with MessageResourceReadException
    final case class ToNumberNotValid()
        extends IllegalStateException(
          "Invalid 'To' Phone Number. More info: https://www.twilio.com/docs/api/errors/21211"
        )
        with MessageResourceReadException
    final case class FromNumberNotValid()
        extends IllegalStateException(
          "Invalid From Number. More info: https://www.twilio.com/docs/api/errors/21212"
        )
        with MessageResourceReadException
    final case class NotMessageCapableNumber()
        extends IllegalStateException(
          "Attempt to use a 'From' number which is not capable of sending SMS messages. More info: https://www.twilio.com/docs/api/errors/21606"
        )
        with MessageResourceReadException
    final case class MessageBodyCharLimitExceeded()
        extends IllegalStateException(
          "Concatenated message body exceeds the maximum 1600 character limit. More info: https://www.twilio.com/docs/api/errors/21617"
        )
        with MessageResourceReadException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to fetch sms resources"
          ),
          cause.orNull
        )
        with MessageResourceReadException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
    }
  }

}
