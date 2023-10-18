package com.dixa.twilio.client.impl.general

import akka.http.scaladsl.HttpExt
import akka.stream.Materializer
import com.dixa.twilio.client.general.{
  ApplicationCreateRequestExecutor,
  TwilioClientGeneral,
  UsageTriggerDeleteRequestExecutor,
  UsageTriggerReadRequestExecutor
}
import com.dixa.twilio.client.impl.ApiVersion

import scala.concurrent.ExecutionContext

private[impl] final class TwilioClientGeneralImpl()(
    implicit materializer: Materializer,
    executionContext: ExecutionContext,
    httpExt: HttpExt
) extends TwilioClientGeneral {

  private implicit val apiVersion: ApiVersion = ApiVersion.`2010-04-01`

  override val usageTriggerRead: UsageTriggerReadRequestExecutor =
    new UsageTriggerReadRequestExecutorImpl()

  override val usageTriggerDelete: UsageTriggerDeleteRequestExecutor =
    new UsageTriggerDeleteRequestExecutorImpl()

  override def applicationCreate: ApplicationCreateRequestExecutor =
    new ApplicationCreateRequestExecutorImpl()
}
