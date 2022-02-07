package com.dixa.twilio.client.impl.messaging

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{
  ContentTypes,
  HttpEntity,
  HttpMethods,
  ResponseEntity,
  StatusCode,
  StatusCodes
}
import akka.stream.Materializer
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.impl.{ApiSubDomain, DefaultApiErrorEntityJsonRep, HttpEntityString}
import com.dixa.twilio.client.messaging.PhoneNumberCreateRequestExecutor
import com.dixa.twilio.client.messaging.PhoneNumberCreateRequestExecutor.{
  PhoneNumberCreateException,
  PhoneNumberCreateRequest
}
import com.dixa.twilio.client.model.messaging.{TwilioMessagingPhoneNumber, TwilioMessagingService}
import com.dixa.twilio.client.model.phonenumber.TwilioPhoneNumberSid
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

private[impl] final class PhoneNumberCreateRequestExecutorImpl()(
    implicit http: HttpExt,
    materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends PhoneNumberCreateRequestExecutor {

  import PhoneNumberCreateRequestExecutorImpl._

  override def run(
      connSettings: TwilioConnectionSettings,
      req: PhoneNumberCreateRequest
  ): Future[Either[PhoneNumberCreateException, TwilioMessagingPhoneNumber]] = {
    val postParam = s"PhoneNumberSid=${req.phoneNumberSid}"
    val httpReq = TwilioPath(
      ApiSubDomain.Messaging,
      HttpMethods.POST,
      s"/v1/Services/${req.serviceSid}/PhoneNumbers"
    )
      .createHttpRequest(connSettings)
      .withEntity(HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, postParam))
    http.singleRequest(httpReq).flatMap { httpResp =>
      httpResp.status match {
        case StatusCodes.OK => buildSuccessResponse(httpResp.entity, connSettings.timeouts)
        case StatusCodes.Conflict =>
          buildResultForConflictResponse(httpResp.entity, connSettings.timeouts)
        case other => Future.successful(unexpectedStatusCodeResult(req, other))

      }
    }
  }.recover { case e: Exception =>
    Left(new PhoneNumberCreateException.UnspecifiedError(e))
  }

  private def buildSuccessResponse(
      entity: ResponseEntity,
      timeouts: TwilioConnectionSettings.Timeouts
  ) = {
    entity.toStrict(timeouts.requestEntityTimeout).map { entity =>
      val entityString = HttpEntityString(entity.data.utf8String)
      val decoded      = entityString.parseUnsafe[MessagingPhoneNumberJsonRep]()
      Right(decoded.toModel)
    }
  }

  private def buildResultForConflictResponse(
      entity: ResponseEntity,
      timeouts: TwilioConnectionSettings.Timeouts
  ) = {
    entity.toStrict(timeouts.requestEntityTimeout).map { entity =>
      val entityString = HttpEntityString(entity.data.utf8String)
      val decoded      = entityString.parseUnsafe[DefaultApiErrorEntityJsonRep]()
      decoded.code match {
        case 21710L =>
          Left(PhoneNumberCreateException.PhoneNumberAlreadyInMessagingService())
        case 21712L =>
          Left(PhoneNumberCreateException.PhoneNumberAssociatedWithOtherMessagingService())
        case other =>
          Left(
            new PhoneNumberCreateException.UnspecifiedError(
              s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
                s"$other represent. Full error entity from Twilio: $entityString"
            )
          )
      }
    }
  }
}

private object PhoneNumberCreateRequestExecutorImpl {

  private final case class MessagingPhoneNumberJsonRep(sid: String, service_sid: String) {
    def toModel: TwilioMessagingPhoneNumber =
      TwilioMessagingPhoneNumber(
        TwilioPhoneNumberSid(sid),
        TwilioMessagingService.Sid(service_sid)
      )
  }

  private def unexpectedStatusCodeResult(
      req: PhoneNumberCreateRequest,
      status: StatusCode
  ) = Left(
    PhoneNumberCreateException.UnspecifiedError(
      Some(
        s"Could not create: $req, due to getting status code $status from Twilio"
      ),
      None
    )
  )

}
