package com.dixa.twilio.client.impl.messaging

import akka.Done
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpMethods, StatusCodes}
import akka.stream.Materializer
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.impl.ApiSubDomain
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.messaging.TwilioClientMessaging
import org.scalactic.TypeCheckedTripleEquals._

import scala.concurrent.{ExecutionContext, Future}

private[impl] final class PhoneNumberDeleteRequest()(
    implicit http: HttpExt,
    materializer: Materializer,
    executionContext: ExecutionContext
) {
  def apply(
      connSettings: TwilioConnectionSettings,
      req: TwilioClientMessaging.PhoneNumberDeleteRequest
  ): Future[Done] = {
    val httpReq = TwilioPath(
      ApiSubDomain.Messaging,
      HttpMethods.DELETE,
      s"/v1/Services/${req.serviceSid}/PhoneNumbers/${req.phoneNumberSid}"
    )
      .createHttpRequest(connSettings)
    http.singleRequest(httpReq).map { httpResp =>
      if (httpResp.status !== StatusCodes.OK) {
        throw new RuntimeException(
          s"Could not delete: $req, due to getting status code ${httpResp.status} from Twilio"
        )
      }
      httpResp.entity.discardBytes()
      Done
    }
  }
}
