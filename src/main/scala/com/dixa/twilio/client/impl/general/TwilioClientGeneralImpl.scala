package com.dixa.twilio.client.impl.general

import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.stream.Materializer
import com.dixa.twilio.client.general.{
  ApplicationCreateRequestExecutor,
  ApplicationDeleteRequestExecutor,
  ApplicationReadRequestExecutor,
  TwilioClientGeneral,
  UsageTriggerCreateRequestExecutor,
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

  override val usageTriggerCreate: UsageTriggerCreateRequestExecutor =
    new UsageTriggerCreateRequestExecutorImpl()

  override val usageTriggerRead: UsageTriggerReadRequestExecutor =
    new UsageTriggerReadRequestExecutorImpl()

  override val usageTriggerDelete: UsageTriggerDeleteRequestExecutor =
    new UsageTriggerDeleteRequestExecutorImpl()

  override val applicationCreate: ApplicationCreateRequestExecutor =
    new ApplicationCreateRequestExecutorImpl()

  override val applicationDelete: ApplicationDeleteRequestExecutor =
    new ApplicationDeleteRequestExecutorImpl()

  override val applicationRead: ApplicationReadRequestExecutor =
    new ApplicationReadRequestExecutorImpl()
}
