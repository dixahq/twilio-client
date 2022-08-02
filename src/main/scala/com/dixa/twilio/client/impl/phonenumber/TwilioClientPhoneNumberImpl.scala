package com.dixa.twilio.client.impl.phonenumber

import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import com.dixa.twilio.client.phonenumber.{
  ActiveNumbersReadRequestExecutor,
  IncomingNumbersReadRequestExecutor,
  OutgoingCallerIdDeleteRequestExecutor,
  OutgoingCallerIdReadRequestExecutor,
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

  override val outgoingCallerIdList: OutgoingCallerIdReadRequestExecutor =
    new OutgoingCallerIdReadRequestExecutorImpl()

  override val outgoingCallerIdDelete: OutgoingCallerIdDeleteRequestExecutor =
    new OutgoingCallerIdDeleteRequestExecutorImpl()
}
