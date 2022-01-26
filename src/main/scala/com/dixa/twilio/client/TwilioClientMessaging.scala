package com.dixa.twilio.client

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.model.messaging.TwilioMessagingService

trait TwilioClientMessaging {

  def readServices(conSettings: TwilioConnectionSettings): Source[TwilioMessagingService, NotUsed]
}
