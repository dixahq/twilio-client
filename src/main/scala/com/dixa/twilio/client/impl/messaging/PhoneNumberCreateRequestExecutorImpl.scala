package com.dixa.twilio.client.impl.messaging

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{
  ContentTypes,
  HttpEntity,
  HttpMethods,
  HttpRequest,
  HttpResponse,
  StatusCodes
}
import akka.stream.Materializer
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.impl.{ApiSubDomain, DefaultApiErrorEntityJsonRep, HttpEntityString}
import com.dixa.twilio.client.messaging.PhoneNumberCreateRequestExecutor
import com.dixa.twilio.client.messaging.PhoneNumberCreateRequestExecutor.{
  PhoneNumberCreateException,
  PhoneNumberCreateRequest
}
import com.dixa.twilio.client.model.messaging.{
  ServiceSid,
  TwilioMessagingPhoneNumber,
  TwilioMessagingService
}
import com.dixa.twilio.client.model.phonenumber.TwilioPhoneNumberSid
import com.dixa.twilio.client.{ApiException, TwilioConnectionSettings}
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

private[impl] final class PhoneNumberCreateRequestExecutorImpl()(
    implicit override protected val http: HttpExt,
    override protected val materializer: Materializer,
    override protected val executionContext: ExecutionContext
) extends PhoneNumberCreateRequestExecutor {

  import PhoneNumberCreateRequestExecutorImpl._

  override protected def createHttpReq(
      connSettings: TwilioConnectionSettings,
      req: PhoneNumberCreateRequest
  ): HttpRequest = {
    val postParam = s"PhoneNumberSid=${req.phoneNumberSid}"
    TwilioPath(
      ApiSubDomain.Messaging,
      HttpMethods.POST,
      s"/v1/Services/${req.serviceSid}/PhoneNumbers"
    )
      .createHttpRequest(connSettings)
      .withEntity(HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, postParam))
  }

  override protected def mapApiException(apiException: ApiException): ApiExceptionWrapper =
    PhoneNumberCreateException.Api.apply(apiException)

  /** Create the request specific Unspecified exception. */
  override protected def createUnspecifiedException(
      msg: Option[String],
      cause: Option[Exception]
  ): UnspecifiedException = PhoneNumberCreateException.Unspecified(msg, cause)

  override protected def parseHttpResponse(
      req: PhoneNumberCreateRequest,
      httpReq: HttpRequest,
      httpResponse: HttpResponse,
      entity: HttpEntity.Strict
  ): Either[PhoneNumberCreateException, TwilioMessagingPhoneNumber] = httpResponse.status match {
    case StatusCodes.OK => buildSuccessResponse(entity)
    case StatusCodes.Conflict =>
      buildResultForConflictResponse(entity)
    case _ => buildResultForUnhandledResponse(req, httpReq, httpResponse, entity)
  }

  private def buildSuccessResponse(entity: HttpEntity.Strict) = {
    val entityString = HttpEntityString(entity.data.utf8String)
    val decoded      = entityString.parseUnsafe[MessagingPhoneNumberJsonRep]()
    Right(decoded.toModel)
  }

  private def buildResultForConflictResponse(entity: HttpEntity.Strict) = {
    val entityString = HttpEntityString(entity.data.utf8String)
    val decoded      = entityString.parseUnsafe[DefaultApiErrorEntityJsonRep]()
    decoded.code match {
      case 21710L =>
        Left(PhoneNumberCreateException.PhoneNumberAlreadyInMessagingService())
      case 21712L =>
        Left(PhoneNumberCreateException.PhoneNumberAssociatedWithOtherMessagingService())
      case other =>
        Left(
          new PhoneNumberCreateException.Unspecified(
            s"Got status ${decoded.status} from Twilio, but we do not know what code: " +
              s"$other represent. Full error entity from Twilio: $entityString"
          )
        )
    }
  }
}

private object PhoneNumberCreateRequestExecutorImpl {

  private final case class MessagingPhoneNumberJsonRep(sid: String, service_sid: String) {
    def toModel: TwilioMessagingPhoneNumber =
      TwilioMessagingPhoneNumber(
        TwilioPhoneNumberSid(sid),
        ServiceSid(service_sid)
      )
  }
}
