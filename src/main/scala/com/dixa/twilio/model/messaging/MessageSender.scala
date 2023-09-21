package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.messaging.MessageSender.Alphanumeric.AlphanumericException.AlphanumericInvalidException
import com.dixa.twilio.model.messaging.MessageSender.MessageSenderException.MessageSenderInvalidException
import com.dixa.twilio.model.phonenumber.PhoneNumberE164

import scala.annotation.nowarn

// There is also WirelessSIM, not included
sealed abstract class MessageSender {
  def asString: String
}

object MessageSender {

  trait MessageSenderException extends RuntimeException

  object MessageSenderException {
    case class MessageSenderInvalidException(s: String)
        extends RuntimeException(s"$s is not a valid phone number or alphanumeric sender id")
        with MessageSenderException
  }

  def fromString(s: String): Either[MessageSenderException, MessageSender] = {
    PhoneNumberE164(s) match {
      case Some(pn) => Right(E164(pn))
      case None =>
        Alphanumeric.fromString(s).toOption match {
          case Some(sender) => Right(sender)
          case None         => Left(MessageSenderInvalidException(s))
        }
    }
  }

  def fromStringUnsafe(s: String): MessageSender = {
    fromString(s).toTry.get
  }

  final case class E164(phoneNumber: PhoneNumberE164) extends MessageSender {
    override def asString: String = phoneNumber.asString
  }

  final case class Alphanumeric private (override val asString: String) extends MessageSender

  object Alphanumeric {

    // override apply method as private to ensure clients cannot create invalid instances
    @nowarn(value = "cat=unused")
    private def apply(asString: String): Alphanumeric = new Alphanumeric(asString)

    trait AlphanumericException extends RuntimeException
    object AlphanumericException {
      case class AlphanumericInvalidException(s: String)
          extends RuntimeException(s"$s is not a valid alphanumeric sender id")
          with AlphanumericException
    }

    /** Alphanumeric sender ID restrictions (did not include some special symbols that Twilio claims
      * to support): 1.Alphanumeric Sender IDs may be up to 11 characters long. 2. Accepted
      * characters include both upper-case and lower-case Ascii letters, digits 0 through 9, and the
      * space character. 3. Alphanumeric Sender IDs may not be only numerals. They must include at
      * least one letter.
      *
      * @see
      *   https://www.twilio.com/docs/glossary/what-alphanumeric-sender-id
      * @see
      *   https://gatewayapi.com/help-center/sender-id/
      */
    def isValid(s: String): Boolean = {
      val alphanumericRegex = "^(?=.*[a-zA-Z])[a-zA-Z0-9 ]{1,11}$"
      if (!s.matches(alphanumericRegex) || s.trim.isEmpty)
        false
      else true
    }

    def fromString(s: String): Either[AlphanumericException, Alphanumeric] = {
      if (isValid(s)) Right(new Alphanumeric(s)) else Left(AlphanumericInvalidException(s))
    }

    def fromStringUnsafe(s: String): Alphanumeric = {
      if (isValid(s)) new Alphanumeric(s) else throw AlphanumericInvalidException(s)
    }
  }
}
