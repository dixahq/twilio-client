package com.dixa.twilio.client.impl.messaging

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.impl.messaging.MessageSendRequestExecutorImpl.{
  parseDate,
  parseMessagingServiceSid
}
import com.dixa.twilio.client.impl.{
  ApiSubDomain,
  DefaultApiErrorEntityJsonRep,
  Formatter,
  HttpEntityString
}
import com.dixa.twilio.client.messaging.MessageSendRequestExecutor
import com.dixa.twilio.client.messaging.MessageSendRequestExecutor.{
  MessageSendException,
  MessageSendRequest
}
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import com.dixa.twilio.model.Iso4127CountryCode
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.messaging._
import com.dixa.twilio.model.phonenumber.PhoneNumberE164

import java.time.Instant
import scala.concurrent.ExecutionContext

private[impl] final class MessageSendRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends MessageSendRequestExecutor {

  override protected def subDomain: ApiSubDomain = ApiSubDomain.Api

  override protected def method: HttpMethod = HttpMethods.POST

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: MessageSendRequest
  ): Either[MessageSendException, HttpRequest] = {
    val reqEntity = FormData(
      Map(
        "From"           -> req.from.asString,
        "To"             -> req.to.asString,
        "Body"           -> req.body.toString,
        "StatusCallback" -> req.statusCallback.toString
      )
    ).toEntity

    createHttpRequestFor(s"/2010-04-01/Accounts/${req.accountSid}/Messages.json", connSettings)
      .map(_.withEntity(reqEntity))
  }

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    MessageSendException.Api.apply(apiException)

  /** Create the request specific Unspecified exception. */
  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Throwable]
  ): UnspecifiedException = MessageSendException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      req: MessageSendRequest,
      httpReq: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntityString
  ): Either[MessageSendException, MessageResource] = httpResponse.status match {
    case StatusCodes.Created =>
      buildSuccessResponse(req, entity)
    case StatusCodes.BadRequest =>
      buildResultForBadRequestResponse(entity)
    case _ => buildResultForUnhandledResponse(req, httpReq, httpResponse, entity)
  }

  private def buildSuccessResponse(
      req: MessageSendRequest,
      entity: HttpEntityString
  ): Either[MessageSendException, MessageResource] = {
    parseEntityAs[MessageJsonRep](entity).flatMap { decoded =>
      MessageDirection.values.find(_.twilioString == decoded.direction) match {
        case None =>
          Left(
            new MessageSendException.Unspecified(
              s"Could not parse MessageDirection, ${decoded.direction} is not part of possible values"
            )
          )
        case Some(direction) =>
          req.from.asString == decoded.from match {
            case false =>
              Left(
                new MessageSendException.Unspecified(
                  s"Could not parse MessageSender, ${req.from.asString} is not the same as ${decoded.from}"
                )
              )
            case true =>
              MessageStatus.values.find(_.twilioString == decoded.status) match {
                case None =>
                  Left(
                    new MessageSendException.Unspecified(
                      s"Could not parse MessageStatus, ${decoded.status} is not part of possible values"
                    )
                  )
                case Some(status) =>
                  val price = (decoded.price, decoded.price_unit) match {
                    case (Some(amount), Some(currency)) =>
                      Some(MessagePrice(BigDecimal(amount), Iso4127CountryCode(currency)))
                    case _ => None
                  }
                  val messageError = (decoded.error_code, decoded.error_message) match {
                    case (Some(code), Some(message)) => Some(MessageError(message, code))
                    case _                           => None
                  }
                  Right(
                    MessageResource(
                      accountSid = TwilioAccount.Sid.unsafe(decoded.account_sid),
                      body = MessageBody(decoded.body),
                      dateCreated = decoded.date_created.flatMap(parseDate),
                      dateSent = decoded.date_sent.flatMap(parseDate),
                      dateUpdated = decoded.date_updated.flatMap(parseDate),
                      direction = direction,
                      from = MessageSender.E164(PhoneNumberE164.unsafe(decoded.from)),
                      messagingServiceSid =
                        decoded.messaging_service_sid.flatMap(parseMessagingServiceSid),
                      numMedia = decoded.num_media.toInt,
                      numSegments = MessageNumSegments(decoded.num_segments.toInt),
                      price = price,
                      sid = Message.Sid.unsafe(decoded.sid),
                      status = status,
                      to = PhoneNumberE164.unsafe(decoded.to),
                      error = messageError
                    )
                  )
              }
          }
      }
    }
  }

  private def buildResultForBadRequestResponse(
      entity: HttpEntityString
  ) = {
    parseEntityAs[DefaultApiErrorEntityJsonRep](entity).flatMap { decoded =>
      decoded.code match {
        case 21211L => Left(MessageSendException.ToNumberNotValid())
        case 21212L => Left(MessageSendException.FromNumberNotValid())
        case 21606L => Left(MessageSendException.NotMessageCapableNumber())
        case 21617L => Left(MessageSendException.MessageBodyCharLimitExceeded())
        case other =>
          Left(
            new MessageSendException.Unspecified(
              s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                s"$other represents. Full error entity from Twilio: $entity"
            )
          )
      }
    }
  }
}

private object MessageSendRequestExecutorImpl {
  private def parseDate(date: String): Option[Instant] = date match {
    case null => None
    case _    => Some(Instant.from(Formatter.dateTime.parse(date)))
  }

  private def parseMessagingServiceSid(
      messagingServiceSid: String
  ): Option[TwilioMessagingService.Sid] =
    TwilioMessagingService.Sid.safe(messagingServiceSid).toOption
}
