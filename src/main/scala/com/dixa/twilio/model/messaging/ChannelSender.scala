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

import com.dixa.twilio.model.{EnumWithTwilioString, HttpMethod, SidAbstract}
import com.dixa.twilio.model.SidAbstract.Prefix

import scala.collection.immutable

/** Channel Sender represents a sender that can send messages through a specific channel
  */
sealed trait ChannelSender {
  val status: ChannelSender.Status
  val profile: ChannelSender.Profile
  val senderId: MessageSender
  val sid: ChannelSender.Sid
  val webhooks: ChannelSender.Webhooks
  val configuration: ChannelSender.Configuration
  val properties: Option[ChannelSender.Properties.WhatsappProperties]

  /** Why the sender is not usable. Twilio only populates this when [[status]] is
    * [[ChannelSender.Status.Offline]]; it is empty for every other status.
    */
  val offlineReasons: List[ChannelSender.OfflineReason]
}

object ChannelSender {

  final case class WhatsappSender(
      status: ChannelSender.Status,
      profile: ChannelSender.Profile.WhatsappProfile,
      senderId: MessageSender,
      sid: ChannelSender.Sid,
      webhooks: ChannelSender.Webhooks,
      configuration: ChannelSender.Configuration,
      properties: Option[ChannelSender.Properties.WhatsappProperties],
      offlineReasons: List[ChannelSender.OfflineReason] = Nil
  ) extends ChannelSender

  /** A single reason a sender is offline, as reported by Twilio.
    *
    * `code` is Twilio's error code, but note it arrives as a string rather than a number (e.g.
    * `"410"`), so it is kept as a string here rather than guessing a numeric type. `message`
    * frequently embeds the underlying reason from Meta after a "Root Cause from provider:" prefix,
    * and is the only place that reason is available to us.
    */
  final case class OfflineReason(
      code: Option[String],
      message: Option[String],
      moreInfo: Option[String]
  )

  final case class Sid private[ChannelSender] (override val toString: String) extends SidAbstract

  object Sid extends SidAbstract.SidCompanionObject(List(Prefix("XE")), new Sid(_))

  sealed abstract class Status(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object Status extends EnumWithTwilioString[Status] {
    override val values: immutable.IndexedSeq[Status] = findValues

    case object Online                    extends Status("ONLINE")
    case object OnlineUpdating            extends Status("ONLINE:UPDATING")
    case object OnlinePendingVerification extends Status("ONLINE:PENDING_VERIFICATION")
    case object Offline                   extends Status("OFFLINE")
    case object Creating                  extends Status("CREATING")
    case object Verifying                 extends Status("VERIFYING")
    case object PendingVerification       extends Status("PENDING_VERIFICATION")
    case object Unknown                   extends Status("UNKNOWN")
  }

  sealed trait Profile

  object Profile {

    /** The profile of the sender in the case of Waba only containing the waba name.
      */
    final case class WhatsappProfile(about: Option[String] = None, phoneNumberDisplayName: String)
        extends Profile
  }

  sealed abstract class VerificationMethod(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object VerificationMethod extends EnumWithTwilioString[VerificationMethod] {
    override val values: immutable.IndexedSeq[VerificationMethod] = findValues

    case object SMS   extends VerificationMethod("sms")
    case object Voice extends VerificationMethod("voice")
  }

  final case class Configuration(
      wabaId: Option[String] = None,
      verificationMethod: Option[VerificationMethod] = None,
  )

  final case class VerificationCodeConfiguration(
      verificationCode: String
  ) {
    override def toString: String =
      s"VerificationCodeConfiguration(*****)"
  }

  sealed abstract class QualityRating(override val twilioString: String)
      extends EnumWithTwilioString.EnumEntry

  object QualityRating extends EnumWithTwilioString[QualityRating] {
    override val values: immutable.IndexedSeq[QualityRating] = findValues

    case object High    extends QualityRating("HIGH")
    case object Unknown extends QualityRating("UNKNOWN")
  }

  sealed trait Properties

  object Properties {
    final case class WhatsappProperties(
        messagingLimit: Option[String],
        qualityRating: QualityRating
    ) extends Properties
  }

  final case class Webhook(method: HttpMethod, url: String)

  final case class Webhooks(
      fallback: Option[Webhook],
      statusCallback: Option[Webhook],
      callback: Option[Webhook]
  )
}
