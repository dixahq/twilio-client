package com.dixa.twilio.client.impl.phonenumber

import akka.NotUsed
import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.TwilioConnectionSettings
import com.dixa.twilio.model.phonenumber.{
  TwilioActivePhoneNumber,
  TwilioIncomingPhoneNumber,
  TwilioPhoneNumberSid
}
import com.dixa.twilio.client.phonenumber.{
  ActiveNumbersReadRequestExecutor,
  IncomingNumbersReadRequestExecutor,
  TwilioClientPhoneNumber
}

import scala.concurrent.ExecutionContext

private[impl] final class TwilioClientPhoneNumberImpl()(
    implicit http: HttpExt,
    materializer: Materializer,
    executionContext: ExecutionContext
) extends TwilioClientPhoneNumber {

  @deprecated("Use incomingPhoneNumberListV2 instead", "0.11.0")
  override def incomingPhoneNumberList(
      connSettings: TwilioConnectionSettings,
      filter: Option[TwilioIncomingPhoneNumber.PhoneNumberFilter]
  ): Source[TwilioIncomingPhoneNumber, NotUsed] =
    new IncomingPhoneNumberListRequest().apply(connSettings, filter)

  @deprecated("Use mediaResourceReadV2 instead", "0.11.0")
  override def activePhoneNumberList(
      connSettings: TwilioConnectionSettings,
      phoneNumber: Option[TwilioPhoneNumberSid] = None
  ): Source[TwilioActivePhoneNumber, NotUsed] =
    new ActivePhoneNumberListRequest().apply(connSettings, phoneNumber)

  override val activePhoneNumberListV2: ActiveNumbersReadRequestExecutor =
    new ActiveNumbersReadRequestExecutorImpl()

  override val incomingPhoneNumberListV2: IncomingNumbersReadRequestExecutor =
    new IncomingNumbersReadRequestExecutorImpl()
}
