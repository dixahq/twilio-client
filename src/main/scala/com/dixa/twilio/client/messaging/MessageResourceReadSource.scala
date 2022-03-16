package com.dixa.twilio.client.messaging

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.client.messaging.MessageResourceReadSource.MessageResourceException
import com.dixa.twilio.client.messaging.TwilioClientMessaging.MessageResourceReadRequest
import com.dixa.twilio.model.messaging._

import scala.concurrent.Future

trait MessageResourceReadSource {

  def source(): Source[Either[MessageResourceException, MessageResource], NotUsed]
  def semisafeSource(): Future[Either[MessageResourceException, Source[MessageResource, NotUsed]]]
  def unsafeSource(): Source[MessageResource, NotUsed]

  def apply(
      connSettings: TwilioConnectionSettings,
      req: MessageResourceReadRequest
  )(
      implicit httpExt: HttpExt,
      materializer: Materializer
  ): Source[Either[MessageResourceException, MessageResource], NotUsed]

}

object MessageResourceReadSource {
  sealed trait MessageResourceException extends RuntimeException
  object MessageResourceException {
    final case class Api(cause: ApiException)
        extends RuntimeException(cause)
        with MessageResourceException
    final case class ToNumberNotValid()
        extends IllegalStateException(
          "Invalid 'To' Phone Number. More info: https://www.twilio.com/docs/api/errors/21211"
        )
        with MessageResourceException
    final case class FromNumberNotValid()
        extends IllegalStateException(
          "Invalid From Number. More info: https://www.twilio.com/docs/api/errors/21212"
        )
        with MessageResourceException
    final case class NotMessageCapableNumber()
        extends IllegalStateException(
          "Attempt to use a 'From' number which is not capable of sending SMS messages. More info: https://www.twilio.com/docs/api/errors/21606"
        )
        with MessageResourceException
    final case class MessageBodyCharLimitExceeded()
        extends IllegalStateException(
          "Concatenated message body exceeds the maximum 1600 character limit. More info: https://www.twilio.com/docs/api/errors/21617"
        )
        with MessageResourceException
    final case class Unspecified(msg: Option[String], cause: Option[Throwable])
        extends RuntimeException(
          msg.getOrElse(
            "Unspecified error happened trying to fetch sms resources"
          ),
          cause.orNull
        )
        with MessageResourceException {
      def this(msg: String) = this(Some(msg), None)
      def this(cause: Throwable) = this(Option(cause.getMessage), Some(cause))
    }
  }

}
