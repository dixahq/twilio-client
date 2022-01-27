package com.dixa.twilio.client.impl.request.messaging

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, StatusCodes}
import akka.stream.Materializer
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString}
import com.dixa.twilio.client.model.messaging.{TwilioMessagingPhoneNumber, TwilioMessagingService}
import com.dixa.twilio.client.model.phonenumber.ActiveNumber
import com.dixa.twilio.client.{TwilioClientMessaging, TwilioConnectionSettings}
import io.circe.generic.auto._
import org.scalactic.TypeCheckedTripleEquals._

import scala.concurrent.{ExecutionContext, Future}
private[impl] final class PhoneNumberCreateRequest()(
    implicit http: HttpExt,
    materializer: Materializer,
    executionContext: ExecutionContext
) {

  import PhoneNumberCreateRequest._

  def apply(
      connSettings: TwilioConnectionSettings,
      req: TwilioClientMessaging.PhoneNumberCreateRequest
  ): Future[TwilioMessagingPhoneNumber] = {
    val postParam = s"PhoneNumberSid=${req.activeNumberSid}"
    val httpReq = TwilioPath(
      ApiSubDomain.Messaging,
      HttpMethods.POST,
      s"/v1/Services/${req.serviceSid}/PhoneNumbers"
    )
      .createHttpRequest(connSettings)
      .withEntity(HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, postParam))
    http.singleRequest(httpReq).flatMap { httpResp =>
      if (httpResp.status !== StatusCodes.OK) {
        throw new RuntimeException(
          s"Could not create: $req, due to getting status code ${httpResp.status} from Twilio"
        )
      }
      httpResp.entity.toStrict(connSettings.timeouts.requestEntityTimeout).map { entity =>
        val entityString = HttpEntityString(entity.data.utf8String)
        val decoded      = entityString.parseUnsafe[MessagingPhoneNumberJsonRep]()
        decoded.toModel
      }
    }
  }

}

private object PhoneNumberCreateRequest {

  private final case class MessagingPhoneNumberJsonRep(sid: String, service_sid: String) {
    def toModel: TwilioMessagingPhoneNumber =
      TwilioMessagingPhoneNumber(ActiveNumber.Sid(sid), TwilioMessagingService.Sid(service_sid))
  }
}
