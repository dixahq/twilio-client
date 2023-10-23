package com.dixa.twilio.client.impl.phonenumber

import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import com.dixa.twilio.client.impl.ApiVersion
import com.dixa.twilio.client.phonenumber.{
  ActiveNumbersReadRequestExecutor,
  IncomingNumbersReadRequestExecutor,
  IncomingPhoneNumberDeleteRequestExecutor,
  OutgoingCallerIdCreateRequestExecutor,
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

  private implicit val apiVersion: ApiVersion = ApiVersion.`2010-04-01`

  override val incomingPhoneNumberList: IncomingNumbersReadRequestExecutor =
    new IncomingNumbersReadRequestExecutorImpl()

  override val incomingPhoneNumberDelete: IncomingPhoneNumberDeleteRequestExecutor =
    new IncomingPhoneNumberDeleteRequestExecutorImpl()

  override val activePhoneNumberList: ActiveNumbersReadRequestExecutor =
    new ActiveNumbersReadRequestExecutorImpl()

  override val outgoingCallerIdList: OutgoingCallerIdReadRequestExecutor =
    new OutgoingCallerIdReadRequestExecutorImpl()

  override val outgoingCallerIdDelete: OutgoingCallerIdDeleteRequestExecutor =
    new OutgoingCallerIdDeleteRequestExecutorImpl()

  override val outgoingCallerIdCreate: OutgoingCallerIdCreateRequestExecutor =
    new OutgoingCallerIdCreateRequestExecutorImpl()
}
