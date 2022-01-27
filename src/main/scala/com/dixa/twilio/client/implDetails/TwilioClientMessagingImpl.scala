package com.dixa.twilio.client.implDetails

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.implDetails.request.messaging.ReadServicesRequest
import com.dixa.twilio.client.model.messaging.TwilioMessagingService
import com.dixa.twilio.client.model.messaging.TwilioMessagingService.SidAttribute
import com.dixa.twilio.client.{TwilioClientMessaging, TwilioConnectionSettings}

final class TwilioClientMessagingImpl(
    implicit httpExt: HttpExt,
    materializer: Materializer
) extends TwilioClientMessaging {

  override def readServices(
      conSettings: TwilioConnectionSettings
  ): Source[TwilioMessagingService with SidAttribute, NotUsed] = {
    new ReadServicesRequest().apply(conSettings)
  }
}
