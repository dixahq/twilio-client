package com.dixa.twilio.client.impl.messaging

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.impl.messaging.SmsSendRequestExecutorImpl.{
  parseDate,
  parsePrice,
  parsePriceUnit,
  unexpectedStatusCodeResult
}
import com.dixa.twilio.client.impl.{ApiSubDomain, DefaultApiErrorEntityJsonRep, HttpEntityString}
import com.dixa.twilio.client.messaging.SmsSendRequestExecutor
import com.dixa.twilio.client.messaging.SmsSendRequestExecutor.{
  Response,
  SmsSendException,
  SmsSendRequest
}
import com.dixa.twilio.client.model.Iso4127CountryCode
import com.dixa.twilio.client.model.iam.TwilioAccount
import com.dixa.twilio.client.model.messaging._
import com.dixa.twilio.client.model.phonenumber.PhoneNumberE164
import io.circe.generic.auto._
import org.scalactic.TypeCheckedTripleEquals._

import java.time.Instant
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}

private[impl] final class SmsSendRequestExecutorImpl()(
    implicit http: HttpExt,
    materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends SmsSendRequestExecutor {

  override def run(
      connSettings: TwilioConnectionSettings,
      req: SmsSendRequest
  ): Future[Either[SmsSendException, Response]] = {

    // postParam is a send-able sms - actually in plural postParams
    // all send-able sms params should be in SmsSendRequestExecutor.SmsSendRequest
    // potentially might need to be FormData(Map(...)).toEntity cuz in the past we had troubles sending sms when having HttpEntity(ContentTypes.`application/x-www-form-urlencoded`
    val postParam =
      s"From=${req.from.asString}&To=${req.to.asString}&Body=${req.body.toString}&StatusCallback=${req.statusCallback.toString}"

    val httpReq = TwilioPath(
      ApiSubDomain.Api,
      HttpMethods.POST,
      s"/2010-04-01/Accounts/${req.accountSid}/Messages.json"
    ).createHttpRequest(connSettings)
      .withEntity(HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, postParam))

    http.singleRequest(httpReq).flatMap { httpResp =>
      httpResp.status match {
        case StatusCodes.Created =>
          buildSuccessResponse(req, httpResp.entity, connSettings.timeouts)
        case StatusCodes.BadRequest =>
          buildResultForBadRequestResponse(httpResp.entity, connSettings.timeouts)
        case other => Future.successful(unexpectedStatusCodeResult(req, other))
      }
    }

  }.recover { case e: Exception =>
    Left(new SmsSendException.UnspecifiedError(e))
  }

  private def buildSuccessResponse(
      req: SmsSendRequest,
      entity: ResponseEntity,
      timeouts: TwilioConnectionSettings.Timeouts
  ): Future[Either[SmsSendException, Response]] = {
    entity.toStrict(timeouts.requestEntityTimeout).map { entity =>
      val entityString = HttpEntityString(entity.data.utf8String)
      val decoded      = entityString.parseUnsafe[MessageSendRespJsonRep]()
      MessageDirection.values.find(_.twilioApiName === decoded.direction) match {
        case None =>
          Left(
            new SmsSendException.UnspecifiedError(
              s"Could not parse MessageDirection, ${decoded.direction} is not part of possible values"
            )
          )
        case Some(direction) =>
          req.from.asString === decoded.from match {
            case false =>
              Left(
                new SmsSendException.UnspecifiedError(
                  s"Could not parse MessageSender, ${req.from.asString} is not the same as ${decoded.from}"
                )
              )
            case true =>
              MessageStatus.values.find(_.twilioApiName === decoded.status) match {
                case None =>
                  Left(
                    new SmsSendException.UnspecifiedError(
                      s"Could not parse MessageStatus, ${decoded.status} is not part of possible values"
                    )
                  )
                case Some(status) =>
                  Right(
                    Response(
                      accountSid = TwilioAccount.Sid(decoded.account_sid),
                      body = MessageBody(decoded.body),
                      dateCreated = parseDate(decoded.date_created),
                      dateSent = parseDate(decoded.date_sent),
                      dateUpdated = parseDate(decoded.date_updated),
                      direction = direction,
                      from = MessageSender.E164(PhoneNumberE164(decoded.from)),
                      messagingServiceSid =
                        TwilioMessagingService.Sid(decoded.messaging_service_sid),
                      numMedia = decoded.num_media.toInt,
                      numSegments = MessageNumSegments(decoded.num_segments),
                      price = parsePrice(decoded.price),
                      priceUnit = parsePriceUnit(decoded.price_unit),
                      sid = MessageSid(decoded.sid),
                      status = status,
                      to = PhoneNumberE164(decoded.to)
                    )
                  )
              }
          }
      }
    }
  }

  private def buildResultForBadRequestResponse(
      entity: ResponseEntity,
      timeouts: TwilioConnectionSettings.Timeouts
  ): Future[Left[SmsSendException, Nothing]] = {
    entity.toStrict(timeouts.requestEntityTimeout).map { entity =>
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
            new SmsSendException.UnspecifiedError(
              s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                s"$other represents. Full error entity from Twilio: $entityString"
            )
          )
      }
    }
  }
}

private object SmsSendRequestExecutorImpl {
  private def unexpectedStatusCodeResult(req: SmsSendRequest, status: StatusCode) = Left(
    SmsSendException.UnspecifiedError(
      Some(s"Could not create: $req, due to getting status code $status from Twilio"),
      None
    )
  )

  private def parseDate(date: String): Instant =
    Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(date))

  private def parsePrice(price: String): Option[BigDecimal] =
    if (price.isBlank) None else Some(BigDecimal(price))

  private def parsePriceUnit(priceUnit: String): Option[Iso4127CountryCode] =
    if (priceUnit.isBlank) None else Some(Iso4127CountryCode(priceUnit))
}
