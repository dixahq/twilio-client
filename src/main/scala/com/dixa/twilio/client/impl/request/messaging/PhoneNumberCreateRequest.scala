package com.dixa.twilio.client.impl.request.messaging

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, StatusCodes}
import akka.stream.Materializer
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.impl.request.messaging.ServiceCreateRequest.createPostParamString
import com.dixa.twilio.client.impl.{ApiSubDomain, HttpEntityString}
import com.dixa.twilio.client.model.messaging.{TwilioMessagingPhoneNumber, TwilioMessagingService}
import com.dixa.twilio.client.{TwilioClientMessaging, TwilioConnectionSettings}
import io.circe.generic.auto._
import org.scalactic.TypeCheckedTripleEquals._

import scala.concurrent.{ExecutionContext, Future}
import com.dixa.twilio.client.model.phonenumber.ActiveNumber
private[impl] final class PhoneNumberCreateRequest()(
    implicit http: HttpExt,
    materializer: Materializer,
    executionContext: ExecutionContext
) {

  import PhoneNumberCreateRequest._

  def apply(
      connSettings: TwilioConnectionSettings,
      toCreate: TwilioClientMessaging.PhoneNumberCreateRequest
  ): Future[TwilioMessagingPhoneNumber] = {
    val postParam = s"PhoneNumberSid=${toCreate.activeNumberSid}"
    val req = TwilioPath(
      ApiSubDomain.Messaging,
      HttpMethods.POST,
      s"/v1/Services/${toCreate.serviceSid}/PhoneNumbers"
    )
      .createHttpRequest(connSettings)
      .withEntity(HttpEntity(ContentTypes.`application/x-www-form-urlencoded`, postParam))
    http.singleRequest(req).flatMap { resp =>
      if (resp.status !== StatusCodes.OK) {
        throw new RuntimeException(
          s"Could not create: $toCreate, due to getting status code ${resp.status} from Twilio"
        )
      }
      resp.entity.toStrict(connSettings.timeouts.requestEntityTimeout).map { entity =>
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
