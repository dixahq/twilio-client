package com.dixa.twilio.client.implDetails.request.messaging

import akka.Done
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpMethods, StatusCodes}
import akka.stream.Materializer
import com.dixa.twilio.client.implDetails.ApiSubDomain
import com.dixa.twilio.client.implDetails.TwilioUri.TwilioPath
import com.dixa.twilio.client.{TwilioClientMessaging, TwilioConnectionSettings}
import org.scalactic.TypeCheckedTripleEquals._

import scala.concurrent.{ExecutionContext, Future}

private[implDetails] final class PhoneNumberDeleteRequest()(
    implicit http: HttpExt,
    materializer: Materializer,
    executionContext: ExecutionContext
) {
  def apply(
      conSettings: TwilioConnectionSettings,
      toDelete: TwilioClientMessaging.PhoneNumberDeleteRequest
  ): Future[Done] = {
    val req = TwilioPath(
      ApiSubDomain.Messaging,
      HttpMethods.DELETE,
      s"/v1/Services/${toDelete.serviceSid}/PhoneNumbers/${toDelete.activeNumberSid}"
    )
      .createHttpRequest(conSettings)
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
