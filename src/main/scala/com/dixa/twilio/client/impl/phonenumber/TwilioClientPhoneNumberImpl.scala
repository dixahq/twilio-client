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

  override val incomingPhoneNumberList: IncomingNumbersReadRequestExecutor =
    new IncomingNumbersReadRequestExecutorImpl()

  override val activePhoneNumberList: ActiveNumbersReadRequestExecutor =
    new ActiveNumbersReadRequestExecutorImpl()
}
