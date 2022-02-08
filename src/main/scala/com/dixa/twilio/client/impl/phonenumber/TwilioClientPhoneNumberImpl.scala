package com.dixa.twilio.client.impl.phonenumber

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.client.model.phonenumber.TwilioIncomingPhoneNumber
import com.dixa.twilio.client.phonenumber.TwilioClientPhoneNumber

import scala.concurrent.ExecutionContext

private[impl] final class TwilioClientPhoneNumberImpl()(
    implicit http: HttpExt,
    materializer: Materializer,
    executionContext: ExecutionContext
) extends TwilioClientPhoneNumber {

  override def incomingPhoneNumberList(
      connSettings: TwilioConnectionSettings,
      filter: Option[TwilioIncomingPhoneNumber.PhoneNumberFilter]
  ): Source[TwilioIncomingPhoneNumber, NotUsed] =
    new IncomingPhoneNumberListRequest().apply(connSettings, filter)
}
