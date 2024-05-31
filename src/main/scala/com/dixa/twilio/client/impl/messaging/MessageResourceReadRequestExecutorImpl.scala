package com.dixa.twilio.client.impl.messaging

import com.dixa.twilio.client.impl.{ApiSubDomain, Formatter, HttpEntityString}
import com.dixa.twilio.client.messaging.MessageResourceReadRequestExecutor
import com.dixa.twilio.client.messaging.MessageResourceReadRequestExecutor.MessageResourceReadException
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.Iso4127CountryCode
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging._
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model.Uri.Query
import org.apache.pekko.http.scaladsl.model.{HttpMethod, HttpMethods, HttpRequest, HttpResponse}
import org.apache.pekko.stream.Materializer

import java.time.Instant
import scala.concurrent.ExecutionContext

private[impl] final class MessageResourceReadRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends MessageResourceReadRequestExecutor {

  import MessageResourceReadRequestExecutorImpl._

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.GET

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: MessageResourceReadRequestExecutor.MessageResourceReadRequest
  ): Either[MessageResourceReadException, HttpRequest] = {
    val query = {
      val dateSentAfterParameter: Option[(String, String)] = req.filter.dateSentAfter.map { date =>
        "DateSent>" -> date.toString
      }
      val dateSentBeforeParameter: Option[(String, String)] = req.filter.dateSentBefore.map {
        date =>
          "DateSent<" -> date.toString
      }
      val toParameter: Option[(String, String)] = req.filter.to.map { recipient =>
        "To" -> recipient.toMessageRecipient
      }
      val fromParameter: Option[(String, String)] = req.filter.from.map { sender =>
        "From" -> sender.asString
      }
      Query(
        Map("PageSize" -> req.filter.pageSize.toString) ++
          List(
            dateSentAfterParameter,
            dateSentBeforeParameter,
            toParameter,
            fromParameter
          ).flatten.toMap
      )
    }

    createHttpRequestFor(
      // the `:` character present in the time instances in dateSent parameter should be URL encoded
      // org.apache.pekko.http.scaladsl.model.Uri.Query doesn't URL encode the `:` character
      s"/2010-04-01/Accounts/${req.accountSid}/Messages.json?${query.toString().replace(":", "%3A")}",
      connSettings
    )
  }

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    MessageResourceReadException.Api.apply(apiException)

  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UnspecifiedException = MessageResourceReadException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      connectionSettings: TwilioConnectionSettings,
      request: MessageResourceReadRequestExecutor.MessageResourceReadRequest,
      httpRequest: HttpRequest,
      httpResponse: HttpResponse,
      responseEntity: HttpEntityString
  ): List[
    Either[MessageResourceReadRequestExecutor.MessageResourceReadException, MessageResource]
  ] = {
    responseEntity
      .parse[MessageListJsonRep]() match {
      case Left(ex) =>
        List(
          Left(
            MessageResourceReadException.Unspecified(Some(ex.cause.getMessage), Some(ex.cause))
          )
        )
      case Right(listJsonRep) =>
        listJsonRep.messages.map { toModel }
    }
  }

  private def toModel(
      jsonRep: MessageJsonRep
  ): Either[MessageResourceReadException, MessageResource] = {
    val accountSid = TwilioAccount.Sid.unsafe(jsonRep.account_sid)
    val messageSid = Message.Sid.unsafe(jsonRep.sid)
    for {
      messageDirection <- MessageDirection.fromTwilioStringEither(jsonRep.direction).left.map {
        err =>
          new MessageResourceReadException.Unspecified(err.msg)
      }
      messageStatus <- MessageStatus.fromTwilioStringEither(jsonRep.status).left.map { err =>
        new MessageResourceReadException.Unspecified(err.msg)
      }
      messageResource = MessageResource(
        sid = messageSid,
        dateCreated = jsonRep.date_created.flatMap(parseDate),
        dateUpdated = jsonRep.date_updated.flatMap(parseDate),
        dateSent = jsonRep.date_sent.flatMap(parseDate),
        accountSid = accountSid,
        to = MessageRecipient.fromStringUnsafe(jsonRep.to),
        from = MessageSender.fromStringUnsafe(jsonRep.from),
        messagingServiceSid = jsonRep.messaging_service_sid.flatMap(parseMessagingServiceSid),
        body = MessageBody(jsonRep.body),
        status = messageStatus,
        numSegments = MessageNumSegments(jsonRep.num_segments.toInt),
        numMedia = jsonRep.num_media.toInt,
        direction = messageDirection,
        price = parsePrice(jsonRep.price, jsonRep.price_unit),
        error = parseError(jsonRep.error_code, jsonRep.error_message)
      )
    } yield messageResource
  }
}

private object MessageResourceReadRequestExecutorImpl {
  private def parseDate(date: String): Option[Instant] = {
    date match {
      case null => None
      case _    => Some(Instant.from(Formatter.dateTime.parse(date)))
    }
  }

  private def parseError(code: Option[Int], message: Option[String]): Option[MessageError] = {
    (code, message) match {
      case (Some(c), Some(m)) => Some(MessageError(m, c))
      case _                  => None
    }
  }

  private def parsePrice(price: Option[String], unit: Option[String]): Option[MessagePrice] = {
    (price, unit) match {
      case (Some(amount), Some(currency)) =>
        Some(MessagePrice(BigDecimal(amount), Iso4127CountryCode(currency)))
      case _ => None
    }
  }

  private def parseMessagingServiceSid(
      messagingServiceSid: String
  ): Option[TwilioMessagingService.Sid] =
    TwilioMessagingService.Sid.safe(messagingServiceSid).toOption
}
