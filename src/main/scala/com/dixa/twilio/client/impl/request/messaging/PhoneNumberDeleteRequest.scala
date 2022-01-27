package com.dixa.twilio.client.impl.request.messaging

import akka.Done
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpMethods, StatusCodes}
import akka.stream.Materializer
import com.dixa.twilio.client.impl.ApiSubDomain
import com.dixa.twilio.client.impl.TwilioUri.TwilioPath
import com.dixa.twilio.client.{TwilioClientMessaging, TwilioConnectionSettings}
import org.scalactic.TypeCheckedTripleEquals._

import scala.concurrent.{ExecutionContext, Future}

private[impl] final class PhoneNumberDeleteRequest()(
    implicit http: HttpExt,
    materializer: Materializer,
    executionContext: ExecutionContext
) {
  def apply(
      connSettings: TwilioConnectionSettings,
      toDelete: TwilioClientMessaging.PhoneNumberDeleteRequest
  ): Future[Done] = {
    val req = TwilioPath(
      ApiSubDomain.Messaging,
      HttpMethods.DELETE,
      s"/v1/Services/${toDelete.serviceSid}/PhoneNumbers/${toDelete.activeNumberSid}"
    )
      .createHttpRequest(connSettings)
    http.singleRequest(req).map { resp =>
      if (resp.status !== StatusCodes.OK) {
        throw new RuntimeException(
          s"Could not delete: $toDelete, due to getting status code ${resp.status} from Twilio"
        )
      }
      resp.entity.discardBytes()
      Done
    }
  }
}
