package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.messaging.MessageSendRequestExecutor.Response
import com.dixa.twilio.client.model.Iso4127CountryCode
import com.dixa.twilio.client.model.iam.TwilioAccount
import com.dixa.twilio.client.model.iam.TwilioAccount.Sid
import com.dixa.twilio.client.model.messaging._
import com.dixa.twilio.client.model.phonenumber.PhoneNumberE164
import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}

import java.time.Instant

trait MessageSendRequestExecutor
    extends SingleRequestExecutor[
      MessageSendRequestExecutor.MessageSendRequest,
      MessageSendRequestExecutor.MessageSendException,
      Response
    ]

object MessageSendRequestExecutor {

  final case class MessageSendRequest(
      accountSid: Sid,
      from: MessageSender,
      to: PhoneNumberE164,
      body: MessageBody,
      statusCallback: StatusCallback
  )

  // Most common Bad Request errors: https://support.twilio.com/hc/en-us/articles/223181868-Troubleshooting-Undelivered-Twilio-SMS-Messages
  sealed trait MessageSendException extends RuntimeException
  object MessageSendException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with MessageSendException
    final case class PermissionDenied()
        extends IllegalStateException(
          "Account SID and/or AuthToken may be incorrect. More info: https://www.twilio.com/docs/api/errors/20003"
        )
        with MessageSendException
    final case class NotMessageCapableNumber()
        extends IllegalStateException(
          "Attempt to use a 'From' number which is not capable of sending SMS messages. More info: https://www.twilio.com/docs/api/errors/21606"
        )
        with MessageSendException
    final case class ToNumberNotReachable()
        extends IllegalStateException(
          "Destination carrier is not supported or 'To' number is not properly formatted. More info: https://www.twilio.com/docs/api/errors/21612"
        )
        with MessageSendException
    final case class ToNumberNotValid()
        extends IllegalStateException(
          "'To' number is not a valid mobile number. More info: https://www.twilio.com/docs/api/errors/21614"
        )
        with MessageSendException
    final case class MessageBodyCharLimitExceeded()
        extends IllegalStateException(
          "Concatenated message body exceeds the maximum 1600 character limit. More info: https://www.twilio.com/docs/api/errors/21617"
        )
        with MessageSendException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to send an sms"
          ),
          cause.orNull
        )
        with MessageSendException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
    }
  }

  /** Items from the json response not included in this Response:<br> <ul> <li>error_code,
    * error_message - the message delivery errors are handled through status callbacks</li>
    * <li>uri</li> <li>subresourceUris</li> </ul>
    */
  final case class Response(
      accountSid: TwilioAccount.Sid,
      body: MessageBody,
      dateCreated: Option[Instant],
      dateSent: Option[Instant],
      dateUpdated: Option[Instant],
      direction: MessageDirection,
      from: MessageSender,
      messagingServiceSid: Option[TwilioMessagingService.Sid],
      numMedia: Int, // number of media files associated with the message
      numSegments: MessageNumSegments,
      price: Option[BigDecimal],
      priceUnit: Option[Iso4127CountryCode],
      sid: MessageSid,
      status: MessageStatus,
      to: PhoneNumberE164
  )
}
