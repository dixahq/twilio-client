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

package com.dixa.twilio.model.content

sealed trait ContentType

object ContentType {

  final case class Text(body: String) extends ContentType

  final case class QuickReply(body: String, actions: List[QuickReplyAction]) extends ContentType

  final case class QuickReplyAction(title: String, id: String)

  final case class Card(
      title: Option[String],
      subtitle: Option[String],
      body: Option[String],
      media: List[String],
      actions: List[CardAction]
  ) extends ContentType

  sealed trait CardAction

  object CardAction {
    final case class Url(title: String, url: String)           extends CardAction
    final case class PhoneNumber(title: String, phone: String) extends CardAction
    final case class QuickReply(title: String, id: String)     extends CardAction
    final case class Unknown(title: String, rawType: String)   extends CardAction
  }

  final case class Unknown(typeKey: String, rawJson: String) extends ContentType
}
