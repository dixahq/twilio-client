// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dixa.twilio.model.messaging

import com.dixa.twilio.model.SidAbstract.Prefix
import com.dixa.twilio.model.callback.CallbackUrl.MessageStatusCallback
import com.dixa.twilio.model.iam.TwilioAccount
import com.dixa.twilio.model.{HttpMethod, SidAbstract}
import enumeratum.{Enum, EnumEntry}

import java.net.URL
import scala.collection.immutable

/** Represent a Service in the Twilios Messaging API.
  *
  * At time of writing, only some attributes are here. The rest could be added when needed.
  */
trait TwilioMessagingService {

  import TwilioMessagingService._
  def sid: TwilioMessagingService.Sid
  def accountSid: TwilioAccount.Sid
  def friendlyName: FriendlyName
  def inboundRequestWebhook: Option[InboundRequestWebhook]
  def fallbackWebhook: Option[FallbackWebhook]
  def statusCallback: Option[MessageStatusCallback]
  def useInboundWebhookOnNumber: UseInboundWebhookOnNumber
}

object TwilioMessagingService {

  final case class Sid private[TwilioMessagingService] (override val toString: String)
      extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject(List(Prefix("MG")), new Sid(_))

  final case class FriendlyName(override val toString: String)
  final case class InboundRequestWebhook(method: HttpMethod, url: URL)
  final case class FallbackWebhook(method: HttpMethod, url: URL)

  sealed abstract class UseInboundWebhookOnNumber(asBoolean: Boolean) extends EnumEntry {
    override val toString: String = asBoolean.toString
  }
  object UseInboundWebhookOnNumber extends Enum[UseInboundWebhookOnNumber] {
    override val values: immutable.IndexedSeq[UseInboundWebhookOnNumber] = findValues
    case object True  extends UseInboundWebhookOnNumber(true)
    case object False extends UseInboundWebhookOnNumber(false)

    def fromBoolean(b: Boolean): UseInboundWebhookOnNumber = b match {
      case true  => True
      case false => False
    }
  }

  def apply(
      sid: TwilioMessagingService.Sid,
      accountSid: TwilioAccount.Sid,
      friendlyName: FriendlyName,
      inboundRequestWebhook: Option[InboundRequestWebhook],
      fallbackWebhook: Option[FallbackWebhook],
      statusCallback: Option[MessageStatusCallback],
      useInboundWebhookOnNumber: UseInboundWebhookOnNumber
  ): TwilioMessagingService = DefaultImpl(
    sid,
    accountSid,
    friendlyName,
    inboundRequestWebhook,
    fallbackWebhook,
    statusCallback,
    useInboundWebhookOnNumber
  )

  private final case class DefaultImpl(
      sid: TwilioMessagingService.Sid,
      accountSid: TwilioAccount.Sid,
      friendlyName: FriendlyName,
      inboundRequestWebhook: Option[InboundRequestWebhook],
      fallbackWebhook: Option[FallbackWebhook],
      statusCallback: Option[MessageStatusCallback],
      useInboundWebhookOnNumber: UseInboundWebhookOnNumber
  ) extends TwilioMessagingService

}
