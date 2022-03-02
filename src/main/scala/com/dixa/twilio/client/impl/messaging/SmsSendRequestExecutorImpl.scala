package com.dixa.twilio.client.impl.messaging

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.impl.messaging.SmsSendRequestExecutorImpl.{parseDate, parseMessagingServiceSid, parsePrice, parsePriceUnit}
import com.dixa.twilio.client.impl.{ApiSubDomain, DefaultApiErrorEntityJsonRep, HttpEntityString}
import com.dixa.twilio.client.messaging.SmsSendRequestExecutor
import com.dixa.twilio.client.messaging.SmsSendRequestExecutor.{Response, SmsSendException, SmsSendRequest}
import com.dixa.twilio.client.model.Iso4127CountryCode
import com.dixa.twilio.client.model.iam.TwilioAccount
import com.dixa.twilio.client.model.messaging._
import com.dixa.twilio.client.model.phonenumber.PhoneNumberE164
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import io.circe.generic.auto._
import org.scalactic.TypeCheckedTripleEquals._

import java.time.Instant
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext

private[impl] final class SmsSendRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends SmsSendRequestExecutor {

  override protected type ApiExceptionWrapper = SmsSendException.Api

  override protected type UnspecifiedException = SmsSendException.Unspecified

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: SmsSendRequest
  ): HttpRequest = {
    val reqEntity = FormData(
      Map(
        "From"           -> req.from.asString,
        "To"             -> req.to.asString,
        "Body"           -> req.body.toString,
        "StatusCallback" -> req.statusCallback.toString
      )
    ).toEntity

    TwilioPath(
      ApiSubDomain.Api,
      HttpMethods.POST,
      s"/2010-04-01/Accounts/${req.accountSid}/Messages.json"
    ).createHttpRequest(connSettings).withEntity(reqEntity)
  }

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    SmsSendException.Api.apply(apiException)

  /** Create the request specific Unspecified exception. */
  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Exception]
  ): UnspecifiedException = SmsSendException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      req: SmsSendRequest,
      httpReq: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntity.Strict
  ): Either[SmsSendException, Response] = httpResponse.status match {
    case StatusCodes.Created =>
      buildSuccessResponse(req, entity)
    case StatusCodes.BadRequest =>
      buildResultForBadRequestResponse(entity)
    case _ => buildResultForUnhandledResponse(req, httpReq, httpResponse, entity)
  }

  private def buildSuccessResponse(
      req: SmsSendRequest,
      entity: HttpEntity.Strict
  ): Either[SmsSendException, Response] = {
    val entityString = HttpEntityString(entity.data.utf8String)
    val decoded      = entityString.parseUnsafe[MessageSendRespJsonRep]()
    MessageDirection.values.find(_.twilioApiName === decoded.direction) match {
      case None =>
        Left(
          new SmsSendException.Unspecified(
            s"Could not parse MessageDirection, ${decoded.direction} is not part of possible values"
          )
        )
      case Some(direction) =>
        req.from.asString === decoded.from match {
          case false =>
            Left(
              new SmsSendException.Unspecified(
                s"Could not parse MessageSender, ${req.from.asString} is not the same as ${decoded.from}"
              )
            )
          case true =>
            MessageStatus.values.find(_.twilioApiName === decoded.status) match {
              case None =>
                Left(
                  new SmsSendException.Unspecified(
                    s"Could not parse MessageStatus, ${decoded.status} is not part of possible values"
                  )
                )
              case Some(status) =>
                Right(
                  Response(
                    accountSid = TwilioAccount.Sid(decoded.account_sid),
                    body = MessageBody(decoded.body),
                    dateCreated = decoded.date_created.flatMap(parseDate),
                    dateSent = decoded.date_sent.flatMap(parseDate),
                    dateUpdated = decoded.date_updated.flatMap(parseDate),
                    direction = direction,
                    from = MessageSender.E164(PhoneNumberE164(decoded.from)),
                    messagingServiceSid =
                      decoded.messaging_service_sid.flatMap(parseMessagingServiceSid),
                    numMedia = decoded.num_media.toInt,
                    numSegments = MessageNumSegments(decoded.num_segments),
                    price = decoded.price.flatMap(parsePrice),
                    priceUnit = decoded.price_unit.flatMap(parsePriceUnit),
                    sid = MessageSid(decoded.sid),
                    status = status,
                    to = PhoneNumberE164(decoded.to)
                  )
                )
            }
        }
    }
  }

  private def buildResultForBadRequestResponse(
      entity: HttpEntity.Strict
  ): Left[SmsSendException, Nothing] = {
    val entityString = HttpEntityString(entity.data.utf8String)
    val decoded      = entityString.parseUnsafe[DefaultApiErrorEntityJsonRep]()
    decoded.code match {
      case 20003L => Left(SmsSendException.PermissionDenied())
      case 21606L => Left(SmsSendException.NotMessageCapableNumber())
      case 21612L => Left(SmsSendException.ToNumberNotReachable())
      case 21614L => Left(SmsSendException.ToNumberNotValid())
      case 21617L => Left(SmsSendException.MessageBodyCharLimitExceeded())
      case other =>
        Left(
          new SmsSendException.Unspecified(
            s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
              s"$other represents. Full error entity from Twilio: $entityString"
          )
        )
    }
  }
}

private object SmsSendRequestExecutorImpl {
  private def parseDate(date: String): Option[Instant] = date match {
    case null => None
    case _    => Some(Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(date)))
  }

  private def parsePrice(price: String): Option[BigDecimal] =
    if (price.isBlank) None else Some(BigDecimal(price))

  private def parsePriceUnit(priceUnit: String): Option[Iso4127CountryCode] =
    if (priceUnit.isBlank) None else Some(Iso4127CountryCode(priceUnit))

  private def parseMessagingServiceSid(
      messagingServiceSid: String
  ): Option[TwilioMessagingService.Sid] = messagingServiceSid match {
    case null => None
    case _    => Some(TwilioMessagingService.Sid(messagingServiceSid))
  }
}
