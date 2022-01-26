package com.dixa.twilio.client.implDetails

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.model.messaging.TwilioMessagingService
import com.dixa.twilio.client.{TwilioClientMessaging, TwilioConnectionSettings}

final class TwilioClientMessagingImpl extends TwilioClientMessaging {

  override def readServices(
      conSettings: TwilioConnectionSettings
  ): Source[TwilioMessagingService, NotUsed] = ???
}
