package com.dixa.twilio.client.messaging

import com.dixa.twilio.client.{ApiException, SingleRequestExecutor}
import com.dixa.twilio.client.messaging.SmsSendRequestExecutor.Response
import com.dixa.twilio.client.model.Iso4127CountryCode
import com.dixa.twilio.client.model.iam.TwilioAccount
import com.dixa.twilio.client.model.iam.TwilioAccount.Sid
import com.dixa.twilio.client.model.messaging._
import com.dixa.twilio.client.model.phonenumber.PhoneNumberE164

import java.time.Instant

trait SmsSendRequestExecutor
    extends SingleRequestExecutor[
      SmsSendRequestExecutor.SmsSendRequest,
      SmsSendRequestExecutor.SmsSendException,
      Response
    ]

object SmsSendRequestExecutor {

  final case class SmsSendRequest(
      accountSid: Sid,
      from: MessageSender,
      to: PhoneNumberE164,
      body: MessageBody,
      statusCallback: StatusCallback
  )

  // Most common Bad Request errors: https://support.twilio.com/hc/en-us/articles/223181868-Troubleshooting-Undelivered-Twilio-SMS-Messages
  sealed trait SmsSendException extends RuntimeException
  object SmsSendException {
    final case class Api(cause: ApiException) extends RuntimeException(cause) with SmsSendException
    final case class PermissionDenied()
        extends IllegalStateException(
          "Account SID and/or AuthToken may be incorrect. More info: https://www.twilio.com/docs/api/errors/20003"
        )
        with SmsSendException
    final case class NotMessageCapableNumber()
        extends IllegalStateException(
          "Attempt to use a 'From' number which is not capable of sending SMS messages. More info: https://www.twilio.com/docs/api/errors/21606"
        )
        with SmsSendException
    final case class ToNumberNotReachable()
        extends IllegalStateException(
          "Destination carrier is not supported or 'To' number is not properly formatted. More info: https://www.twilio.com/docs/api/errors/21612"
        )
        with SmsSendException
    final case class ToNumberNotValid()
        extends IllegalStateException(
          "'To' number is not a valid mobile number. More info: https://www.twilio.com/docs/api/errors/21614"
        )
        with SmsSendException
    final case class MessageBodyCharLimitExceeded()
        extends IllegalStateException(
          "Concatenated message body exceeds the maximum 1600 character limit. More info: https://www.twilio.com/docs/api/errors/21617"
        )
        with SmsSendException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to send an sms"
          ),
          cause.orNull
        )
        with SmsSendException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
    }
  }


  // TODO add comments here Not included: error_code, error_message, from, to, uri, subresourceUris
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
