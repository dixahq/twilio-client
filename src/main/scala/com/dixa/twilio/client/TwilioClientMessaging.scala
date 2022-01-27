package com.dixa.twilio.client

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.model.messaging.TwilioMessagingService
import com.dixa.twilio.client.model.messaging.TwilioMessagingService.SidAttribute

trait TwilioClientMessaging {

  def readServices(
      conSettings: TwilioConnectionSettings
  ): Source[TwilioMessagingService with SidAttribute, NotUsed]
}
