package com.dixa.twilio.client

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.dixa.twilio.client.model.iam.TwilioAccount
import com.dixa.twilio.client.model.messaging.TwilioMessagingService
import com.dixa.twilio.client.model.messaging.TwilioMessagingService.{
  FallbackWebhook,
  FriendlyName,
  InboundRequestWebhook,
  Sid,
  StatusCallback,
  UseInboundWebhookOnNumber
}

import scala.concurrent.Future

trait TwilioClientMessaging {

  def readServices(
      conSettings: TwilioConnectionSettings
  ): Source[TwilioMessagingService, NotUsed]

  def createService(
      conSettings: TwilioConnectionSettings,
      toCreate: TwilioClientMessaging.ServiceCreateRequest
  ): Future[TwilioMessagingService]
}

object TwilioClientMessaging {

  final case class ServiceCreateRequest(
      friendlyName: FriendlyName,
      inboundRequestWebhook: Option[InboundRequestWebhook],
      fallbackWebhook: Option[FallbackWebhook],
      statusCallback: Option[StatusCallback],
      useInboundWebhookOnNumber: UseInboundWebhookOnNumber
  )
}
