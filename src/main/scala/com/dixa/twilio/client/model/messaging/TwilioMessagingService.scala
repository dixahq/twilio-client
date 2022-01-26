package com.dixa.twilio.client.model.messaging

import com.dixa.twilio.client.model.HttpMethod
import com.dixa.twilio.client.model.iam.TwilioAccount
import enumeratum.{Enum, EnumEntry}

import java.net.URL
import scala.collection.immutable

sealed trait TwilioMessagingService {

  import TwilioMessagingService._

  def sid: Sid
  def accountSid: TwilioAccount.Sid
  def friendlyName: FriendlyName
  def inboundRequestWebhook: Option[InboundRequestWebhook]
  def fallbackWebhook: Option[FallbackWebhook]
  def statusCallback: Option[StatusCallback]
  def useInboundWebhookOnNumber: UseInboundWebhookOnNumber
}

object TwilioMessagingService {

  final case class Sid(override val toString: String)
  final case class FriendlyName(override val toString: String)
  final case class InboundRequestWebhook(method: HttpMethod, url: URL)
  final case class FallbackWebhook(method: HttpMethod, url: URL)
  final case class StatusCallback(url: URL)

  sealed abstract class UseInboundWebhookOnNumber(asBoolean: Boolean) extends EnumEntry
  object UseInboundWebhookOnNumber extends Enum[UseInboundWebhookOnNumber] {
    override val values: immutable.IndexedSeq[UseInboundWebhookOnNumber] = findValues
    case object True  extends UseInboundWebhookOnNumber(true)
    case object False extends UseInboundWebhookOnNumber(false)
  }

  def apply(
      sid: Sid,
      accountSid: TwilioAccount.Sid,
      friendlyName: FriendlyName,
      inboundRequestWebhook: Option[InboundRequestWebhook],
      fallbackWebhook: Option[FallbackWebhook],
      statusCallback: Option[StatusCallback],
      useInboundWebhookOnNumber: UseInboundWebhookOnNumber
  ): TwilioMessagingService = ???
}
