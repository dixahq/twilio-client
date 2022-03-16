package com.dixa.twilio.client.impl.messaging

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.HttpMethods
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.impl.{ApiSubDomain, Formatter, HttpEntityString, TwilioPagingFlow}
import com.dixa.twilio.client.messaging.TwilioClientMessaging.MessageResourceReadRequest
import com.dixa.twilio.model.Iso4127CountryCode
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging.{
  MessageBody,
  MessageDirection,
  MessageError,
  MessageNumSegments,
  MessagePrice,
  MessageResource,
  MessageSender,
  MessageSid,
  MessageStatus,
  ServiceSid
}
import com.dixa.twilio.model.phonenumber.PhoneNumberE164
import io.circe.generic.auto._

import java.time.Instant

private[impl] object MessageResourceReadSourceImpl {

  def apply(
      connSettings: TwilioConnectionSettings,
      req: MessageResourceReadRequest
  )(
      implicit httpExt: HttpExt,
      materializer: Materializer
      // TODO: msf - define exception and find where to implement it
  ): Source[MessageResource, NotUsed] = {
    val query = req.filter.buildFilterQuery.toString
    TwilioPagingFlow
      .createPagingSrc(
        connSettings,
        TwilioPath(
          ApiSubDomain.Api,
          HttpMethods.GET,
          s"/2010-04-01/Accounts/${req.accountSid}/Messages.json?$query"
        )
      )
      .map(entityToMessageList)
      .mapConcat(identity)
  }

  private final case class TwilioMessagesOuterJsonRep(messages: Vector[MessageJsonRep])
  private def entityToMessageList(entity: HttpEntityString): Seq[MessageResource] = {
    val decoded = entity.parseUnsafe[TwilioMessagesOuterJsonRep]()
    decoded.messages.map { jsonRep =>
      val dateSent = jsonRep.date_sent.map { string =>
        Instant.from(Formatter.dateTime.parse(string))
      }
      val dateCreated = jsonRep.date_created.map { string =>
        Instant.from(Formatter.dateTime.parse(string))
      }
      val dateUpdated = jsonRep.date_updated.map { string =>
        Instant.from(Formatter.dateTime.parse(string))
      }
      val direction = MessageDirection
        .fromTwilioString(jsonRep.direction)
        .getOrElse(
          throw new RuntimeException(
            s"Could not parse MessageDirection, ${jsonRep.direction} is not part of possible values"
          )
        )
      val price = (jsonRep.price, jsonRep.price_unit) match {
        case (Some(amount), Some(currency)) =>
          Some(MessagePrice(BigDecimal(amount), Iso4127CountryCode(currency)))
        case _ => None
      }
      val messageError = (jsonRep.error_code, jsonRep.error_message) match {
        case (Some(code), Some(message)) => Some(MessageError(code, message))
        case _                           => None
      }
      val status = MessageStatus
        .fromTwilioString(jsonRep.status)
        .getOrElse(
          throw new RuntimeException(
            s"Could not parse MessageStatus, ${jsonRep.status} is not part of possible values"
          )
        )
      MessageResource(
        sid = MessageSid(jsonRep.sid),
        body = MessageBody(jsonRep.body),
        accountSid = TwilioAccount.Sid(jsonRep.account_sid),
        dateCreated = dateCreated,
        dateSent = dateSent,
        dateUpdated = dateUpdated,
        numSegments = MessageNumSegments(jsonRep.num_segments.toInt),
        direction = direction,
        from = MessageSender.Alphanumeric(jsonRep.from),
        to = PhoneNumberE164(jsonRep.to),
        price = price,
        error = messageError,
        numMedia = jsonRep.num_media.toInt,
        status = status,
        messagingServiceSid = jsonRep.messaging_service_sid.map { ServiceSid }
      )
    }
  }
}
